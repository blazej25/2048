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
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class JavaFxDisplay implements Display {

    private static final int SIZE = 4;
    private static final int TILE_SIZE = 88;
    private static final int GAP = 8;
    private static final int BOARD_SIZE = SIZE * (TILE_SIZE + GAP) + GAP;
    private static final Duration SLIDE_DURATION = Duration.millis(130);
    private static final Interpolator SLIDE_EASING = Interpolator.SPLINE(0.22, 0.61, 0.36, 1);

    private final Board board;
    private final VBox view = new VBox(16);
    private final Group tileLayer = new Group();
    private final Map<Cell, Label> tiles = new HashMap<>();
    private final Label instructions = new Label();
    private Animation animation;
    private Runnable queuedInput;

    public JavaFxDisplay(Board board) {
        this.board = board;
        view.setPadding(new Insets(24));
        view.setStyle("-fx-background-color: #f8f5ef;");

        Label title = new Label("2048");
        title.setStyle("-fx-font-size: 40px; -fx-font-weight: bold; -fx-text-fill: #332921;");
        instructions.setStyle("-fx-font-size: 14px; -fx-text-fill: #51443a;");

        Pane playfield = new Pane();
        playfield.setPrefSize(BOARD_SIZE, BOARD_SIZE);
        playfield.setStyle("-fx-background-color: #9b8574; -fx-background-radius: 12;");
        for (int row = 0; row < SIZE; row++) {
            for (int column = 0; column < SIZE; column++) {
                Rectangle slot = new Rectangle(position(column), position(row), TILE_SIZE, TILE_SIZE);
                slot.setArcWidth(16);
                slot.setArcHeight(16);
                slot.setFill(Color.web("#c7b7a9"));
                playfield.getChildren().add(slot);
            }
        }

        Rectangle clip = new Rectangle(BOARD_SIZE, BOARD_SIZE);
        clip.setArcWidth(24);
        clip.setArcHeight(24);
        tileLayer.setManaged(false);
        tileLayer.setClip(clip);
        playfield.getChildren().add(tileLayer);
        view.getChildren().addAll(title, playfield, instructions);
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
        instructions.setText("Use the arrow keys to move the tiles.");
    }

    @Override
    public void showBoard() {
        showMenu();
        refreshBoard();
    }

    @Override
    public void refreshBoard() {
        tileLayer.getChildren().clear();
        tiles.clear();
        for (int row = 0; row < SIZE; row++) {
            for (int column = 0; column < SIZE; column++) {
                int value = board.getValue(row, column);
                if (value != 0) {
                    Cell cell = new Cell(row, column);
                    Label tile = createTile(cell, value);
                    tiles.put(cell, tile);
                    tileLayer.getChildren().add(tile);
                }
            }
        }
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
        destinations.add(new KeyValue(tile.translateXProperty(), position(to.column()) - position(from.column()),
                SLIDE_EASING));
        destinations.add(new KeyValue(tile.translateYProperty(), position(to.row()) - position(from.row()),
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
            DropShadow glow = new DropShadow(0, Color.web("#ffe3a0"));
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
            tile.setTranslateY(-Math.min(76, position(spawned.row()) + 30));
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

    private static int position(int index) {
        return GAP + index * (TILE_SIZE + GAP);
    }

    private static Label createTile(Cell cell, int value) {
        String background = switch (value) {
            case 2 -> "#f3ebe1";
            case 4 -> "#f2d9b2";
            case 8 -> "#a34e32";
            case 16 -> "#923d36";
            case 32 -> "#8b344e";
            case 64 -> "#6d3a68";
            default -> "#51426c";
        };
        String foreground = value <= 4 ? "#332921" : "#ffffff";
        Label tile = new Label(Integer.toString(value));
        tile.setManaged(false);
        tile.resizeRelocate(position(cell.column()), position(cell.row()), TILE_SIZE, TILE_SIZE);
        tile.setAlignment(Pos.CENTER);
        tile.setStyle("-fx-background-radius: 8; -fx-font-size: 26px; -fx-font-weight: bold;"
                + " -fx-background-color: " + background + "; -fx-text-fill: " + foreground + ";");
        return tile;
    }
}
