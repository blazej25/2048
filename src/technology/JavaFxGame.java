package technology;

import engine.Board;
import engine.Game2048;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.UncheckedIOException;

public final class JavaFxGame extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        String selection = getParameters().getNamed().getOrDefault("randomizer", RandomizerKind.LXM.name());
        Board board = new Board(RandomizerKind.valueOf(selection).create());
        board.generateBoard();

        JavaFxDisplay display = new JavaFxDisplay(board);
        JavaFxInput input = new JavaFxInput();
        Game2048 game = new Game2048(board, display, input);
        Scene scene = new Scene(display.getView());

        input.listen(scene, () -> display.whenReady(() -> {
            try {
                game.processInput();
            } catch (IOException exception) {
                throw new UncheckedIOException(exception);
            }
        }));

        game.start();
        stage.setTitle("2048");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }
}
