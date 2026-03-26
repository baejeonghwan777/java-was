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
import java.nio.file.Paths;
import java.util.Map;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;

import static util.HttpRequestUtils.parseQueryString;

public class RequestHandler extends Thread {
    private static final int PATH_INDEX = 0;
    private static final int USER_INDEX = 1;

    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.

            BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String line = br.readLine();
            log.debug("request line : {}", line);
            String url = HttpRequestUtils.extractPath(line);

            // ?를 통해 아이디와 비밀번호를 받아오는지 확인하기
            String path = url;
            if(url.contains("?")) path = saveUser(url);
            url = Paths.get(path).normalize().toString().replace("\\", "/");

            // 클라이언트 데이터를 line by line으로 읽음 로그를 사용하면 쓰레드 출처도 확인 가능
            while(!line.isEmpty()) {
                line = br.readLine();
                log.debug("header line : {}", line);
            }
            DataOutputStream dos = new DataOutputStream(out);
            byte[] body = HttpRequestUtils.readPath("./webapp", url);
            response200Header(dos, body.length);
            responseBody(dos, body);
        } catch (IOException | NullPointerException e) {
            log.error(e.getMessage());
        }
    }

    private String saveUser(String url) throws UnsupportedEncodingException{
        String[] information = url.split("\\?");
        String path = information[PATH_INDEX];
        String data = information[USER_INDEX];
        makeUser(data);
        return path;
    }

    private void makeUser(String data) throws UnsupportedEncodingException {
        Map<String, String> userInstance = parseQueryString(data);
        for (Map.Entry<String, String> entry : userInstance.entrySet()) {
            userInstance.put(entry.getKey(), URLDecoder.decode(entry.getValue(), "UTF-8"));
        }
        User user = new User(userInstance.get("userId"), userInstance.get("password"),
                userInstance.get("name"), userInstance.get("email"));
        DataBase.addUser(user);
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
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
}
