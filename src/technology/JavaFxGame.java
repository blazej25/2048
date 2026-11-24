package technology;

import engine.Board;
import engine.Game2048;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
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
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                event.consume();
                stage.close();
            }
        });

        input.listen(scene, () -> display.whenReady(() -> {
            try {
                game.processInput();
            } catch (IOException exception) {
                throw new UncheckedIOException(exception);
            }
        }));

        game.start();
        stage.setTitle(BoardTheme.TITLE);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }
}
