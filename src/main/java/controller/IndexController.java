package controller;

import db.DataBase;
import model.Memo;
import util.HttpRequestUtils;
import webserver.HttpRequest;
import webserver.HttpResponse;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.stream.Collectors;

public class IndexController implements Controller {
    @Override
    public void execute(HttpRequest request, HttpResponse response) throws IOException {
        String url = request.getPath();
        byte[] fileBytes = HttpRequestUtils.readPath("./webapp", "/index.html");
        String htmlString = new String(fileBytes, StandardCharsets.UTF_8);

        StringBuilder memoRows = new StringBuilder();
        for (Memo m : DataBase.findAllMemos().stream()
                .sorted(Comparator.comparing(Memo::getDate, Comparator.reverseOrder())) // 변환 없이 문자열 역순 정렬
                .limit(5)
                .collect(Collectors.toList())) {
            memoRows.append("<tr>")
                    .append("<td>").append(m.getDate()).append("</td>")
                    .append("<td>").append(m.getWriter()).append("</td>")
                    .append("<td>").append(m.getContent()).append("</td>")
                    .append("</tr>");
        }

        htmlString = htmlString.replace("${memoList}", memoRows.toString());

        response.build(url, htmlString);
    }
}
