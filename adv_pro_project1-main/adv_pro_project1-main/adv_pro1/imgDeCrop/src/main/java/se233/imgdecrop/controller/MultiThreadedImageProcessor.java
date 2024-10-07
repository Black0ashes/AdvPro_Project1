package se233.imgdecrop.controller;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import se233.imgdecrop.Launcher;
import se233.imgdecrop.edgeDetecAlgo.GaussianEdgeDetector;
import se233.imgdecrop.edgeDetecAlgo.LaplacianEdgeDetector;
import se233.imgdecrop.edgeDetecAlgo.RobertCrossEdgeDetector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class MultiThreadedImageProcessor {

    private ExecutorService executorService;

    // Constructor that initializes the thread pool
    public MultiThreadedImageProcessor(int threadCount) {
        this.executorService = Executors.newFixedThreadPool(threadCount);
    }

    // Method to process multiple images concurrently
    public List<WritableImage> processImages(List<Image> images, double opacity, double strength) throws InterruptedException, ExecutionException {
        List<Callable<WritableImage>> tasks = new ArrayList<>();
        List<Future<WritableImage>> results;

        // Add tasks for each image to process
        for (Image image : images) {
            tasks.add(() -> {
                WritableImage temp = null;
                if (Launcher.selectedAlgo == 1) {
                    GaussianEdgeDetector detector = new GaussianEdgeDetector();
                    temp = detector.detectEdges(image, opacity);
                } else if (Launcher.selectedAlgo == 2) {
                    LaplacianEdgeDetector detector = new LaplacianEdgeDetector();
                    temp = detector.processImage(image);
                } else if (Launcher.selectedAlgo == 3) {
                    RobertCrossEdgeDetector detector = new RobertCrossEdgeDetector();
                    temp = detector.processImage(image, strength);
                } else {
                    // for alert
                }
                Launcher.counter = 1;
                return temp;
            });
        }

        // Submit all tasks to the ExecutorService and get a list of Futures
        results = executorService.invokeAll(tasks);

        // Collect processed images from Future results
        List<WritableImage> processedImages = new ArrayList<>();
        for (Future<WritableImage> result : results) {
            processedImages.add(result.get());  // Get the processed image (blocking until done)
        }
        return  processedImages;
    }



    // Shutdown the executor service
    public void shutdown() {
        executorService.shutdown();
    }

}