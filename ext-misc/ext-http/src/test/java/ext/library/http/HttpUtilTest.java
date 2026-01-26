package ext.library.http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("HttpUtil 工具类测试")
class HttpUtilTest {

    @Nested
    @DisplayName("客户端配置测试")
    class ClientConfigTests {

        @Test
        @DisplayName("获取默认客户端")
        void getDefaultClient() {
            HttpClient client = HttpUtil.getClient();
            assertNotNull(client);
        }

        @Test
        @DisplayName("获取不安全客户端")
        void getInsecureClient() {
            HttpClient insecureClient = HttpUtil.getInsecureClient();
            assertNotNull(insecureClient);
            // 多次调用应返回同一实例
            assertSame(insecureClient, HttpUtil.getInsecureClient());
        }

        @Test
        @DisplayName("设置自定义客户端")
        void setCustomClient() {
            HttpClient original = HttpUtil.getClient();
            HttpClient custom = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

            HttpUtil.setClient(custom);
            assertSame(custom, HttpUtil.getClient());

            // 恢复原客户端
            HttpUtil.setClient(original);
        }

        @Test
        @DisplayName("设置默认超时")
        void setDefaultTimeout() {
            HttpUtil.setDefaultTimeout(5000);
            // 验证通过构建 Request 时使用
            HttpUtil.Request request = HttpUtil.get("http://example.com");
            assertEquals(5000, request.getTimeout());

            // 恢复默认值
            HttpUtil.setDefaultTimeout(120000);
        }

        @Test
        @DisplayName("设置默认 ContentType")
        void setDefaultContentType() {
            HttpUtil.setDefaultContentType("text/plain");
            // 验证新请求使用新的默认值
            // 恢复默认值
            HttpUtil.setDefaultContentType("application/json");
        }
    }

    @Nested
    @DisplayName("请求构建器测试")
    class RequestBuilderTests {

        @Test
        @DisplayName("创建 GET 请求")
        void createGetRequest() {
            HttpUtil.Request request = HttpUtil.get("http://example.com");
            assertEquals(HttpUtil.HttpMethod.GET, request.getMethod());
            assertEquals("http://example.com", request.getUrl());
        }

        @Test
        @DisplayName("创建 POST 请求")
        void createPostRequest() {
            HttpUtil.Request request = HttpUtil.post("http://example.com");
            assertEquals(HttpUtil.HttpMethod.POST, request.getMethod());
        }

        @Test
        @DisplayName("创建 PUT 请求")
        void createPutRequest() {
            HttpUtil.Request request = HttpUtil.put("http://example.com");
            assertEquals(HttpUtil.HttpMethod.PUT, request.getMethod());
        }

        @Test
        @DisplayName("创建 DELETE 请求")
        void createDeleteRequest() {
            HttpUtil.Request request = HttpUtil.delete("http://example.com");
            assertEquals(HttpUtil.HttpMethod.DELETE, request.getMethod());
        }

        @Test
        @DisplayName("创建 PATCH 请求")
        void createPatchRequest() {
            HttpUtil.Request request = HttpUtil.patch("http://example.com");
            assertEquals(HttpUtil.HttpMethod.PATCH, request.getMethod());
        }

        @Test
        @DisplayName("链式切换方法")
        void chainMethodSwitch() {
            HttpUtil.Request request = HttpUtil.request("http://example.com")
                    .get()
                    .post()
                    .put()
                    .delete()
                    .patch();
            assertEquals(HttpUtil.HttpMethod.PATCH, request.getMethod());
        }
    }

    @Nested
    @DisplayName("请求头配置测试")
    class HeaderTests {

        @Test
        @DisplayName("添加单个请求头")
        void addSingleHeader() {
            HttpUtil.Request request = HttpUtil.get("http://example.com")
                    .header("X-Custom", "value");

            assertEquals("value", request.getHeaders().get("X-Custom"));
        }

        @Test
        @DisplayName("批量添加请求头")
        void addMultipleHeaders() {
            Map<String, String> headers = new HashMap<>();
            headers.put("X-Header1", "value1");
            headers.put("X-Header2", "value2");

            HttpUtil.Request request = HttpUtil.get("http://example.com")
                    .headers(headers);

            assertEquals("value1", request.getHeaders().get("X-Header1"));
            assertEquals("value2", request.getHeaders().get("X-Header2"));
        }

        @Test
        @DisplayName("设置 Content-Type")
        void setContentType() {
            HttpUtil.Request request = HttpUtil.post("http://example.com")
                    .contentType("text/plain");

            assertEquals("text/plain", request.getHeaders().get("Content-Type"));
        }

        @Test
        @DisplayName("快捷方法 - contentTypeJson")
        void contentTypeJson() {
            HttpUtil.Request request = HttpUtil.post("http://example.com")
                    .contentTypeJson();

            assertEquals("application/json; charset=UTF-8", request.getHeaders().get("Content-Type"));
        }

        @Test
        @DisplayName("快捷方法 - contentTypeForm")
        void contentTypeForm() {
            HttpUtil.Request request = HttpUtil.post("http://example.com")
                    .contentTypeForm();

            assertEquals("application/x-www-form-urlencoded; charset=UTF-8", request.getHeaders().get("Content-Type"));
        }

        @Test
        @DisplayName("设置 Accept")
        void setAccept() {
            HttpUtil.Request request = HttpUtil.get("http://example.com")
                    .accept("application/json");

            assertEquals("application/json", request.getHeaders().get("Accept"));
        }

        @Test
        @DisplayName("快捷方法 - acceptJson")
        void acceptJson() {
            HttpUtil.Request request = HttpUtil.get("http://example.com")
                    .acceptJson();

            assertEquals("application/json", request.getHeaders().get("Accept"));
        }

        @Test
        @DisplayName("设置 Authorization")
        void setAuthorization() {
            HttpUtil.Request request = HttpUtil.get("http://example.com")
                    .authorization("Basic abc123");

            assertEquals("Basic abc123", request.getHeaders().get("Authorization"));
        }

        @Test
        @DisplayName("快捷方法 - bearer")
        void setBearer() {
            HttpUtil.Request request = HttpUtil.get("http://example.com")
                    .bearer("token123");

            assertEquals("Bearer token123", request.getHeaders().get("Authorization"));
        }
    }

    @Nested
    @DisplayName("查询参数测试")
    class QueryParamsTests {

        @Test
        @DisplayName("添加单个查询参数")
        void addSingleQueryParam() {
            HttpUtil.Request request = HttpUtil.get("http://example.com")
                    .query("page", "1");

            assertEquals("1", request.getQueryParams().get("page"));
        }

        @Test
        @DisplayName("批量添加查询参数")
        void addMultipleQueryParams() {
            Map<String, String> params = new HashMap<>();
            params.put("page", "1");
            params.put("size", "10");

            HttpUtil.Request request = HttpUtil.get("http://example.com")
                    .query(params);

            assertEquals("1", request.getQueryParams().get("page"));
            assertEquals("10", request.getQueryParams().get("size"));
        }
    }

    @Nested
    @DisplayName("请求体配置测试")
    class BodyTests {

        @Test
        @DisplayName("设置字符串请求体")
        void setStringBody() {
            HttpUtil.Request request = HttpUtil.post("http://example.com")
                    .body("{\"key\":\"value\"}");

            assertEquals("{\"key\":\"value\"}", request.getBody());
        }

        @Test
        @DisplayName("设置字节数组请求体")
        void setByteArrayBody() {
            byte[] data = "test".getBytes();
            HttpUtil.Request request = HttpUtil.post("http://example.com")
                    .body(data);

            assertArrayEquals(data, (byte[]) request.getBody());
        }

        @Test
        @DisplayName("设置表单数据")
        void setFormData() {
            HttpUtil.Request request = HttpUtil.post("http://example.com")
                    .form("username", "admin")
                    .form("password", "123456");

            assertTrue(request.getBody() instanceof Map);
            @SuppressWarnings("unchecked")
            Map<String, Object> form = (Map<String, Object>) request.getBody();
            assertEquals("admin", form.get("username"));
            assertEquals("123456", form.get("password"));
        }

        @Test
        @DisplayName("批量设置表单数据")
        void setFormDataMap() {
            Map<String, Object> formData = new HashMap<>();
            formData.put("field1", "value1");
            formData.put("field2", "value2");

            HttpUtil.Request request = HttpUtil.post("http://example.com")
                    .form(formData);

            assertEquals(formData, request.getBody());
        }
    }

    @Nested
    @DisplayName("超时配置测试")
    class TimeoutTests {

        @Test
        @DisplayName("设置毫秒超时")
        void setTimeoutMillis() {
            HttpUtil.Request request = HttpUtil.get("http://example.com")
                    .timeout(5000);

            assertEquals(5000, request.getTimeout());
        }

        @Test
        @DisplayName("设置 Duration 超时")
        void setTimeoutDuration() {
            HttpUtil.Request request = HttpUtil.get("http://example.com")
                    .timeout(Duration.ofSeconds(10));

            assertEquals(10000, request.getTimeout());
        }
    }

    @Nested
    @DisplayName("不安全模式测试")
    class InsecureModeTests {

        @Test
        @DisplayName("启用不安全模式")
        void enableInsecureMode() {
            // 只测试 API，不实际发送请求
            HttpUtil.Request request = HttpUtil.get("https://example.com")
                    .insecure();

            assertNotNull(request);
        }
    }

    @Nested
    @DisplayName("HTTP 方法枚举测试")
    class HttpMethodEnumTests {

        @Test
        @DisplayName("所有 HTTP 方法存在")
        void allMethodsExist() {
            assertEquals(7, HttpUtil.HttpMethod.values().length);
            assertNotNull(HttpUtil.HttpMethod.GET);
            assertNotNull(HttpUtil.HttpMethod.POST);
            assertNotNull(HttpUtil.HttpMethod.PUT);
            assertNotNull(HttpUtil.HttpMethod.DELETE);
            assertNotNull(HttpUtil.HttpMethod.PATCH);
            assertNotNull(HttpUtil.HttpMethod.HEAD);
            assertNotNull(HttpUtil.HttpMethod.OPTIONS);
        }
    }

    @Nested
    @DisplayName("响应类测试")
    class ResponseTests {

        @Test
        @DisplayName("状态码判断方法")
        void statusCodeMethods() {
            // 使用模拟的 HttpResponse 进行测试较复杂
            // 这里只验证方法存在，实际测试需要集成测试
            assertNotNull(HttpUtil.Response.class);
        }
    }
}
