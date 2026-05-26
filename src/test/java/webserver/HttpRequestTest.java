package webserver;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;

import static org.junit.Assert.assertEquals;

public class HttpRequestTest {
    private final String testDirectory = "./src/test/resources/";

    @Test
    public void request_GET() throws Exception {
        InputStream in = Files.newInputStream(new File(testDirectory + "Http_GET.txt").toPath());
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        HttpRequest request = HttpRequest.from(br);

        assertEquals("GET", request.getMethod());
        assertEquals("/user/create", request.getPath());
        assertEquals("keep-alive", request.getHeader("Connection"));
        assertEquals("javajigi", request.getParameter("userId"));
    }

    @Test
    public void request_POST() throws Exception {
        InputStream in = Files.newInputStream(new File(testDirectory + "Http_POST.txt").toPath());
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        HttpRequest request = HttpRequest.from(br);

        assertEquals("POST", request.getMethod());
        assertEquals("/user/create", request.getPath());
        assertEquals("keep-alive", request.getHeader("Connection"));
        assertEquals("javajigi", request.getParameter("userId"));
    }
}
