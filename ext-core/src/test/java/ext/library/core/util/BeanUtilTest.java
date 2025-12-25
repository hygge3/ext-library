package ext.library.core.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * BeanUtil 工具类测试
 */
@DisplayName("BeanUtil 工具类测试")
class BeanUtilTest {

    // ========== 测试实体类 ==========

    public static class User implements Serializable {
        private String name;
        private Integer age;
        private String email;
        private Address address;

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

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public Address getAddress() {
            return address;
        }

        public void setAddress(Address address) {
            this.address = address;
        }
    }

    public static class Address implements Serializable {
        private String city;
        private String street;

        public Address() {
        }

        public Address(String city, String street) {
            this.city = city;
            this.street = street;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getStreet() {
            return street;
        }

        public void setStreet(String street) {
            this.street = street;
        }
    }

    public static class UserDTO {
        private String name;
        private Integer age;
        private String email;

        public UserDTO() {
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

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    // ========== beanToMap 测试 ==========

    @Test
    @DisplayName("测试 Bean 转 Map")
    void testBeanToMap() {
        User user = new User("张三", 25);
        user.setEmail("zhangsan@example.com");

        Map<String, Object> map = BeanUtil.beanToMap(user);

        assertNotNull(map, "Map 不应为 null");
        assertEquals("张三", map.get("name"), "name 属性应正确转换");
        assertEquals(25, map.get("age"), "age 属性应正确转换");
        assertEquals("zhangsan@example.com", map.get("email"), "email 属性应正确转换");
        assertTrue(!map.containsKey("class"), "不应包含 class 属性");
    }

    @Test
    @DisplayName("测试 Bean 转 Map 包含 null 值")
    void testBeanToMapWithNullValues() {
        User user = new User();
        user.setName("李四");

        Map<String, Object> map = BeanUtil.beanToMap(user);

        assertNotNull(map, "Map 不应为 null");
        assertEquals("李四", map.get("name"), "name 属性应正确转换");
        assertNull(map.get("age"), "age 属性应为 null");
        assertNull(map.get("email"), "email 属性应为 null");
    }

    // ========== mapToBean 测试 ==========

    @Test
    @DisplayName("测试 Map 转 Bean")
    void testMapToBean() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "王五");
        map.put("age", 30);
        map.put("email", "wangwu@example.com");

        User user = BeanUtil.mapToBean(map, User.class);

        assertNotNull(user, "User 不应为 null");
        assertEquals("王五", user.getName(), "name 属性应正确设置");
        assertEquals(30, user.getAge(), "age 属性应正确设置");
        assertEquals("wangwu@example.com", user.getEmail(), "email 属性应正确设置");
    }

    @Test
    @DisplayName("测试 Map 转 Bean 包含 null 值")
    void testMapToBeanWithPartialData() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "赵六");
        // 不设置 age 和 email

        User user = BeanUtil.mapToBean(map, User.class);

        assertNotNull(user, "User 不应为 null");
        assertEquals("赵六", user.getName(), "name 属性应正确设置");
        assertNull(user.getAge(), "age 属性应为 null");
        assertNull(user.getEmail(), "email 属性应为 null");
    }

    // ========== getProperty/setProperty 测试 ==========

    @Test
    @DisplayName("测试获取 Bean 属性值")
    void testGetProperty() {
        User user = new User("孙七", 28);
        user.setEmail("sunqi@example.com");

        assertEquals("孙七", BeanUtil.getProperty(user, "name"), "应正确获取 name 属性");
        assertEquals(28, BeanUtil.getProperty(user, "age"), "应正确获取 age 属性");
        assertEquals("sunqi@example.com", BeanUtil.getProperty(user, "email"), "应正确获取 email 属性");
    }

    @Test
    @DisplayName("测试获取嵌套属性值")
    void testGetNestedProperty() {
        Address address = new Address("北京", "长安街");
        User user = new User();
        user.setAddress(address);

        assertEquals("北京", BeanUtil.getProperty(user, "address.city"), "应正确获取嵌套属性");
        assertEquals("长安街", BeanUtil.getProperty(user, "address.street"), "应正确获取嵌套属性");
    }

    @Test
    @DisplayName("测试设置 Bean 属性值")
    void testSetProperty() {
        User user = new User();

        BeanUtil.setProperty(user, "name", "周八");
        BeanUtil.setProperty(user, "age", 35);

        assertEquals("周八", user.getName(), "应正确设置 name 属性");
        assertEquals(35, user.getAge(), "应正确设置 age 属性");
    }

    @Test
    @DisplayName("测试设置嵌套属性值")
    void testSetNestedProperty() {
        User user = new User();
        user.setAddress(new Address());

        BeanUtil.setProperty(user, "address.city", "上海");
        BeanUtil.setProperty(user, "address.street", "南京路");

        assertEquals("上海", user.getAddress().getCity(), "应正确设置嵌套属性");
        assertEquals("南京路", user.getAddress().getStreet(), "应正确设置嵌套属性");
    }

    @Test
    @DisplayName("测试 getProperty 为 null Bean")
    void testGetPropertyWithNullBean() {
        assertThrows(IllegalArgumentException.class, () -> {
            BeanUtil.getProperty(null, "name");
        }, "null Bean 应抛出 IllegalArgumentException");
    }

    @Test
    @DisplayName("测试 setProperty 为 null Bean")
    void testSetPropertyWithNullBean() {
        assertThrows(NullPointerException.class, () -> {
            BeanUtil.setProperty(null, "name", "测试");
        }, "null Bean 应抛出 NullPointerException");
    }

    // ========== deepClone 测试 ==========

    @Test
    @DisplayName("测试深度克隆对象")
    void testDeepClone() {
        User original = new User("吴九", 40);
        original.setEmail("wujiu@example.com");
        original.setAddress(new Address("深圳", "深南大道"));

        User cloned = BeanUtil.deepClone(original);

        assertNotNull(cloned, "克隆对象不应为 null");
        assertNotSame(original, cloned, "克隆对象应是不同的实例");
        assertEquals(original.getName(), cloned.getName(), "name 属性应相同");
        assertEquals(original.getAge(), cloned.getAge(), "age 属性应相同");
        assertEquals(original.getEmail(), cloned.getEmail(), "email 属性应相同");
        assertEquals(original.getAddress().getCity(), cloned.getAddress().getCity(), "嵌套对象应相同");
        assertEquals(original.getAddress().getStreet(), cloned.getAddress().getStreet(), "嵌套对象应相同");
    }

    @Test
    @DisplayName("测试深度克隆修改不影响原对象")
    void testDeepCloneIndependence() {
        User original = new User("郑十", 45);
        User cloned = BeanUtil.deepClone(original);

        // 修改克隆对象
        cloned.setName("郑十-克隆");
        cloned.setAge(50);

        // 原对象不应受影响
        assertEquals("郑十", original.getName(), "原对象 name 不应改变");
        assertEquals(45, original.getAge(), "原对象 age 不应改变");
        assertEquals("郑十-克隆", cloned.getName(), "克隆对象 name 应已改变");
        assertEquals(50, cloned.getAge(), "克隆对象 age 应已改变");
    }

    // ========== convert 测试 ==========

    @Test
    @DisplayName("测试对象转换")
    void testConvertObject() {
        User user = new User("钱十一", 32);
        user.setEmail("qianshiyi@example.com");

        UserDTO dto = BeanUtil.convert(user, UserDTO.class);

        assertNotNull(dto, "转换结果不应为 null");
        assertEquals(user.getName(), dto.getName(), "name 属性应正确转换");
        assertEquals(user.getAge(), dto.getAge(), "age 属性应正确转换");
        assertEquals(user.getEmail(), dto.getEmail(), "email 属性应正确转换");
    }

    @Test
    @DisplayName("测试 null 对象转换")
    void testConvertNullObject() {
        User nullUser = null;
        UserDTO dto = BeanUtil.convert(nullUser, UserDTO.class);

        assertNotNull(dto, "转换结果不应为 null");
        assertNull(dto.getName(), "name 应为 null");
        assertNull(dto.getAge(), "age 应为 null");
    }

    @Test
    @DisplayName("测试相同类型转换")
    void testConvertSameType() {
        User user = new User("孙十二", 22);

        User result = BeanUtil.convert(user, User.class);

        // 相同类型转换应返回同一个实例
        assertEquals(user, result, "相同类型转换应返回原对象");
    }

    @Test
    @DisplayName("测试列表转换")
    void testConvertList() {
        List<User> users = List.of(
                new User("用户1", 20),
                new User("用户2", 25),
                new User("用户3", 30)
        );

        List<UserDTO> dtos = BeanUtil.convert(users, UserDTO.class);

        assertNotNull(dtos, "转换结果不应为 null");
        assertEquals(3, dtos.size(), "列表大小应相同");
        assertEquals("用户1", dtos.get(0).getName(), "第一个元素 name 应正确");
        assertEquals(20, dtos.get(0).getAge(), "第一个元素 age 应正确");
    }

    @Test
    @DisplayName("测试空列表转换")
    void testConvertEmptyList() {
        List<User> users = List.of();

        List<UserDTO> dtos = BeanUtil.convert(users, UserDTO.class);

        assertNotNull(dtos, "转换结果不应为 null");
        assertTrue(dtos.isEmpty(), "结果应为空列表");
    }

    @Test
    @DisplayName("测试 null 列表转换")
    void testConvertNullList() {
        List<User> nullList = null;
        List<UserDTO> dtos = BeanUtil.convert(nullList, UserDTO.class);

        assertNotNull(dtos, "转换结果不应为 null");
        assertTrue(dtos.isEmpty(), "结果应为空列表");
    }

    @Test
    @DisplayName("测试 Map 转 Bean")
    void testConvertMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "周十三");
        map.put("age", 38);
        map.put("email", "zhoushisan@example.com");

        User user = BeanUtil.convert(map, User.class);

        assertNotNull(user, "转换结果不应为 null");
        assertEquals("周十三", user.getName(), "name 属性应正确设置");
        assertEquals(38, user.getAge(), "age 属性应正确设置");
        assertEquals("zhoushisan@example.com", user.getEmail(), "email 属性应正确设置");
    }

    @Test
    @DisplayName("测试空 Map 转 Bean")
    void testConvertEmptyMap() {
        Map<String, Object> map = new HashMap<>();

        User user = BeanUtil.convert(map, User.class);

        assertNotNull(user, "转换结果不应为 null");
        assertNull(user.getName(), "name 应为 null");
        assertNull(user.getAge(), "age 应为 null");
    }
}
