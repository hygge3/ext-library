package ext.library.eatpick.service.impl;

import java.util.List;

import com.google.common.collect.Lists;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import ext.library.core.util.BeanUtil;
import ext.library.eatpick.constant.OrderStatus;
import ext.library.eatpick.entity.Order;
import ext.library.eatpick.entity.OrderRecipeBind;
import ext.library.eatpick.entity.Recipe;
import ext.library.eatpick.mapper.OrderMapper;
import ext.library.eatpick.service.OrderRecipeBindService;
import ext.library.eatpick.service.OrderService;
import ext.library.eatpick.service.RecipeService;
import ext.library.eatpick.vo.OrderVO;
import ext.library.security.util.SecurityUtil;
import ext.library.tool.$;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static ext.library.eatpick.entity.table.OrderRecipeBindTableDef.ORDER_RECIPE_BIND;
import static ext.library.eatpick.entity.table.RecipeTableDef.RECIPE;

/**
 * 订单 服务层实现。
 *
 * @author Auto Codegen By Ext
 * @since 2024-12-13
 */
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final OrderRecipeBindService orderRecipeBindService;
    private final RecipeService recipeService;

    @Override
    public String createOrder(List<Long> recipeIds, String notes) {
        String orderId = $.toStr(System.currentTimeMillis());
        String loginId = SecurityUtil.getCurrentLoginId();

        Order order = new Order();
        order.setId(orderId);
        order.setCustomerId($.toLong(loginId));
        order.setStatus(OrderStatus.ORDER);
        order.setNotes(notes);
        save(order);

        List<OrderRecipeBind> binds = Lists.newArrayListWithCapacity(recipeIds.size());
        for (Long recipeId : recipeIds) {
            OrderRecipeBind orderRecipeBind = new OrderRecipeBind(orderId, recipeId);
            binds.add(orderRecipeBind);
        }
        orderRecipeBindService.saveBatch(binds);
        return orderId;
    }

    @Override
    public OrderVO detail(String id) {
        Order order = getById(id);
        if ($.isNull(order)) {
            return null;
        }
        List<Recipe> recipes = recipeService.list(QueryWrapper.create()
                .select(RECIPE.DEFAULT_COLUMNS)
                .from(ORDER_RECIPE_BIND)
                .leftJoin(RECIPE)
                .on(ORDER_RECIPE_BIND.RECIPE_ID.eq(RECIPE.ID))
                .where(ORDER_RECIPE_BIND.ORDER_ID.eq(id)));
        OrderVO orderVO = BeanUtil.convert(order, OrderVO.class);
        orderVO.setRecipes(recipes);
        return orderVO;
    }
}
