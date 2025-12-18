import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Kitchen {
    private final BlockingQueue<Order> orderQueue;
    private final ExecutorService cooksPool;
    private volatile boolean running = true;

    public Kitchen(BlockingQueue<Order> orderQueue, int cookCount) {
        this.orderQueue = orderQueue;
        this.cooksPool = Executors.newFixedThreadPool(cookCount, new NamedThreadFactory("Cook"));

        for (int i = 0; i < cookCount; i++) {
            cooksPool.submit(this::cookLoop);
        }
    }

    private void cookLoop() {
        String cookName = Thread.currentThread().getName();
        try {
            while (running || !orderQueue.isEmpty()) {
                Order order;
                try {
                    // Ожидание заказа, но не вечно, чтобы можно было корректно завершиться
                    order = orderQueue.poll(500, TimeUnit.MILLISECONDS);
                    if (order == null) {
                        continue; 
                    }
                } catch (InterruptedException e) {
                    if (!running) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                    continue;
                }

                cookOrder(order, cookName);
            }
        } finally {
            Log.msg("Kitchen", cookName + " завершает работу");
        }
    }

    private void cookOrder(Order order, String cookName) {
        Log.msg(cookName,
                "Начал готовить " + order +
                        " (время приготовления " + order.getDishType().getCookTimeMs() + " мс)");
        try {
            Thread.sleep(order.getDishType().getCookTimeMs());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        order.markReady();
        long totalTime = System.currentTimeMillis() - order.getCreatedAt();
        Log.msg(cookName,
                "Закончил готовить " + order +
                        " (суммарное время " + totalTime + " мс)");
    }

    public void shutdown() {
        running = false; 
        cooksPool.shutdown();
        try {
            if (!cooksPool.awaitTermination(5, TimeUnit.SECONDS)) {
                cooksPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            cooksPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
        Log.msg("Kitchen", "Кухня остановлена");
    }

    private static final class NamedThreadFactory implements ThreadFactory {
        private final String prefix;
        private final AtomicInteger counter = new AtomicInteger(1);

        private NamedThreadFactory(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName(prefix + "-" + counter.getAndIncrement());
            return thread;
        }
    }
}
