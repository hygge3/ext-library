package ext.library.tool.domain;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.collect.Maps;


/**
 * 用于计数的 Map
 */
public final class CountMap {

    private final Map<Object, AtomicLong> data;

    public CountMap() {
        this(Maps.newHashMap());
    }

    /**
     * 参数构造器，方便自定义数据承载，例如：ConcurrentMap
     *
     * @param data 集合
     */
    public CountMap(Map<Object, AtomicLong> data) {
        this.data = data;
    }

    /**
     * 添加数据
     *
     * @param value 数据
     */
    public void add(Object value) {
        data.compute(value, (k, v) -> {
            if (v == null) {
                return new AtomicLong(1);
            } else {
                v.incrementAndGet();
                return v;
            }
        });
    }

    /**
     * 递减计数
     *
     * @param value 数据
     */
    public void decr(Object value) {
        data.computeIfPresent(value, (k, v) -> {
            v.decrementAndGet();
            return v;
        });
    }

    /**
     * 删除计数
     *
     * @param value 数据
     */
    public void remove(Object value) {
        data.remove(value);
    }

    /**
     * 获取数据的 count
     *
     * @param value 数据
     * @return count
     */
    public long get(Object value) {
        AtomicLong counter = data.get(value);
        if (counter == null) {
            return 0L;
        }
        return counter.get();
    }

    /**
     * 获取计数最大的数据
     *
     * @return 数据
     */
    @SuppressWarnings("unchecked")
    public <T> T getMax() {
        return (T) data.entrySet()
                .stream()
                .max(Comparator.comparingLong(entry -> entry.getValue().longValue()))
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    /**
     * 获取计数最小的数据
     *
     * @return 数据
     */
    @SuppressWarnings("unchecked")
    public <T> T getMin() {
        return (T) data.entrySet()
                .stream()
                .min(Comparator.comparingLong(entry -> entry.getValue().longValue()))
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    /**
     * 数据大小
     *
     * @return 返回数据大小
     */
    public int size() {
        return data.size();
    }

    /**
     * 重置，实际上是清空数据，方便复用
     */
    public void clear() {
        data.clear();
    }

    @Override
    public String toString() {
        return data.toString();
    }

}
