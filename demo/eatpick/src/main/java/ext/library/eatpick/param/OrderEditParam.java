package ext.library.eatpick.param;

import ext.library.web.validation.constraints.Exclusion;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Exclusion({"status", "review"})
public class OrderEditParam {
    private Integer status;
    private String review;
}
