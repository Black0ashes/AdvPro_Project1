package se233.imgdecrop.edgeDetecAlgo;


import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class LaplacianEdgeDetector {
    private final int[][] kernel = {
            {0, 0, -1, 0, 0},
            {0, -1, -2, -1, 0},
            {-1, -2, 16, -2, -1},
            {0, -1, -2, -1, 0},
            {0, 0, -1, 0, 0}
    };

    public WritableImage processImage(Image image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        WritableImage writableImage = new WritableImage(width, height);
        PixelReader pixelReader = image.getPixelReader();
        PixelWriter pixelWriter = writableImage.getPixelWriter();

        // Choose the appropriate kernel based on mask size
        int kernelSize = kernel.length;
        int offset = kernelSize / 2;

        // Apply convolution using the kernel
        for (int y = offset; y < height - offset; y++) {
            for (int x = offset; x < width - offset; x++) {
                int gradient = 0;

                // Apply convolution
                for (int i = -offset; i <= offset; i++) {
                    for (int j = -offset; j <= offset; j++) {
                        int pixel = pixelReader.getArgb(x + j, y + i) & 0xff; // Extract the grayscale value
                        gradient += kernel[i + offset][j + offset] * pixel;
                    }
                }

                // Scale and constrain gradient values to be within the 0-255 range, invert for black edges on white background
                gradient = Math.abs(gradient); // Make sure gradient is positive
                gradient = Math.min(255, Math.max(0, gradient));
                int colorValue = gradient > 50 ? 0 : 255; // Set a binary threshold, black lines on white background
                int newColor = (0xff << 24) | (colorValue << 16) | (colorValue << 8) | colorValue;
                pixelWriter.setArgb(x, y, newColor);
            }
        }

        return writableImage;
    }

}
