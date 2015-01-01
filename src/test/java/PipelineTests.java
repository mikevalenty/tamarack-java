import com.github.mikevalenty.tamarack.*;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class PipelineTests {

    public static class AddToInput extends AbstractFilter<Integer, String> {
        private final int value;

        public AddToInput(int value) {
            this.value = value;
        }

        @Override
        public String execute(Integer context, Provider<Filter<Integer, String>> next) {
            return next.get().execute(context + value, next);
        }
    }

    public static class AppendToOutput extends AbstractFilter<Integer, String> {
        private final String value;

        public AppendToOutput(String value) {
            this.value = value;
        }

        @Override
        public String execute(Integer context, Provider<Filter<Integer, String>> next) {
            String result = next.get().execute(context, next);
            return result + value;
        }
    }

    public static class InputToString extends AbstractFilter<Integer, String> {
        @Override
        public String execute(Integer context, Provider<Filter<Integer, String>> next) {
            return context.toString();
        }
    }

    public static class DoubleWhenInputIsEven implements Filter<Integer, String> {
        @Override
        public boolean canExecute(Integer context) {
            return context % 2 == 0;
        }

        @Override
        public String execute(Integer context, Provider<Filter<Integer, String>> next) {
            return next.get().execute(context * 2, next);
        }
    }

    @Test
    public void should_execute_filters_in_order() {

        String result = new Pipeline<Integer, String>()
            .add(new AddToInput(2))
            .add(new AddToInput(1))
            .add(new AppendToOutput("*"))
            .add(new AppendToOutput("!"))
            .add(new InputToString())
            .execute(5);

        assertThat(result, is("8!*"));
    }

    @Test
    public void should_create_new_instance_of_filter() {

        String result = new Pipeline<Integer, String>()
            .add(InputToString.class)
            .execute(5);

        assertThat(result, is("5"));
    }

    @Test
    public void should_build_filters_with_guice() {

        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            public void configure() {
                bind(AppendToOutput.class).toInstance(new AppendToOutput("$"));
            }
        });

        String result = new Pipeline<Integer, String>(new GuiceFilterFactory(injector))
            .add(new AddToInput(2))
            .add(AppendToOutput.class)
            .add(InputToString.class)
            .execute(5);

        assertThat(result, is("7$"));
    }

    @Test(expected = EndOfChainException.class)
    public void should_throw_end_of_chain_exception() {

        new Pipeline<Integer, String>()
            .add(new AddToInput(2))
            .execute(5);
    }

    @Test(expected = EndOfChainException.class)
    public void should_throw_end_of_chain_exception_when_only_filter_cant_execute() {

        new Pipeline<Integer, String>()
            .add(new DoubleWhenInputIsEven())
            .execute(5);
    }

    @Test
    public void should_skip_filter_when_can_execute_is_false() {

        Pipeline<Integer, String> pipeline = new Pipeline<Integer, String>()
            .add(DoubleWhenInputIsEven.class)
            .add(InputToString.class);

        assertThat(pipeline.execute(2), is("4"));
        assertThat(pipeline.execute(3), is("3"));
    }
}
