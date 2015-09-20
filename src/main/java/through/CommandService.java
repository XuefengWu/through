package through;

import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.concurrent.Promise;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class CommandService {

    private static final Map<String, BlockingQueue<Command>> pool = new ConcurrentHashMap<String, BlockingQueue<Command>>();
    private static final Map<String, CommandRunner> runners = new ConcurrentHashMap<String, CommandRunner>();

    private static final ExecutorService queueExecutorService = Executors.newSingleThreadExecutor();
    private static final ExecutorService schudlerExecutor = Executors.newSingleThreadExecutor();


    private static BlockingQueue<Command> get(final String key) {
        final BlockingQueue<Command> queue;
        if (pool.containsKey(key)) {
            queue = pool.get(key);
        } else {
            //TODO: pool should be init when start and use HashMap to improve runtime performance.
            queue = new LinkedBlockingQueue<Command>(1000000);

            final ExecutorService executorService = ExecutorServicePool.get(key);
            CommandRunner runner = new CommandRunner(queue, executorService);

            runners.put(key, runner);
            //TODO: save schudlerExecutor for key
            schudlerExecutor.execute(runner);

            pool.put(key, queue);
        }
        return queue;
    }


    private static final AtomicLong total = new AtomicLong();
    private static final AtomicLong refuse = new AtomicLong();

    public static <T> Future<Result<T>> submit(Command<T> cmd) {
        String key = cmd.getClass().getName();
        BlockingQueue<Command> queue = get(key);
        final Promise<Result<T>> promise = GlobalEventExecutor.INSTANCE.newPromise();
        queueExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                total.incrementAndGet();
                cmd.setPromise(promise);

                if (!queue.offer(cmd)) {
                    //System.out.println("Queue is too full");
                    refuse.incrementAndGet();
                    promise.setSuccess(new Result.Failure());
                }
            }
        });

        return promise;
    }


    public static void shutdown() throws InterruptedException {
        System.out.println("Total: " + total.get());
        System.out.println("Refuse: " + refuse.get());

        Integer sum = pool.values().stream().map(q -> q.size()).reduce((a, b) -> a + b).get();
        System.out.println("Queue size: " + sum);
        System.out.println("Submitted : " + (total.get() - refuse.get() - sum));

        runners.values().forEach(r -> r.shutdown());
        queueExecutorService.shutdown();
        schudlerExecutor.shutdownNow();
        ExecutorServicePool.shutdown();
    }

}
