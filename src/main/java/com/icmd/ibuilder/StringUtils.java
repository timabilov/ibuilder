package com.icmd.ibuilder;public class StringUtils {


    public static String repeat(char s, int repeatCount){

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < repeatCount; i++){
            sb.append(s);
        }

        return sb.toString();
    }

    static String indentNewLine(String text, int level){


        return text.replaceAll("\n", "\n" + StringUtils.repeat(' ', level + 3));

    }
}
