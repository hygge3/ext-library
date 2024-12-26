package ext.library.eatpick.controller;

import com.mybatisflex.core.paginate.Page;
import ext.library.eatpick.constant.Permission;
import ext.library.eatpick.entity.Order;
import ext.library.eatpick.param.OrderEditParam;
import ext.library.eatpick.param.OrderParam;
import ext.library.eatpick.service.OrderService;
import ext.library.eatpick.vo.OrderVO;
import ext.library.mybatis.page.PageParam;
import ext.library.mybatis.page.PageResult;
import ext.library.mybatis.util.PageUtil;
import ext.library.security.annotion.RequiresPermissions;
import ext.library.tool.$;
import ext.library.web.annotation.RestWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static ext.library.eatpick.entity.table.OrderTableDef.ORDER;


/**
 * 订单控制器
 */
@Validated
@RestWrapper
@RestController
@RequiredArgsConstructor
@RequestMapping("orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @RequiresPermissions(Permission.ORDER_ADD)
    public String createOrder(@Validated @RequestBody OrderParam param) {
        return orderService.createOrder(param.getRecipeIds(), param.getNotes());
    }

    @PutMapping("{id}")
    @RequiresPermissions(Permission.ORDER_EDIT)
    public void edit(@PathVariable String id, @Validated @RequestBody OrderEditParam param) {
        if ($.isNotNull(param.getStatus())) {
            orderService.updateChain().set(ORDER.STATUS, param.getStatus()).where(ORDER.ID.eq(id)).update();
        }
        if ($.isNotBlank(param.getReview())) {
            orderService.updateChain().set(ORDER.REVIEW, param.getReview()).where(ORDER.ID.eq(id)).update();
        }
    }

    @GetMapping("{id}")
    @RequiresPermissions(Permission.ORDER_QUERY)
    public OrderVO get(@PathVariable String id) {
        return orderService.detail(id);
    }

    @GetMapping
    @RequiresPermissions(Permission.ORDER_QUERY)
    public PageResult<Order> page(PageParam param) {
        Page<Order> page = orderService.page(PageUtil.build(param));
        return new PageResult<>(page);
    }

}
