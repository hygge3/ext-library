package ext.library.eatpick.service.impl;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import ext.library.eatpick.entity.Resource;
import ext.library.eatpick.mapper.ResourceMapper;
import ext.library.eatpick.query.ResourceQuery;
import ext.library.eatpick.service.ResourceService;
import ext.library.mybatis.page.PageResult;
import ext.library.mybatis.util.PageUtil;
import ext.library.tool.$;
import org.springframework.stereotype.Service;

import static ext.library.eatpick.entity.table.ResourceTableDef.RESOURCE;

/**
 * 资源 服务层实现。
 *
 * @author Auto Codegen By Ext
 * @since 2024-11-29
 */
@Service
public class ResourceServiceImpl extends ServiceImpl<ResourceMapper, Resource> implements ResourceService {

    @Override
    public PageResult<Resource> query(ResourceQuery query) {
        QueryWrapper wrapper = QueryWrapper.create();
        wrapper.like(RESOURCE.TITLE.getName(), query.getTitle());
        if ($.isNoneBlank(query.getAttributeKey(), query.getAttributeValue())) {
            wrapper.where($.format("{}->'$.{}'='{}'", RESOURCE.ATTRIBUTE.getName(), query.getAttributeKey(), query.getAttributeValue()));
        }
        Page<Resource> resourcePage = pageAs(PageUtil.build(query), wrapper, Resource.class);
        return new PageResult<>(resourcePage);


    }
}
