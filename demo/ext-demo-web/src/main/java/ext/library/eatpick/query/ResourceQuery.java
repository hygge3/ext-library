package ext.library.eatpick.query;

import ext.library.mybatis.page.PageParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResourceQuery extends PageParam {
    /** 姓名 */
    private String title;
    /** 属性 key */
    private String attributeKey;
    /** 属性 value */
    private String attributeValue;
}
