package com.github.mikevalenty.tamarack;

import com.google.inject.Provider;

public interface Filter<T, TOut> {

    boolean canExecute(T context);

    TOut execute(T context, Provider<Filter<T, TOut>> nextProvider);
}
