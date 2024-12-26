package ext.library.eatpick.controller;

import ext.library.core.util.BeanUtil;
import ext.library.eatpick.entity.Resource;
import ext.library.eatpick.param.ResourceParam;
import ext.library.eatpick.query.ResourceQuery;
import ext.library.eatpick.service.ResourceService;
import ext.library.mybatis.page.PageResult;
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

@Validated
@RestWrapper
@RestController
@RequiredArgsConstructor
@RequestMapping("resources")
public class ResourceController {

    private final ResourceService resourceService;

    @PostMapping
    @RequiresPermissions("data:resource:add")
    public Long save(@RequestBody ResourceParam param) {
        Resource resource = BeanUtil.convert(param, Resource.class);
        resourceService.save(resource);
        return resource.getId();
    }

    @PutMapping("{id}")
    @RequiresPermissions("data:resource:edit")
    public void update(@PathVariable Long id, @RequestBody ResourceParam param) {
        Resource resource = BeanUtil.convert(param, Resource.class);
        resourceService.updateById(resource);
    }

    @DeleteMapping("{id}")
    @RequiresPermissions("data:resource:delete")
    public void remove(@PathVariable Long id) {
        resourceService.removeById(id);
    }

    @GetMapping("{id}")
    @RequiresPermissions("data:resource:query")
    public Resource get(@PathVariable Long id) {
        return resourceService.getById(id);
    }

    @GetMapping
    @RequiresPermissions("data:resource:query")
    public PageResult<Resource> page(ResourceQuery query) {
        return resourceService.query(query);
    }

}
