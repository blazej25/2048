package technology;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;

import java.io.IOException;

public final class TerminalModeMenu {

    public enum Mode { TERMINAL, GUI, EXIT }

    private final Screen screen;
    private final TextGraphics graphics;

    public TerminalModeMenu(Screen screen) {
        this.screen = screen;
        this.graphics = screen.newTextGraphics();
    }

    public Mode select() throws IOException {
        boolean guiSelected = false;
        screen.setCursorPosition(null);
        draw(guiSelected);

        while (!Thread.currentThread().isInterrupted()) {
            KeyStroke key = screen.pollInput();
            boolean resized = screen.doResizeIfNecessary() != null;
            if (key == null) {
                if (resized) draw(guiSelected);
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
                    guiSelected = !guiSelected;
                    draw(guiSelected);
                }
                case Enter -> { return guiSelected ? Mode.GUI : Mode.TERMINAL; }
                case Escape, EOF -> { return Mode.EXIT; }
                default -> {
                    if (resized) draw(guiSelected);
                }
            }
        }
        return Mode.EXIT;
    }

    private void draw(boolean guiSelected) throws IOException {
        screen.clear();
        TerminalSize size = screen.getTerminalSize();
        int top = Math.max(0, (size.getRows() - 6) / 2);
        boolean roomForHelp = size.getRows() >= 6;
        int firstOption = roomForHelp ? top + 2 : top;
        if (size.getRows() == 1) {
            write(size, 0, option(guiSelected ? "GUI" : "Terminal", true), true);
        } else {
            write(size, firstOption, option("Terminal", !guiSelected), !guiSelected);
            write(size, firstOption + 1, option("GUI", guiSelected), guiSelected);
        }
        if (roomForHelp) {
            write(size, top, "2048  ·  Choose a mode", false);
            write(size, top + 5, "↑ ↓  Select   Enter  Play   Esc  Quit", false);
        }
        screen.refresh();
    }

    private static String option(String label, boolean selected) {
        return (selected ? " ›  " : "    ") + label + " ".repeat(9 - label.length());
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
