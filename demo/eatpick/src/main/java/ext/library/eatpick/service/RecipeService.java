package ext.library.eatpick.service;

import com.mybatisflex.core.service.IService;
import ext.library.eatpick.entity.Recipe;
import ext.library.eatpick.param.RecipeParam;
import ext.library.eatpick.vo.RecipeVO;

/**
 * 菜谱 服务层。
 *
 * @author Auto Codegen By Ext
 * @since 2024-12-13
 */
public interface RecipeService extends IService<Recipe> {
    /**
     * 保存
     *
     * @param param 参数
     * @return {@code Long }
     */
    Long save(RecipeParam param);

    /**
     * 更新
     *
     * @param id    主键
     * @param param 参数
     */
    void update(Long id, RecipeParam param);

    /**
     * 详情
     *
     * @param id 主键
     * @return {@code RecipeVO }
     */
    RecipeVO detail(Long id);
}
