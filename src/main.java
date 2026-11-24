import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import engine.Board;
import engine.Game2048;
import javafx.application.Application;
import technology.JavaFxGame;
import technology.Randomizer;
import technology.RandomizerKind;
import technology.TerminalDisplay;
import technology.TerminalInput;
import technology.TerminalMenu;
import technology.TerminalMenu.Choice;

import java.io.IOException;

public class main {

    public static void main(String[] args) throws IOException {
        RandomizerKind randomizer;
        try (Screen screen = new DefaultTerminalFactory().createScreen()) {
            screen.startScreen();
            TerminalMenu menu = new TerminalMenu(screen);
            Choice mode = menu.select("Choose a mode", "Terminal", "GUI");
            if (mode == Choice.EXIT) return;

            Choice selection = menu.select("Choose a randomizer", "LXM (recommended)", "Classic (Java Random)");
            if (selection == Choice.EXIT) return;

            randomizer = selection == Choice.FIRST ? RandomizerKind.LXM : RandomizerKind.CLASSIC;
            if (mode == Choice.FIRST) {
                runTerminal(screen, randomizer.create());
                return;
            }
        }

        Application.launch(JavaFxGame.class, "--randomizer=" + randomizer.name());
    }

    private static void runTerminal(Screen screen, Randomizer randomizer) throws IOException {
        Board board = new Board(randomizer);
        board.generateBoard();

        TerminalDisplay display = new TerminalDisplay(screen, screen.newTextGraphics(), board);
        TerminalInput input = new TerminalInput(screen, display);
        new Game2048(board, display, input).run();
    }
}
