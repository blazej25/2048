package engine;

import technology.Randomizer;

import java.util.*;
import java.util.function.BiFunction;

public class Board {

    private List<List<Integer>> board;
    private final Randomizer randomizer;
    private int highest;

    public Board(Randomizer randomizer) {
        this.randomizer = randomizer;
        board = new ArrayList<>();
        highest = 0;
    }

    public Board(List<List<Integer>> board, Randomizer randomizer) {
        this.randomizer = randomizer;
        this.board = board; // not an empty board
        this.highest = board.stream().map(row -> row.stream().max(Comparator.naturalOrder()).get())
                                    .max(Comparator.naturalOrder()).get();
    }

    public void generateBoard() {
        List<List<Integer>> board = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            List<Integer> row = new ArrayList<>();

            for (int j = 0; j < 4; j++) {
                row.add(0);
            }

            board.add(row);
        }

        this.board = board;

        for (int i = 0; i < 2; i++) {
            this.addNewCell();
        }

        this.highest = 4;
    }

    public record Cell(int row, int column) {}

    public record TileMove(Cell from, Cell mergedFrom, Cell to) {}

    private record Tile(int value, Cell from, Cell mergedFrom) {}

    public List<TileMove> moveDown() {
        return move((line, offset) -> new Cell(3 - offset, line), true);
    }

    public List<TileMove> moveUp() {
        return move((line, offset) -> new Cell(offset, line), true);
    }

    public List<TileMove> moveLeft() {
        return move((line, offset) -> new Cell(line, offset), false);
    }

    public List<TileMove> moveRight() {
        return move((line, offset) -> new Cell(line, 3 - offset), false);
    }

    private List<TileMove> move(BiFunction<Integer, Integer, Cell> cellAt, boolean mergeFromFarEdge) {
        List<TileMove> moves = new ArrayList<>();
        for (int line = 0; line < 4; line++) {
            Deque<Tile> tiles = new ArrayDeque<>();
            // Vertical moves pair tiles from the far edge, while horizontal moves pair from the destination edge.
            for (int offset = 0; offset < 4; offset++) {
                Cell from = cellAt.apply(line, mergeFromFarEdge ? 3 - offset : offset);
                int value = getValue(from.row(), from.column());
                if (value == 0) continue;

                Tile previous = tiles.peekLast();
                if (previous != null && previous.mergedFrom() == null && previous.value() == value) {
                    tiles.removeLast();
                    tiles.addLast(new Tile(value * 2, from, previous.from()));
                } else {
                    tiles.addLast(new Tile(value, from, null));
                }
            }

            for (int offset = 0; offset < 4; offset++) {
                Cell to = cellAt.apply(line, offset);
                Tile tile = mergeFromFarEdge ? tiles.pollLast() : tiles.pollFirst();
                board.get(to.row()).set(to.column(), tile == null ? 0 : tile.value());
                if (tile != null) {
                    moves.add(new TileMove(tile.from(), tile.mergedFrom(), to));
                }
            }
        }
        return moves;
    }

    public Cell addNewCell() {
        List<Cell> emptyCells = new ArrayList<>();
        for (int row = 0; row < board.size(); row++) {
            for (int column = 0; column < board.get(row).size(); column++) {
                if (getValue(row, column) == 0) {
                    emptyCells.add(new Cell(row, column));
                }
            }
        }
        if (emptyCells.isEmpty()) return null;

        Cell cell = emptyCells.get(randomizer.getRandomIndex(emptyCells.size()));
        board.get(cell.row()).set(cell.column(), randomizer.getRandomValue());
        return cell;
    }

    public int getValue(int row, int column) {
        return this.board.get(row).get(column);
    }

    public int getHighest() {
        return highest;
    }

    public void printBoard() {
        board.forEach(row -> {
            row.forEach(value -> System.out.print(value + " "));
            System.out.println();
        });
    }
}
