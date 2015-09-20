package through;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorServicePool {

    private static final Map<String, ExecutorService> pool = new ConcurrentHashMap<String, ExecutorService>();

    public static ExecutorService get(String key) {
        ExecutorService executorService;
        if(pool.containsKey(key)) {
            executorService = pool.get(key);
        } else {
            executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 10);
            pool.put(key, executorService);
        }
        return executorService;
    }

    public static void shutdown() {
        pool.values().stream().forEach(v -> v.shutdown());
    }

}
