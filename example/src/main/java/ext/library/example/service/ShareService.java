package ext.library.example.service;

import cn.xbatis.core.sql.executor.chain.QueryChain;
import ext.library.example.domain.NoteShare;
import ext.library.example.mapper.NoteShareMapper;
import ext.library.qrcode.QrCode;
import ext.library.tool.constant.Singletons;
import ext.library.tool.exception.BizException;
import ext.library.tool.runtime.Logs;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 分享链接：token 是 72 位随机串（12 字符），库里那一行就是凭据。
 * <p>
 * 不用密文：密文要带上 IV 和认证标签，Base64 后 95 字符，贴进二维码太密；随机串猜不到，有效期看行里的 expires_at。
 */
@Service
@RequiredArgsConstructor
public class ShareService {

    private static final String MODULE = "SHARE";

    private static final int QR_SIZE = 240;

    private static final int TOKEN_BYTES = 9;

    private static final Base64.Encoder TOKEN_ENCODER = Base64.getUrlEncoder().withoutPadding();

    private final NoteShareMapper shareMapper;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    /** 天数为 0 或负时用默认 7 天。 */
    public NoteShare create(String noteId, int days) {
        int realDays = days > 0 ? days : 7;
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(realDays);

        NoteShare share = new NoteShare();
        share.setToken(randomToken());
        share.setNoteId(noteId);
        share.setExpiresAt(expiresAt);
        share.setCreatedAt(LocalDateTime.now());
        shareMapper.save(share);
        Logs.info(MODULE, "生成分享：noteId={} days={}", noteId, realDays);
        return share;
    }

    public String url(NoteShare share) {
        return baseUrl + "/share/" + share.getToken();
    }

    /** 有效分享：库里存在且未过期。 */
    public NoteShare requireValid(String token) {
        NoteShare share = shareMapper.getOptionalById(token)
                .orElseThrow(() -> new BizException(404, "分享链接不存在"));
        if (share.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BizException(410, "分享链接已过期");
        }
        return share;
    }

    /** 72 位随机 → 12 字符，猜不到又能塞进二维码。 */
    private static String randomToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        Singletons.SECURE_RANDOM.nextBytes(bytes);
        return TOKEN_ENCODER.encodeToString(bytes);
    }

    public List<NoteShare> listByNote(String noteId) {
        return QueryChain.of(shareMapper)
                .eq(NoteShare::getNoteId, noteId)
                .orderByDesc(NoteShare::getCreatedAt)
                .list();
    }

    /** 由 ext-job 的定时任务调用。 */
    public int cleanupExpired() {
        int removed = shareMapper.delete(where -> where.lt(NoteShare::getExpiresAt, LocalDateTime.now()));
        if (removed > 0) {
            Logs.info(MODULE, "清理过期分享：count={}", removed);
        }
        return removed;
    }

    public byte[] qrCode(String content) {
        return QrCode.from(content).size(QR_SIZE).toBytes();
    }

    public Instant expiresAt(String token) {
        return requireValid(token).getExpiresAt().atZone(ZoneId.systemDefault()).toInstant();
    }
}
