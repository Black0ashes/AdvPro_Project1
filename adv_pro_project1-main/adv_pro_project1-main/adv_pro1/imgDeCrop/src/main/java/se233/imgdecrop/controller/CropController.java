package se233.imgdecrop.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSlider;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import se233.imgdecrop.CustomException.ImageProcessingException;
import se233.imgdecrop.Launcher;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class CropController {
    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    private ImageView inputImgView;
    @FXML
    private BorderPane imagePane;
    @FXML
    private ScrollPane imageScrollPane;
    @FXML
    private AnchorPane rootPane;
    @FXML
    private JFXButton next_btn;
    @FXML
    private JFXButton back_btn;

    private List<Image> imagesList = new ArrayList<>();
    private int currentIndex = 0;
    private CropHandler cropHandler;
    private List<String> inputListView = new ArrayList<String>();
    private volatile boolean cropConfirmed;


    public void switchToCropScene(ActionEvent event) throws IOException {
        Launcher.selectedAlgo = 0;
        Launcher.counter = 0;
        root = FXMLLoader.load(getClass().getResource("/se233/imgdecrop/cropScene.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void switchToEdgeScene(ActionEvent event) throws IOException {
        Launcher.selectedAlgo = 0;
        Launcher.counter = 0;
        root = FXMLLoader.load(getClass().getResource("/se233/imgdecrop/edgeScene.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    private void handleDragOver(DragEvent event) {
        if (event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY);
        }
        event.consume();
    }

    @FXML
    private void handleDragDropped(DragEvent event) throws ImageProcessingException {
        var db = event.getDragboard();
        if (db.hasFiles()) {
            for (File file : db.getFiles()) {
                if (file.getName().toLowerCase().endsWith(".zip")) {
                    processZipFile(file);
                } else {
                    loadAndAddImage(file);
                }
            }
        }
        event.setDropCompleted(true);
        event.consume();
    }

    @FXML
    private void initialize() {
        cropHandler = new CropHandler(inputImgView, imagePane, imageScrollPane);
    }

    @FXML
    private void handleClickToUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        List<File> files = fileChooser.showOpenMultipleDialog(imageScrollPane.getScene().getWindow());
        if (files != null) {
            for (File file : files) {
                try {
                    loadAndAddImage(file);
                } catch (ImageProcessingException e) {
                    showAlert("Error", e.getMessage());
                }
            }
        }
    }

    private void loadAndAddImage(File file) throws ImageProcessingException {
        try {
            Image image = new Image(file.toURI().toString());
            imagesList.add(image);
            inputListView.add(file.getAbsolutePath());

            if (imagesList.size() == 1) {
                inputImgView.setImage(image);
                currentIndex = 0;
            }
        } catch (IllegalArgumentException e) { // runtime exceptions  1
            throw new ImageProcessingException("The file is not a valid image format.", e);
        } catch (Exception e) {
            throw new ImageProcessingException("An error occurred while loading the image.", e);
        }
    }

    private boolean isImageFile(File file) throws IOException {
        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg");
    }

    private boolean isImageFile(String fileName) throws IOException {
        fileName = fileName.toLowerCase();
        return fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg");
    }


    private void processZipFile(File zipFile) throws ImageProcessingException {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                // Get the entry name (file path within the zip)
                String entryName = entry.getName();

                // Check if the entry is a file and an image
                if (!entry.isDirectory() && isImageFile(entryName)) {
                    // Create a temporary file or save it to a specific directory
                    Path tempFile = Files.createTempFile("temp_image", ".png");
                    Files.copy(zis, tempFile, StandardCopyOption.REPLACE_EXISTING);

                    // Get the absolute path of the temporary file
                    String absolutePath = tempFile.toAbsolutePath().toString();

                    // Load the image from the temporary file
                    Image image = new Image(tempFile.toUri().toString());

                    // Add the image to the list for display
                    imagesList.add(image);
                    inputListView.add(absolutePath);
                }

                // Close the current entry
                zis.closeEntry();
            }

            // Set the first image for display if the list is not empty
            if (!imagesList.isEmpty()) {
                currentIndex = 0;
                inputImgView.setImage(imagesList.get(currentIndex));
            }
        } catch (FileNotFoundException e) {
            // Handle specific exception for file not found
            throw new ImageProcessingException("Zip file not found: " + zipFile.getName(), e);
        } catch (IOException e) { // runtime exceptions  2
            throw new ImageProcessingException("An error occurred while processing the zip file.", e);
        }
    }

    @FXML
    private void showNextImage() {
        if (currentIndex < imagesList.size() - 1) {
            currentIndex++;
            inputImgView.setImage(imagesList.get(currentIndex));
        } else {
            showAlert("No next image", "This the last image."); //Alert message
        }
    }

    @FXML
    private void showBackImage() {
        if (currentIndex > 0) {
            currentIndex--;
            inputImgView.setImage(imagesList.get(currentIndex));
        } else {
            showAlert("No previous image", "This the first image."); //Alert message
        }
    }

    @FXML
    private void clear() {
        // Clear the list of images
        imagesList.clear();
        // Reset the current index
        currentIndex = 0;
        // Clear the ImageView
        inputImgView.setImage(null);
        inputListView.clear();
        // Reset crop handler
        resetCropHandler();
        showAlert("Clear all image", "All image have been cleared."); //Alert message
    }

    private void resetCropHandler() {
        // Reset CropHandler and ensure it is properly initialized
        if (cropHandler != null) {
            cropHandler.removeExistingSelection();
            cropHandler = new CropHandler(inputImgView, imagePane, imageScrollPane);
        }
    }

    @FXML
    public void onConfirmCrop() {
        if (inputImgView.getImage() != null && cropHandler != null) {
            cropHandler.confirmCrop();
            cropConfirmed = true;  // Set the flag to true, allowing the batch task to continue
        } else {
            showAlert("No Image", "Please load an image before start cropping."); //Alert message
        }
    }


    @FXML
    private void onBatchProcess() {
        try {
            next_btn.setVisible(false);
            next_btn.setDisable(true); // disable the button
            back_btn.setVisible(false);
            back_btn.setDisable(true); // disable the button

            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Select Output Directory");
            File outputDir = directoryChooser.showDialog(inputImgView.getScene().getWindow());

            if (outputDir == null) {
                throw new IllegalArgumentException("No output directory selected.");
            }
            configureCroppingForAllImages(outputDir);
        } catch (IllegalArgumentException e) {
            showAlert("Invalid Directory", e.getMessage());
        }

    }

    private void configureCroppingForAllImages(File outputDir) {
        configureNextImage(0, outputDir, new ArrayList<>());
    }

    private void configureNextImage(int currentIndex, File outputDir, List<Image> croppedImages) {
        if (currentIndex >= inputListView.size()) {
            startConcurrentProcessing(croppedImages, outputDir);
            return;
        }

        String filePath = inputListView.get(currentIndex);
        File file = new File(filePath);

        try {
            if (isImageFile(file)) {
                Image image = new Image(file.toURI().toString());
                inputImgView.setImage(image);
                cropHandler.startCrop();
                cropHandler.setOnCropConfirmed(() -> {
                    croppedImages.add(inputImgView.getImage());
                    configureNextImage(currentIndex + 1, outputDir, croppedImages);
                });
            } else {
                showAlert("Invalid File", "The file " + file.getName() + " is not a valid image.");
            }
        } catch (IOException e) {
            showAlert("Error", "An error occurred while loading the image: " + e.getMessage());
        } catch (RuntimeException e) { // runtime exceptions  3
            showAlert("Runtime Error", "An unexpected error occurred: " + e.getMessage());
        }
    }

    private void startConcurrentProcessing(List<Image> croppedImages, File outputDir) {
        ExecutorService executorService = Executors.newFixedThreadPool(4);  // Adjust thread count as needed

        // Create a progress indicator or a list to display processing progress
        ProgressBar progressBar = new ProgressBar(0);
        rootPane.getChildren().add(progressBar);

        AtomicInteger processedCount = new AtomicInteger(0);
        List<Task<Void>> tasks = new ArrayList<>();

        for (int i = 0; i < croppedImages.size(); i++) {
            Image image = croppedImages.get(i);
            String fileName = new File(inputListView.get(i)).getName();

            // Create a Task for each image processing job
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    try {
                        // Detect edges and save the processed image
                        Image processedImage = detectEdges(image);
                        saveProcessedImage(processedImage, outputDir, fileName);

                        next_btn.setVisible(true);
                        next_btn.setDisable(false);
                        back_btn.setVisible(true);
                        back_btn.setDisable(false);

                        // Update progress (JavaFX updates must be on the UI thread)
                        Platform.runLater(() -> {
                            int completed = processedCount.incrementAndGet();
                            progressBar.setProgress((double) completed / croppedImages.size());
                        });

                    } catch (IOException e) {
                        e.printStackTrace();
                        Platform.runLater(() -> {
                            showAlert("Error", "Failed to save processed image: " + fileName);
                        });
                    }
                    return null;
                }
            };
            tasks.add(task);
        }

        // Submit tasks to ExecutorService and execute them concurrently
        for (Task<Void> task : tasks) {
            executorService.submit(task);
        }

        // Shutdown the executor service after all tasks are completed
        executorService.shutdown();
        new Thread(() -> {
            try {
                // Wait for all tasks to complete
                executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

                // Once processing is complete, remove the ProgressBar
                Platform.runLater(() -> {
                    rootPane.getChildren().remove(progressBar);  // Remove progress box from the root pane
                    showAlert("Completed", "All images have been processed successfully!");
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showAlert("Error", "Processing interrupted: " + e.getMessage());
                });
            }
        }).start();
    }

    private Image detectEdges(Image croppedImage) {
        return croppedImage;
    }

    private void saveProcessedImage(Image image, File outputDir, String originalFileName) throws IOException {
        String outputFileName = originalFileName.substring(0, originalFileName.lastIndexOf('.')) + "_processed.png";
        File outputFile = new File(outputDir, outputFileName);
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
        ImageIO.write(bufferedImage, "png", outputFile);
    }

    //Alert message
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
