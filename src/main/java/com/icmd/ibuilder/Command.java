package com.icmd.ibuilder;import java.util.Scanner;

import static com.icmd.ibuilder.StringUtils.indentNewLine;

public class Command {

    String name; // used as key for typing in CMD
    String description; // used as hint in HEADER section
    String action;
    boolean hasParams = true;
    boolean isInteractive = true;
    CommandCallback cc = null;

    private Command(String name, String description) {

        this.name = name;
        this.description = description;
    }


    static Command create(String name, String action ){
        Command c = new Command(name, "");
        c.action = action;
        return c;

    }

    public static Command create(String name, String action, String description ){
        Command c = new Command(name, description);
        c.action = action;
        return c;

    }

    public Command attach(CommandCallback cc){

        this.cc = cc;
        return this;
    }


    public Object call(String argument, Scanner sc){
        return cc.call(argument, sc);

    }

    protected CommandCallback getCommandCallback(){

        return cc;
    }


    public boolean isInteractive() {
        return isInteractive;
    }

    public void setInteractive(boolean interactive) {
        isInteractive = interactive;
    }


    public String renderCommand(String indentation, int index){

        if (!isInteractive())
            return  indentNewLine(call("", Loop.s) + " (read only)", indentation.length() + 1);

        if (description != null && (!description.isEmpty()))
            return indentNewLine(description, indentation.length() + 1);


        return "If you want " + action  + (
                // Do not hint what to type if it is index itself
                (index + "").equals(name)? "":" (type: " + name + ")"
        );

    }


}
