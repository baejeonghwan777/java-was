package webserver;

import enumfile.HeaderIndex;
import enumfile.PathIndex;
import util.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static util.HttpRequestUtils.parseQueryString;

public class HttpRequest {
    private String method;
    private String path;
    private final Map<String, String> headers = new HashMap<>();
    private String body;
    private String cookie;
    private String queryString;

    public static HttpRequest from(BufferedReader br) throws IOException {
        HttpRequest request = new HttpRequest();

        String line = br.readLine();
        if (line == null || line.isEmpty()) {
            throw new IllegalArgumentException("Invalid HTTP Request: Request line is empty");
        }

        String[] firstLine = line.split(" ");
        if (firstLine.length == PathIndex.MAX_INDEX.getIndex()) {
            request.method = firstLine[PathIndex.METHOD_INDEX.getIndex()];
            String rawUrl = firstLine[PathIndex.URL_INDEX.getIndex()];
            URI uri = URI.create(rawUrl);
            request.path = Paths.get(uri.getPath()).normalize().toString().replace("\\", "/");
            request.queryString = uri.getQuery();
        }

        while ((line = br.readLine()) != null && !line.isEmpty()) {
            String[] headerTokens = line.split(": ");
            if (headerTokens.length == HeaderIndex.MAX_INDEX.getIndex()) {
                request.headers.put(headerTokens[HeaderIndex.KEY_INDEX.getIndex()], headerTokens[HeaderIndex.VALUE_INDEX.getIndex()]);
            }
        }

        request.cookie = request.headers.getOrDefault("Cookie", null);

        if ("POST".equals(request.method)) {
            int contentLength = Integer.parseInt(request.headers.getOrDefault("Content-Length", "0"));
            request.body = IOUtils.readData(br, contentLength);
        }

        return request;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getBody() {
        return body;
    }

    public String getCookie() {
        return cookie;
    }

    // 공통 헤더 추가
    public String getHeader(String key) {
        return headers.get(key);
    }

    public String getParameter(String key) throws UnsupportedEncodingException {
        String data = "";
        if ("GET".equalsIgnoreCase(this.method)) {
            data = this.queryString; // URL 뒤에 붙은 쿼리 스트링 사용
        } else if ("POST".equalsIgnoreCase(this.method)) {
            data = this.body;        // HTTP Body에 담긴 데이터 사용
        }
        if (data == null || data.isEmpty()) {
            return null;
        }

        Map<String, String> instance = parseQueryString(data);
        String value = instance.get(key);

        if (value == null) {
            return null;
        }

        return URLDecoder.decode(value, "UTF-8");
    }
}
