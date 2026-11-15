package ext.library.example.service;

import ext.library.crypto.AESUtil;
import ext.library.crypto.DigestUtil;
import ext.library.http.HttpUtil;
import ext.library.json.util.JsonUtil;
import ext.library.postgres.util.PostgresUtil;
import ext.library.redis.util.RedisUtil;
import ext.library.tool.util.IdUtil;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

/**
 * 三条跟中间件打交道的路径：
 * <ul>
 *   <li>阅读量落在 PostgreSQL（{@link PostgresUtil}，重启不丢）</li>
 *   <li>最近浏览落在 Redis 列表（{@link RedisUtil}，天然带 TTL 与多实例共享）</li>
 *   <li>开放接口自调用：exthttp 发请求，ext-api-crypto 验签 + 响应加密</li>
 * </ul>
 */
@Service
public class StatsService {

    private static final String VIEW_KEY_PREFIX = "ext:example:view:";

    private static final String RECENT_KEY_PREFIX = "ext:example:recent:";

    private static final int RECENT_KEEP = 8;

    private final JdbcClient jdbcClient;

    private final String baseUrl;

    private final String signSecret;

    private final String cryptoSecret;

    private final String cryptoSalt;

    public StatsService(JdbcClient jdbcClient,
                        @Value("${app.base-url:http://localhost:8080}") String baseUrl,
                        @Value("${ext.api-crypto.sign.secret:ext-example-sign-secret}") String signSecret,
                        @Value("${ext.api-crypto.secret-key:ext-example-apicrypto-secret-32byte}") String cryptoSecret,
                        @Value("${ext.api-crypto.salt:ext-example-salt}") String cryptoSalt) {
        this.jdbcClient = jdbcClient;
        this.baseUrl = baseUrl;
        this.signSecret = signSecret;
        this.cryptoSecret = cryptoSecret;
        this.cryptoSalt = cryptoSalt;
    }

    public long incrementView(String noteId) {
        return PostgresUtil.cacheIncrement(VIEW_KEY_PREFIX + noteId, 1, Duration.ofHours(24));
    }

    public long viewCount(String noteId) {
        Long value = PostgresUtil.cacheGet(VIEW_KEY_PREFIX + noteId, Long.class);
        return value == null ? 0 : value;
    }

    public void touchRecent(String owner, String noteId) {
        String key = RECENT_KEY_PREFIX + owner;
        ListOperations<String, String> list = RedisUtil.listOps();
        list.remove(key, 0, noteId);
        list.leftPush(key, noteId);
        list.trim(key, 0, RECENT_KEEP - 1);
        RedisUtil.expire(key, Duration.ofDays(7));
    }

    public List<String> recentNoteIds(String owner) {
        List<String> ids = RedisUtil.listOps().range(RECENT_KEY_PREFIX + owner, 0, RECENT_KEEP - 1);
        return ids == null ? List.of() : ids;
    }

    /** 聚合统计直接写 SQL，比链式 API 更直观；JdbcClient 由 spring-boot-starter-jdbc 提供。 */
    public List<Map<String, Object>> ownerStats(int limit) {
        return jdbcClient.sql("""
                        SELECT owner_email, COUNT(*) AS notes, to_char(MAX(updated_at), 'MM-DD HH24:MI') AS last_active
                        FROM note GROUP BY owner_email ORDER BY notes DESC LIMIT ?
                        """)
                .param(limit)
                .query()
                .listOfRows();
    }

    /**
     * 模拟第三方调用开放接口：exthttp 拼签名头，响应体是 ext-api-crypto 用 AES 加密后的密文。
     *
     * @return 解密后的业务数据
     */
    public Map<String, Object> callOpenShareApi(String shareToken) {
        String path = "/api/open/share/" + shareToken;
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String nonce = IdUtil.random(16);
        String payload = "GET\n" + path + "\n" + "" + "\n" + timestamp + "\n" + nonce + "\n" + "";
        String sign = DigestUtil.hmac("HmacSHA256", signSecret, payload);

        String body = HttpUtil.get(baseUrl + path)
                .header("X-Timestamp", timestamp)
                .header("X-Nonce", nonce)
                .header("X-Sign", sign)
                .acceptJson()
                .executeAsString();

        // 响应被包装成 JSON 字符串，先去掉外层引号再解密
        String cipherText = body.startsWith("\"") ? JsonUtil.readObj(body, String.class) : body;
        String json = AESUtil.decrypt(cryptoSecret, cryptoSalt, cipherText);
        return JsonUtil.readMap(json);
    }

    /** 页面上的阅读量展示，顺便把 Redis 里的记录合并成完整笔记列表。 */
    public List<Map<String, Object>> recentViews(String owner, NoteService noteService) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (String id : recentNoteIds(owner)) {
            try {
                var note = noteService.find(id);
                rows.add(Map.of("id", note.getId(), "title", note.getTitle(), "views", viewCount(id)));
            } catch (RuntimeException _) {
                // 笔记已删，列表里残留的 id 直接跳过
            }
        }
        return rows;
    }

    public String encodeForLog(String text) {
        return new String(text.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
    }
}
