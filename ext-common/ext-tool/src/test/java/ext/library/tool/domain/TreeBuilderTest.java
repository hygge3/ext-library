package ext.library.tool.domain;

import ext.library.tool.exception.ToolException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("树构建")
class TreeBuilderTest {

    /**
     * 节点
     */
    static class Node {

        private final Long id;

        private final Long parentId;

        private final String name;

        private List<Node> children = new ArrayList<>();

        Node(Long id, Long parentId, String name) {
            this.id = id;
            this.parentId = parentId;
            this.name = name;
        }

        Long getId() {
            return id;
        }

        Long getParentId() {
            return parentId;
        }

        String getName() {
            return name;
        }

        List<Node> getChildren() {
            return children;
        }

        void setChildren(List<Node> children) {
            this.children = children;
        }

    }

    private final TreeBuilder<Node, Long> builder = TreeBuilder.createWithNaturalOrder(
            Node::getId, Node::getParentId, Node::setChildren
    );

    @Test
    @DisplayName("根节点 parentId 为 null 时正常建树")
    void buildsTreeWithNullParent() {
        Node root = new Node(1L, null, "root");
        Node child = new Node(2L, 1L, "child");

        List<Node> tree = builder.buildTree(List.of(root, child));

        assertThat(tree).containsExactly(root);
        assertThat(root.getChildren()).containsExactly(child);
    }

    @Test
    @DisplayName("父节点不存在的孤儿节点作为根节点返回")
    void treatsMissingParentAsRoot() {
        Node root = new Node(1L, null, "root");
        Node orphan = new Node(3L, 99L, "orphan");

        List<Node> tree = builder.buildTree(List.of(root, orphan));

        assertThat(tree).containsExactly(root, orphan);
    }

    @Test
    @DisplayName("循环依赖抛 ToolException")
    void detectsCycle() {
        Node a = new Node(1L, 2L, "a");
        Node b = new Node(2L, 1L, "b");

        assertThatThrownBy(() -> builder.buildTree(List.of(a, b))).isInstanceOf(ToolException.class);
    }

    @Test
    @DisplayName("搜索结果树包含命中节点及其祖先")
    void buildsSearchTree() {
        Node root = new Node(1L, null, "root");
        Node middle = new Node(2L, 1L, "middle");
        Node leaf = new Node(3L, 2L, "leaf");

        List<Node> tree = builder.buildSearchTree(List.of(root, middle, leaf), Set.of(3L));

        assertThat(tree).containsExactly(root);
        assertThat(root.getChildren()).extracting(Node::getName).containsExactly("middle");
        assertThat(root.getChildren().getFirst().getChildren()).extracting(Node::getName).containsExactly("leaf");
    }

}
