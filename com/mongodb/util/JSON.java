/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.util;

import com.mongodb.util.JSONParser;
import com.mongodb.util.JSONSerializers;
import org.bson.BSONCallback;

@Deprecated
public class JSON {
    public static String serialize(Object object) {
        StringBuilder buf = new StringBuilder();
        JSON.serialize(object, buf);
        return buf.toString();
    }

    public static void serialize(Object object, StringBuilder buf) {
        JSONSerializers.getLegacy().serialize(object, buf);
    }

    public static Object parse(String jsonString) {
        return JSON.parse(jsonString, null);
    }

    public static Object parse(String s, BSONCallback c) {
        if (s == null || s.trim().equals("")) {
            return null;
        }
        JSONParser p = new JSONParser(s, c);
        return p.parse();
    }

    static void string(StringBuilder a, String s) {
        a.append("\"");
        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            if (c == '\\') {
                a.append("\\\\");
                continue;
            }
            if (c == '\"') {
                a.append("\\\"");
                continue;
            }
            if (c == '\n') {
                a.append("\\n");
                continue;
            }
            if (c == '\r') {
                a.append("\\r");
                continue;
            }
            if (c == '\t') {
                a.append("\\t");
                continue;
            }
            if (c == '\b') {
                a.append("\\b");
                continue;
            }
            if (c < ' ') continue;
            a.append(c);
        }
        a.append("\"");
    }
}

