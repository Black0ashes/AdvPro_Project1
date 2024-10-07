package se233.imgdecrop.edgeDetecAlgo;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class GaussianEdgeDetector {

    // Apply Gaussian blur followed by edge detection (Sobel operator)
    public WritableImage detectEdges(Image image, double opacity) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        WritableImage blurredImage = applyGaussianBlur(image, width, height);
        WritableImage edgeImage = applyEdgeDetection(blurredImage, width, height, opacity);
        return edgeImage;
    }

    // Apply Gaussian Blur to an image
    private WritableImage applyGaussianBlur(Image image, int width, int height) {
        WritableImage blurredImage = new WritableImage(width, height);
        PixelReader pixelReader = image.getPixelReader();
        PixelWriter pixelWriter = blurredImage.getPixelWriter();

        double[][] gaussianKernel = {
                {1/16.0, 2/16.0, 1/16.0},
                {2/16.0, 4/16.0, 2/16.0},
                {1/16.0, 2/16.0, 1/16.0}
        };

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                double r = 0, g = 0, b = 0;

                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        Color color = pixelReader.getColor(x + kx, y + ky);
                        double kernelValue = gaussianKernel[ky + 1][kx + 1];
                        r += color.getRed() * kernelValue;
                        g += color.getGreen() * kernelValue;
                        b += color.getBlue() * kernelValue;
                    }
                }

                pixelWriter.setColor(x, y, new Color(r, g, b, 1.0));
            }
        }

        return blurredImage;
    }

    // Apply Sobel operator for edge detection
    private WritableImage applyEdgeDetection(WritableImage blurredImage, int width, int height, double opacity) {
        WritableImage edgeImage = new WritableImage(width, height);
        PixelReader pixelReader = blurredImage.getPixelReader();
        PixelWriter pixelWriter = edgeImage.getPixelWriter();

        int[][] sobelX = {
                {-1, 0, 1},
                {-2, 0, 2},
                {-1, 0, 1}
        };

        int[][] sobelY = {
                {-1, -2, -1},
                {0,  0,  0},
                {1,  2,  1}
        };

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                double gxRed = 0, gyRed = 0;
                double gxGreen = 0, gyGreen = 0;
                double gxBlue = 0, gyBlue = 0;

                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        Color color = pixelReader.getColor(x + kx, y + ky);
                        double red = color.getRed();
                        double green = color.getGreen();
                        double blue = color.getBlue();

                        gxRed += sobelX[ky + 1][kx + 1] * red;
                        gyRed += sobelY[ky + 1][kx + 1] * red;

                        gxGreen += sobelX[ky + 1][kx + 1] * green;
                        gyGreen += sobelY[ky + 1][kx + 1] * green;

                        gxBlue += sobelX[ky + 1][kx + 1] * blue;
                        gyBlue += sobelY[ky + 1][kx + 1] * blue;
                    }
                }

                double magnitudeRed = Math.sqrt(gxRed * gxRed + gyRed * gyRed);
                double magnitudeGreen = Math.sqrt(gxGreen * gxGreen + gyGreen * gyGreen);
                double magnitudeBlue = Math.sqrt(gxBlue * gxBlue + gyBlue * gyBlue);

                Color edgeColor = new Color(
                        clamp(magnitudeRed),
                        clamp(magnitudeGreen),
                        clamp(magnitudeBlue),
                        opacity
                );

                pixelWriter.setColor(x, y, edgeColor);
            }
        }

        return edgeImage;
    }


    // Clamp values between 0 and 1
    private double clamp(double value) {return Math.max(0, Math.min(1, value));
    }
}
