package webserver;

import enumfile.HeaderIndex;
import enumfile.PathIndex;
import util.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private String method;
    private String path;
    private final Map<String, String> headers = new HashMap<>();
    private String body;
    private String cookie;

    public static HttpRequest from(BufferedReader br) throws IOException {
        HttpRequest request = new HttpRequest();

        String line = br.readLine();
        if (line == null || line.isEmpty()) {
            throw new IllegalArgumentException("Invalid HTTP Request: Request line is empty");
        }

        String[] firstLine = line.split(" ");
        if (firstLine.length == PathIndex.MAX_INDEX.getIndex()) {
            request.method = firstLine[PathIndex.METHOD_INDEX.getIndex()];
            request.path = Paths.get(firstLine[PathIndex.URL_INDEX.getIndex()]).normalize().toString().replace("\\", "/");
        }

        while (!(line = br.readLine()).isEmpty()) {
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
}
