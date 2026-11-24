package technology;

public interface Randomizer {

    /** Returns a uniformly sampled index from zero (inclusive) to bound (exclusive). */
    int getRandomIndex(int bound);

    default int getRandomValue() {
        return getRandomIndex(10) == 0 ? 4 : 2;
    }
}
