package webserver;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import db.DataBase;
import model.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

public class RequestHandlerTest {

    @Before
    public void setUp() {
        DataBase.clear();
    }

    private static class TestSocket extends Socket {
        private final InputStream in;
        private final OutputStream out;

        TestSocket(byte[] requestBytes, OutputStream responseCapture) {
            this.in = new ByteArrayInputStream(requestBytes);
            this.out = responseCapture;
        }

        @Override
        public InputStream getInputStream() {
            return in;
        }

        @Override
        public OutputStream getOutputStream() {
            return out;
        }

        @Override
        public InetAddress getInetAddress() {
            return InetAddress.getLoopbackAddress();
        }

        @Override
        public int getPort() {
            return 0;
        }
    }

    private byte[] buildGetRequest(String path) {
        String request = "GET " + path + " HTTP/1.1\r\n"
                + "Host: localhost:8080\r\n"
                + "\r\n";
        return request.getBytes(StandardCharsets.UTF_8);
    }

    private byte[] buildPostRequest(String path, String body) {
        byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
        String request = "POST " + path + " HTTP/1.1\r\n"
                + "Host: localhost:8080\r\n"
                + "Content-Length: " + bodyBytes.length + "\r\n"
                + "\r\n"
                + body;
        return request.getBytes(StandardCharsets.UTF_8);
    }

    private String runHandler(byte[] request) {
        ByteArrayOutputStream responseCapture = new ByteArrayOutputStream();
        TestSocket socket = new TestSocket(request, responseCapture);
        RequestHandler handler = new RequestHandler(socket);
        handler.run();
        return responseCapture.toString();
    }

    // --- 테스트 구현 ---

    @DisplayName("GET /index.html 요청 시 200 OK 응답을 반환한다")
    @Test
    public void GET_index_html_200응답() {
        // given
        byte[] request = buildGetRequest("/index.html");

        // when
        String response = runHandler(request);

        // then
        assertAll(
                () -> assertTrue(response.startsWith("HTTP/1.1 200 OK")),
                () -> assertTrue(response.contains("Content-Type: text/html;charset=utf-8"))
        );
    }

    @DisplayName("존재하지 않는 파일 요청 시 File Not Found 본문을 반환한다")
    @Test
    public void GET_존재하지않는파일() {
        // given
        byte[] request = buildGetRequest("/baejeonghwan is happy");

        // when
        String response = runHandler(request);
        String result = "File Not Found";

        // then
        assertTrue(response.contains(result));
    }

    @DisplayName("POST /user/create 요청 시 회원가입 후 302 리다이렉트한다")
    @Test
    public void POST_회원가입_성공_302리다이렉트() {
        // given
        byte[] request = buildPostRequest("/user/create", "userId=baejeonghwan777&password=123456&name=%EB%B0%B0%EC%A0%95%ED%99%98&email=baejeonghwon777%40gmail.com");

        // when
        String response = runHandler(request);

        // then
        assertAll(
                () -> assertTrue(response.startsWith("HTTP/1.1 302 Found"))
        );
    }

    @DisplayName("중복 ID로 회원가입 시 기존 유저가 유지된다")
    @Test
    public void POST_중복회원가입_기존유저유지() {
        // given
        byte[] request = buildPostRequest("/user/create", "userId=baejeonghwan777&password=123456&name=%EB%B0%B0%EC%A0%95%ED%99%98&email=baejeonghwon777%40gmail.com");
        String response = runHandler(request);
        int expected = DataBase.findAll().size();

        // when
        byte[] reRequest = buildPostRequest("/user/create", "userId=baejeonghwan777&password=1234567&name=%EB%B0%B0%EC%A0%95%ED%99%98&email=baejeonghwon777%40gmail.com");
        String reResponse = runHandler(request);
        int result = DataBase.findAll().size();

        // then
        assertEquals(expected, result);
    }

    @DisplayName("로그인 성공 시 302 리다이렉트와 Set-Cookie를 반환한다")
    @Test
    public void POST_로그인성공_쿠키설정() {
        // given
        byte[] requestCreate = buildPostRequest("/user/create", "userId=baejeonghwan777&password=123456&name=%EB%B0%B0%EC%A0%95%ED%99%98&email=baejeonghwon777%40gmail.com");
        String responseCreate = runHandler(requestCreate);

        // when
        byte[] request = buildPostRequest("/user/login", "userId=baejeonghwan777&password=123456");
        String response = runHandler(request);

        // then
        assertAll(
                () -> assertTrue(response.startsWith("HTTP/1.1 302 Found")),
                () -> assertTrue(response.contains("Set-Cookie"))
        );
    }

    @DisplayName("비밀번호 불일치 시 login_failed.html로 리다이렉트한다")
    @Test
    public void POST_로그인실패_비밀번호불일치() {
        // given
        byte[] requestCreate = buildPostRequest("/user/create", "userId=baejeonghwan777&password=123456&name=%EB%B0%B0%EC%A0%95%ED%99%98&email=baejeonghwon777%40gmail.com");
        String responseCreate = runHandler(requestCreate);

        // when
        byte[] request = buildPostRequest("/user/login", "userId=baejeonghwan777&password=123457");
        String response = runHandler(request);

        // then
        assertAll(
                () -> assertTrue(response.startsWith("HTTP/1.1 302 Found")),
                () -> assertTrue(response.contains("login_failed.html"))
        );
    }

    @DisplayName("존재하지 않는 유저로 로그인 시 login_failed.html로 리다이렉트한다")
    @Test
    public void POST_로그인실패_존재하지않는유저() {
        // given
        byte[] requestCreate = buildPostRequest("/user/create", "userId=baejeonghwan777&password=123456&name=%EB%B0%B0%EC%A0%95%ED%99%98&email=baejeonghwon777%40gmail.com");
        String responseCreate = runHandler(requestCreate);

        // when
        byte[] request = buildPostRequest("/user/login", "userId=jincheon&password=123456");
        String response = runHandler(request);

        // then
        assertAll(
                () -> assertTrue(response.startsWith("HTTP/1.1 302 Found")),
                () -> assertTrue(response.contains("login_failed.html"))
        );
    }

    @DisplayName("빈 요청(null) 시 에러 없이 종료한다")
    @Test
    public void null요청_에러없이종료() {
        // given
        byte[] request = buildGetRequest(null);

        // when
        String response = runHandler(request);
    }
}
