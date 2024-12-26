package ext.library.eatpick.param;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;

import lombok.Getter;
import lombok.Setter;

/**
 * 用户角色关联
 */
@Getter
@Setter
public class UserRoleParam {
    @NotEmpty
    private List<Long> userIds;
    @NotEmpty
    private List<Long> roleIds;
}
