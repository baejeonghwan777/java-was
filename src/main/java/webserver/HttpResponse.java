package webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    private static final Logger log = LoggerFactory.getLogger(HttpResponse.class);
    private final DataOutputStream dos;
    private final Map<String, String> headers = new HashMap<>();

    public HttpResponse(DataOutputStream dos) {
        this.dos = dos;
    }

    public void forward(String url) {
        try {
            byte[] body = HttpRequestUtils.readPath("./webapp", url);

            String contentType = "text/html;charset=utf-8"; // 기본값

            if (url.endsWith(".css")) {
                contentType = "text/css";
            } else if (url.endsWith(".png")) {
                contentType = "image/png";
            } else if (url.endsWith(".ico")) {
                contentType = "image/x-icon";
            }

            addHeader(url, contentType);
            response200Header(body.length, contentType);
            responseBody(body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void build(String url, String builder) {
        byte[] body = builder.getBytes(StandardCharsets.UTF_8);
        response200Header(body.length, "text/html;charset=utf-8");
        responseBody(body);
    }

    public void sendRedirect(String redirectPath) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Location: " + redirectPath + "\r\n");
            processHeaders(headers);
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(int lengthOfBodyContent, String contentType) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: " + contentType + "\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void processHeaders(Map<String, String> headers) throws IOException {
        for (String key : headers.keySet()) {
            dos.writeBytes(key + ": " + headers.get(key) + "\r\n");
        }
    }

    // 공통 헤더 추가
    public void addHeader(String key, String value) {
        headers.put(key, value);
    }
}
