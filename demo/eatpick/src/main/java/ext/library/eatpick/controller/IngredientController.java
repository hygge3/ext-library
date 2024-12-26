package ext.library.eatpick.controller;

import com.github.pagehelper.PageInfo;
import ext.library.core.util.BeanUtil;
import ext.library.eatpick.constant.Permission;
import ext.library.eatpick.entity.Ingredient;
import ext.library.eatpick.param.IngredientParam;
import ext.library.eatpick.query.RecipeQuery;
import ext.library.eatpick.service.IngredientService;
import ext.library.mybatis.page.PageResult;
import ext.library.mybatis.util.PageUtil;
import ext.library.security.annotion.RequiresPermissions;
import ext.library.web.annotation.RestWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 食材控制器
 */
@Validated
@RestWrapper
@RestController
@RequiredArgsConstructor
@RequestMapping("ingredients")
public class IngredientController {

    private final IngredientService ingredientService;

    @PostMapping
    @RequiresPermissions(Permission.INGREDIENT_ADD)
    public Long save(@RequestBody @Validated IngredientParam param) {
        Ingredient ingredient = BeanUtil.convert(param, Ingredient.class);
        ingredientService.save(ingredient);
        return ingredient.getId();
    }

    @PutMapping("{id}")
    @RequiresPermissions(Permission.INGREDIENT_EDIT)
    public void update(@PathVariable Long id, @RequestBody @Validated IngredientParam param) {
        Ingredient ingredient = BeanUtil.convert(param, Ingredient.class);
        ingredient.setId(id);
        ingredientService.updateById(ingredient);
    }

    @DeleteMapping("{id}")
    @RequiresPermissions(Permission.INGREDIENT_DELETE)
    public void remove(@PathVariable Long id) {
        ingredientService.removeById(id);
    }

    @GetMapping("{id}")
    @RequiresPermissions(Permission.INGREDIENT_QUERY)
    public Ingredient get(@PathVariable Long id) {
        return ingredientService.getById(id);
    }

    @GetMapping
    @RequiresPermissions(Permission.INGREDIENT_QUERY)
    public PageResult<Ingredient> page(RecipeQuery query) {
        PageInfo<Ingredient> pageInfo = PageUtil.startPage(query, ingredientService::list);
        return new PageResult<>(pageInfo);
    }

}
