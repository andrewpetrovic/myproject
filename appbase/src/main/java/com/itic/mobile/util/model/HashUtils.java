package com.itic.mobile.util.model;

import java.util.Locale;

/**
 * 生成hashcode
 * @author Andrea Ji
 */
public class HashUtils {
    public static String computeWeakHash(String string) {
        return String.format(Locale.CHINA, "%08x%08x", string.hashCode(), string.length());
    }
}
