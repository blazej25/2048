package technology;

import java.util.Random;

public class BasicRandomizer implements Randomizer{

    private Random random;

    public BasicRandomizer() {
        this.random = new Random();
    }

    @Override
    public int getRandomCoordinate() {
        return random.nextInt(4);
    }

    @Override
    public int getRandomValue() {
        int value = random.nextInt(0, 10);

        if (value == 0) return 4;

        return 2;
    }
}
