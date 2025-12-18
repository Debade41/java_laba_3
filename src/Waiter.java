import java.util.Random;
import java.util.concurrent.BlockingQueue;

public class Waiter implements Runnable {
    private static final long ORDER_WAIT_CHUNK_MS = 1000L;
    private static final long MIN_TOTAL_ORDER_WAIT_MS = 10_000L;

    private final String name;
    private final BlockingQueue<Order> kitchenQueue;
    private final Random random = new Random();
    private volatile boolean running = true;

    public Waiter(String name, BlockingQueue<Order> kitchenQueue) {
        this.name = name;
        this.kitchenQueue = kitchenQueue;
    }

    public String getName() {
        return name;
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        while (running) {
            try {
                String clientName = "Client-" + random.nextInt(1000);
                DishType dish = DishType.randomDish(random);
                Order order = new Order(clientName, name, dish);

                Log.msg(name, "Принял заказ: " + order);

                if (kitchenQueue.remainingCapacity() == 0) {
                    Log.msg(name, "Очередь кухни заполнена, официант ждёт, чтобы передать заказ: " + order);
                }

                kitchenQueue.put(order);
                Log.msg(name, "Передал заказ на кухню: " + order);

                Log.msg(name, "Ждёт готовности блюда по " + order);
                boolean orderReady = waitForDishReady(order);
                if (!orderReady) {
                    if (!running) {
                        break;
                    }
                    Log.msg(name, "Не дождался готовности блюда по " + order + ", переходит к новому заказу");
                    continue;
                }

                Log.msg(name, "Отнёс блюдо клиенту по " + order);

                if (!sleepRespectingStop(300 + random.nextInt(700))) {
                    break;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        Log.msg(name, "Заканчивает смену");
    }

    private boolean sleepRespectingStop(long totalSleepMs) throws InterruptedException {
        long slept = 0;
        while (running && slept < totalSleepMs) {
            long chunk = Math.min(200, totalSleepMs - slept);
            Thread.sleep(chunk);
            slept += chunk;
        }
        return running;
    }

    private boolean waitForDishReady(Order order) throws InterruptedException {
        long maxWait = Math.max(order.getDishType().getCookTimeMs() * 2L, MIN_TOTAL_ORDER_WAIT_MS);
        long waited = 0;

        while (running && waited < maxWait) {
            long chunk = Math.min(ORDER_WAIT_CHUNK_MS, maxWait - waited);
            if (order.awaitReady(chunk)) {
                return true;
            }
            waited += chunk;
        }

        return false;
    }
}
