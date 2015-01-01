package com.github.mikevalenty.tamarack;

import com.google.inject.Inject;
import com.google.inject.Injector;

public class GuiceFilterFactory implements FilterFactory {
  private final Injector injector;

  @Inject
  public GuiceFilterFactory(Injector injector) {
    this.injector = injector;
  }

  @Override
  public <T, TOut> Filter<T, TOut> create(Class<? extends Filter<T, TOut>> filterClass) {
    return injector.getInstance(filterClass);
  }
}
