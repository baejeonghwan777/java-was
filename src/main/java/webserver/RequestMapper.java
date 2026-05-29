package webserver;

import controller.Controller;
import controller.ForwardController;
import controller.IndexController;
import controller.ListController;
import controller.LoginController;
import controller.MemoController;
import controller.SignUpController;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class RequestMapper {
    private final HttpRequest httpRequest;
    private final HttpResponse httpResponse;

    public RequestMapper(HttpRequest httpRequest, HttpResponse httpResponse) {
        this.httpRequest = httpRequest;
        this.httpResponse = httpResponse;
    }

    private final List<Controller> controllers = Arrays.asList(
            new IndexController(),
            new SignUpController(),
            new LoginController(),
            new MemoController(),
            new ListController(),
            new ForwardController()
    );

    public void proceed() throws IOException {
        Controller targetController = controllers.stream()
                .filter(controller -> controller.supports(httpRequest))
                .findFirst()
                .orElse(null);

        if (targetController != null) {
            targetController.execute(httpRequest, httpResponse);
        }
    }
}
