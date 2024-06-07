package com.so.filesystem.util;

public class StringUtils {
    public static String formatFreeSpace(int freeSpace){
        StringBuilder sb = new StringBuilder();
        String value = String.valueOf(freeSpace);
        if (value.length() > 9){
            String s1 = value.substring(value.length() - 9,value.length() - 6);
            String s = value.substring(0, value.length() - 9);
            sb.append(s).append(".").append(s1).append(" Gb");
        }else if(value.length() > 6){
            String s1 = value.substring(value.length() - 6,value.length() - 3);
            String s = value.substring(0, value.length() - 6);
            sb.append(s).append(".").append(s1).append(" Mb");
        } else if (value.length() > 3) {
            String s1 = value.substring(value.length() - 3);
            String s = value.substring(0,value.length() - 3);
            sb.append(s1).append(".").append(s).append(" Kb");
        } else {
            sb.append(value).append(" Bytes");
        }
        return sb.toString();
    }
}
