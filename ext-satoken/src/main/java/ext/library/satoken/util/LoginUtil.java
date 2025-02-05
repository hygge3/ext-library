package ext.library.satoken.util;

import java.util.Objects;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.StpUtil;
import ext.library.satoken.domain.SecurityUser;
import ext.library.tool.$;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;

/**
 * 登录鉴权助手
 * <p>
 * user_type 为 用户类型 同一个用户表 可以有多种用户类型 例如 pc,app deivce 为 设备类型 同一个用户类型 可以有 多种设备类型 例如 web,ios
 * 可以组成 用户类型与设备类型多对多的 权限灵活控制
 * <p>
 * 多用户体系 针对 多种用户类型 但权限控制不一致 可以组成 多用户类型表与多设备类型 分别控制权限
 */
@UtilityClass
public class LoginUtil {

    public static final String LOGIN_USER_KEY = "loginUser";

    public static final String TENANT_KEY = "tenantId";

    public static final String USER_KEY = "userId";

    public static final String USER_NAME_KEY = "userName";

    public static final String DEPT_KEY = "deptId";

    public static final String DEPT_NAME_KEY = "deptName";

    public static final String DEPT_CATEGORY_KEY = "deptCategory";

    public static final String CLIENT_KEY = "clientid";

    /**
     * 登录系统 基于 设备类型 针对相同用户体系不同设备
     *
     * @param loginUser 登录用户信息
     * @param model     配置参数
     */
    public static void login( SecurityUser loginUser, SaLoginModel model) {
        model = $.defaultIfNull(model, new SaLoginModel());
        StpUtil.login(loginUser.loginId(), model.setExtra(TENANT_KEY, loginUser.tenantId()).setExtra(USER_KEY, loginUser.loginId()).setExtra(USER_NAME_KEY, loginUser.username()));
        StpUtil.getTokenSession().set(LOGIN_USER_KEY, loginUser);
    }

    /**
     * 获取用户 (多级缓存)
     */
    @Nullable
    public static SecurityUser getLoginUser() {
        SaSession session = StpUtil.getTokenSession();
        if (Objects.isNull(session)) {
            return null;
        }
        return (SecurityUser) session.get(LOGIN_USER_KEY);
    }

    /**
     * 获取用户基于 token
     */
    @Nullable
    public static SecurityUser getLoginUser(String token) {
        SaSession session = StpUtil.getTokenSessionByToken(token);
        if (Objects.isNull(session)) {
            return null;
        }
        return (SecurityUser) session.get(LOGIN_USER_KEY);
    }

    /**
     * 获取用户 id
     */
    public static Long getUserId() {
        return $.toLong(getExtra(USER_KEY));
    }

    /**
     * 获取租户 ID
     */
    public static String getTenantId() {
        return $.toStr(getExtra(TENANT_KEY));
    }

    /**
     * 获取部门 ID
     */
    public static Long getDeptId() {
        return $.toLong(getExtra(DEPT_KEY));
    }

    /**
     * 获取部门名
     */
    public static String getDeptName() {
        return $.toStr(getExtra(DEPT_NAME_KEY));
    }

    /**
     * 获取部门类别编码
     */
    public static String getDeptCategory() {
        return $.toStr(getExtra(DEPT_CATEGORY_KEY));
    }

    /**
     * 获取当前 Token 的扩展信息
     *
     * @param key 键值
     * @return 对应的扩展数据
     */
    @Nullable
    private static Object getExtra(String key) {
        try {
            return StpUtil.getExtra(key);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取用户账户
     */
    public static String getUsername() {
        return getLoginUser().username();
    }

    /**
     * 获取用户类型
     */
    public static String getUserType() {
        return StpUtil.getLoginIdAsString();
    }

    /**
     * 检查当前用户是否已登录
     *
     * @return 结果
     */
    public static boolean isLogin() {
        return getLoginUser() != null;
    }

}
