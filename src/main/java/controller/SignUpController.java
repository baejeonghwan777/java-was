package controller;

import db.DataBase;
import model.User;
import webserver.HttpRequest;
import webserver.HttpResponse;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

import static util.HttpRequestUtils.parseQueryString;

public class SignUpController extends AbstractController {
    @Override
    public void doPost(HttpRequest request, HttpResponse response) throws UnsupportedEncodingException {
        String userdata = request.getBody();
        makeUser(userdata);
        response.sendRedirect("/index.html");
    }

    @Override
    public boolean service(HttpRequest request) {
        return request.getPath().equals("/user/create");
    }

    private void makeUser(String data) throws UnsupportedEncodingException {
        Map<String, String> userInstance = parseQueryString(data);

        String userId = URLDecoder.decode(userInstance.get("userId"), "UTF-8");
        String password = URLDecoder.decode(userInstance.get("password"), "UTF-8");
        String name = URLDecoder.decode(userInstance.get("name"), "UTF-8");
        String email = URLDecoder.decode(userInstance.get("email"), "UTF-8");

        User user = DataBase.findUserById(userId);
        if(user == null) {
            user = new User(userId, password, name, email);
            DataBase.addUser(user);
        }
    }
}
