package ext.library.tool.util;

import jakarta.annotation.Nonnull;

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
 */
public class TreeBuilder<T, K> {
    private final Function<T, K> idGetter;
    private final Function<T, K> parentIdGetter;
    private final ChildSetter<T> childSetter;
    private final Comparator<T> comparator;

    /**
     * 构造方法
     */
    public TreeBuilder(Function<T, K> idGetter,
                       Function<T, K> parentIdGetter,
                       ChildSetter<T> childSetter,
                       Comparator<T> comparator) {

        this.idGetter = Objects.requireNonNull(idGetter, "ID 获取器不能为 null");
        this.parentIdGetter = Objects.requireNonNull(parentIdGetter, "父 ID 获取器不能为 null");
        this.childSetter = Objects.requireNonNull(childSetter, "子节点设置器不能为 null");
        this.comparator = Objects.requireNonNull(comparator, "排序比较器不能为 null");
    }

    /**
     * 构建完整树结构
     */
    public List<T> buildTree(List<T> items) {
        Objects.requireNonNull(items, "节点列表不能为 null");
        if (items.isEmpty()) {return Collections.emptyList();}

        // 1. 构建数据索引
        Map<K, T> nodeMap = createNodeMap(items);
        Map<K, List<T>> parentChildrenMap = items.stream()
                .collect(Collectors.groupingBy(
                        parentIdGetter,
                        LinkedHashMap::new,  // 保持插入顺序
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
     * 判断是否为根节点（抽离方法提升可读性）
     */
    private boolean isRootNode(T item, Map<K, T> nodeMap) {
        K parentId = parentIdGetter.apply(item);
        return parentId == null || !nodeMap.containsKey(parentId);
    }

    /**
     * 构建搜索结果树
     */
    public List<T> buildSearchTree(List<T> allItems, Set<K> matchIds) {
        Objects.requireNonNull(allItems, "节点列表不能为 null");
        Objects.requireNonNull(matchIds, "匹配 ID 集合不能为 null");

        Set<K> relatedIds = findRelatedIds(allItems, matchIds);
        List<T> relatedItems = allItems.stream()
                .filter(item -> relatedIds.contains(idGetter.apply(item)))
                .collect(Collectors.toList());

        return buildTree(relatedItems);
    }

    /**
     * 创建节点 ID 映射表（含重复检测）
     */
    @Nonnull
    private Map<K, T> createNodeMap(@Nonnull List<T> items) {
        Map<K, T> map = new LinkedHashMap<>(items.size());
        for (T item : items) {
            K id = idGetter.apply(item);
            if (map.containsKey(id)) {
                throw new IllegalArgumentException(String.format(
                        "发现重复节点 ID: %s (冲突对象 1: %s, 冲突对象 2: %s)",
                        id, map.get(id), item));
            }
            map.put(id, item);
        }
        return map;
    }

    /**
     * 循环依赖检测核心逻辑
     */
    private void detectCyclicDependencies(@Nonnull List<T> items, Map<K, T> nodeMap) {
        Set<K> verifiedNodes = new HashSet<>();
        Map<K, K> idToParentMap = items.stream()
                .collect(Collectors.toMap(idGetter, parentIdGetter));

        for (T item : items) {
            K currentId = idGetter.apply(item);
            if (verifiedNodes.contains(currentId)) {continue;}

            Set<K> path = new LinkedHashSet<>();
            K tracingId = currentId;

            while (tracingId != null) {
                if (!path.add(tracingId)) {
                    throw new CyclicDependencyException(buildCyclePath(path, tracingId));
                }

                // 短路已验证节点
                if (verifiedNodes.contains(tracingId)) {break;}

                K parentId = idToParentMap.get(tracingId);
                if (parentId == null) {break;}

                // 直接循环检测
                if (parentId.equals(tracingId)) {
                    throw new CyclicDependencyException("直接循环依赖：" + tracingId);
                }

                tracingId = parentId;
            }
            verifiedNodes.addAll(path);
        }
    }

    /**
     * 构造循环路径描述
     */
    @Nonnull
    private String buildCyclePath(Set<K> path, K duplicateId) {
        List<K> pathList = new ArrayList<>(path);
        int index = pathList.indexOf(duplicateId);
        List<K> cycle = pathList.subList(index, pathList.size());
        return "检测到循环依赖链：" + cycle.stream()
                .map(Object::toString)
                .collect(Collectors.joining(" → "));
    }

    /**
     * 查找相关 ID 集合（匹配节点 + 路径节点）
     */
    private Set<K> findRelatedIds(@Nonnull List<T> allItems, @Nonnull Set<K> matchIds) {
        Map<K, K> idToParentMap = allItems.stream()
                .collect(Collectors.toMap(idGetter, parentIdGetter));

        return matchIds.stream()
                .flatMap(id -> traceAncestors(id, idToParentMap).stream())
                .collect(Collectors.toSet());
    }

    /**
     * 追溯父节点链
     */
    @Nonnull
    private Set<K> traceAncestors(K startId, Map<K, K> idToParentMap) {
        Set<K> ancestors = new LinkedHashSet<>();
        K currentId = startId;

        while (currentId != null && ancestors.add(currentId)) {
            currentId = idToParentMap.get(currentId);
        }
        return ancestors;
    }

    /**
     * 自定义循环依赖异常
     */
    public static class CyclicDependencyException extends RuntimeException {
        public CyclicDependencyException(String message) {
            super(message);
        }
    }

    /**
     * 子节点设置接口
     */
    @FunctionalInterface
    public interface ChildSetter<T> {
        void setChildren(T parent, List<T> children);
    }

    /* 快捷构造方法 */

    @Nonnull
    public static <T, K> TreeBuilder<T, K> create(
            Function<T, K> idGetter,
            Function<T, K> parentIdGetter,
            ChildSetter<T> childSetter,
            Comparator<T> comparator) {
        return new TreeBuilder<>(idGetter, parentIdGetter, childSetter, comparator);
    }

    @Nonnull
    public static <T, K extends Comparable<? super K>> TreeBuilder<T, K> createWithNaturalOrder(
            Function<T, K> idGetter,
            Function<T, K> parentIdGetter,
            ChildSetter<T> childSetter) {
        return new TreeBuilder<>(
                idGetter,
                parentIdGetter,
                childSetter,
                Comparator.comparing(idGetter, Comparator.nullsLast(Comparator.naturalOrder()))
        );
    }
}
