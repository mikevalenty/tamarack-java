package com.github.mikevalenty.tamarack;

public abstract class AbstractFilter<T, TOut> implements Filter<T, TOut> {
    @Override
    public boolean canExecute(T context) {
        return true;
    }
}
