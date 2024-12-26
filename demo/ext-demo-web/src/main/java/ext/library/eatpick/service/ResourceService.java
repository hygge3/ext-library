package ext.library.eatpick.service;

import com.mybatisflex.core.service.IService;
import ext.library.eatpick.entity.Resource;
import ext.library.eatpick.query.ResourceQuery;
import ext.library.mybatis.page.PageResult;

/**
 * 资源 服务层。
 *
 * @author Auto Codegen By Ext
 * @since 2024-11-29
 */
public interface ResourceService extends IService<Resource> {
    PageResult<Resource> query(ResourceQuery query);
}
