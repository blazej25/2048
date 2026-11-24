import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import engine.Board;
import engine.Game2048;
import javafx.application.Application;
import technology.BasicRandomizer;
import technology.JavaFxGame;
import technology.TerminalDisplay;
import technology.TerminalInput;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;

public class main {

    public static void main(String[] args) throws IOException {
        BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.print("Choose a mode (terminal or gui): ");
            System.out.flush();
            String choice = console.readLine();
            if (choice == null) {
                return;
            }

            switch (choice.strip().toLowerCase(Locale.ROOT)) {
                case "terminal" -> {
                    runTerminal();
                    return;
                }
                case "gui" -> {
                    Application.launch(JavaFxGame.class);
                    return;
                }
                default -> System.out.println("Enter terminal or gui.");
            }
        }
    }

    private static void runTerminal() throws IOException {
        Board board = new Board(new BasicRandomizer());
        board.generateBoard();

        try (Screen screen = new DefaultTerminalFactory().createScreen()) {
            screen.startScreen();
            TerminalDisplay display = new TerminalDisplay(screen, screen.newTextGraphics(), board);
            TerminalInput input = new TerminalInput(screen, display);
            new Game2048(board, display, input).run();
        }
    }
}
