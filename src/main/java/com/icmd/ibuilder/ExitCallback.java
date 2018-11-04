package com.icmd.ibuilder;import com.icmd.ibuilder.exception.BreakLoopException;

import java.util.Scanner;

public class ExitCallback implements CommandCallback {


    public String call(String param, Scanner s) {
        throw new BreakLoopException();
    }
}
