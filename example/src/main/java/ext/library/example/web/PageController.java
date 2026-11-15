package ext.library.example.web;

import cn.xbatis.core.sql.executor.chain.QueryChain;
import ext.library.example.domain.LoginLog;
import ext.library.example.domain.Note;
import ext.library.example.domain.NoteShare;
import ext.library.example.mapper.LoginLogMapper;
import ext.library.example.service.AttachmentService;
import ext.library.example.service.AuthService;
import ext.library.example.service.NoteService;
import ext.library.example.service.ShareService;
import ext.library.example.service.StatsService;
import ext.library.example.support.JobConfig;
import ext.library.job.JobDispatcher;
import ext.library.security.annotation.RequiresRoles;
import ext.library.security.annotation.SecurityIgnore;
import ext.library.servlet.response.PageQuery;
import ext.library.tool.exception.BizException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 页面路由。数据在服务端直接取，交互再走 /api/*。
 */
@Controller
@RequiredArgsConstructor
public class PageController {

    private final AuthService authService;

    private final NoteService noteService;

    private final ShareService shareService;

    private final AttachmentService attachmentService;

    private final StatsService statsService;

    private final LoginLogMapper loginLogMapper;

    private final JobDispatcher jobDispatcher;

    /** 审计日志没有实体，只有审计 SP1 的落库表，因此这一处直接用 SQL。 */
    private final JdbcClient jdbcClient;

    /** 队列积压：[待处理, 处理中, 已完成, 失败]。 */
    private record QueueStat(String name, long pending, long processing, long completed, long failed) {
    }

    @SecurityIgnore
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @SecurityIgnore
    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/notes")
    public String notes(@RequestParam(defaultValue = "") String keyword, Model model) {
        String owner = authService.currentUser().getEmail();
        PageQuery query = new PageQuery();
        model.addAttribute("page", noteService.page(owner, keyword, query));
        model.addAttribute("keyword", keyword);
        model.addAttribute("recent", statsService.recentViews(owner, noteService));
        model.addAttribute("user", authService.currentUser());
        return "notes";
    }

    @GetMapping("/notes/{id}")
    public String note(@PathVariable String id, Model model) {
        String owner = authService.currentUser().getEmail();
        Note note = noteService.requireOwner(id, owner);
        List<NoteShare> shares = shareService.listByNote(id);
        model.addAttribute("note", note);
        model.addAttribute("attachments", attachmentService.list(id, owner));
        model.addAttribute("shares", shares);
        model.addAttribute("views", statsService.viewCount(id));
        model.addAttribute("user", authService.currentUser());
        return "note-detail";
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        String email = authService.currentUser().getEmail();
        model.addAttribute("user", authService.currentUser());
        model.addAttribute("session", authService.currentSession());
        model.addAttribute("logins", latestLogins(email, 10));
        model.addAttribute("recent", statsService.recentViews(email, noteService));
        return "profile";
    }

    @RequiresRoles("ADMIN")
    @GetMapping("/admin")
    public String admin(Model model) {
        model.addAttribute("user", authService.currentUser());
        model.addAttribute("ownerStats", statsService.ownerStats(10));
        model.addAttribute("audits", jdbcClient.sql(
                        "SELECT operator, module, type, description, success, duration_ms, "
                                + "to_char(created_at, 'MM-DD HH24:MI:SS') AS created_at "
                                + "FROM audit_log ORDER BY id DESC LIMIT 20")
                .query().listOfRows());
        model.addAttribute("logs", latestLogins(null, 20));
        model.addAttribute("queues", List.of(queueStat(JobConfig.QUEUE_MAIL), queueStat(JobConfig.QUEUE_CLEANUP)));
        return "admin";
    }

    private QueueStat queueStat(String queue) {
        long[] stats = jobDispatcher.stats(queue);
        return new QueueStat(queue, stats[0], stats[1], stats[2], stats[3]);
    }

    /** email 为 null 时取全部人的最近登录。 */
    private List<LoginLog> latestLogins(String email, int limit) {
        QueryChain<LoginLog> chain = QueryChain.of(loginLogMapper);
        if (email != null) {
            chain.eq(LoginLog::getEmail, email);
        }
        return chain.orderByDesc(LoginLog::getCreatedAt).limit(limit).list();
    }

    /** 公开分享页：谁持有 token 谁能看，不看登录态。 */
    @SecurityIgnore
    @GetMapping("/share/{token}")
    public String share(@PathVariable String token, Model model) {
        try {
            NoteShare share = shareService.requireValid(token);
            Note note = noteService.find(share.getNoteId());
            model.addAttribute("note", note);
            model.addAttribute("expiresAt", share.getExpiresAt());
            model.addAttribute("views", statsService.viewCount(note.getId()));
        } catch (BizException e) {
            model.addAttribute("error", e.getMessage());
        }
        model.addAttribute("token", token);
        return "share";
    }
}
