package ext.library.security.util;

import ext.library.core.util.SpringUtil;
import ext.library.security.authority.SecurityAuthority;
import ext.library.security.domain.SecuritySession;
import ext.library.security.enums.Logical;
import ext.library.tool.util.ObjectUtil;
import ext.library.tool.util.StringUtil;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 权限校验工具
 */
@UtilityClass
public class PermissionUtil {

    private static final SecurityAuthority authority = SpringUtil.getBean(SecurityAuthority.class);

    /**
     * 当前用户是否有角色
     */
    public boolean hasRole(String role) {
        SecuritySession securitySession = SecurityUtil.getCurrentSecuritySession();
        List<String> roleList = authority.getRoleCodeList(securitySession.getLoginId());
        return null != roleList && roleList.stream().anyMatch(item -> strMatch(item, role));
    }

    /**
     * 当前用户是否有权限
     */
    public boolean hasPermission(String permission) {
        SecuritySession securitySession = SecurityUtil.getCurrentSecuritySession();
        List<String> permissionCodeList = authority.getPermissionCodeList(securitySession.getLoginId());
        return null != permissionCodeList && permissionCodeList.stream().anyMatch(item -> strMatch(item, permission));
    }

    /**
     * 当前用户是否有角色
     */
    public List<String> getRoles() {
        SecuritySession securitySession = SecurityUtil.getCurrentSecuritySession();
        return authority.getRoleCodeList(securitySession.getLoginId());
    }

    /**
     * 当前用户是否有权限
     */
    public List<String> getPermissions() {
        SecuritySession securitySession = SecurityUtil.getCurrentSecuritySession();
        return authority.getPermissionCodeList(securitySession.getLoginId());
    }

    /**
     * 拥有角色/权限校验
     *
     * @param requires 需要权限
     * @param logical  条件类型
     * @param has      已有权限
     *
     * @return true 条件成立 false 条件不成立
     */
    public boolean hasMultiPermValid(List<String> requires, Logical logical, List<String> has) {
        // 如果没有指定要校验的角色，那么直接跳过
        if (ObjectUtil.isEmpty(requires)) {
            return true;
        }

        return switch (logical) {
            case AND:
                for (String req : requires) {
                    if (!hasElement(has, req)) {
                        yield false;
                    }
                }
            case OR:
                for (String req : requires) {
                    if (hasElement(has, req)) {
                        yield true;
                    }
                }
                yield false;
        };
    }

    /**
     * 判断：集合中是否包含指定元素（模糊匹配）
     *
     * @param list    列表
     * @param element 元素
     *
     * @return boolean
     */
    public boolean hasElement(List<String> list, String element) {
        // 空集合直接返回 false
        if (ObjectUtil.isEmpty(list)) {
            return false;
        }
        // 先尝试一下简单匹配，如果可以匹配成功则无需继续模糊匹配
        if (list.contains(element)) {
            return true;
        }
        // 开始模糊匹配
        for (String patt : list) {
            if (strMatch(element, patt)) {
                return true;
            }
        }
        // 走出 for 循环说明没有一个元素可以匹配成功
        return false;
    }

    /**
     * 两个字符串是否匹配，支持正则表达式
     */
    public boolean strMatch(String s1, String s2) {
        // 两者均为 null 时，直接返回 true
        if (s1 == null && s2 == null) {
            return true;
        }
        // 两者其一为 null 时，直接返回 false
        if (StringUtil.isAnyBlank(s1, s2)) {
            return false;
        }
        // 如果表达式不带有*号，则只需简单 equals 即可 (这样可以使速度提升 200 倍左右)
        if (!s2.contains("*")) {
            return ObjectUtil.equalsSafe(s1, s2);
        }
        return Pattern.matches(s2.replace("*", ".*"), s1);
    }

}