package ext.library.example.web;

import ext.library.apicrypto.annotation.ApiSign;
import ext.library.apicrypto.annotation.ResponseEncrypt;
import ext.library.example.domain.Note;
import ext.library.example.service.AuthService;
import ext.library.example.service.NoteService;
import ext.library.example.service.ShareService;
import ext.library.example.service.StatsService;
import ext.library.example.support.JobConfig;
import ext.library.job.JobDispatcher;
import ext.library.security.annotation.SecurityIgnore;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 分享：生成链接 / 二维码，公开页，以及给第三方调用的加密开放接口。
 */
@RestController
@RequiredArgsConstructor
public class ShareController {

    private final ShareService shareService;

    private final NoteService noteService;

    private final AuthService authService;

    private final JobDispatcher jobDispatcher;

    private final StatsService statsService;

    /** 生成分享：返回链接与二维码 base64，页面上直接展示。 */
    @PostMapping("/api/notes/{id}/share")
    public Map<String, Object> share(@PathVariable String id,
                                     @RequestParam(defaultValue = "0") int days,
                                     @RequestParam(defaultValue = "false") boolean mail) {
        Note note = noteService.requireOwner(id, authService.currentUser().getEmail());
        var share = shareService.create(note.getId(), days);
        String url = shareService.url(share);
        if (mail) {
            // 发信走 ext-job 队列，接口不等 SMTP
            jobDispatcher.dispatch(JobConfig.QUEUE_MAIL,
                    new JobConfig.ShareMail(note.getOwnerEmail(), note.getTitle(), url));
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("url", url);
        result.put("expiresAt", share.getExpiresAt().toString());
        result.put("qr", "data:image/png;base64," + Base64.getEncoder().encodeToString(shareService.qrCode(url)));
        return result;
    }

    /** 页面二维码图片，单独出一个 URL 便于 <img src>。 */
    @GetMapping(value = "/api/share/{token}/qr.png", produces = "image/png")
    public byte[] qr(@PathVariable String token) {
        return shareService.qrCode(shareService.url(shareService.requireValid(token)));
    }

    /**
     * 开放接口：第三方带签名访问，响应 AES 加密。
     * <p>
     * 签名覆盖 method + uri + query + timestamp + nonce + body，见 ext-api-crypto。
     */
    @ApiSign
    @ResponseEncrypt
    @SecurityIgnore
    @GetMapping("/api/open/share/{token}")
    public Map<String, Object> openShare(@PathVariable String token) {
        var share = shareService.requireValid(token);
        Note note = noteService.find(share.getNoteId());
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("title", note.getTitle());
        payload.put("content", note.getContent());
        payload.put("tags", note.getTags());
        payload.put("updatedAt", note.getUpdatedAt().toString());
        payload.put("views", statsService.viewCount(note.getId()));
        payload.put("expiresAt", share.getExpiresAt().toString());
        return payload;
    }

    /** 演示脚本：自己当第三方调一次开放接口，验证签名与响应解密都能跑通。 */
    @PostMapping("/api/demo/open-call")
    public Map<String, Object> demoOpenCall(@RequestParam String token) {
        return statsService.callOpenShareApi(token);
    }
}
