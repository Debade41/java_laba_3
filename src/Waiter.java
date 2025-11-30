import java.util.Random;
import java.util.concurrent.BlockingQueue;

public class Waiter implements Runnable {
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
                order.awaitReady();

                Log.msg(name, "Отнёс блюдо клиенту по " + order);

                Thread.sleep(300 + random.nextInt(700));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        Log.msg(name, "Заканчивает смену");
    }
}