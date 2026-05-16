package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Memo {
    private String writer;
    private String content;
    private String date;

    public Memo(String writer, String content) {
        this.writer = writer;
        this.content = content;
        this.date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    public String getWriter() {
        return writer;
    }

    public String getContent() {
        return content;
    }

    public String getDate() {
        return date;
    }
}
