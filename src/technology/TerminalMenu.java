package technology;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;

import java.io.IOException;

public final class TerminalMenu {

    public enum Choice { FIRST, SECOND, EXIT }

    private final Screen screen;
    private final TextGraphics graphics;

    public TerminalMenu(Screen screen) {
        this.screen = screen;
        this.graphics = screen.newTextGraphics();
    }

    public Choice select(String title, String first, String second) throws IOException {
        boolean secondSelected = false;
        screen.setCursorPosition(null);
        draw(title, first, second, secondSelected);

        while (!Thread.currentThread().isInterrupted()) {
            KeyStroke key = screen.pollInput();
            boolean resized = screen.doResizeIfNecessary() != null;
            if (key == null) {
                if (resized) draw(title, first, second, secondSelected);
                try {
                    Thread.sleep(20);
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    break;
                }
                continue;
            }

            switch (key.getKeyType()) {
                case ArrowUp, ArrowDown -> {
                    secondSelected = !secondSelected;
                    draw(title, first, second, secondSelected);
                }
                case Enter -> { return secondSelected ? Choice.SECOND : Choice.FIRST; }
                case Escape, EOF -> { return Choice.EXIT; }
                default -> {
                    if (resized) draw(title, first, second, secondSelected);
                }
            }
        }
        return Choice.EXIT;
    }

    private void draw(String title, String first, String second, boolean secondSelected) throws IOException {
        screen.clear();
        TerminalSize size = screen.getTerminalSize();
        int top = Math.max(0, (size.getRows() - 6) / 2);
        boolean roomForHelp = size.getRows() >= 6;
        int firstOption = roomForHelp ? top + 2 : top;
        int labelWidth = Math.max(first.length(), second.length()) + 1;
        if (size.getRows() == 1) {
            write(size, 0, option(secondSelected ? second : first, labelWidth, true), true);
        } else {
            write(size, firstOption, option(first, labelWidth, !secondSelected), !secondSelected);
            write(size, firstOption + 1, option(second, labelWidth, secondSelected), secondSelected);
        }
        if (roomForHelp) {
            write(size, top, "2048  ·  " + title, false);
            write(size, top + 5, "↑ ↓  Select   Enter  Confirm   Esc  Quit", false);
        }
        screen.refresh();
    }

    private static String option(String label, int width, boolean selected) {
        return (selected ? " ›  " : "    ") + label + " ".repeat(width - label.length());
    }

    private void write(TerminalSize size, int row, String text, boolean selected) {
        if (row >= size.getRows()) return;
        String visible = text.substring(0, Math.min(text.length(), size.getColumns()));
        int column = Math.max(0, (size.getColumns() - text.length()) / 2);
        graphics.clearModifiers().setForegroundColor(TextColor.ANSI.DEFAULT)
                .setBackgroundColor(TextColor.ANSI.DEFAULT);
        if (selected) {
            graphics.setForegroundColor(TextColor.ANSI.BLACK).setBackgroundColor(TextColor.ANSI.YELLOW_BRIGHT)
                    .enableModifiers(SGR.BOLD);
        }
        graphics.putString(column, row, visible);
    }
}
