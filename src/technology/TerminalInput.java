package technology;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;

import java.io.IOException;

public class TerminalInput implements Input {

    private final Screen screen;
    private final Display display;

    public TerminalInput(Screen screen, Display display) {
        this.screen = screen;
        this.display = display;
    }

    @Override
    public KeyPress pollKeyboard() throws IOException {
        if (Thread.currentThread().isInterrupted()) return KeyPress.QUIT;
        KeyStroke key = screen.pollInput();
        if (screen.doResizeIfNecessary() != null) {
            display.refreshBoard();
        }
        if (key == null) {
            try {
                Thread.sleep(16);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                return KeyPress.QUIT;
            }
            return null;
        }

        return switch (key.getKeyType()) {
            case ArrowDown -> KeyPress.DOWN;
            case ArrowUp -> KeyPress.UP;
            case ArrowLeft -> KeyPress.LEFT;
            case ArrowRight -> KeyPress.RIGHT;
            case Escape, EOF -> KeyPress.QUIT;
            default -> null;
        };
    }
}
