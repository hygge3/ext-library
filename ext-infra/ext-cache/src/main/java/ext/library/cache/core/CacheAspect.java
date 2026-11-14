package ext.library.cache.core;

import ext.library.cache.annotation.Cache;
import ext.library.cache.properties.CacheProperties;
import ext.library.cache.strategy.CacheStrategy;
import ext.library.core.util.spel.SpelUtil;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import ext.library.tool.util.DateUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 缓存切面
 * <p>
 * 拦截 {@link Cache} 注解标记的方法，根据缓存类型执行相应的缓存操作
 *
 * @since 2025.08.29
 */
@Aspect
public class CacheAspect {

    /**
     * 负缓存的 key 后缀
     * <p>
     * 空值必须和真实值分开存：共用同一个 key 时，返回 {@code Boolean} 的方法会把负缓存读成 {@code false}。
     */
    private static final String NULL_KEY_SUFFIX = ":null";

    /**
     * {@link CompletableFuture} 存不了 null
     */
    private static final Object NULL_RESULT = new Object();

    private final CacheStrategy cacheStrategy;

    /**
     * 等领头请求的上限，超时自己回源：无穷等会把整个线程池耗在同一个热 key 上
     */
    private final long loadTimeoutMillis;

    /**
     * 正在回源的 key，同 key 的并发请求合并成一次加载
     */
    private final ConcurrentMap<String, CompletableFuture<Object>> inflight = new ConcurrentHashMap<>();

    /**
     * 本线程持有哪些 key 的加载权，同线程重入同一个 key 时不能再等自己
     */
    private final ThreadLocal<Set<String>> holding = ThreadLocal.withInitial(HashSet::new);

    public CacheAspect(CacheStrategy cacheStrategy, CacheProperties properties) {
        this.cacheStrategy = cacheStrategy;
        this.loadTimeoutMillis = properties.getLoadTimeout().toMillis();
    }

    @Pointcut("@annotation(ext.library.cache.annotation.Cache)")
    public void cacheAspect() {
    }

    @Around("cacheAspect()")
    public Object doAround(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        Object[] args = point.getArgs();

        Cache annotation = method.getAnnotation(Cache.class);
        String cacheName = annotation.cacheName();
        String key = SpelUtil.parseValueToString(point.getThis(), method, args, annotation.key());

        return switch (annotation.type()) {
            case PUT -> {
                Object result = point.proceed();
                putValue(cacheName, key, result, annotation);
                yield result;
            }
            case DELETE -> {
                evictValue(cacheName, key);
                yield point.proceed();
            }
            case FULL -> getOrLoad(point, signature, cacheName, key, annotation);
        };
    }

    /**
     * 获取缓存或加载数据
     */
    private Object getOrLoad(ProceedingJoinPoint point, MethodSignature signature,
                             String cacheName, String key, Cache annotation) throws Throwable {
        Object cached = cacheStrategy.get(cacheName, key, signature.getMethod().getGenericReturnType());
        if (cached != null) {
            return cached;
        }
        if (isNegativeCached(cacheName, key, annotation)) {
            Logs.debug(EmojiSymbol.CACHE, "命中空值缓存: {}:{}", cacheName, key);
            return null;
        }
        return load(point, cacheName, key, annotation);
    }

    /**
     * 回源加载，同 key 的并发请求合并成一次（防击穿）
     * <p>
     * 只合并同一实例内的请求：跨实例要靠分布式锁或让每个实例各打一次数据库。
     */
    private Object load(ProceedingJoinPoint point, String cacheName, String key, Cache annotation) throws Throwable {
        // 方法身份必须进键：同名 key 的不同方法返回类型不同，共享一条 inflight 会把 A 的结果交给 B
        String guard = point.getSignature().toLongString() + ":" + cacheName + ":" + key;
        if (!holding.get().add(guard)) {
            // 同一线程重入同一个 key，等自己就是死锁
            return point.proceed();
        }
        CompletableFuture<Object> mine = new CompletableFuture<>();
        CompletableFuture<Object> running = inflight.putIfAbsent(guard, mine);
        try {
            if (running != null) {
                return await(running, point);
            }
            try {
                Logs.debug(EmojiSymbol.CACHE, "从数据库获取数据");
                Object result = point.proceed();
                putValue(cacheName, key, result, annotation);
                mine.complete(result == null ? NULL_RESULT : result);
                return result;
            } catch (Throwable e) {
                // 回源失败：等待方拿到同一个异常，不会得到一个假的成功结果
                mine.completeExceptionally(e);
                throw e;
            } finally {
                inflight.remove(guard, mine);
            }
        } finally {
            // 集空了就连 ThreadLocal 一起删，否则每个池线程永久挂一个空 HashSet
            Set<String> held = holding.get();
            held.remove(guard);
            if (held.isEmpty()) {
                holding.remove();
            }
        }
    }

    /**
     * 等待同 key 的首次加载结果，异常按原样抛出
     * <p>
     * 领头者卡住时不能无限等：等满 {@code loadTimeout} 就自己回源，多打一次库换线程不吊死。
     */
    private Object await(CompletableFuture<Object> future, ProceedingJoinPoint point) throws Throwable {
        try {
            Object value = future.get(loadTimeoutMillis, TimeUnit.MILLISECONDS);
            return value == NULL_RESULT ? null : value;
        } catch (TimeoutException _) {
            Logs.warn(EmojiSymbol.CACHE, "等待回源超时 {}ms，改为自行回源", loadTimeoutMillis);
            return point.proceed();
        } catch (ExecutionException e) {
            throw e.getCause() == null ? e : e.getCause();
        }
    }

    /**
     * 是否有该 key 的负缓存
     */
    private boolean isNegativeCached(String cacheName, String key, Cache annotation) {
        return annotation.cacheNull()
                && cacheStrategy.get(cacheName, key + NULL_KEY_SUFFIX, Boolean.class) != null;
    }

    /**
     * 写缓存：正常值用请求的 TTL，空值用短 TTL 的标记
     */
    private void putValue(String cacheName, String key, Object result, Cache annotation) {
        if (result != null) {
            cacheStrategy.put(cacheName, key, result, DateUtil.convert(annotation.timeout(), annotation.timeUnit()));
        } else if (annotation.cacheNull()) {
            cacheStrategy.put(cacheName, key + NULL_KEY_SUFFIX, Boolean.TRUE,
                    DateUtil.convert(annotation.nullTimeout(), annotation.timeUnit()));
        }
    }

    /**
     * 删缓存：负缓存标记一起删，否则新写入的数据会被标记挡住
     */
    private void evictValue(String cacheName, String key) {
        cacheStrategy.evict(cacheName, key);
        cacheStrategy.evict(cacheName, key + NULL_KEY_SUFFIX);
    }

}
