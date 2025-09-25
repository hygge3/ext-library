package ext.library.satoken.util;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import ext.library.satoken.domain.SecurityUser;
import ext.library.tool.util.GeneralTypeCastUtil;
import ext.library.tool.util.ObjectUtil;

import jakarta.annotation.Nonnull;
import java.util.Objects;

/**
 * 登录鉴权助手
 * <p>
 * user_type 为 用户类型 同一个用户表 可以有多种用户类型 例如 pc,app deivce 为 设备类型 同一个用户类型 可以有 多种设备类型 例如 web,ios
 * 可以组成 用户类型与设备类型多对多的 权限灵活控制
 * <p>
 * 多用户体系 针对 多种用户类型 但权限控制不一致 可以组成 多用户类型表与多设备类型 分别控制权限
 */
public class LoginUtil {

    private static final String LOGIN_USER_KEY = "loginUser";

    private static final String TENANT_KEY = "tenantId";

    private static final String USER_KEY = "userId";

    private static final String USER_NAME_KEY = "userName";

    private static final String DEPT_KEY = "deptId";

    private static final String DEPT_NAME_KEY = "deptName";

    private static final String DEPT_CATEGORY_KEY = "deptCategory";

    private static final String CLIENT_KEY = "clientid";

    /**
     * 登录系统 基于 设备类型 针对相同用户体系不同设备
     *
     * @param loginUser 登录用户信息
     * @param param     配置参数
     */
    public static void login(@Nonnull SecurityUser loginUser, SaLoginParameter param) {
        param = ObjectUtil.defaultIfNull(param, new SaLoginParameter());
        StpUtil.login(loginUser.loginId(), param.setExtra(TENANT_KEY, loginUser.tenantId()).setExtra(USER_KEY, loginUser.loginId()).setExtra(USER_NAME_KEY, loginUser.username()));
        StpUtil.getTokenSession().set(LOGIN_USER_KEY, loginUser);
    }

    /**
     * 获取用户 (多级缓存)
     */
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
        return GeneralTypeCastUtil.getAsLong(getExtra(USER_KEY));
    }

    /**
     * 获取租户 ID
     */
    public static String getTenantId() {
        return GeneralTypeCastUtil.getAsString(getExtra(TENANT_KEY));
    }

    /**
     * 获取部门 ID
     */
    public static Long getDeptId() {
        return GeneralTypeCastUtil.getAsLong(getExtra(DEPT_KEY));
    }

    /**
     * 获取部门名
     */
    public static String getDeptName() {
        return GeneralTypeCastUtil.getAsString(getExtra(DEPT_NAME_KEY));
    }

    /**
     * 获取部门类别编码
     */
    public static String getDeptCategory() {
        return GeneralTypeCastUtil.getAsString(getExtra(DEPT_CATEGORY_KEY));
    }

    /**
     * 获取当前 Token 的扩展信息
     *
     * @param key 键值
     *
     * @return 对应的扩展数据
     */
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