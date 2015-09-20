package through;


import io.netty.util.concurrent.Promise;

public abstract class Command<T>  {

    private Promise<Result<T>> promise;

    public abstract Result<T> run();

    public Promise<Result<T>> getPromise() {
        return promise;
    }

    public void setPromise(Promise<Result<T>> promise) {
        this.promise = promise;
    }
}
