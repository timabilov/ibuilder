package com.icmd.ibuilder;

import java.util.Scanner;

public class CLIUtils {

    static Scanner sc = new Scanner(System.in);

    public static String waitInput(String key){

        System.out.print(key + ":");
        return sc.next();
    }
}
