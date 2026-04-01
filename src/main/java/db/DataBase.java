package db;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.Maps;

import model.User;

public class DataBase {
    private static Map<String, User> users = Maps.newHashMap();
    private static Map<String, User> cookies= Maps.newHashMap();

    public static void addUser(User user) {
        users.put(user.getUserId(), user);
    }

    public static void addCookie(String id, User user) {
        cookies.put(id, user);
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
}
