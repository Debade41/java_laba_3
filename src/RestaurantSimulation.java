import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class RestaurantSimulation {
    public static void main(String[] args) throws InterruptedException {
        int waiterCount = 5;          
        int cookCount = 1;            
        int simulationTimeSec = 18;   

        BlockingQueue<Order> kitchenQueue = new ArrayBlockingQueue<>(3);

        QueueMonitor monitor = new QueueMonitor(kitchenQueue);
        Thread monitorThread = new Thread(monitor, "Queue-Monitor");
        monitorThread.start();

        Kitchen kitchen = new Kitchen(kitchenQueue, cookCount);

        List<Waiter> waiters = new ArrayList<>();
        List<Thread> waiterThreads = new ArrayList<>();

        for (int i = 1; i <= waiterCount; i++) {
            Waiter waiter = new Waiter("Waiter-" + i, kitchenQueue);
            waiters.add(waiter);
            Thread t = new Thread(waiter, waiter.getName());
            waiterThreads.add(t);
            t.start();
        }

        Log.msg("Main", "Симуляция ресторана запущена на " + simulationTimeSec + " секунд");

        Thread.sleep(simulationTimeSec * 1000L);

        for (Waiter w : waiters) {
            w.stop();
        }
        for (Thread t : waiterThreads) {
            t.interrupt();
        }
        for (Thread t : waiterThreads) {
            t.join();
        }

        kitchen.shutdown();

        monitor.stop();
        monitorThread.interrupt();
        monitorThread.join();

        Log.msg("Main", "Симуляция ресторана завершена");
    }
}