package ext.library.eatpick.param;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import ext.library.eatpick.entity.RecipeStep;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AutoMapper(target = RecipeStep.class)
public class RecipeStepParam {
    @NotNull
    private Integer order;
    @NotBlank
    private String desc;
}
