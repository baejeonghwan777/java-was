package controller;

import db.DataBase;
import enumfile.Cookie;
import enumfile.Login;
import model.Memo;
import model.User;
import webserver.HttpRequest;
import webserver.HttpResponse;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

import static util.HttpRequestUtils.parseQueryString;

public class MemoController implements Controller {
    @Override
    public void execute(HttpRequest request, HttpResponse response) throws UnsupportedEncodingException {
        String[] cookieInfo = request.getCookie().split("[:=]");
        User loginUser = DataBase.findUserByCookieId(cookieInfo[Cookie.COOKIE_VALUE_INDEX.getIndex()].trim());
        String bodyData = request.getBody();

        int loginAfterFlag = checkCookie(request, loginUser);
        if(loginAfterFlag == Login.UNDEFINED.getFlag()) {
            response.sendRedirect("/user/login.html");
            return;
        }

        Map<String, String> params = parseQueryString(bodyData);
        String content = URLDecoder.decode(params.get("content"), "UTF-8");

        Memo memo = new Memo(loginUser.getName(), content);
        DataBase.addMemo(memo);
        response.sendRedirect("/index.html");
    }

    public int checkCookie(HttpRequest request, User loginUser) {
        if(loginUser != null)
            return Login.LOGIN_SUCCESS.getFlag();
        return Login.UNDEFINED.getFlag();
    }
}
