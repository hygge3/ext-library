package ext.library.tool.util.tree;

import ext.library.tool.$;
import java.util.List;

/**
 * 树形节点标识，同时定义树形结果中节点的基本行为。
 * <p>
 * 请注意，树形结果的实现类必须实现该接口，同时需要添加{@link Comparable#compareTo(T)}方法，用于排序<p/>
 * 根据自己的实际业务需求，自行删除或实现排序方法
 *
 * @param <T> 节点类型，实现该接口的实现类
 * @param <I> 节点 ID 类型
 */
public interface TreeNode<T, I> extends Comparable<T> {

    /**
     * 获取当前节点的 ID，此 ID 值一般推荐使用数据库中的 ID。<p/>
     * 这里的 ID 值为了通用性，定义为泛型，根据实际业务需求注入自己的 ID 类型。
     *
     * @return 当前节点的 ID 值
     */
    I getId();

    /**
     * 获取当前节点的父节点 ID。默认实现返回 null，表示没有父节点。
     *
     * @return 父节点的 ID 值，如果没有父节点则返回 null。
     */
    I getParentId();

    /**
     * 设置节点的子节点列表
     *
     * @param children 子节点
     */
    void setChildren(List<T> children);

    /**
     * 获取当前节点的子节点集合，节点和子节点根据 id 和 parentId 进行关联
     *
     * @return 当前节点的子节点集合
     * @see TreeNode#getId()
     * @see TreeNode#getParentId()
     */
    List<T> getChildren();

    /**
     * 判断当前节点是否拥有子节点。该方法主要用于前端展示的判断依据。
     *
     * @return 是否拥有子节点，如果当前节点拥有子节点，则返回 true，否则返回 false。
     */
    @SuppressWarnings("unused")
    default boolean hasChild() {
        return $.isNotEmpty(getChildren());
    }

}
