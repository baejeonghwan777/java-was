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

public class MemoController extends AbstractController {
    @Override
    public void doPost(HttpRequest request, HttpResponse response) throws UnsupportedEncodingException {
        String cookieHeader = request.getCookie();

        // cookie 자체가 null일때 에러 방지
        if (cookieHeader == null || cookieHeader.trim().isEmpty()) {
            response.sendRedirect("/user/login.html");
            return;
        }

        String[] cookieInfo = request.getCookie().split("[:=]");
        User loginUser = DataBase.findUserByCookieId(cookieInfo[Cookie.COOKIE_VALUE_INDEX.getIndex()].trim());
        String bodyData = request.getBody();

        int loginAfterFlag = checkCookie(loginUser);
        if(loginAfterFlag == Login.UNDEFINED.getFlag()) {
            response.sendRedirect("/user/login.html");
            return;
        }

        makeMemo(loginUser, bodyData);
        response.sendRedirect("/index.html");
    }

    @Override
    public boolean service(HttpRequest request) {
        return request.getPath().equals("/memo");
    }

    private void makeMemo(User loginUser, String bodyData) throws UnsupportedEncodingException {
        Map<String, String> params = parseQueryString(bodyData);
        String content = URLDecoder.decode(params.get("content"), "UTF-8");

        Memo memo = new Memo(loginUser.getName(), content);
        DataBase.addMemo(memo);
    }

    private int checkCookie(User loginUser) {
        if(loginUser != null)
            return Login.LOGIN_SUCCESS.getFlag();
        return Login.UNDEFINED.getFlag();
    }
}
