package com.perfexpert.parsetest;

import com.parse.ParseInstallation;
import com.parse.ParseUser;

import java.lang.reflect.Method;

public class ParseCacheUtils {

    static public void clearInstallationCache() throws Exception {
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        Class clazz = installation.getClass();
        Method method = clazz.getDeclaredMethod("getCurrentInstallationController");
        method.setAccessible(true);
        Object result = method.invoke(installation);

        method = result.getClass().getDeclaredMethod("clearFromDisk");
        method.setAccessible(true);
        method.invoke(result);
    }

    static public void clearUserCache() throws Exception {
        ParseUser user = ParseUser.getCurrentUser();
        Class clazz = user.getClass();
        Method method = clazz.getDeclaredMethod("getCurrentUserController");
        method.setAccessible(true);
        Object result = method.invoke(user);

        method = result.getClass().getDeclaredMethod("clearFromDisk");
        method.setAccessible(true);
        method.invoke(result);
    }
}
