package ext.library.tool.util.tree;


import jakarta.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import ext.library.tool.$;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.experimental.UtilityClass;

/**
 * TreeUtils 类提供了树形结构的构建方法，当一个列表数据需要转换成树形结构时，可以使用该类提供的方法。
 * <p>
 * 但列表类型必须实现接口{@link TreeNode}，在接口中定义了父子结构的关联属性，此处构建则是依据该关联属性进行构建。
 */
@UtilityClass
public class TreeUtil {

    /**
     * 根据一个 TreeNode 集合，返回构建好的树列表
     *
     * @param nodes  TreeNode 集合
     * @param rootId 根节点 Id
     * @param <T>    TreeNode 的子类
     * @param <I>    TreeNodeId 的类型
     * @return 树列表
     */
    public <T extends TreeNode<T, I>, I> List<T> buildTree(List<T> nodes, I rootId) {
        return buildTree(nodes, rootId, Function.identity(), null);
    }

    /**
     * 根据一个 TreeNode 集合，返回构建好的树列表
     *
     * @param nodes      TreeNode 集合
     * @param rootId     根节点 Id
     * @param comparator 树节点排序规则
     * @param <T>        TreeNode 的子类
     * @param <I>        TreeNodeId 的类型
     * @return 树列表
     */
    public <T extends TreeNode<T, I>, I> List<T> buildTree(List<T> nodes, I rootId,
                                                           Comparator<? super T> comparator) {
        return buildTree(nodes, rootId, Function.identity(), comparator);
    }

    /**
     * 根据源数据列表转换为树
     *
     * @param list          源数据列表
     * @param rootId        根节点 Id
     * @param convertToTree 转换方法
     * @param <T>           TreeNode 的子类
     * @param <I>           TreeNodeId 的类型
     * @param <R>           源数据类型
     * @return 树列表
     */
    public <T extends TreeNode<T, I>, I, R> List<T> buildTree(List<R> list, I rootId,
                                                              Function<R, T> convertToTree) {
        return buildTree(list, rootId, convertToTree, null);
    }

    /**
     * 根据源数据列表转换为树
     *
     * @param list          源数据列表
     * @param rootId        根节点 Id
     * @param convertToTree 转换方法
     * @param comparator    树节点排序规则
     * @param <T>           TreeNode 的子类
     * @param <I>           TreeNodeId 的类型
     * @param <R>           源数据类型
     * @return 树列表
     */
    public <T extends TreeNode<T, I>, I, R> List<T> buildTree(List<R> list, I rootId, Function<R, T> convertToTree,
                                                              Comparator<? super T> comparator) {
        if ($.isEmpty(list)) {
            return Lists.newArrayList();
        }

        // 转换为 TreeNode
        Stream<T> tStream = list.stream().map(convertToTree);
        // 如果需要排序，则在收集时进行排序处理
        if (comparator != null) {
            tStream = tStream.sorted(comparator);
        }
        // 根据 parentId 进行分组
        Map<I, List<T>> childrenMap = tStream
                .collect(Collectors.groupingBy(T::getParentId, LinkedHashMap::new, Collectors.toList()));

        // 根据根节点 ID 拿到一级节点
        List<T> treeList = childrenMap.get(rootId);
        // 异常数据校验
        Preconditions.checkArgument($.isNotEmpty(treeList), "Wrong data, no child node of the root node can be found");
        // 遍历所有一级节点，赋值其子节点
        treeList.forEach(node -> TreeUtil.setChildren(node, childrenMap));
        return treeList;
    }

    /**
     * 从所有节点列表中查找并设置 parent 的所有子节点
     *
     * @param parent      父节点
     * @param childrenMap 子节点集合 Map(k: parentId, v: Node)
     */
    public <T extends TreeNode<T, I>, I> void setChildren(@Nonnull T parent, @Nonnull Map<I, List<T>> childrenMap) {
        I parentId = parent.getId();
        List<T> children = childrenMap.get(parentId);
        // 如果有孩子节点则赋值，且给孩子节点的孩子节点赋值
        if ($.isNotEmpty(children)) {
            parent.setChildren(children);
            children.forEach(node -> TreeUtil.setChildren(node, childrenMap));
        } else {
            parent.setChildren(Lists.newArrayList());
        }
    }

    /**
     * 获取指定树节点下的所有叶子节点
     *
     * @param parent 父节点
     * @param <T>    树节点的类型
     * @param <I>    树节点的 id 类型
     * @return 叶子节点
     */
    public <T extends TreeNode<T, I>, I> List<T> getLeafs(T parent) {
        List<T> leafs = Lists.newArrayList();
        fillLeaf(parent, leafs);
        return leafs;
    }

    /**
     * 将 parent 的所有叶子节点填充至 leafs 列表中
     *
     * @param parent 父节点
     * @param leafs  叶子节点列表
     * @param <T>    实际节点类型
     */
    public <T extends TreeNode<T, I>, I> void fillLeaf(@Nonnull T parent, List<T> leafs) {
        List<T> children = parent.getChildren();
        // 如果节点没有子节点则说明为叶子节点
        if ($.isEmpty(children)) {
            leafs.add(parent);
            return;
        }
        // 递归调用子节点，查找叶子节点
        for (T child : children) {
            fillLeaf(child, leafs);
        }
    }

    /**
     * 获取树节点 Id
     *
     * @param treeList 树列表
     * @param <T>      TreeNode 实现类
     * @param <I>      TreeNodeId 类型
     * @return List<I> 节点 Id 列表
     */
    public <T extends TreeNode<T, I>, I> List<I> getTreeNodeIds(List<T> treeList) {
        List<I> ids = Lists.newArrayList();
        fillTreeNodeIds(ids, treeList);
        return ids;
    }

    /**
     * 填充树节点 Id
     *
     * @param ids      节点 Id 列表
     * @param treeList 树列表
     * @param <T>      TreeNode 实现类
     * @param <I>      TreeNodeId 类型
     */
    public <T extends TreeNode<T, I>, I> void fillTreeNodeIds(List<I> ids, List<T> treeList) {
        // 如果节点没有子节点则说明为叶子节点
        if ($.isEmpty(treeList)) {
            return;
        }
        for (T treeNode : treeList) {
            ids.add(treeNode.getId());
            List<T> children = treeNode.getChildren();
            if ($.isNotEmpty(children)) {
                fillTreeNodeIds(ids, children);
            }
        }
    }

    /**
     * 将一颗树的所有节点平铺到一个 list 中
     *
     * @param treeNode 树节点
     * @param <T>      树节点的类型
     * @param <I>      树节点的 id 类型
     * @return 所有树节点组成的列表
     */
    public <T extends TreeNode<T, I>, I> List<T> treeToList(T treeNode) {
        return treeToList(treeNode, Function.identity());
    }

    /**
     * 将一颗树的所有节点平铺到 list 中
     *
     * @param treeNode  树节点
     * @param converter 转换器，用于将树节点的类型进行转换，再存储到 list 中
     * @param <T>       树节点的类型
     * @param <I>       树节点的 id 类型
     * @param <R>       转换器转换后的类型
     * @return List<R>
     */
    public <T extends TreeNode<T, I>, I, R> List<R> treeToList(T treeNode, Function<T, R> converter) {
        List<R> list = Lists.newArrayList();

        // 使用队列存储未处理的树节点
        Queue<T> queue = Lists.newLinkedList();
        queue.add(treeNode);

        while (!queue.isEmpty()) {
            // 弹出一个树节点
            T node = queue.poll();
            if (node == null) {
                continue;
            }

            // 如果当前节点的含有子节点，则添加到队列中
            List<T> children = node.getChildren();
            if ($.isNotEmpty(children)) {
                queue.addAll(children);
            }

            // 不再保留对子节点的引用
            node.setChildren(null);
            // 转换树节点，并将结果添加到 list 中
            list.add(converter.apply(node));
        }
        return list;
    }

    /**
     * 将一组树的所有节点平铺到一个 list 中
     *
     * @param treeNodes 树节点集合
     * @param <T>       树节点的类型
     * @param <I>       树节点的 id 类型
     * @return 所有树节点组成的列表
     */
    public <T extends TreeNode<T, I>, I> List<T> treeToList(List<T> treeNodes) {
        return treeToList(treeNodes, Function.identity());
    }

    /**
     * 将一组树的所有节点平铺到一个 list 中
     *
     * @param treeNodes 树节点集合
     * @param converter 转换器，用于将树节点的类型进行转换，再存储到 list 中
     * @param <T>       树节点的类型
     * @param <I>       树节点的 id 类型
     * @param <R>       转换器转换后的类型
     * @return 所有树节点组成的列表
     */
    public <T extends TreeNode<T, I>, I, R> List<R> treeToList(@Nonnull List<T> treeNodes, Function<T, R> converter) {
        return treeNodes.stream()
                .map(node -> treeToList(node, converter))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /**
     * 根据指定规则进行树剪枝
     *
     * @param treeNodes 待剪枝的树节点列表
     * @param <T>       TreeNode
     * @param matcher   匹配规则
     * @return 剪枝完成后的树节点列表
     */
    public <T extends TreeNode<T, I>, I> List<T> pruneTree(List<T> treeNodes, Predicate<T> matcher) {
        List<T> result = Lists.newArrayList();
        if ($.isEmpty(treeNodes)) {
            return result;
        }
        for (T treeNode : treeNodes) {
            List<T> children = pruneTree(treeNode.getChildren(), matcher);
            if (!children.isEmpty()) {
                treeNode.setChildren(children);
                result.add(treeNode);
            } else if (matcher.test(treeNode)) {
                treeNode.setChildren(null);
                result.add(treeNode);
            }
        }
        return result;
    }

    /**
     * 根据指定规则进行树剪枝
     *
     * @param treeNode 待剪枝的树节点
     * @param <T>      TreeNode
     * @param matcher  匹配规则
     * @return 剪枝完成后的树节点
     */
    public <T extends TreeNode<T, I>, I> T pruneTree(@Nonnull T treeNode, Predicate<T> matcher) {
        List<T> children = pruneTree(treeNode.getChildren(), matcher);
        boolean childrenMatched = !children.isEmpty();
        if (childrenMatched) {
            treeNode.setChildren(children);
        }
        boolean nodeMatched = matcher.test(treeNode);
        return (nodeMatched || childrenMatched) ? treeNode : null;
    }

    /**
     * 遍历树节点（深度优先）
     */
    public <T extends TreeNode<T, I>, I> void forEachDFS(T treeNode, T parentTreeNode, @Nonnull BiConsumer<T, T> action) {
        action.accept(treeNode, parentTreeNode);
        List<T> children = treeNode.getChildren();
        forEachDFS(children, parentTreeNode, action);
    }

    /**
     * 遍历树节点（深度优先）
     */
    public <T extends TreeNode<T, I>, I> void forEachDFS(List<T> treeNodes, T parentTreeNode,
                                                         BiConsumer<T, T> action) {
        if ($.isEmpty(treeNodes)) {
            return;
        }
        for (T treeNode : treeNodes) {
            List<T> children = treeNode.getChildren();
            action.accept(treeNode, parentTreeNode);
            forEachDFS(children, treeNode, action);
        }
    }

}
