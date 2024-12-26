package ext.library.eatpick.controller;

import java.time.LocalDateTime;

import ext.library.holidays.core.HolidaysApi;
import ext.library.security.annotion.SecurityIgnore;
import ext.library.tool.$;
import ext.library.web.annotation.RestWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 工具控制器
 */
@RestWrapper
@SecurityIgnore
@RestController
@RequiredArgsConstructor
@RequestMapping("tool")
public class ToolController {
    private final HolidaysApi holidaysApi;

    /**
     * 今天不工作
     *
     * @return boolean
     */
    @GetMapping("work")
    public String work(@RequestParam(required = false) LocalDateTime date) {
        return holidaysApi.isWeekdays($.defaultIfNull(date, LocalDateTime.now())) ? "😞" : "😄";
    }
}
