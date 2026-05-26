package controller;

import webserver.HttpRequest;
import webserver.HttpResponse;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public interface Controller {
    void execute(HttpRequest request, HttpResponse response) throws IOException;
}
