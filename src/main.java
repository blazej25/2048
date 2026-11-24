import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import engine.Board;
import engine.Game2048;
import javafx.application.Application;
import technology.BasicRandomizer;
import technology.JavaFxGame;
import technology.TerminalDisplay;
import technology.TerminalInput;
import technology.TerminalModeMenu;

import java.io.IOException;

public class main {

    public static void main(String[] args) throws IOException {
        TerminalModeMenu.Mode mode;
        try (Screen screen = new DefaultTerminalFactory().createScreen()) {
            screen.startScreen();
            mode = new TerminalModeMenu(screen).select();
            if (mode == TerminalModeMenu.Mode.TERMINAL) {
                runTerminal(screen);
            }
        }

        if (mode == TerminalModeMenu.Mode.GUI) {
            Application.launch(JavaFxGame.class);
        }
    }

    private static void runTerminal(Screen screen) throws IOException {
        Board board = new Board(new BasicRandomizer());
        board.generateBoard();

        TerminalDisplay display = new TerminalDisplay(screen, screen.newTextGraphics(), board);
        TerminalInput input = new TerminalInput(screen, display);
        new Game2048(board, display, input).run();
    }
}
