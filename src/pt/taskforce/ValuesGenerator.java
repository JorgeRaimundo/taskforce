package pt.taskforce;

import java.util.Random;

public class ValuesGenerator {
    private static final int MIN_VALUE = 0;
    private static final int MAX_VALUE = 50;
    private static final int MAX_STEP = 6;
    private static final Random rand = new Random();

    public static int getNextValue(int value) {
        int change = rand.nextInt(MAX_STEP + 1) - (MAX_STEP / 2);
        value += change;

        if (value < MIN_VALUE) return MIN_VALUE;
        if (value > MAX_VALUE) return MAX_VALUE;
        return value;
    }
}
