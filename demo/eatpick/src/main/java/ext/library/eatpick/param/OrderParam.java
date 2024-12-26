package ext.library.eatpick.param;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;

import ext.library.eatpick.entity.Order;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AutoMapper(target = Order.class)
public class OrderParam {
    @NotEmpty
    private List<Long> recipeIds;
    private String notes;
}
