package controller;

import webserver.HttpRequest;
import webserver.HttpResponse;
import java.io.IOException;

public abstract class AbstractController implements Controller {
    @Override
    public void execute(HttpRequest request, HttpResponse response) throws IOException {
        String method = request.getMethod();

        if ("GET".equalsIgnoreCase(method)) {
            doGet(request, response);
        } else if ("POST".equalsIgnoreCase(method)) {
            doPost(request, response);
        } else {
            throw new IllegalArgumentException("Invalid HTTP Method " + method);
        }
    }

    protected void doGet(HttpRequest request, HttpResponse response) throws IOException {
    }

    protected void doPost(HttpRequest request, HttpResponse response) throws IOException {
    }
}
