package controller;

import webserver.HttpRequest;
import webserver.HttpResponse;

public class ForwardController implements Controller {
    @Override
    public void execute(HttpRequest request, HttpResponse response) {
        String url = request.getPath();
        response.forward(url);
    }

    @Override
    public boolean supports(HttpRequest request) {
        return request.getMethod().equals("GET");
    }
}
