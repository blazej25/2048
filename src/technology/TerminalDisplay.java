package technology;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;
import engine.Board;
import engine.Board.Cell;
import engine.Board.TileMove;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class TerminalDisplay implements Display {

    private static final int SIZE = 4;
    private static final TextColor INK = color(20, 18, 29);
    private static final TextColor PANEL = color(32, 29, 44);
    private static final TextColor SLOT = color(49, 44, 63);
    private static final TextColor BORDER = color(102, 88, 120);
    private static final TextColor MUTED = color(195, 179, 205);
    private static final TextColor PAPER = color(255, 247, 229);
    private static final TextColor GOLD = color(255, 199, 105);
    private static final TextColor FLASH = color(255, 228, 155);
    private static final TextColor[] TILE_COLORS = {
            color(241, 223, 202), color(255, 202, 133), color(245, 161, 114),
            color(233, 129, 121), color(222, 119, 159), color(206, 148, 227),
            color(176, 169, 247), color(137, 192, 244), color(123, 223, 216),
            color(160, 227, 173), GOLD
    };

    private final Screen screen;
    private final TextGraphics graphics;
    private final Board board;
    private final int[][] previous = new int[SIZE][SIZE];

    private record Tile(int value, double row, double column, boolean lit) {}
    private record Layout(int x, int y, int tileWidth, int tileHeight) {
        int boardWidth() { return SIZE * (tileWidth + 1) - 1; }
        int boardHeight() { return SIZE * (tileHeight + 1) - 1; }
        int width() { return boardWidth() + 4; }
        int height() { return boardHeight() + 6; }
    }

    public TerminalDisplay(Screen screen, TextGraphics textGraphics, Board board) {
        this.screen = screen;
        this.graphics = textGraphics;
        this.board = board;
    }

    @Override
    public void showMenu() {
        // Controls are drawn in the footer of every frame.
    }

    @Override
    public void showBoard() throws IOException {
        screen.setCursorPosition(null);
        if (layout() != null) {
            playIntro();
        }
        refreshBoard();
    }

    @Override
    public void refreshBoard() throws IOException {
        List<Tile> tiles = boardTiles(Set.of(), null);
        drawFrame(tiles, false);
        for (int row = 0; row < SIZE; row++) {
            for (int column = 0; column < SIZE; column++) {
                previous[row][column] = board.getValue(row, column);
            }
        }
    }

    @Override
    public void refreshBoard(List<TileMove> moves, Cell spawned) throws IOException {
        boolean moved = moves.stream().anyMatch(move -> !move.from().equals(move.to()) || move.mergedFrom() != null);
        if (layout() != null) {
            if (moved) {
                for (int frame = 1; frame <= 4; frame++) {
                    double progress = 1 - Math.pow(1 - frame / 4.0, 2);
                    drawFrame(slidingTiles(moves, progress), false);
                    pause(23);
                }
            }

            Set<Cell> merges = moves.stream().filter(move -> move.mergedFrom() != null)
                    .map(TileMove::to).collect(Collectors.toSet());
            if (spawned != null || !merges.isEmpty()) {
                List<Tile> settled = boardTiles(merges, spawned);
                for (double offset : new double[]{-0.85, -0.48, 0, -0.25}) {
                    List<Tile> tiles = new ArrayList<>(settled);
                    if (spawned != null) {
                        int value = board.getValue(spawned.row(), spawned.column());
                        tiles.add(new Tile(value, spawned.row() + offset, spawned.column(), offset == 0));
                    }
                    drawFrame(tiles, false);
                    pause(27);
                }
            }
        }
        refreshBoard();
    }

    private void playIntro() throws IOException {
        int[] digits = {2, 0, 4, 8};
        int fallFrames = 18;
        int staggerFrames = 4;
        int lastFrame = fallFrames + (digits.length - 1) * staggerFrames;
        for (int frame = 0; frame <= lastFrame; frame++) {
            List<Tile> tiles = new ArrayList<>();
            for (int column = 0; column < SIZE; column++) {
                double time = Math.clamp((frame - column * staggerFrames) / (double) fallFrames, 0, 1);
                double row = -1 + 3 * time * time;
                tiles.add(new Tile(digits[column], row, column, time == 1));
            }
            drawFrame(tiles, true);
            pause(45);
        }
        pause(1100);
    }

    private List<Tile> slidingTiles(List<TileMove> moves, double progress) {
        List<Tile> stationary = new ArrayList<>();
        List<Tile> moving = new ArrayList<>();
        for (TileMove move : moves) {
            addSlidingTile(stationary, moving, move.from(), move.to(), progress);
            if (move.mergedFrom() != null) {
                addSlidingTile(stationary, moving, move.mergedFrom(), move.to(), progress);
            }
        }
        stationary.addAll(moving);
        return stationary;
    }

    private void addSlidingTile(List<Tile> stationary, List<Tile> moving, Cell from, Cell to, double progress) {
        double row = from.row() + (to.row() - from.row()) * progress;
        double column = from.column() + (to.column() - from.column()) * progress;
        Tile tile = new Tile(previous[from.row()][from.column()], row, column, false);
        (from.equals(to) ? stationary : moving).add(tile);
    }

    private List<Tile> boardTiles(Set<Cell> lit, Cell hidden) {
        List<Tile> tiles = new ArrayList<>();
        for (int row = 0; row < SIZE; row++) {
            for (int column = 0; column < SIZE; column++) {
                int value = board.getValue(row, column);
                Cell cell = new Cell(row, column);
                if (value != 0 && !cell.equals(hidden)) {
                    tiles.add(new Tile(value, row, column, lit.contains(cell)));
                }
            }
        }
        return tiles;
    }

    private Layout layout() {
        TerminalSize size = screen.getTerminalSize();
        int columns = size.getColumns();
        int rows = size.getRows();
        if (columns < 31 || rows < 13) return null;

        int tileWidth = Math.min(9, (columns - 7) / SIZE);
        int tileHeight = rows >= 21 ? 3 : rows >= 17 ? 2 : 1;
        int width = SIZE * (tileWidth + 1) + 3;
        int height = SIZE * (tileHeight + 1) + 5;
        return new Layout((columns - width) / 2, (rows - height) / 2, tileWidth, tileHeight);
    }

    private void drawFrame(List<Tile> tiles, boolean intro) throws IOException {
        screen.doResizeIfNecessary();
        TerminalSize size = screen.getTerminalSize();
        fill(0, 0, size.getColumns(), size.getRows(), INK);
        Layout layout = layout();
        if (layout == null) {
            String message = size.getColumns() >= 26 ? "Resize to at least 31 x 13" : "Resize terminal";
            String visible = message.substring(0, Math.min(size.getColumns(), message.length()));
            write((size.getColumns() - visible.length()) / 2, size.getRows() / 2, visible, GOLD, INK);
        } else {
            int highest = tiles.stream().mapToInt(Tile::value).max().orElse(0);
            drawPanel(layout, intro, highest);
            for (Tile tile : tiles) {
                drawTile(layout, tile);
            }
        }
        screen.refresh();
    }

    private void drawPanel(Layout layout, boolean intro, int highest) {
        int x = layout.x();
        int y = layout.y();
        int width = layout.width();
        int height = layout.height();
        fill(x, y, width, height, PANEL);
        String line = "─".repeat(width - 2);
        write(x, y, "╭" + line + "╮", BORDER, PANEL);
        write(x, y + 2, "├" + line + "┤", BORDER, PANEL);
        write(x, y + height - 3, "├" + line + "┤", BORDER, PANEL);
        write(x, y + height - 1, "╰" + line + "╯", BORDER, PANEL);
        for (int row = 1; row < height - 1; row++) {
            if (row == 2 || row == height - 3) continue;
            write(x, y + row, "│", BORDER, PANEL);
            write(x + width - 1, y + row, "│", BORDER, PANEL);
        }

        if (!intro) {
            String best = "HIGHEST  " + highest;
            write(x + 2, y + 1, " 2048 ", INK, GOLD, SGR.BOLD);
            write(x + width - 2 - best.length(), y + 1, best, PAPER, PANEL, SGR.BOLD);
        }
        String controls = "↑ ↓ ← →  Move   Esc  Quit";
        write(x + (width - controls.length()) / 2, y + height - 2, controls, MUTED, PANEL);

        for (int row = 0; row < SIZE; row++) {
            for (int column = 0; column < SIZE; column++) {
                fill(x + 2 + column * (layout.tileWidth() + 1), y + 3 + row * (layout.tileHeight() + 1),
                        layout.tileWidth(), layout.tileHeight(), SLOT);
            }
        }
    }

    private void drawTile(Layout layout, Tile tile) {
        int top = layout.y() + 3;
        int x = layout.x() + 2 + (int) Math.round(tile.column() * (layout.tileWidth() + 1));
        int y = top + (int) Math.round(tile.row() * (layout.tileHeight() + 1));
        int bottom = Math.min(top + layout.boardHeight(), y + layout.tileHeight());
        int visibleTop = Math.max(top, y);
        if (bottom <= visibleTop) return;

        int index = Math.clamp(Integer.numberOfTrailingZeros(tile.value()) - 1, 0, TILE_COLORS.length - 1);
        TextColor background = tile.lit() ? FLASH : TILE_COLORS[index];
        fill(x, visibleTop, layout.tileWidth(), bottom - visibleTop, background);

        int textRow = y + layout.tileHeight() / 2;
        if (textRow >= visibleTop && textRow < bottom) {
            String number = Integer.toString(tile.value());
            if (number.length() > layout.tileWidth()) {
                number = number.substring(0, layout.tileWidth() - 1) + "…";
            }
            write(x + (layout.tileWidth() - number.length()) / 2, textRow, number, INK, background, SGR.BOLD);
        }
    }

    private void fill(int x, int y, int width, int height, TextColor background) {
        graphics.setBackgroundColor(background).setForegroundColor(background).clearModifiers();
        graphics.fillRectangle(new TerminalPosition(x, y), new TerminalSize(width, height), ' ');
    }

    private void write(int x, int y, String text, TextColor foreground, TextColor background, SGR... modifiers) {
        graphics.setForegroundColor(foreground).setBackgroundColor(background).clearModifiers();
        graphics.enableModifiers(modifiers).putString(x, y, text);
    }

    private static TextColor color(int red, int green, int blue) {
        return new TextColor.RGB(red, green, blue);
    }

    private static void pause(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }
}
