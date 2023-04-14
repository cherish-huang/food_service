package com.cherish.utils;

public class StringUtils {
    public static boolean isEqual(String str01, String str02){
        if(str01 == null) return str02 == null;
        return str01.equals(str02);
    }
}
