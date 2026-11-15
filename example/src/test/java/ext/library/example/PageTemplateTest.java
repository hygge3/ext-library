package ext.library.example;

import ext.library.example.domain.AppUser;
import ext.library.example.domain.LoginLog;
import ext.library.example.domain.Note;
import ext.library.example.domain.NoteAttachment;
import ext.library.security.domain.SecuritySession;
import ext.library.servlet.response.PageResult;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 每个页面渲染一遍：模板语法、消息键、字段名写错在构建期就暴露，不用等到浏览器点开。
 */
class PageTemplateTest {

    private static SpringTemplateEngine engine() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false);

        ResourceBundleMessageSource messages = new ResourceBundleMessageSource();
        messages.setBasename("i18n/messages");
        messages.setDefaultEncoding("UTF-8");

        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        engine.setTemplateEngineMessageSource(messages);
        return engine;
    }

    private static WebContext context(Map<String, Object> variables) {
        JakartaServletWebApplication application =
                JakartaServletWebApplication.buildApplication(new MockServletContext());
        WebContext context = new WebContext(application.buildExchange(
                new MockHttpServletRequest(), new MockHttpServletResponse()));
        context.setVariables(new HashMap<>(variables));
        return context;
    }

    private static LoginLog loginLog(boolean success) {
        LoginLog log = new LoginLog();
        log.setEmail("admin@example.com");
        log.setIp("127.0.0.1");
        log.setBrowser("Chrome");
        log.setOs("Windows");
        log.setDevice("PC");
        log.setSuccess(success);
        log.setCreatedAt(LocalDateTime.now());
        return log;
    }

    private static Note note() {
        Note note = new Note();
        note.setId("n1");
        note.setOwnerEmail("admin@example.com");
        note.setTitle("示例笔记");
        note.setContent("正文");
        note.setTags("demo");
        note.setTraceId("4bf92f3577b34da6a3ce929d0e0e4736");
        note.setCreatedAt(LocalDateTime.now());
        note.setUpdatedAt(LocalDateTime.now());
        return note;
    }

    private static Map<String, Object> base() {
        AppUser user = new AppUser();
        user.setEmail("admin@example.com");
        user.setNickname("管理员");
        user.setRole("ADMIN");

        Map<String, Object> variables = new HashMap<>();
        variables.put("user", user);
        return variables;
    }

    @Test
    void authPagesRender() {
        SpringTemplateEngine engine = engine();
        assertTrue(engine.process("login", context(Map.of())).contains("login-form"));
        assertTrue(engine.process("register", context(Map.of())).contains("captcha-img"));
    }

    @Test
    void notePagesRender() {
        SpringTemplateEngine engine = engine();

        Map<String, Object> list = base();
        list.put("page", new PageResult<>(1, 10, 1L, 1L, List.of(note())));
        list.put("recent", List.of(Map.of("id", "n1", "title", "示例笔记", "views", 3)));
        String notesHtml = engine.process("notes", context(list));
        assertTrue(notesHtml.contains("示例笔记"));

        NoteAttachment attachment = new NoteAttachment();
        attachment.setId("a1");
        attachment.setNoteId("n1");
        attachment.setFileName("a.png");
        attachment.setSize(12L);
        attachment.setContentType("image/png");

        Map<String, Object> detail = base();
        detail.put("note", note());
        detail.put("attachments", List.of(attachment));
        detail.put("shares", List.of());
        detail.put("views", 0L);
        String detailHtml = engine.process("note-detail", context(detail));
        assertTrue(detailHtml.contains("a.png"));
        assertTrue(detailHtml.contains("data-note-id=\"n1\""), "页面需要把 noteId 交给脚本");
    }

    @Test
    void sharePageRendersBothStates() {
        SpringTemplateEngine engine = engine();

        Map<String, Object> ok = new HashMap<>();
        ok.put("note", note());
        ok.put("expiresAt", LocalDateTime.now().plusDays(1));
        ok.put("views", 0L);
        ok.put("token", "t1");
        assertTrue(engine.process("share", context(ok)).contains("示例笔记"));

        Map<String, Object> expired = new HashMap<>();
        expired.put("error", "分享链接已过期");
        expired.put("token", "t1");
        assertTrue(engine.process("share", context(expired)).contains("分享链接已过期"));
    }

    @Test
    void profileAndAdminRender() {
        SpringTemplateEngine engine = engine();

        Map<String, Object> profile = base();
        profile.put("session", new SecuritySession());
        profile.put("logins", List.of(loginLog(true)));
        profile.put("recent", List.of());
        assertTrue(engine.process("profile", context(profile)).contains("127.0.0.1"));

        Map<String, Object> admin = base();
        admin.put("ownerStats", List.of(Map.of("owner_email", "admin@example.com", "notes", 2L,
                "last_active", LocalDateTime.now())));
        admin.put("audits", List.of(Map.of("operator", "admin@example.com", "module", "note", "type", "DELETE",
                "description", "删除笔记", "success", true, "duration_ms", 12L,
                "created_at", LocalDateTime.now())));
        admin.put("logs", List.of(loginLog(false)));
        admin.put("queues", List.of(Map.of("name", "share-mail", "pending", 0L, "processing", 0L,
                "completed", 3L, "failed", 1L)));
        String adminHtml = engine.process("admin", context(admin));
        assertFalse(adminHtml.isBlank());
        assertTrue(adminHtml.contains("删除笔记"));
    }
}
