import java.util.concurrent.BlockingQueue;

public class QueueMonitor implements Runnable {
    private final BlockingQueue<Order> kitchenQueue;
    private volatile boolean running = true;

    public QueueMonitor(BlockingQueue<Order> kitchenQueue) {
        this.kitchenQueue = kitchenQueue;
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        Log.msg("Monitor", "Монитор очереди запущен");
        Log.msg("Monitor", String.format("%-10s | %-15s", "Тик", "Размер очереди"));

        int tick = 0;

        try {
            while (running) {
                int size = kitchenQueue.size();
                Log.msg("Monitor", String.format("%-10d | %-15d", tick, size));
                tick++;
                if (!sleepRespectingStop(200)) {
                    break;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Log.msg("Monitor", "Монитор очереди остановлен");
    }

    private boolean sleepRespectingStop(long totalSleepMs) throws InterruptedException {
        long slept = 0;
        while (running && slept < totalSleepMs) {
            long chunk = Math.min(100, totalSleepMs - slept);
            Thread.sleep(chunk);
            slept += chunk;
        }
        return running;
    }
}
