package ext.library.eatpick.controller;

import com.github.pagehelper.PageInfo;
import ext.library.eatpick.constant.Permission;
import ext.library.eatpick.entity.Recipe;
import ext.library.eatpick.param.RecipeBatchDisplayParam;
import ext.library.eatpick.param.RecipeParam;
import ext.library.eatpick.query.RecipeQuery;
import ext.library.eatpick.service.RecipeService;
import ext.library.eatpick.vo.RecipeVO;
import ext.library.mybatis.page.PageResult;
import ext.library.mybatis.util.PageUtil;
import ext.library.security.annotion.RequiresPermissions;
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

import static ext.library.eatpick.entity.table.RecipeTableDef.RECIPE;

/**
 * 菜谱控制器
 */
@Validated
@RestWrapper
@RestController
@RequiredArgsConstructor
@RequestMapping("recipes")
public class RecipeController {

    private final RecipeService recipeService;

    @PostMapping
    @RequiresPermissions(Permission.RECIPE_ADD)
    public Long save(@RequestBody RecipeParam param) {
        return recipeService.save(param);
    }

    @PutMapping("{id}")
    @RequiresPermissions(Permission.RECIPE_EDIT)
    public void update(@PathVariable Long id, @RequestBody RecipeParam param) {
        recipeService.update(id, param);
    }

    @PutMapping("display")
    @RequiresPermissions(Permission.RECIPE_DELETE)
    public void display(@RequestBody RecipeBatchDisplayParam param) {
        recipeService.updateChain().set(RECIPE.DISPLAY, param.getDisplay()).where(RECIPE.ID.in(param.getIds()));
    }

    @GetMapping("{id}")
    @RequiresPermissions(Permission.RECIPE_QUERY)
    public RecipeVO get(@PathVariable Long id) {
        return recipeService.detail(id);
    }

    @GetMapping
    @RequiresPermissions(Permission.RECIPE_QUERY)
    public PageResult<Recipe> page(RecipeQuery query) {
        PageInfo<Recipe> page = PageUtil.startPage(query, () -> recipeService.list(RECIPE.NAME.like(query.getName())));
        return new PageResult<>(page);
    }

}
