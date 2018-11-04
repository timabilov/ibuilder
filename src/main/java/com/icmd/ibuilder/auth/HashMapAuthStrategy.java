package com.icmd.ibuilder.auth;

import java.util.HashMap;
import java.util.Scanner;

import static com.icmd.ibuilder.CLIUtils.waitInput;


/**
 * Thin and simple implemented auth strategy with HashMap(String,String)
 */
public class HashMapAuthStrategy  extends AuthStrategy {

    HashMap<String, String> credentials = new HashMap<String, String>();


    public HashMapAuthStrategy(HashMap<String, String> credentials) {
        this.credentials = credentials;
    }

    @Override
    public boolean authenticate() {
        Scanner sc = new Scanner(System.in);
        String username = waitInput("username");
        String password = waitInput("password");
        String storedPassword = credentials.get(username);
        return (storedPassword != null)  && storedPassword.equals(password);



    }

//
    @Override
    public boolean register(String username, String password) {
        if (credentials.get(username) != null ) {
            System.out.println("Username already exists!");
            return false;
        }
        credentials.put(username, password);
//        System.out.println(credentials);
        return true;
    }
}
