package com.github.mikevalenty.tamarack;

public interface Filter<T, TOut> {

  boolean canExecute(T context);

  TOut execute(T context, Filter<T, TOut> next);
}
