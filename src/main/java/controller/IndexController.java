package controller;

import db.DataBase;
import model.Memo;
import util.HttpRequestUtils;
import webserver.HttpRequest;
import webserver.HttpResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.stream.Collectors;

public class IndexController implements Controller {
    @Override
    public void execute(HttpRequest request, HttpResponse response) throws IOException {
        byte[] fileBytes = HttpRequestUtils.readPath("./webapp", "/index.html");
        String htmlString = new String(fileBytes, StandardCharsets.UTF_8);

        StringBuilder memoRows = readMemo();
        htmlString = htmlString.replace("${memoList}", memoRows.toString());

        response.build(htmlString);
    }

    @Override
    public boolean supports(HttpRequest request) {
        return request.getPath().equals("/index.html") || request.getPath().equals("/");
    }

    private StringBuilder readMemo() {
        StringBuilder memoRows = new StringBuilder();
        for (Memo m : DataBase.findAllMemos().stream()
                .sorted(Comparator.comparing(Memo::getDate, Comparator.reverseOrder())) // 변환 없이 문자열 역순 정렬
                .limit(5)
                .collect(Collectors.toList())) {
            memoRows.append("<tr>")
                    // escape 적용
                    .append("<td>").append(escapeHtml(m.getDate())).append("</td>")
                    .append("<td>").append(escapeHtml(m.getWriter())).append("</td>")
                    .append("<td>").append(escapeHtml(m.getContent())).append("</td>")
                    .append("</tr>");
        }
        return memoRows;
    }

    private String escapeHtml(String input) {
        if (input == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder(input.length() + 16);
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (c) {
                case '<':  builder.append("&lt;"); break;
                case '>':  builder.append("&gt;"); break;
                case '&':  builder.append("&amp;"); break;
                case '"':  builder.append("&quot;"); break;
                case '\'': builder.append("&#x27;"); break;
                case '/':  builder.append("&#x2F;"); break;
                default:   builder.append(c); break;
            }
        }
        return builder.toString();
    }
}
