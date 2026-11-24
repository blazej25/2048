package technology;

import java.util.random.RandomGenerator;

public final class LxmRandomizer implements Randomizer {

    private final RandomGenerator random = RandomGenerator.of("L64X128MixRandom");

    @Override
    public int getRandomIndex(int bound) {
        return random.nextInt(bound);
    }
}
