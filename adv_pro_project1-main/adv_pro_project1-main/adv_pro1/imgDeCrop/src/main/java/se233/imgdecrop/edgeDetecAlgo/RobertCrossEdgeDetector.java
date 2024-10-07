package se233.imgdecrop.edgeDetecAlgo;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

public class RobertCrossEdgeDetector {

    public WritableImage processImage(Image image, double strength) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        WritableImage writableImage = new WritableImage(width, height);
        PixelReader pixelReader = image.getPixelReader();
        PixelWriter pixelWriter = writableImage.getPixelWriter();


        for (int y = 0; y < height - 1; y++) {
            for (int x = 0; x < width - 1; x++) {
                int pixel1 = pixelReader.getArgb(x, y) & 0xff;
                int pixel2 = pixelReader.getArgb(x + 1, y + 1) & 0xff;
                int pixel3 = pixelReader.getArgb(x + 1, y) & 0xff;
                int pixel4 = pixelReader.getArgb(x, y + 1) & 0xff;

                int gradientX = Math.abs(pixel1 - pixel2);
                int gradientY = Math.abs(pixel3 - pixel4);

                // Scale gradient based on strength to make edges more visible
                int magnitude = Math.min(255, (int) ((Math.sqrt(gradientX * gradientX + gradientY * gradientY)) * (strength / 50.0)));
                int colorValue = 255 - magnitude;

                // Set the pixel to the calculated edge value
                int newColor = (0xff << 24) | (colorValue << 16) | (colorValue << 8) | colorValue;
                pixelWriter.setArgb(x, y, newColor);

            }
        }

        return writableImage;
    }
}
