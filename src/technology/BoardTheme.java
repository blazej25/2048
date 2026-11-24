package technology;

import java.util.List;

final class BoardTheme {

    static final int SIZE = 4;
    static final int TILE_COLUMNS = 9;
    static final int TILE_ROWS = 3;
    static final String TITLE = "2048";
    static final String HIGHEST = "HIGHEST  ";
    static final String CONTROLS = "↑ ↓ ← →  Move   Esc  Quit";
    static final int INTRO_FALL_MS = 810;
    static final int INTRO_STAGGER_MS = 180;
    static final int INTRO_HOLD_MS = 1100;

    static final String INK = "#14121d";
    static final String PANEL = "#201d2c";
    static final String SLOT = "#312c3f";
    static final String BORDER = "#665878";
    static final String MUTED = "#c3b3cd";
    static final String PAPER = "#fff7e5";
    static final String GOLD = "#ffc769";
    static final String FLASH = "#ffe49b";
    static final List<String> TILE_COLORS = List.of(
            "#f1dfca", "#ffca85", "#f5a172", "#e98179",
            "#de779f", "#ce94e3", "#b0a9f7", "#89c0f4",
            "#7bdfd8", "#a0e3ad", GOLD
    );

    private BoardTheme() {}

    static int tileColorIndex(int value) {
        return Math.clamp(Integer.numberOfTrailingZeros(value) - 1, 0, TILE_COLORS.size() - 1);
    }
}
