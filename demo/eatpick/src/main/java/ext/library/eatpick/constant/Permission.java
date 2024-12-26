package ext.library.eatpick.constant;

import java.util.List;

import jakarta.validation.constraints.NotNull;

import com.google.common.collect.Lists;

public interface Permission {
    String USER_ADD = "user:add";
    String USER_EDIT = "user:edit";
    String USER_DELETE = "user:delete";
    String USER_QUERY = "user:query";

    String RECIPE_ADD = "recipe:add";
    String RECIPE_EDIT = "recipe:edit";
    String RECIPE_DELETE = "recipe:delete";
    String RECIPE_QUERY = "recipe:query";

    String CATEGORY_ADD = "category:add";
    String CATEGORY_EDIT = "category:edit";
    String CATEGORY_DELETE = "category:delete";
    String CATEGORY_QUERY = "category:query";

    String ORDER_ADD = "order:add";
    String ORDER_EDIT = "order:edit";
    String ORDER_DELETE = "order:delete";
    String ORDER_QUERY = "order:query";

    String INGREDIENT_ADD = "ingredient:add";
    String INGREDIENT_EDIT = "ingredient:edit";
    String INGREDIENT_DELETE = "ingredient:delete";
    String INGREDIENT_QUERY = "ingredient:query";

    @NotNull
    static List<String> getCodeSet(@NotNull String role) {
        List<String> permissionCodes = Lists.newArrayList();
        permissionCodes.add(USER_QUERY);
        permissionCodes.add(RECIPE_QUERY);
        permissionCodes.add(CATEGORY_QUERY);
        permissionCodes.add(ORDER_QUERY);
        permissionCodes.add(INGREDIENT_QUERY);
        switch (role) {
            case Role.ADMIN:
                permissionCodes.add(USER_ADD);
                permissionCodes.add(USER_DELETE);
            case Role.CHEF:
                permissionCodes.add(RECIPE_ADD);
                permissionCodes.add(RECIPE_EDIT);
                permissionCodes.add(RECIPE_DELETE);
                permissionCodes.add(CATEGORY_ADD);
                permissionCodes.add(CATEGORY_EDIT);
                permissionCodes.add(CATEGORY_DELETE);
                permissionCodes.add(ORDER_EDIT);
                permissionCodes.add(INGREDIENT_ADD);
                permissionCodes.add(INGREDIENT_EDIT);
                permissionCodes.add(INGREDIENT_DELETE);
            case Role.CUSTOMER:
                permissionCodes.add(USER_EDIT);
                permissionCodes.add(ORDER_ADD);
                permissionCodes.add(ORDER_DELETE);
        }
        return permissionCodes;
    }

}
