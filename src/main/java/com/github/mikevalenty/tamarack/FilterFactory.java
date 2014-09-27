package com.github.mikevalenty.tamarack;

public interface FilterFactory {
    <T, TOut> Filter<T, TOut> create(Class<? extends Filter<T, TOut>> filterClass);
}
