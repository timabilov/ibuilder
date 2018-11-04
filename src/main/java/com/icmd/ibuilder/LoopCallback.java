package com.icmd.ibuilder;

public interface LoopCallback<E> extends Callback<E, Loop>{

    public Loop call(E item);
}