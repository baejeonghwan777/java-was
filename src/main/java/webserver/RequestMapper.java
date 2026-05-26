package webserver;

import controller.Controller;
import controller.ForwardController;
import controller.IndexController;
import controller.ListController;
import controller.LoginController;
import controller.MemoController;
import controller.SignUpController;

import java.io.IOException;

public class RequestMapper {
    private final HttpRequest httpRequest;
    private final HttpResponse httpResponse;
    private Controller controller = new ForwardController();

    public RequestMapper(HttpRequest httpRequest, HttpResponse httpResponse) {
        this.httpRequest = httpRequest;
        this.httpResponse = httpResponse;
    }

    public void proceed() throws IOException {
        if (httpRequest.getMethod().equals("GET") && httpRequest.getPath().endsWith(".html")) {
            controller = new ForwardController();
        }

        if (httpRequest.getPath().equals("/index.html") || httpRequest.getPath().equals("/")) {
            controller = new IndexController();
        }

        if (httpRequest.getPath().equals("/user/create")) {
            controller = new SignUpController();
        }

        if (httpRequest.getPath().equals("/user/login")) {
            controller = new LoginController();
        }

        if (httpRequest.getPath().equals("/memo")) {
            controller = new MemoController();
        }

        if (httpRequest.getPath().equals("/user/list")) {
            controller = new ListController();
        }
        controller.execute(httpRequest, httpResponse);
    }
}
