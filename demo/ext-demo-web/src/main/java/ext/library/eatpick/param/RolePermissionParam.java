package ext.library.eatpick.param;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;

import lombok.Getter;
import lombok.Setter;

/**
 * 角色权限关联
 */
@Getter
@Setter
public class RolePermissionParam {
    @NotEmpty
    private List<Long> permissionIds;
    @NotEmpty
    private List<Long> roleIds;
}
