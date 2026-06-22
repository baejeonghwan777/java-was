package webserver;

import controller.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RequestMapper {
    private final HttpRequest httpRequest;
    private final HttpResponse httpResponse;

    private final Map<String, Controller> controllers = new HashMap<>();

    public RequestMapper(HttpRequest httpRequest, HttpResponse httpResponse) {
        this.httpRequest = httpRequest;
        this.httpResponse = httpResponse;

        initControllers();
    }

    private void initControllers() {
        controllers.put("/", new IndexController());
        controllers.put("/index.html", new IndexController());
        controllers.put("/user/create", new SignUpController());
        controllers.put("/user/login", new LoginController());
        controllers.put("/memo", new MemoController());
        controllers.put("/user/list", new ListController());
    }

    public void proceed() throws IOException {
        String path = httpRequest.getPath();
        Controller targetController = controllers.get(path);

        if (targetController != null) {
            targetController.execute(httpRequest, httpResponse);
        } else {
            handleStaticResource(path);
        }
    }

    private void handleStaticResource(String path) throws IOException {
        if ("GET".equalsIgnoreCase(httpRequest.getMethod())) {
            httpResponse.forward(path);
        } else {
            httpResponse.response404Header();
        }
    }
}
