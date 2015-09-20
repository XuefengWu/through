package through;

import java.util.Optional;

/**
 * Created by twer on 9/19/15.
 */
public interface Result<T> {

    Boolean isOk();
    Optional<T> value();

    public static class Failure<T> implements Result<T> {

        @Override
        public Boolean isOk() {
            return false;
        }

        @Override
        public Optional<T> value() {
            return Optional.empty();
        }

        @Override
        public String toString() {
            return "Failure{}";
        }

    }

    public static class Ok<T> implements Result<T> {
        private T value;
        public Ok(T value) {
            this.value = value;
        }
        @Override
        public Boolean isOk() {
            return true;
        }

        @Override
        public Optional<T> value() {
            return Optional.of(value);
        }

        @Override
        public String toString() {
            return "Ok{" +
                    "value=" + value +
                    '}';
        }
    }


}
