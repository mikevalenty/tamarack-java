package com.github.mikevalenty.tamarack;

import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Providers;

import java.util.LinkedList;
import java.util.Queue;

public class Pipeline<T, TOut> {
    private final FilterFactory factory;
    private final Queue<Filter<T, TOut>> queue;

    public Pipeline(FilterFactory factory) {
        this.factory = factory;
        this.queue = new LinkedList<Filter<T, TOut>>();
    }

    public Pipeline() {
        this(new DefaultFilterFactory());
    }

    public Pipeline<T, TOut> add(Filter<T, TOut> filter) {
        queue.add(filter);
        return this;
    }

    public Pipeline<T, TOut> add(Class<? extends Filter<T, TOut>> filterClass) {
        Filter<T, TOut> filter = factory.create(filterClass);
        queue.add(filter);
        return this;
    }

    public Pipeline<T, TOut> add(TypeLiteral<? extends Filter<T, TOut>> typeLiteral) {
        @SuppressWarnings("unchecked")
        Class<? extends Filter<T, TOut>> filterClass = (Class<? extends Filter<T, TOut>>) typeLiteral.getRawType();
        Filter<T, TOut> filter = factory.create(filterClass);
        queue.add(filter);
        return this;
    }

    private class NextFilter extends AbstractFilter<T, TOut> {
        private final Queue<Filter<T, TOut>> queue;

        public NextFilter(Queue<Filter<T, TOut>> queue) {
            this.queue = new LinkedList<Filter<T, TOut>>(queue);
        }

        @Override
        public TOut execute(T context, Provider<Filter<T, TOut>> nextProvider) {
            while (!queue.isEmpty()) {
                Filter<T, TOut> filter = queue.remove();
                if (filter.canExecute(context)) {
                    Filter<T, TOut> next = this;
                    return filter.execute(context, Providers.of(next));
                }
            }

            throw new EndOfChainException();
//            return nextProvider.get().execute(context, nextProvider);
        }
    }

    public TOut execute(T context) {
        Filter<T, TOut> endOfChain = new EndOfChainFilter<T, TOut>();
        return new NextFilter(queue).execute(context, Providers.of(endOfChain));
    }

    public static class EndOfChainFilter<T, TOut> extends AbstractFilter<T, TOut> {
        @Override
        public TOut execute(T context, Provider<Filter<T, TOut>> nextProvider) {
            throw new EndOfChainException();
        }
    }
}
