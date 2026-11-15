package ext.library.example.web;

import ext.library.security.annotation.SecurityIgnore;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 错误页
 * <p>
 * 不接管时由 Spring Boot 的 {@code BasicErrorController} 兜底，而它也是 {@code HandlerMethod}，
 * 会被安全拦截器当成业务接口去校验登录态——未登录访问不存在的地址会拿到 401 而不是错误页。
 * 这里用自己的控制器接手并标 {@link SecurityIgnore}：错误页任何人可见。
 */
@Controller
@SecurityIgnore
public class AppErrorController implements ErrorController {

    @RequestMapping("/error")
    public String error(HttpServletRequest request, Model model) {
        Object status = request.getAttribute("jakarta.servlet.error.status_code");
        Object path = request.getAttribute("jakarta.servlet.error.request_uri");
        model.addAttribute("status", status == null ? 500 : status);
        model.addAttribute("path", path == null ? "" : path);
        return "error";
    }
}
