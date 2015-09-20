package through;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by twer on 9/20/15.
 */
public class CommandRunner implements Runnable {

    private final Semaphore semaphore = new Semaphore(Runtime.getRuntime().availableProcessors() * 50);
    private final AtomicLong tps = new AtomicLong();
    private final AtomicLong failure = new AtomicLong();
    private final AtomicLong delay = new AtomicLong();
    private final AtomicInteger takenPermit = new AtomicInteger();

    private final BlockingQueue<Command> queue;
    private final ExecutorService executorService;

    public CommandRunner(BlockingQueue<Command> queue, ExecutorService executorService) {
        this.queue = queue;
        this.executorService = executorService;
    }

    private Boolean running = true;
    @Override
    public void run() {
        while (running) {
            try {
                final Command cmd = queue.take();
                semaphore.acquire();
                if (tps.get() < 1000000) {
                    try {
                        executorService.execute(new Runnable() {
                            @Override
                            public void run() {
                                try {

                                    tps.incrementAndGet();
                                    Result result = cmd.run();
                                    cmd.getPromise().setSuccess(result);
                                    //TODO: mark metrics finished
                                    if (!result.isOk()) {
                                        //TODO: failure count
                                        failure.incrementAndGet();
                                    } else {
                                        failure.decrementAndGet();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    //TODO: failure count
                                    failure.incrementAndGet();
                                } finally {
                                    semaphore.release();
                                    tps.decrementAndGet();
                                }

                                if (failure.get() > 1000) {
                                    if (semaphore.tryAcquire(10)) {
                                        takenPermit.set(takenPermit.get() + 10);
                                    }
                                } else {
                                    semaphore.release(takenPermit.get());
                                }

                            }
                        });

                    } catch (Exception e) {
                        semaphore.release();
                        //TODO: failure count
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                //TODO: failure count
                e.printStackTrace();
            }
        }
        System.out.println("Terminal");
    }

    public void shutdown(){
        this.running = false;

        System.out.println("Shutdown runner");
    }

}
