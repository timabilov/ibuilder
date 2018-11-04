package com.icmd.ibuilder.forms;

import java.lang.reflect.Field;

public class Form {

    public static <E> E scan(Class<E> clazz){

        for (Field f: clazz.getDeclaredFields()){

            /*
            get all supported raw types
            and force user to enter this fields with result return
             */
        }

        return null;

    }
}
