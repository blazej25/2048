package technology;

import java.util.Random;

public class BasicRandomizer implements Randomizer {

    private final Random random = new Random();

    @Override
    public int getRandomIndex(int bound) {
        return random.nextInt(bound);
    }
}
