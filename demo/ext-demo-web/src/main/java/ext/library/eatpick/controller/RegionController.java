package ext.library.eatpick.controller;

import java.util.Collections;
import java.util.List;

import ext.library.eatpick.pojo.Region;
import ext.library.eatpick.service.RegionService;
import ext.library.tool.$;
import ext.library.web.annotation.RestWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 行政区 控制层。
 *
 * @author Mybatis-Flex Codegen
 * @since 2024-05-27
 */
@RestWrapper
@RestController
@RequiredArgsConstructor
@RequestMapping("regions")
public class RegionController {

    private final static int PROVINCE_CODE_LENGTH = 2;

    private final static int CITY_CODE_LENGTH = 4;

    private final RegionService regionService;

    /**
     * 根据提供的行政区划代码查询下级行政区划列表。
     * <p>
     * 本方法通过不同的代码长度来判断查询的级别，支持查询省、市、区/县三级行政区划。如果提供的代码为空或长度不匹配任何已知行政区划级别，则返回所有省份的列表。
     *
     * @param code 行政区划代码，可以是省、市、区/县的代码，根据代码长度决定查询的下级行政区划类型。
     * @return 下级行政区划的列表。如果无法确定查询类型或查询结果为空，则返回空列表。
     */
    @GetMapping
    public List<Region> subList(@RequestParam(required = false) String code) {
        // 判断代码是否为空或长度小于省的代码长度
        if ($.isEmpty(code) || code.length() < PROVINCE_CODE_LENGTH) {
            // 返回所有省份
            return regionService.provinces();
        } else if (code.length() == PROVINCE_CODE_LENGTH) {
            // 如果代码长度等于省的代码长度，查询该省的所有城市
            return regionService.cities(code);
        } else if (code.length() == CITY_CODE_LENGTH) {
            // 如果代码长度等于市的代码长度，查询该市的所有区/县
            return regionService.areas(code);
        } else {
            // 对于其他长度的代码，返回空列表，表示无法查询到下级行政区划
            return Collections.emptyList();
        }
    }

}
