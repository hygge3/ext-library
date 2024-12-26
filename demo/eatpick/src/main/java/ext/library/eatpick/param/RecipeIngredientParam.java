package ext.library.eatpick.param;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import ext.library.eatpick.entity.RecipeIngredientBind;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AutoMapper(target = RecipeIngredientBind.class)
public class RecipeIngredientParam {
    @NotNull
    private Long ingredientId;
    @Min(0)
    private Double quantity;
    @NotBlank
    private String unit;
}
