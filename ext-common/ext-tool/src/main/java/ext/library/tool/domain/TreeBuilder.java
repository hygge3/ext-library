package ext.library.tool.domain;

import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.ToolException;
import ext.library.tool.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 通用树结构构建工具类
 *
 * <p>重要说明：
 * <ol>
 *   <li>所有节点必须具有唯一 ID</li>
 *   <li>父节点不存在时自动成为根节点</li>
 *   <li>节点排序依赖 comparator 实现</li>
 *   <li>支持循环依赖检测和错误路径提示</li>
 * </ol>
 *
 * @param <T> 原始数据类型
 * @param <K> 节点 ID 类型（建议使用包装类型）
 * @since 2025.01.01
 */
public record TreeBuilder<T, K>(
        Function<T, K> idGetter,
        Function<T, K> parentIdGetter,
        ChildSetter<T> childSetter,
        Comparator<T> comparator
) {

    /**
     * Compact constructor，校验参数非空
     */
    public TreeBuilder {
        Objects.requireNonNull(idGetter, "idGetter 不能为空");
        Objects.requireNonNull(parentIdGetter, "parentIdGetter 不能为空");
        Objects.requireNonNull(childSetter, "childSetter 不能为空");
        Objects.requireNonNull(comparator, "comparator 不能为空");
    }

    /**
     * 创建 TreeBuilder 实例
     *
     * @param idGetter       节点 ID 获取函数
     * @param parentIdGetter 父节点 ID 获取函数
     * @param childSetter    子节点设置器
     * @param comparator     节点排序比较器
     * @param <T>            原始数据类型
     * @param <K>            节点 ID 类型
     * @return TreeBuilder 实例
     */
    public static <T, K> TreeBuilder<T, K> create(
            Function<T, K> idGetter,
            Function<T, K> parentIdGetter,
            ChildSetter<T> childSetter,
            Comparator<T> comparator
    ) {
        return new TreeBuilder<>(idGetter, parentIdGetter, childSetter, comparator);
    }

    /**
     * 创建使用自然排序的 TreeBuilder 实例
     *
     * @param idGetter       节点 ID 获取函数
     * @param parentIdGetter 父节点 ID 获取函数
     * @param childSetter    子节点设置器
     * @param <T>            原始数据类型
     * @param <K>            节点 ID 类型（必须实现 Comparable）
     * @return TreeBuilder 实例
     */
    public static <T, K extends Comparable<? super K>> TreeBuilder<T, K> createWithNaturalOrder(
            Function<T, K> idGetter,
            Function<T, K> parentIdGetter,
            ChildSetter<T> childSetter
    ) {
        return new TreeBuilder<>(
                idGetter,
                parentIdGetter,
                childSetter,
                Comparator.comparing(idGetter, Comparator.nullsLast(Comparator.naturalOrder()))
        );
    }

    /**
     * 构建完整树结构
     *
     * @param items 待构建的节点列表
     * @return 根节点列表
     * @throws IllegalArgumentException 如果存在重复节点 ID
     * @throws ToolException            如果检测到循环依赖
     */
    public List<T> buildTree(List<T> items) {
        if (items.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. 构建数据索引
        Map<K, T> nodeMap = createNodeMap(items);
        // 保持插入顺序
        Map<K, List<T>> parentChildrenMap = items.stream()
                .collect(Collectors.groupingBy(
                        parentIdGetter,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        // 2. 循环依赖检测
        detectCyclicDependencies(items, nodeMap);

        // 3. 构建树结构
        nodeMap.forEach((nodeId, node) -> {
            List<T> children = parentChildrenMap.getOrDefault(nodeId, Collections.emptyList())
                    .stream()
                    .sorted(comparator)
                    .toList();
            childSetter.setChildren(node, children);
        });

        // 4. 获取根节点（parentId 为 null 或不存在于 nodeMap）
        return items.stream()
                .filter(item -> isRootNode(item, nodeMap))
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    /**
     * 判断是否为根节点
     *
     * @param item    节点
     * @param nodeMap 节点映射表
     * @return 如果是根节点返回 true
     */
    private boolean isRootNode(T item, Map<K, T> nodeMap) {
        K parentId = parentIdGetter.apply(item);
        return !nodeMap.containsKey(parentId);
    }

    /**
     * 构建搜索结果树
     * <p>
     * 根据匹配的节点 ID 集合，构建包含这些节点及其所有祖先节点的树结构
     *
     * @param allItems 所有节点列表
     * @param matchIds 匹配的节点 ID 集合
     * @return 包含匹配节点及其祖先的树结构
     */
    public List<T> buildSearchTree(List<T> allItems, Set<K> matchIds) {
        Set<K> relatedIds = findRelatedIds(allItems, matchIds);
        List<T> relatedItems = allItems.stream()
                .filter(item -> relatedIds.contains(idGetter.apply(item)))
                .collect(Collectors.toList());

        return buildTree(relatedItems);
    }

    /**
     * 创建节点 ID 映射表（含重复检测）
     *
     * @param items 节点列表
     * @return 节点 ID 到节点的映射表
     * @throws IllegalArgumentException 如果存在重复节点 ID
     */
    private Map<K, T> createNodeMap(List<T> items) {
        Map<K, T> map = new LinkedHashMap<>(items.size());
        for (T item : items) {
            K id = idGetter.apply(item);
            if (map.containsKey(id)) {
                throw new IllegalArgumentException(StringUtil.format(
                        "发现重复节点 ID: {} (冲突对象 1: {}, 冲突对象 2: {})",
                        id, map.get(id), item
                ));
            }
            map.put(id, item);
        }
        return map;
    }

    /**
     * 循环依赖检测核心逻辑
     *
     * @param items   节点列表
     * @param nodeMap 节点映射表
     * @throws ToolException 如果检测到循环依赖
     */
    private void detectCyclicDependencies(List<T> items, Map<K, T> nodeMap) {
        Set<K> verifiedNodes = new HashSet<>();
        Map<K, K> idToParentMap = items.stream()
                .collect(Collectors.toMap(idGetter, parentIdGetter));

        for (T item : items) {
            K currentId = idGetter.apply(item);
            if (verifiedNodes.contains(currentId)) {
                continue;
            }

            Set<K> path = new LinkedHashSet<>();
            K tracingId = currentId;

            while (true) {
                if (!path.add(tracingId)) {
                    throw new ToolException(EmojiSymbol.TOOL,
                            "检测到循环依赖链：{}",
                            buildCyclePath(path, tracingId));
                }

                // 短路已验证节点
                if (verifiedNodes.contains(tracingId)) {
                    break;
                }

                K parentId = idToParentMap.get(tracingId);
                if (parentId == null) {
                    break;
                }

                // 直接循环检测
                if (parentId.equals(tracingId)) {
                    throw new ToolException(EmojiSymbol.TOOL, "直接循环依赖：{}", tracingId);
                }

                tracingId = parentId;
            }
            verifiedNodes.addAll(path);
        }
    }

    /**
     * 构造循环路径描述
     *
     * @param path        已遍历的路径
     * @param duplicateId 重复出现的 ID（循环起点）
     * @return 循环路径描述字符串
     */
    private String buildCyclePath(Set<K> path, K duplicateId) {
        List<K> pathList = new ArrayList<>(path);
        int index = pathList.indexOf(duplicateId);
        List<K> cycle = pathList.subList(index, pathList.size());
        return cycle.stream()
                .map(Object::toString)
                .collect(Collectors.joining(" → "));
    }

    /**
     * 查找相关 ID 集合（匹配节点 + 祖先节点）
     *
     * @param allItems 所有节点列表
     * @param matchIds 匹配的节点 ID 集合
     * @return 相关节点 ID 集合
     */
    private Set<K> findRelatedIds(List<T> allItems, Set<K> matchIds) {
        Map<K, K> idToParentMap = allItems.stream()
                .collect(Collectors.toMap(idGetter, parentIdGetter));

        return matchIds.stream()
                .flatMap(id -> traceAncestors(id, idToParentMap).stream())
                .collect(Collectors.toSet());
    }

    /**
     * 追溯祖先节点链
     *
     * @param startId       起始节点 ID
     * @param idToParentMap ID 到父 ID 的映射表
     * @return 包含起始节点及其所有祖先的 ID 集合
     */
    private Set<K> traceAncestors(K startId, Map<K, K> idToParentMap) {
        Set<K> ancestors = new LinkedHashSet<>();
        K currentId = startId;

        while (currentId != null && ancestors.add(currentId)) {
            currentId = idToParentMap.get(currentId);
        }
        return ancestors;
    }

    /**
     * 子节点设置接口
     *
     * @param <T> 节点类型
     */
    @FunctionalInterface
    public interface ChildSetter<T> {

        /**
         * 设置父节点的子节点列表
         *
         * @param parent   父节点
         * @param children 子节点列表
         */
        void setChildren(T parent, List<T> children);
    }
}
