package com.icmd.ibuilder;

public interface Callback<E, R> {

    public R call(E param);
}
