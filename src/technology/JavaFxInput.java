package technology;

import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;

public final class JavaFxInput implements Input {

    private KeyPress pendingKey;

    public void listen(Scene scene, Runnable onInput) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            KeyPress key = switch (event.getCode()) {
                case UP -> KeyPress.UP;
                case DOWN -> KeyPress.DOWN;
                case LEFT -> KeyPress.LEFT;
                case RIGHT -> KeyPress.RIGHT;
                default -> null;
            };

            if (key != null) {
                pendingKey = key;
                event.consume();
                onInput.run();
            }
        });
    }

    @Override
    public KeyPress pollKeyboard() {
        KeyPress key = pendingKey;
        pendingKey = null;
        return key;
    }
}
