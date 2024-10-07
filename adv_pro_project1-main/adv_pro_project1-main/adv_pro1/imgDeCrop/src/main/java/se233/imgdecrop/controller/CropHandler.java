package se233.imgdecrop.controller;

import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import se233.imgdecrop.Cropping.ResizableRectangle;


public class CropHandler {

    private final ImageView inputImgView;
    private final BorderPane imagePane;
    private final ScrollPane imageScrollPane;
    private ResizableRectangle selectionRectangle;
    private Runnable onCropConfirmed;
    private Rectangle darkArea;
    private boolean isAreaSelected = false;
    private boolean isCroppingActive = false;

    public CropHandler(ImageView imageView, BorderPane imagePane, ScrollPane imageScrollPane) {
        this.inputImgView = imageView;
        this.imagePane = imagePane;
        this.imageScrollPane = imageScrollPane;
        setupCropArea();
    }

    public void setOnCropConfirmed(Runnable onCropConfirmed) {
        this.onCropConfirmed = onCropConfirmed;
    }

    private void setupCropArea() {
        darkArea = new Rectangle();
        darkArea.setFill(Color.color(0, 0, 0, 0.5));
        darkArea.setVisible(false);
        imagePane.getChildren().add(darkArea);
    }

    public void startCrop() {
        isCroppingActive = true;
        imageScrollPane.setPannable(false);
        removeExistingSelection();

        Bounds viewportBounds = imageScrollPane.getViewportBounds();

        double viewportWidth = viewportBounds.getWidth();
        double viewportHeight = viewportBounds.getHeight();
        double imageWidth = inputImgView.getBoundsInParent().getWidth();
        double imageHeight = inputImgView.getBoundsInParent().getHeight();
        double rectWidth = imageWidth / 2.5;
        double rectHeight = imageHeight / 2.5;
        double rectX = (viewportWidth - rectWidth) / 2.5;
        double rectY = (viewportHeight - rectHeight) / 2.5;

        selectionRectangle = new ResizableRectangle(rectX, rectY, rectWidth, rectHeight, imagePane);

        isAreaSelected = true;
        imagePane.requestFocus();
    }


    public void confirmCrop() {
        if (isAreaSelected && selectionRectangle != null) {
            imageScrollPane.setPannable(true);
            cropImage(selectionRectangle.getBoundsInParent());
            removeExistingSelection();
            selectionRectangle = null;
            isAreaSelected = false;
            darkArea.setVisible(false);
            isCroppingActive = false;
        }
        if (onCropConfirmed != null) {
            onCropConfirmed.run();
        }
    }

    private void cropImage(Bounds bounds) {
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);
        parameters.setViewport(new Rectangle2D(bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight()));
        WritableImage croppedImageWritable = new WritableImage((int) bounds.getWidth(), (int) bounds.getHeight());
        inputImgView.snapshot(parameters, croppedImageWritable);
        inputImgView.setImage(croppedImageWritable);
    }

    void removeExistingSelection() {
        // Remove the existing selection rectangle if it exists
        if (selectionRectangle != null) {
            selectionRectangle.removeResizeHandles(imagePane);
            imagePane.getChildren().remove(selectionRectangle);
            selectionRectangle = null;  // Set to null to ensure fresh initialization
        }
        isAreaSelected = false;
    }
}

