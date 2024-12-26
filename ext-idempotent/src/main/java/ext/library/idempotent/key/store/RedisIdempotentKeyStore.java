package ext.library.idempotent.key.store;

import java.util.concurrent.TimeUnit;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

/**
 * 基于 Redis 的幂等 Key 的存储器
 */
@RequiredArgsConstructor
public class RedisIdempotentKeyStore implements IdempotentKeyStore {

	@Autowired
	@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
	private StringRedisTemplate stringRedisTemplate;

	@Override
	public boolean saveIfAbsent(String key, long duration, TimeUnit timeUnit) {
		ValueOperations<String, String> opsForValue = this.stringRedisTemplate.opsForValue();
		Boolean saveSuccess = opsForValue.setIfAbsent(key, String.valueOf(System.currentTimeMillis()), duration,
				timeUnit);
		return saveSuccess != null && saveSuccess;
	}

	@Override
	public void remove(String key) {
		this.stringRedisTemplate.delete(key);
	}

}
