package ext.library.eatpick.service;

import java.util.List;

import com.mybatisflex.core.service.IService;
import ext.library.eatpick.entity.Order;
import ext.library.eatpick.vo.OrderVO;

/**
 * 订单 服务层。
 *
 * @author Auto Codegen By Ext
 * @since 2024-12-13
 */
public interface OrderService extends IService<Order> {
    String createOrder(List<Long> recipeIds,String notes);
    OrderVO detail(String id);
}
