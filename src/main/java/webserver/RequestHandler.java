package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

import static util.HttpRequestUtils.parseQueryString;

public class RequestHandler extends Thread {
    private static final int UNDEFINED = 0;
    private static final int LOGIN_SUCCESS = 1;
    private static final int LOGIN_FAIL = 2;
    private static final int COOKIE_VALUE_INDEX = 2;
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());
        final Map<String, String> headers = new HashMap<>();

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.

            BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            User loginUser;
            String line = br.readLine();
            if (line == null) return;
            String firstLine = line;
            log.debug("request line : {}", line);

            String url = HttpRequestUtils.extractPath(line); // url을 특정 조건에서만 빼옴 반복문 안에서 하나씩 하다가
            int loginBeforeFlag = UNDEFINED;
            int loginAfterFlag = UNDEFINED;
            int length = 0;

            // 클라이언트 데이터를 line by line으로 읽음 로그를 사용하면 쓰레드 출처도 확인 가능
            while(true) {
                line = br.readLine();
                if (line == null || line.isEmpty()) {
                    break;
                }
                if(line.startsWith("Content-Length")) length = HttpRequestUtils.extractLength(line);
                if(line.startsWith("Cookie")) {
                    String[] cookieInfo = line.split("[:=]");
                    loginUser = DataBase.findUserByCookieId(cookieInfo[COOKIE_VALUE_INDEX].trim());
                    if(loginUser != null)
                        loginAfterFlag = LOGIN_SUCCESS;
                }
                log.debug("request line : {}", line);
            }

            DataOutputStream dos = new DataOutputStream(out);
            byte[] body;

            if (firstLine.startsWith("POST")) {
                String bodyData = IOUtils.readData(br, length);
                log.debug("POST body : {}", bodyData);
                if(url.startsWith("/user/create")) {
                    makeUser(bodyData);
                    response302Header(dos, "/index.html", 0, headers);
                    return;
                }
                if(url.startsWith("/user/login")) loginBeforeFlag = checkUser(bodyData, headers);
                if(loginBeforeFlag == LOGIN_FAIL) {
                    response302Header(dos, "/user/login_failed.html", 0, headers); // 302 리다이렉트 시 body 불필요
                    return;
                }
                response302Header(dos, "/index.html", 0, headers);
            }
            if (firstLine.startsWith("GET")) {
                if(url.startsWith("/user/list")) {
                    if(loginAfterFlag == UNDEFINED) {
                        response302Header(dos, "/user/login.html", 0, headers);
                        return;
                    }
                    StringBuilder builder = new StringBuilder();
                    builder.append("<html><body>");
                    builder.append("<h1>사용자 목록</h1>");
                    builder.append("<table border='1'>");
                    builder.append("<tr><th>아이디</th><th>이름</th><th>이메일</th></tr>");

                    for (User user : DataBase.findAll()) {
                        builder.append("<tr>");
                        builder.append("<td>").append(user.getUserId()).append("</td>");
                        builder.append("<td>").append(user.getName()).append("</td>");
                        builder.append("<td>").append(user.getEmail()).append("</td>");
                        builder.append("</tr>");
                    }

                    builder.append("</table>");
                    builder.append("</body></html>");

                    body = builder.toString().getBytes("UTF-8");
                    response200Header(dos, body.length, "text/html;charset=utf-8"); // Content-Type이 text/html이어야 함
                    responseBody(dos, body);
                    return;
                }
                body = HttpRequestUtils.readPath("./webapp", url);
                response200Header(dos, body.length, "text/html;charset=utf-8");
                responseBody(dos, body);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private int checkUser(String data, Map<String, String> headers) throws UnsupportedEncodingException {
        Map<String, String> userInstance = parseQueryString(data);

        String userId = URLDecoder.decode(userInstance.get("userId"), "UTF-8");
        String password = URLDecoder.decode(userInstance.get("password"), "UTF-8");

        User user = DataBase.findUserById(userId);
        if(user == null) return LOGIN_FAIL;
        if(!user.getPassword().equals(password)) return LOGIN_FAIL;
        String id = UUID.randomUUID().toString();
        DataBase.addCookie(id, user);
        addHeader("Set-Cookie","sessionId=" + id + "; Path=/;", headers);
        return LOGIN_SUCCESS;
    }

    private void makeUser(String data) throws UnsupportedEncodingException {
        Map<String, String> userInstance = parseQueryString(data);

        String userId = URLDecoder.decode(userInstance.get("userId"), "UTF-8");
        String password = URLDecoder.decode(userInstance.get("password"), "UTF-8");
        String name = URLDecoder.decode(userInstance.get("name"), "UTF-8");
        String email = URLDecoder.decode(userInstance.get("email"), "UTF-8");

        User user = DataBase.findUserById(userId);
        if(user == null) {
            user = new User(userId, password, name, email);
            DataBase.addUser(user);
        }
    }

    // 공통 헤더 추가
    public void addHeader(String key, String value, Map<String, String> headers) {
        headers.put(key, value);
    }

    private void response302Header(DataOutputStream dos, String redirectPath, int lengthOfBodyContent, Map<String, String> headers) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Location: " + redirectPath + "\r\n");
            processHeaders(dos, headers);
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent, String contentType) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: " + contentType + "\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void processHeaders(DataOutputStream dos, Map<String, String> headers) throws IOException {
        for (String key : headers.keySet()) {
            dos.writeBytes(key + ": " + headers.get(key) + "\r\n");
        }
    }
}
