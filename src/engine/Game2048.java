package engine;

import technology.Display;
import technology.Input;
import technology.KeyPress;

import java.io.IOException;
import java.util.List;

public class Game2048 {

    private final Board board;
    private final Display display;
    private final Input input;
    private boolean finished;

    public Game2048(Board board, Display display, Input input) {
        this.board = board;
        this.display = display;
        this.input = input;
    }

    public void start() throws IOException {
        display.showBoard();
    }

    public void run() throws IOException {
        start();
        while (!finished) {
            processInput();
        }
    }

    public void processInput() throws IOException {
        if (finished) return;

        KeyPress key = input.pollKeyboard();
        if (key == null) return;
        if (key == KeyPress.QUIT) {
            finished = true;
            return;
        }

        List<Board.TileMove> moves = switch (key) {
            case DOWN -> board.moveDown();
            case UP -> board.moveUp();
            case LEFT -> board.moveLeft();
            case RIGHT -> board.moveRight();
            case QUIT -> throw new IllegalStateException("Quit was already handled");
        };

        Board.Cell spawned = board.addNewCell();
        display.refreshBoard(moves, spawned);
        finished = board.getHighest() == 2048;
    }
}
