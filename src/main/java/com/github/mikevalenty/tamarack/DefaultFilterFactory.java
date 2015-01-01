package com.github.mikevalenty.tamarack;

public class DefaultFilterFactory implements FilterFactory {
  @Override
  public <T, TOut> Filter<T, TOut> create(Class<? extends Filter<T, TOut>> filterClass) {
    try {
      return filterClass.newInstance();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
