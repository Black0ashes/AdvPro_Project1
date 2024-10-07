package se233.imgdecrop.Cropping;

import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

public class ResizableRectangle extends Rectangle {

    private static final double RESIZER_SQUARE_SIDE = 6;
    private final Paint resizerSquareColor = Color.WHITE;
    private final Paint rectangleStrokeColor = Color.BLACK;

    private double mouseClickPozX, mouseClickPozY;
    private final Map<ResizerPosition, Rectangle> resizeHandles = new EnumMap<>(ResizerPosition.class);
    private final Optional<Pane> parentPane;

    public ResizableRectangle(double x, double y, double width, double height, Pane pane) {
        super(x, y, width, height);
        this.parentPane = Optional.ofNullable(pane);
        parentPane.ifPresent(p -> p.getChildren().add(this));

        setStroke(rectangleStrokeColor);
        setStrokeWidth(1);
        setFill(Color.color(1, 1, 1, 0));

        createResizerSquares();
        setupDragBehavior();
    }

    private void createResizerSquares() {
        for (ResizerPosition position : ResizerPosition.values()) {
            Rectangle square = createResizerSquare(position);
            resizeHandles.put(position, square);
            parentPane.ifPresent(p -> p.getChildren().add(square));
        }
    }

    private Rectangle createResizerSquare(ResizerPosition position) {
        Rectangle square = new Rectangle(RESIZER_SQUARE_SIDE, RESIZER_SQUARE_SIDE);
        square.setFill(resizerSquareColor);

        position.bindProperties(this, square);

        square.setOnMouseEntered(e -> getParent().setCursor(position.getCursor()));
        square.setOnMouseExited(e -> getParent().setCursor(Cursor.DEFAULT));
        square.setOnMouseDragged(e -> position.handleDrag(this, e));

        return square;
    }

    private void setupDragBehavior() {
        setOnMousePressed(e -> {
            mouseClickPozX = e.getX();
            mouseClickPozY = e.getY();
            getParent().setCursor(Cursor.MOVE);
        });

        setOnMouseDragged(e -> {
            double offsetX = e.getX() - mouseClickPozX;
            double offsetY = e.getY() - mouseClickPozY;
            double newX = getX() + offsetX;
            double newY = getY() + offsetY;

            parentPane.ifPresent(p -> {
                if (newX >= 0 && newX + getWidth() <= p.getWidth()) {
                    setX(newX);
                }
                if (newY >= 0 && newY + getHeight() <= p.getHeight()) {
                    setY(newY);
                }
            });

            mouseClickPozX = e.getX();
            mouseClickPozY = e.getY();
        });

        setOnMouseReleased(e -> getParent().setCursor(Cursor.DEFAULT));
    }

    public void removeResizeHandles(BorderPane imagePane) {
        parentPane.ifPresent(p ->
                resizeHandles.values().forEach(p.getChildren()::remove)
        );
        resizeHandles.clear();
    }

    private enum ResizerPosition {
        NORTHWEST(Cursor.NW_RESIZE, (r, e) -> {
            double offsetX = e.getX() - r.getX();
            double offsetY = e.getY() - r.getY();
            if (r.getWidth() - offsetX > 0) {
                r.setX(e.getX());
                r.setWidth(r.getWidth() - offsetX);
            }
            if (r.getHeight() - offsetY > 0) {
                r.setY(e.getY());
                r.setHeight(r.getHeight() - offsetY);
            }
        }),
        NORTH(Cursor.N_RESIZE, (r, e) -> {
            double offsetY = e.getY() - r.getY();
            if (r.getHeight() - offsetY > 0) {
                r.setY(e.getY());
                r.setHeight(r.getHeight() - offsetY);
            }
        }),
        NORTHEAST(Cursor.NE_RESIZE, (r, e) -> {
            double offsetX = e.getX() - r.getX();
            double offsetY = e.getY() - r.getY();
            if (offsetX > 0) {
                r.setWidth(offsetX);
            }
            if (r.getHeight() - offsetY > 0) {
                r.setY(e.getY());
                r.setHeight(r.getHeight() - offsetY);
            }
        }),
        EAST(Cursor.E_RESIZE, (r, e) -> {
            double offsetX = e.getX() - r.getX();
            if (offsetX > 0) {
                r.setWidth(offsetX);
            }
        }),
        SOUTHEAST(Cursor.SE_RESIZE, (r, e) -> {
            double offsetX = e.getX() - r.getX();
            double offsetY = e.getY() - r.getY();
            if (offsetX > 0) {
                r.setWidth(offsetX);
            }
            if (offsetY > 0) {
                r.setHeight(offsetY);
            }
        }),
        SOUTH(Cursor.S_RESIZE, (r, e) -> {
            double offsetY = e.getY() - r.getY();
            if (offsetY > 0) {
                r.setHeight(offsetY);
            }
        }),
        SOUTHWEST(Cursor.SW_RESIZE, (r, e) -> {
            double offsetX = e.getX() - r.getX();
            double offsetY = e.getY() - r.getY();
            if (r.getWidth() - offsetX > 0) {
                r.setX(e.getX());
                r.setWidth(r.getWidth() - offsetX);
            }
            if (offsetY > 0) {
                r.setHeight(offsetY);
            }
        }),
        WEST(Cursor.W_RESIZE, (r, e) -> {
            double offsetX = e.getX() - r.getX();
            if (r.getWidth() - offsetX > 0) {
                r.setX(e.getX());
                r.setWidth(r.getWidth() - offsetX);
            }
        });

        private final Cursor cursor;
        private final BiConsumer<ResizableRectangle, MouseEvent> dragHandler;

        ResizerPosition(Cursor cursor, BiConsumer<ResizableRectangle, MouseEvent> dragHandler) {
            this.cursor = cursor;
            this.dragHandler = dragHandler;
        }

        public Cursor getCursor() {
            return cursor;
        }

        public void handleDrag(ResizableRectangle rectangle, MouseEvent event) {
            dragHandler.accept(rectangle, event);
        }

        public void bindProperties(ResizableRectangle parent, Rectangle square) {
            switch (this) {
                case NORTHWEST:
                    square.xProperty().bind(parent.xProperty().subtract(square.widthProperty().divide(2.0)));
                    square.yProperty().bind(parent.yProperty().subtract(square.heightProperty().divide(2.0)));
                    break;
                case NORTH:
                    square.xProperty().bind(parent.xProperty().add(parent.widthProperty().divide(2.0).subtract(square.widthProperty().divide(2.0))));
                    square.yProperty().bind(parent.yProperty().subtract(square.heightProperty().divide(2.0)));
                    break;
                case NORTHEAST:
                    square.xProperty().bind(parent.xProperty().add(parent.widthProperty()).subtract(square.widthProperty().divide(2.0)));
                    square.yProperty().bind(parent.yProperty().subtract(square.heightProperty().divide(2.0)));
                    break;
                case EAST:
                    square.xProperty().bind(parent.xProperty().add(parent.widthProperty()).subtract(square.widthProperty().divide(2.0)));
                    square.yProperty().bind(parent.yProperty().add(parent.heightProperty().divide(2.0).subtract(square.heightProperty().divide(2.0))));
                    break;
                case SOUTHEAST:
                    square.xProperty().bind(parent.xProperty().add(parent.widthProperty()).subtract(square.widthProperty().divide(2.0)));
                    square.yProperty().bind(parent.yProperty().add(parent.heightProperty().subtract(square.heightProperty().divide(2.0))));
                    break;
                case SOUTH:
                    square.xProperty().bind(parent.xProperty().add(parent.widthProperty().divide(2.0).subtract(square.widthProperty().divide(2.0))));
                    square.yProperty().bind(parent.yProperty().add(parent.heightProperty().subtract(square.heightProperty().divide(2.0))));
                    break;
                case SOUTHWEST:
                    square.xProperty().bind(parent.xProperty().subtract(square.widthProperty().divide(2.0)));
                    square.yProperty().bind(parent.yProperty().add(parent.heightProperty().subtract(square.heightProperty().divide(2.0))));
                    break;
                case WEST:
                    square.xProperty().bind(parent.xProperty().subtract(square.widthProperty().divide(2.0)));
                    square.yProperty().bind(parent.yProperty().add(parent.heightProperty().divide(2.0).subtract(square.heightProperty().divide(2.0))));
                    break;
            }
        }
    }
}