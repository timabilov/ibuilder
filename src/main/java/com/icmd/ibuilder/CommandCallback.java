package com.icmd.ibuilder;import java.util.Scanner;

public interface CommandCallback {

    Object call(String param, Scanner sc);

}
