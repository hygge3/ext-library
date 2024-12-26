package ext.library.eatpick.service.impl;

import java.util.List;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import ext.library.core.util.BeanUtil;
import ext.library.eatpick.entity.Ingredient;
import ext.library.eatpick.entity.Recipe;
import ext.library.eatpick.entity.RecipeIngredientBind;
import ext.library.eatpick.entity.RecipeStep;
import ext.library.eatpick.mapper.RecipeMapper;
import ext.library.eatpick.param.RecipeParam;
import ext.library.eatpick.service.IngredientService;
import ext.library.eatpick.service.RecipeIngredientBindService;
import ext.library.eatpick.service.RecipeService;
import ext.library.eatpick.service.RecipeStepService;
import ext.library.eatpick.vo.RecipeVO;
import ext.library.tool.$;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static ext.library.eatpick.entity.table.IngredientTableDef.INGREDIENT;
import static ext.library.eatpick.entity.table.RecipeIngredientBindTableDef.RECIPE_INGREDIENT_BIND;
import static ext.library.eatpick.entity.table.RecipeStepTableDef.RECIPE_STEP;

/**
 * 菜谱 服务层实现。
 *
 * @author Auto Codegen By Ext
 * @since 2024-12-13
 */
@Service
@RequiredArgsConstructor
public class RecipeServiceImpl extends ServiceImpl<RecipeMapper, Recipe> implements RecipeService {

    private final RecipeIngredientBindService recipeIngredientBindService;
    private final IngredientService ingredientService;
    private final RecipeStepService recipeStepService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long save(RecipeParam param) {
        Recipe recipe = BeanUtil.convert(param, Recipe.class);
        save(recipe);
        Long recipeId = recipe.getId();
        saveBind(recipeId, param);
        return recipeId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, RecipeParam param) {
        Recipe recipe = BeanUtil.convert(param, Recipe.class);
        recipe.setId(id);
        updateById(recipe);
        recipeIngredientBindService.remove(RECIPE_INGREDIENT_BIND.RECIPE_ID.eq(id));
        recipeStepService.remove(RECIPE_STEP.RECIPE_ID.eq(id));
        saveBind(id, param);
    }

    private void saveBind(Long recipeId, RecipeParam param) {
        List<RecipeIngredientBind> ingredients = BeanUtil.convert(param.getIngredients(), RecipeIngredientBind.class);
        ingredients.forEach(item -> item.setRecipeId(recipeId));
        List<RecipeStep> steps = BeanUtil.convert(param.getSteps(), RecipeStep.class);
        for (int i = 0; i < steps.size(); i++) {
            RecipeStep step = steps.get(i);
            step.setOrder(i + 1);
            step.setRecipeId(recipeId);
        }
        recipeIngredientBindService.saveBatch(ingredients);
        recipeStepService.saveBatch(steps);
    }

    @Override
    public RecipeVO detail(Long id) {
        Recipe recipe = getById(id);
        if ($.isNull(recipe)) {
            return null;
        }
        List<Ingredient> ingredients = ingredientService.list(QueryWrapper.create()
                .select(INGREDIENT.DEFAULT_COLUMNS)
                .from(RECIPE_INGREDIENT_BIND)
                .leftJoin(INGREDIENT)
                .on(RECIPE_INGREDIENT_BIND.INGREDIENT_ID.eq(INGREDIENT.ID))
                .where(RECIPE_INGREDIENT_BIND.RECIPE_ID.eq(id)));
        List<RecipeStep> recipeSteps = recipeStepService.list(RECIPE_STEP.RECIPE_ID.eq(id));
        RecipeVO recipeVO = BeanUtil.convert(recipe, RecipeVO.class);
        recipeVO.setIngredients(ingredients);
        recipeVO.setSteps(recipeSteps);
        return recipeVO;
    }

}
