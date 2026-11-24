package technology;

public enum RandomizerKind {
    LXM, CLASSIC;

    public Randomizer create() {
        return switch (this) {
            case LXM -> new LxmRandomizer();
            case CLASSIC -> new BasicRandomizer();
        };
    }
}
