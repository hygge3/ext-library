package ext.library.eatpick.param;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecipeBatchDisplayParam {
    @NotEmpty
    List<Long> ids;
    @NotNull
    Boolean display;
}
