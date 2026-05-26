package controller;

import db.DataBase;
import enumfile.Login;
import model.User;
import webserver.HttpRequest;
import webserver.HttpResponse;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;
import java.util.UUID;

import static util.HttpRequestUtils.parseQueryString;

public class LoginController implements Controller {
    int loginBeforeFlag = Login.UNDEFINED.getFlag();

    @Override
    public void execute(HttpRequest request, HttpResponse response) throws UnsupportedEncodingException {
        String userdata = request.getBody();
        loginBeforeFlag = checkUser(userdata, response);
        if(loginBeforeFlag == Login.LOGIN_FAIL.getFlag()) {
            response.sendRedirect("/user/login_failed.html");
            return;
        }
        response.sendRedirect("/index.html");
    }

    private int checkUser(String data, HttpResponse response) throws UnsupportedEncodingException {
        Map<String, String> userInstance = parseQueryString(data);

        String userId = URLDecoder.decode(userInstance.get("userId"), "UTF-8");
        String password = URLDecoder.decode(userInstance.get("password"), "UTF-8");

        User user = DataBase.findUserById(userId);
        if(user == null) return Login.LOGIN_FAIL.getFlag();
        if(!user.getPassword().equals(password)) return Login.LOGIN_FAIL.getFlag();
        String id = UUID.randomUUID().toString();
        DataBase.addCookie(id, user);
        response.addHeader("Set-Cookie","sessionId=" + id + "; Path=/;");
        return Login.LOGIN_SUCCESS.getFlag();
    }
}
