package com.igeeksky.xredis.examples;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

/**
 * 用户测试类
 *
 * @author Patrick.Lau
 * @since 1.0.0
 */
class UserControllerTest {

    private static final String HOST = "http://localhost:8080";

    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    private static final HttpResponse.BodyHandler<String> RESPONSE_HANDLER =
            response -> HttpResponse.BodySubscribers.ofString(StandardCharsets.UTF_8);

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final JavaType VOID_TYPE = MAPPER.getTypeFactory().constructParametricType(Response.class, User.class);
    private static final JavaType USER_TYPE = MAPPER.getTypeFactory().constructParametricType(Response.class, User.class);

    private static final JacksonCodec<Response<User>> USER_CODEC = new JacksonCodec<>(MAPPER, USER_TYPE);
    private static final JacksonCodec<Response<Void>> VOID_CODEC = new JacksonCodec<>(MAPPER, VOID_TYPE);

    @Test
    void createUser() {
        long id = 1;
        User user = new User(id, "Patrick.Lau", 18);

        Response<Void> response1 = createUser(user.toString()).join();
        System.out.printf("%s : %s\n", "addUser", response1);
        Assertions.assertEquals(Response.OK, response1.getCode());

        Response<User> response2 = getUser(id).join();
        System.out.printf("%s : %s\n\n", "getUserById", response2);
        Assertions.assertEquals(Response.OK, response2.getCode());
        Assertions.assertEquals(user, response2.getData());
    }

    @Test
    void deleteUser() {
        long id = 1;
        User user = new User(id, "Patrick.Lau", 18);

        createUser(user.toString()).join();

        Response<Void> delete = deleteUser(id).join();
        System.out.printf("%s : %s\n", "deleteUser", delete);
        Assertions.assertEquals(Response.OK, delete.getCode());

        Response<User> get = getUser(id).join();
        System.out.printf("%s : %s\n\n", "getUserById", get);
        Assertions.assertEquals(Response.ERROR, get.getCode());
        Assertions.assertNull(get.getData());
    }

    /**
     * 发送创建用户请求
     *
     * @param user 用户信息(JSON String)
     * @return {@code Response<Void>} – 包含创建结果信息的响应对象
     */
    private static CompletableFuture<Response<Void>> createUser(String user) {
        // 创建请求
        HttpRequest request = post("/user/create", user);
        // 发送请求
        return sendAndReceive(request).thenApply(VOID_CODEC::decode);
    }

    /**
     * 发送获取用户请求
     *
     * @param id 用户ID
     * @return 包含用户信息的响应对象，类型为 {@code Response<User>}，其中 User 为获取到的用户信息
     */
    private static CompletableFuture<Response<User>> getUser(long id) {
        HttpRequest request = get("/user/get/" + id);
        return sendAndReceive(request).thenApply(USER_CODEC::decode);
    }

    private static CompletableFuture<Response<Void>> deleteUser(long id) {
        HttpRequest request = delete("/user/delete/" + id);
        return sendAndReceive(request).thenApply(VOID_CODEC::decode);
    }

    /**
     * 发送 HTTP 请求并接收响应
     *
     * @param request HTTP 请求对象
     * @return {@link String} – 响应体
     */
    private static CompletableFuture<String> sendAndReceive(HttpRequest request) {
        return CLIENT.sendAsync(request, RESPONSE_HANDLER).thenApply(HttpResponse::body);
    }

    /**
     * 创建 GET 请求
     *
     * @param url 请求的 URL 路径
     * @return HttpRequest 对象，用于发送 GET 请求
     */
    private static HttpRequest get(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(HOST + url))
                .GET()
                .build();
    }

    /**
     * 创建 POST 请求
     *
     * @param url  请求 URL 路径
     * @param body 请求体，类型为 JSON 格式的字符串
     * @return HttpRequest对象，用于发送 POST 请求
     */
    private static HttpRequest post(String url, String body) {
        return HttpRequest.newBuilder()
                .uri(URI.create(HOST + url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
    }

    /**
     * 创建 DELETE 请求
     *
     * @param url 请求 URL 路径
     * @return HttpRequest 对象，用于发送 DELETE 请求
     */
    private static HttpRequest delete(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(HOST + url))
                .DELETE()
                .build();
    }

}