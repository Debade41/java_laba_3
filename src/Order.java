import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Order {
    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    private final int id;
    private final String clientName;
    private final String waiterName;
    private final DishType dishType;
    private final long createdAt;
    private final CountDownLatch readyLatch = new CountDownLatch(1);

    public Order(String clientName, String waiterName, DishType dishType) {
        this.id = COUNTER.incrementAndGet();
        this.clientName = clientName;
        this.waiterName = waiterName;
        this.dishType = dishType;
        this.createdAt = System.currentTimeMillis();
    }

    public int getId() {
        return id;
    }

    public String getClientName() {
        return clientName;
    }

    public String getWaiterName() {
        return waiterName;
    }

    public DishType getDishType() {
        return dishType;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void markReady() {
        readyLatch.countDown();
    }

    public void awaitReady() throws InterruptedException {
        readyLatch.await();
    }

    public boolean awaitReady(long timeoutMs) throws InterruptedException {
        if (timeoutMs <= 0) {
            return readyLatch.await(0, TimeUnit.MILLISECONDS);
        }
        return readyLatch.await(timeoutMs, TimeUnit.MILLISECONDS);
    }

    @Override
public String toString() {
    return String.format("Заказ#%d{клиент='%s', официант='%s', блюдо=%s}",
            id, clientName, waiterName, dishType);
}
}
