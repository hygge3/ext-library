package ext.library.eatpick.controller;

import java.util.List;

import ext.library.core.util.BeanUtil;
import ext.library.eatpick.constant.Permission;
import ext.library.eatpick.entity.Category;
import ext.library.eatpick.param.CategoryParam;
import ext.library.eatpick.service.CategoryService;
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
 * 类别控制器
 */
@Validated
@RestWrapper
@RestController
@RequiredArgsConstructor
@RequestMapping("categories")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @RequiresPermissions(Permission.CATEGORY_ADD)
    public Long save(@RequestBody @Validated CategoryParam param) {
        Category category = BeanUtil.convert(param, Category.class);
        categoryService.save(category);
        return category.getId();
    }

    @PutMapping("{id}")
    @RequiresPermissions(Permission.CATEGORY_EDIT)
    public void update(@PathVariable Long id, @RequestBody @Validated CategoryParam param) {
        Category category = BeanUtil.convert(param, Category.class);
        category.setId(id);
        categoryService.updateById(category);
    }

    @DeleteMapping("{id}")
    @RequiresPermissions(Permission.CATEGORY_DELETE)
    public void remove(@PathVariable Long id) {
        categoryService.removeById(id);
    }

    @GetMapping("{id}")
    @RequiresPermissions(Permission.CATEGORY_QUERY)
    public Category get(@PathVariable Long id) {
        return categoryService.getById(id);
    }

    @GetMapping
    @RequiresPermissions(Permission.CATEGORY_QUERY)
    public List<Category> list() {
        return categoryService.list();
    }

}
