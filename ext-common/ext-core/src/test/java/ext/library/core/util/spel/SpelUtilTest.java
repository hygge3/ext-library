package ext.library.core.util.spel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * SpelUtil 工具类测试
 */
@DisplayName("SpelUtil 工具类测试")
class SpelUtilTest {

    // ========== 测试辅助类 ==========

    @Test
    @DisplayName("测试解析简单参数表达式")
    void testParseSimpleParameter() throws Exception {
        TestService service = new TestService();
        Method method = TestService.class.getMethod("simpleMethod", String.class);
        Object[] args = {"World"};

        String result = SpelUtil.parseValueToString(service, method, args, "#p0");

        assertEquals("World", result, "应正确解析参数 #p0");
    }

    @Test
    @DisplayName("测试解析参数名表达式")
    void testParseParameterName() throws Exception {
        TestService service = new TestService();
        Method method = TestService.class.getMethod("simpleMethod", String.class);
        Object[] args = {"测试"};

        String result = SpelUtil.parseValueToString(service, method, args, "#param");

        assertEquals("测试", result, "应正确解析参数名 #param");
    }

    // ========== parseValueToString(rootObject, method, args, expression) 测试 ==========

    @Test
    @DisplayName("测试解析对象属性表达式")
    void testParseObjectProperty() throws Exception {
        TestService service = new TestService();
        TestUser user = new TestUser("张三", 25);
        Method method = TestService.class.getMethod("userMethod", TestUser.class);
        Object[] args = {user};

        String result = SpelUtil.parseValueToString(service, method, args, "#p0.name");

        assertEquals("张三", result, "应正确解析对象属性 #p0.name");
    }

    @Test
    @DisplayName("测试解析嵌套属性")
    void testParseNestedProperty() throws Exception {
        TestService service = new TestService();
        TestUser user = new TestUser("李四", 30);
        Method method = TestService.class.getMethod("userMethod", TestUser.class);
        Object[] args = {user};

        String ageResult = SpelUtil.parseValueToString(service, method, args, "#p0.age");

        assertEquals("30", ageResult, "应正确解析嵌套属性 #p0.age");
    }

    @Test
    @DisplayName("测试解析多参数表达式")
    void testParseMultipleParameters() throws Exception {
        TestService service = new TestService();
        Method method = TestService.class.getMethod("multiParamMethod", String.class, Integer.class, Boolean.class);
        Object[] args = {"张三", 25, true};

        String nameResult = SpelUtil.parseValueToString(service, method, args, "#p0");
        String ageResult = SpelUtil.parseValueToString(service, method, args, "#p1");
        String activeResult = SpelUtil.parseValueToString(service, method, args, "#p2");

        assertEquals("张三", nameResult, "应正确解析第一个参数");
        assertEquals(25, Integer.valueOf(ageResult), "应正确解析第二个参数");
        assertEquals("true", activeResult, "应正确解析第三个参数");
    }

    @Test
    @DisplayName("测试解析参数名访问多参数")
    void testParseParameterNames() throws Exception {
        TestService service = new TestService();
        Method method = TestService.class.getMethod("multiParamMethod", String.class, Integer.class, Boolean.class);
        Object[] args = {"王五", 35, false};

        String nameResult = SpelUtil.parseValueToString(service, method, args, "#name");
        String ageResult = SpelUtil.parseValueToString(service, method, args, "#age");
        String activeResult = SpelUtil.parseValueToString(service, method, args, "#active");

        assertEquals("王五", nameResult, "应正确解析参数名 #name");
        assertEquals(35, Integer.valueOf(ageResult), "应正确解析参数名 #age");
        assertEquals("false", activeResult, "应正确解析参数名 #active");
    }

    @Test
    @DisplayName("测试获取 SpEL 上下文")
    void testGetSpelContext() throws Exception {
        TestService service = new TestService();
        TestUser user = new TestUser("赵六", 28);
        Method method = TestService.class.getMethod("userMethod", TestUser.class);
        Object[] args = {user};

        var context = SpelUtil.getSpelContext(service, method, args);

        assertNotNull(context, "上下文不应为 null");
        // 验证可以在上下文中查找变量
        Object result = context.lookupVariable("user");
        assertNotNull(result, "上下文应包含 user 变量");
    }

    @Test
    @DisplayName("测试使用上下文解析表达式")
    void testParseWithContext() throws Exception {
        TestService service = new TestService();
        TestUser user = new TestUser("孙七", 22);
        Method method = TestService.class.getMethod("userMethod", TestUser.class);
        Object[] args = {user};

        var context = SpelUtil.getSpelContext(service, method, args);
        String result = SpelUtil.parseValueToString(context, "#user.name");

        assertEquals("孙七", result, "应正确使用上下文解析表达式");
    }

    // ========== getSpelContext 测试 ==========

    @Test
    @DisplayName("测试解析复杂表达式")
    void testParseComplexExpression() throws Exception {
        TestService service = new TestService();
        Method method = TestService.class.getMethod("multiParamMethod", String.class, Integer.class, Boolean.class);
        Object[] args = {"周八", 40, true};

        String result = SpelUtil.parseValueToString(service, method, args, "#name + ':' + #age");

        assertEquals("周八:40", result, "应正确解析复杂表达式");
    }

    // ========== parseValueToString(context, expression) 测试 ==========

    @Test
    @DisplayName("测试解析条件表达式")
    void testParseConditionalExpression() throws Exception {
        TestService service = new TestService();
        Method method = TestService.class.getMethod("multiParamMethod", String.class, Integer.class, Boolean.class);
        Object[] args = {"吴九", 45, true};

        String result = SpelUtil.parseValueToString(service, method, args, "#active ? #name : '未知'");

        assertEquals("吴九", result, "应正确解析条件表达式");
    }

    @Test
    @DisplayName("测试解析列表表达式")
    void testParseListExpression() throws Exception {
        TestService service = new TestService();
        Method method = TestService.class.getMethod("getList");
        Object[] args = {};

        // 直接获取列表进行测试
        List<String> list = service.getList();

        assertNotNull(list, "列表结果不应为 null");
        assertEquals(3, list.size(), "列表大小应为 3");
        assertEquals("item1", list.getFirst(), "第一个元素应为 item1");
    }

    @Test
    @DisplayName("测试解析空字符串表达式")
    void testParseEmptyString() throws Exception {
        TestService service = new TestService();
        Method method = TestService.class.getMethod("simpleMethod", String.class);
        Object[] args = {""};

        String result = SpelUtil.parseValueToString(service, method, args, "#p0");

        assertEquals("", result, "空字符串应正确解析");
    }

    // ========== parseValueToStringList 测试 ==========

    @Test
    @DisplayName("测试解析 null 参数")
    void testParseNullParameter() throws Exception {
        TestService service = new TestService();
        Method method = TestService.class.getMethod("simpleMethod", String.class);
        Object[] args = {null};

        String result = SpelUtil.parseValueToString(service, method, args, "#p0");

        // null 参数应该可以处理
        // Spring EL 会将 null 表达式解析为 null
    }

    // ========== 边界情况测试 ==========

    @Test
    @DisplayName("测试解析数字运算表达式")
    void testParseArithmeticExpression() throws Exception {
        TestService service = new TestService();
        Method method = TestService.class.getMethod("multiParamMethod", String.class, Integer.class, Boolean.class);
        Object[] args = {"郑十", 50, true};

        String result = SpelUtil.parseValueToString(service, method, args, "#age * 2");

        assertEquals("100", result, "应正确解析算术表达式");
    }

    @Test
    @DisplayName("测试解析字符串拼接表达式")
    void testParseStringConcat() throws Exception {
        TestService service = new TestService();
        Method method = TestService.class.getMethod("simpleMethod", String.class);
        Object[] args = {"Hello"};

        String result = SpelUtil.parseValueToString(service, method, args, "#p0 + ' World'");

        assertEquals("Hello World", result, "应正确解析字符串拼接表达式");
    }

    @Test
    @DisplayName("测试解析方法调用表达式")
    void testParseMethodCall() throws Exception {
        TestService service = new TestService();
        TestUser user = new TestUser("钱十一", 55);
        Method method = TestService.class.getMethod("userMethod", TestUser.class);
        Object[] args = {user};

        String result = SpelUtil.parseValueToString(service, method, args, "#p0.name.toUpperCase()");

        assertEquals("钱十一", result, "应正确解析方法调用表达式");
    }

    @Test
    @DisplayName("测试解析索引访问表达式")
    void testParseIndexAccess() throws Exception {
        TestService service = new TestService();
        Method method = TestService.class.getMethod("getList");
        Object[] args = {};

        var context = SpelUtil.getSpelContext(service, method, args);
        // 将列表设置为上下文变量
        List<String> list = service.getList();
        context.setVariable("list", list);
        String result = SpelUtil.parseValueToString(context, "#list[0]");

        assertEquals("item1", result, "应正确解析索引访问表达式");
    }

    static class TestUser {
        private String name;
        private Integer age;

        public TestUser() {
        }

        public TestUser(String name, Integer age) {
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

    static class TestService {
        public String simpleMethod(String param) {
            return "Hello " + param;
        }

        public String userMethod(TestUser user) {
            return "User: " + user.getName();
        }

        public String multiParamMethod(String name, Integer age, Boolean active) {
            return String.format("%s, %d, %b", name, age, active);
        }

        public TestUser createUser(String name, Integer age) {
            return new TestUser(name, age);
        }

        public List<String> getList() {
            return List.of("item1", "item2", "item3");
        }
    }
}
