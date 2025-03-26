package ext.library.mybatis.page;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

import com.mybatisflex.core.paginate.Page;
import ext.library.core.constant.Pageable;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * 分页查询参数
 */
@Data
@Schema(title = "分页查询参数")
public class PageParam {

    @Schema(title = "当前页码", description = "从 1 开始", defaultValue = "1", example = "1")
    @Min(value = 1, message = "当前页不能小于 1")
     long page = 1;

    @Schema(title = "每页显示条数", description = "最大值为系统设置，默认 100", defaultValue = "10")
    @Min(value = 1, message = "每页显示条数不能小于 1")
     long size = 10;

    @Schema(title = "排序规则")
    @Valid
     List<Sort> sorts = new ArrayList<>();

    @Schema(title = "排序元素载体")
    @Getter
    @Setter
    public static class Sort {

        @Schema(title = "排序字段", example = "id")
        @Pattern(regexp = Pageable.SORT_FILED_REGEX, message = "排序字段格式非法")
        private String field;

        @Schema(title = "是否正序排序", example = "false")
        private boolean asc;

    }

    public <T> Page<T> toPage() {
        return new Page<>(this.getPage(), this.getSize());
    }

}
