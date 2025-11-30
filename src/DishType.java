import java.util.Random;

public enum DishType {
    ПИЦЦА(6000),
    САЛАТ(4000),
    СУП(5000),
    СТЕЙК(8000);

    private final int cookTimeMs;

    DishType(int cookTimeMs) {
        this.cookTimeMs = cookTimeMs;
    }

    public int getCookTimeMs() {
        return cookTimeMs;
    }

    public static DishType randomDish(Random random) {
        DishType[] values = values();
        return values[random.nextInt(values.length)];
    }
}