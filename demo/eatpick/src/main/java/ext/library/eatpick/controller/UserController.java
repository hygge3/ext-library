package ext.library.eatpick.controller;

import ext.library.eatpick.constant.Permission;
import ext.library.eatpick.entity.User;
import ext.library.eatpick.param.PasswordParam;
import ext.library.eatpick.param.UserParam;
import ext.library.eatpick.service.UserService;
import ext.library.security.annotion.RequiresPermissions;
import ext.library.security.crypto.Encipher;
import ext.library.security.exception.UnauthorizedException;
import ext.library.security.util.SecurityUtil;
import ext.library.web.annotation.RestWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static ext.library.eatpick.entity.table.UserTableDef.USER;

/**
 * 用户控制器
 */
@Validated
@RestWrapper
@RestController
@RequiredArgsConstructor
@RequestMapping("users")
public class UserController {
    private final UserService userService;

    @PutMapping
    @RequiresPermissions(Permission.USER_EDIT)
    public void update(@RequestBody UserParam param) {
        String loginId = SecurityUtil.getCurrentLoginId();
        userService.updateChain()
                .set(USER.NICKNAME, param.getNickname())
                .set(USER.AVATAR, param.getAvatar())
                .where(USER.ID.eq(loginId)).update();
    }

    @PutMapping("password")
    @RequiresPermissions(Permission.USER_EDIT)
    public void updatePassword(@RequestBody PasswordParam param) {
        String loginId = SecurityUtil.getCurrentLoginId();
        User user = userService.getById(loginId);
        // 验证密码
        if (!Encipher.checkByBCrypt(param.getOldPassword(), user.getPassword())) {
            throw new UnauthorizedException("旧密码错误");
        }
        String encryptPassword = Encipher.encryptByBCrypt(param.getNewPassword());
        userService.updateChain()
                .set(USER.PASSWORD, encryptPassword)
                .where(USER.ID.eq(loginId)).update();
    }
}
