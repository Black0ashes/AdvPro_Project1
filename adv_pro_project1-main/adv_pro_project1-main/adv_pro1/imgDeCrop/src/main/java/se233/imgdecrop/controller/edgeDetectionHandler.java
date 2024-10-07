package se233.imgdecrop.controller;

import javafx.scene.image.ImageView;

public class edgeDetectionHandler {
    private ImageView inputImgView;
    private final ImageView resultImgView;

    public edgeDetectionHandler(ImageView inputImgView, ImageView resultImgView) {
        this.inputImgView = inputImgView;
        this.resultImgView = resultImgView;
    }
}
