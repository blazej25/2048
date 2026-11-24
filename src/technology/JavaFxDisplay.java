package technology;

import engine.Board;
import engine.Board.Cell;
import engine.Board.TileMove;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static technology.BoardTheme.SIZE;

public final class JavaFxDisplay implements Display {

    private static final int CELL_WIDTH = 10;
    private static final int CELL_HEIGHT = 20;
    private static final int TILE_WIDTH = BoardTheme.TILE_COLUMNS * CELL_WIDTH;
    private static final int TILE_HEIGHT = BoardTheme.TILE_ROWS * CELL_HEIGHT;
    private static final int BOARD_WIDTH = SIZE * (TILE_WIDTH + CELL_WIDTH) - CELL_WIDTH;
    private static final int BOARD_HEIGHT = SIZE * (TILE_HEIGHT + CELL_HEIGHT) - CELL_HEIGHT;
    private static final int PANEL_WIDTH = BOARD_WIDTH + 4 * CELL_WIDTH;
    private static final int PANEL_HEIGHT = BOARD_HEIGHT + 6 * CELL_HEIGHT;
    private static final Duration SLIDE_DURATION = Duration.millis(130);
    private static final Interpolator SLIDE_EASING = Interpolator.SPLINE(0.22, 0.61, 0.36, 1);

    private final Board board;
    private final StackPane view = new StackPane();
    private final Group tileLayer = new Group();
    private final Map<Cell, Label> tiles = new HashMap<>();
    private final Label title = new Label(BoardTheme.TITLE);
    private final Label highest = new Label();
    private final Label instructions = new Label();
    private Animation animation;
    private Runnable queuedInput;

    public JavaFxDisplay(Board board) {
        this.board = board;
        view.setPadding(new Insets(24));
        view.setStyle("-fx-background-color: " + BoardTheme.INK + "; -fx-font-family: 'Monospaced';");

        Pane panel = new Pane();
        panel.setPrefSize(PANEL_WIDTH, PANEL_HEIGHT);
        panel.setMaxSize(PANEL_WIDTH, PANEL_HEIGHT);
        Rectangle frame = new Rectangle(CELL_WIDTH / 2.0, CELL_HEIGHT / 2.0,
                PANEL_WIDTH - CELL_WIDTH, PANEL_HEIGHT - CELL_HEIGHT);
        frame.setArcWidth(16);
        frame.setArcHeight(16);
        frame.setFill(Color.web(BoardTheme.PANEL));
        frame.setStroke(Color.web(BoardTheme.BORDER));

        title.setStyle(labelStyle(BoardTheme.INK, true) + "-fx-background-color: " + BoardTheme.GOLD + ";");
        placeLabel(title, 2 * CELL_WIDTH, CELL_HEIGHT, 6 * CELL_WIDTH, Pos.CENTER);
        highest.setStyle(labelStyle(BoardTheme.PAPER, true));
        placeLabel(highest, 10 * CELL_WIDTH, CELL_HEIGHT, PANEL_WIDTH - 12 * CELL_WIDTH, Pos.CENTER_RIGHT);
        instructions.setStyle(labelStyle(BoardTheme.MUTED, false));
        placeLabel(instructions, CELL_WIDTH, PANEL_HEIGHT - 2 * CELL_HEIGHT,
                PANEL_WIDTH - 2 * CELL_WIDTH, Pos.CENTER);

        Pane playfield = new Pane();
        playfield.setManaged(false);
        playfield.resizeRelocate(2 * CELL_WIDTH, 3 * CELL_HEIGHT, BOARD_WIDTH, BOARD_HEIGHT);
        for (int row = 0; row < SIZE; row++) {
            for (int column = 0; column < SIZE; column++) {
                Rectangle slot = new Rectangle(tileX(column), tileY(row), TILE_WIDTH, TILE_HEIGHT);
                slot.setFill(Color.web(BoardTheme.SLOT));
                playfield.getChildren().add(slot);
            }
        }

        tileLayer.setManaged(false);
        tileLayer.setClip(new Rectangle(BOARD_WIDTH, BOARD_HEIGHT));
        playfield.getChildren().add(tileLayer);
        panel.getChildren().addAll(frame, separator(2.5 * CELL_HEIGHT),
                separator(PANEL_HEIGHT - 2.5 * CELL_HEIGHT), title, highest, playfield, instructions);
        view.getChildren().add(panel);
    }

    private static Line separator(double y) {
        Line line = new Line(CELL_WIDTH / 2.0, y, PANEL_WIDTH - CELL_WIDTH / 2.0, y);
        line.setStroke(Color.web(BoardTheme.BORDER));
        return line;
    }

    private static void placeLabel(Label label, int x, int y, int width, Pos alignment) {
        label.setManaged(false);
        label.resizeRelocate(x, y, width, CELL_HEIGHT);
        label.setAlignment(alignment);
    }

    public Parent getView() {
        return view;
    }

    public void whenReady(Runnable input) {
        if (animation == null) {
            input.run();
        } else {
            queuedInput = input;
        }
    }

    @Override
    public void showMenu() {
        instructions.setText(BoardTheme.CONTROLS);
    }

    @Override
    public void showBoard() {
        showMenu();
        playIntro();
    }

    private void playIntro() {
        title.setVisible(false);
        highest.setVisible(false);
        Timeline intro = new Timeline();
        for (int column = 0; column < SIZE; column++) {
            int digit = Character.digit(BoardTheme.TITLE.charAt(column), 10);
            Label tile = createTile(new Cell(2, column), digit);
            tile.setTranslateY(-3 * (TILE_HEIGHT + CELL_HEIGHT));
            tileLayer.getChildren().add(tile);

            int delay = column * BoardTheme.INTRO_STAGGER_MS;
            intro.getKeyFrames().addAll(
                    new KeyFrame(Duration.millis(delay), new KeyValue(tile.translateYProperty(), tile.getTranslateY())),
                    new KeyFrame(Duration.millis(delay + BoardTheme.INTRO_FALL_MS),
                            event -> styleTile(tile, BoardTheme.FLASH),
                            new KeyValue(tile.translateYProperty(), 0, Interpolator.EASE_IN))
            );
        }
        int end = BoardTheme.INTRO_FALL_MS + (SIZE - 1) * BoardTheme.INTRO_STAGGER_MS;
        intro.getKeyFrames().add(new KeyFrame(Duration.millis(end + BoardTheme.INTRO_HOLD_MS)));
        play(intro, () -> {
            refreshBoard();
            title.setVisible(true);
            highest.setVisible(true);
            finishAnimation();
        });
    }

    @Override
    public void refreshBoard() {
        tileLayer.getChildren().clear();
        tiles.clear();
        int maximum = 0;
        for (int row = 0; row < SIZE; row++) {
            for (int column = 0; column < SIZE; column++) {
                int value = board.getValue(row, column);
                if (value != 0) {
                    maximum = Math.max(maximum, value);
                    Cell cell = new Cell(row, column);
                    Label tile = createTile(cell, value);
                    tiles.put(cell, tile);
                    tileLayer.getChildren().add(tile);
                }
            }
        }
        highest.setText(BoardTheme.HIGHEST + maximum);
    }

    @Override
    public void refreshBoard(List<TileMove> moves, Cell spawned) {
        List<KeyValue> destinations = new ArrayList<>();
        for (TileMove move : moves) {
            addSlide(destinations, move.from(), move.to());
            if (move.mergedFrom() != null) {
                addSlide(destinations, move.mergedFrom(), move.to());
            }
        }

        if (destinations.isEmpty()) {
            animateLanding(moves, spawned);
        } else {
            Timeline slide = new Timeline(new KeyFrame(SLIDE_DURATION, destinations.toArray(KeyValue[]::new)));
            play(slide, () -> animateLanding(moves, spawned));
        }
    }

    private void addSlide(List<KeyValue> destinations, Cell from, Cell to) {
        if (from.equals(to)) return;
        Label tile = tiles.get(from);
        destinations.add(new KeyValue(tile.translateXProperty(), tileX(to.column()) - tileX(from.column()),
                SLIDE_EASING));
        destinations.add(new KeyValue(tile.translateYProperty(), tileY(to.row()) - tileY(from.row()),
                SLIDE_EASING));
    }

    private void animateLanding(List<TileMove> moves, Cell spawned) {
        refreshBoard();

        Timeline landing = new Timeline();
        List<Label> mergedTiles = new ArrayList<>();
        for (TileMove move : moves) {
            if (move.mergedFrom() == null) continue;
            Label tile = tiles.get(move.to());
            mergedTiles.add(tile);
            DropShadow glow = new DropShadow(0, Color.web(BoardTheme.FLASH));
            tile.setEffect(glow);
            landing.getKeyFrames().addAll(
                    new KeyFrame(Duration.millis(55),
                            new KeyValue(tile.scaleXProperty(), 1.16, Interpolator.EASE_OUT),
                            new KeyValue(tile.scaleYProperty(), 1.16, Interpolator.EASE_OUT),
                            new KeyValue(glow.radiusProperty(), 18, Interpolator.EASE_OUT)),
                    new KeyFrame(Duration.millis(155),
                            new KeyValue(tile.scaleXProperty(), 1, Interpolator.EASE_BOTH),
                            new KeyValue(tile.scaleYProperty(), 1, Interpolator.EASE_BOTH),
                            new KeyValue(glow.radiusProperty(), 0, Interpolator.EASE_IN))
            );
        }

        if (spawned != null) {
            Label tile = tiles.get(spawned);
            tile.toFront();
            tile.setTranslateY(-Math.min(76, tileY(spawned.row()) + 30));
            tile.setOpacity(0);
            landing.getKeyFrames().addAll(
                    new KeyFrame(Duration.millis(135),
                            new KeyValue(tile.translateYProperty(), 0, Interpolator.EASE_IN),
                            new KeyValue(tile.opacityProperty(), 1, Interpolator.EASE_OUT)),
                    new KeyFrame(Duration.millis(160),
                            new KeyValue(tile.translateYProperty(), -7, Interpolator.EASE_OUT)),
                    new KeyFrame(Duration.millis(200),
                            new KeyValue(tile.translateYProperty(), 0, Interpolator.EASE_IN))
            );
        }

        if (landing.getKeyFrames().isEmpty()) {
            finishAnimation();
        } else {
            play(landing, () -> {
                mergedTiles.forEach(tile -> tile.setEffect(null));
                finishAnimation();
            });
        }
    }

    private void play(Animation next, Runnable onFinished) {
        animation = next;
        next.setOnFinished(event -> onFinished.run());
        next.play();
    }

    private void finishAnimation() {
        animation = null;
        Runnable input = queuedInput;
        queuedInput = null;
        if (input != null) {
            input.run();
        }
    }

    private static int tileX(int column) {
        return column * (TILE_WIDTH + CELL_WIDTH);
    }

    private static int tileY(int row) {
        return row * (TILE_HEIGHT + CELL_HEIGHT);
    }

    private static Label createTile(Cell cell, int value) {
        Label tile = new Label(Integer.toString(value));
        tile.setManaged(false);
        tile.resizeRelocate(tileX(cell.column()), tileY(cell.row()), TILE_WIDTH, TILE_HEIGHT);
        tile.setAlignment(Pos.CENTER);
        styleTile(tile, BoardTheme.TILE_COLORS.get(BoardTheme.tileColorIndex(value)));
        return tile;
    }

    private static void styleTile(Label tile, String background) {
        tile.setStyle(labelStyle(BoardTheme.INK, true) + "-fx-background-color: " + background + ";");
    }

    private static String labelStyle(String color, boolean bold) {
        return "-fx-font-size: 16px; -fx-text-fill: " + color + ";"
                + (bold ? " -fx-font-weight: bold;" : "");
    }
}
