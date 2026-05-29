package controller;

import db.DataBase;
import enumfile.Cookie;
import enumfile.Login;
import model.User;
import webserver.HttpRequest;
import webserver.HttpResponse;

public class ListController implements Controller {

    @Override
    public void execute(HttpRequest request, HttpResponse response) {
        String cookieHeader = request.getCookie();

        // cookie 자체가 null일때 에러 방지
        if (cookieHeader == null || cookieHeader.trim().isEmpty()) {
            response.sendRedirect("/user/login.html");
            return;
        }

        String[] cookieInfo = cookieHeader.split("[:=]");
        User loginUser = DataBase.findUserByCookieId(cookieInfo[Cookie.COOKIE_VALUE_INDEX.getIndex()].trim());

        int loginAfterFlag = checkCookie(loginUser);
        if(loginAfterFlag == Login.UNDEFINED.getFlag()) {
            response.sendRedirect("/user/login.html");
            return;
        }

        StringBuilder builder = makeList();
        response.build(builder.toString());
    }

    @Override
    public boolean supports(HttpRequest request) {
        return request.getPath().equals("/user/list");
    }

    private StringBuilder makeList() {
        StringBuilder builder = new StringBuilder();
        builder.append("<html><body>");
        builder.append("<h1>사용자 목록</h1>");
        builder.append("<table border='1'>");
        builder.append("<tr><th>아이디</th><th>이름</th><th>이메일</th></tr>");

        for (User user : DataBase.findAll()) {
            builder.append("<tr>");
            builder.append("<td>").append(user.getUserId()).append("</td>");
            builder.append("<td>").append(user.getName()).append("</td>");
            builder.append("<td>").append(user.getEmail()).append("</td>");
            builder.append("</tr>");
        }

        builder.append("</table>");
        builder.append("</body></html>");
        return builder;
    }

    private int checkCookie(User loginUser) {
        if(loginUser != null)
            return Login.LOGIN_SUCCESS.getFlag();
        return Login.UNDEFINED.getFlag();
    }
}
