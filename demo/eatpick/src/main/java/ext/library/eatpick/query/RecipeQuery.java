package ext.library.eatpick.query;

import ext.library.mybatis.page.PageParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecipeQuery extends PageParam {
    /** 名称 */
    private String name;
}
