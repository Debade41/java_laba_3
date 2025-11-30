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
                Thread.sleep(200); 
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Log.msg("Monitor", "Монитор очереди остановлен");
    }
}