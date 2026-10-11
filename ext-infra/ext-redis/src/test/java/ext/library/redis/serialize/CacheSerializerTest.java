package ext.library.redis.serialize;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CacheSerializer 测试
 */
@DisplayName("CacheSerializer 测试")
class CacheSerializerTest {

    // ========== 测试数据类 ==========

    static class User {
        private String name;
        private Integer age;

        public User() {
        }

        public User(String name, Integer age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }
    }

    // ========== getJavaType 测试 ==========

    @Test
    @DisplayName("测试获取简单类型的 JavaType")
    void testGetJavaTypeForSimpleType() {
        Type type = String.class;
        tools.jackson.databind.JavaType javaType = CacheSerializer.getJavaType(type);

        assertNotNull(javaType, "JavaType 不应为 null");
        assertEquals(String.class, javaType.getRawClass(), "原始类型应为 String");
    }

    @Test
    @DisplayName("测试获取泛型类型的 JavaType - List<String>")
    void testGetJavaTypeForGenericList() throws Exception {
        // 通过反射获取带有泛型返回类型的方法
        Method method = getClass().getDeclaredMethod("getStringListMethod");
        Type returnType = method.getGenericReturnType();

        tools.jackson.databind.JavaType javaType = CacheSerializer.getJavaType(returnType);

        assertNotNull(javaType, "JavaType 不应为 null");
        assertEquals(List.class, javaType.getRawClass(), "原始类型应为 List");
        assertTrue(javaType.hasGenericTypes(), "应该有泛型类型");
    }

    @Test
    @DisplayName("测试获取嵌套泛型类型的 JavaType - Map<String, List<User>>")
    void testGetJavaTypeForNestedGeneric() throws Exception {
        Method method = getClass().getDeclaredMethod("getNestedGenericMethod");
        Type returnType = method.getGenericReturnType();

        tools.jackson.databind.JavaType javaType = CacheSerializer.getJavaType(returnType);

        assertNotNull(javaType, "JavaType 不应为 null");
        assertEquals(Map.class, javaType.getRawClass(), "原始类型应为 Map");
        assertTrue(javaType.hasGenericTypes(), "应该有泛型类型");

        tools.jackson.databind.JavaType keyType = javaType.getKeyType();
        assertEquals(String.class, keyType.getRawClass(), "键类型应为 String");

        tools.jackson.databind.JavaType valueType = javaType.getContentType();
        assertEquals(List.class, valueType.getRawClass(), "值类型应为 List");
        assertTrue(valueType.hasGenericTypes(), "List 应该有泛型类型");
    }

    @Test
    @DisplayName("测试获取多级嵌套泛型类型的 JavaType")
    void testGetJavaTypeForMultiLevelNested() throws Exception {
        Method method = getClass().getDeclaredMethod("getMultiLevelNestedMethod");
        Type returnType = method.getGenericReturnType();

        tools.jackson.databind.JavaType javaType = CacheSerializer.getJavaType(returnType);

        assertNotNull(javaType, "JavaType 不应为 null");
        assertEquals(Map.class, javaType.getRawClass(), "原始类型应为 Map");

        tools.jackson.databind.JavaType valueType = javaType.getContentType();
        assertEquals(List.class, valueType.getRawClass(), "值类型应为 List");

        // 获取 List 的泛型类型
        tools.jackson.databind.JavaType listContentType = valueType.getContentType();
        assertEquals(User.class, listContentType.getRawClass(), "List 的泛型类型应为 User");
    }

    @Test
    @DisplayName("测试获取无泛型类型的 JavaType")
    void testGetJavaTypeForNonGeneric() {
        Type type = User.class;
        tools.jackson.databind.JavaType javaType = CacheSerializer.getJavaType(type);

        assertNotNull(javaType, "JavaType 不应为 null");
        assertEquals(User.class, javaType.getRawClass(), "原始类型应为 User");
        assertFalse(javaType.hasGenericTypes(), "不应该有泛型类型");
    }

    // ========== 测试辅助方法 ==========

    // 返回 List<String>
    private List<String> getStringListMethod() {
        return List.of();
    }

    // 返回 Map<String, List<User>>
    private Map<String, List<User>> getNestedGenericMethod() {
        return Map.of();
    }

    // 返回 Map<String, List<User>>
    private Map<String, List<User>> getMultiLevelNestedMethod() {
        return Map.of();
    }
}
