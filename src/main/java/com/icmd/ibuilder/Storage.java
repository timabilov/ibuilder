package com.icmd.ibuilder;

import java.util.HashMap;

public class Storage {


    HashMap<String, String> map;


    Storage(HashMap<String, String> map) {
        this.map = map;
    }

    Storage() {
        this.map = new HashMap<String, String>();
    }


    public String get(String key) {
        return map.get(key);
    }

    public String set(String key, String value) {
        return map.put(key, value);
    }


}
