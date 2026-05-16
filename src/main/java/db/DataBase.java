package db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import model.Memo;
import model.User;

public class DataBase {
    private static Map<String, User> users = Maps.newHashMap();
    private static Map<String, User> cookies= Maps.newHashMap();
    private static List<Memo> memos = new ArrayList<>();

    public static void addUser(User user) {
        users.put(user.getUserId(), user);
    }

    public static void addCookie(String id, User user) {
        cookies.put(id, user);
    }

    public static void addMemo(Memo memo) {
        memos.add(memo);
    }


    public static User findUserById(String userId) {
        return users.get(userId);
    }

    public static User findUserByCookieId(String cookieId) {
        return cookies.get(cookieId);
    }

    public static Collection<User> findAll() {
        return users.values();
    }

    public static List<Memo> findAllMemos() {
        return memos;
    }

    public static void clear() {
        users.clear();
        cookies.clear();
    }
}
