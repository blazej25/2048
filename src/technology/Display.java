package technology;

import engine.Board;

import java.io.IOException;
import java.util.List;

public interface Display {

    public void showMenu();
    public void showBoard() throws IOException;
    public void refreshBoard() throws IOException;

    default void refreshBoard(List<Board.TileMove> moves, Board.Cell spawned) throws IOException {
        refreshBoard();
    }
}
