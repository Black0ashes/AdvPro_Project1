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
import se233.imgdecrop.Launcher;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class SceneController {
    // For main screen
    private Stage stage;
    private Scene scene;
    private Parent root;

    // For edgeDetection
    @FXML
    private JFXSlider opacitySlider;
    @FXML
    private JFXSlider strengthSlider;
    @FXML
    private ImageView inputImgView;
    @FXML
    private ImageView resultImgView;
    @FXML
    private AnchorPane dropbox;

    // For edgeDetection
    private List<Image> imagesList = new ArrayList<>();
    private List<WritableImage> resultImageList = new ArrayList<>();
    private edgeDetectionHandler edgeDetectionHandler;
    private int currentIndex = 0;

    // For main screen
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

    public void switchToEdgeA1Scene(ActionEvent event) throws IOException {
        Launcher.selectedAlgo = 1;
        Launcher.counter = 0;
        root = FXMLLoader.load(getClass().getResource("/se233/imgdecrop/edgeA1_scene.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void switchToEdgeA2Scene(ActionEvent event) throws IOException {
        Launcher.selectedAlgo = 2;
        root = FXMLLoader.load(getClass().getResource("/se233/imgdecrop/edgeA2_scene.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void switchToEdgeA3Scene(ActionEvent event) throws IOException {
        Launcher.selectedAlgo = 3;
        root = FXMLLoader.load(getClass().getResource("/se233/imgdecrop/edgeA3_scene.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }


    // For edgeDetection

    @FXML
    private void handleDragOver(DragEvent event) {
        if (event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY);
        }
        event.consume();
    }

    @FXML
    private void handleDragDropped(DragEvent event) {
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
        edgeDetectionHandler = new edgeDetectionHandler(inputImgView, resultImgView);
    }

    @FXML
    private void handleClickToUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        List<File> files = fileChooser.showOpenMultipleDialog(dropbox.getScene().getWindow());
        if (files != null) {
            for (File file : files) {
                loadAndAddImage(file);
            }
        }
    }

    private void loadAndAddImage(File file) {
        try {
            Image image = new Image(file.toURI().toString());
            imagesList.add(image);
            if (imagesList.size() == 1) {
                inputImgView.setImage(image);
                currentIndex = 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isImageFile(String fileName) {
        fileName = fileName.toLowerCase();
        return fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg");
    }

    private void processZipFile(File zipFile) {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory() && isImageFile(entry.getName())) {
                    Path tempFile = Files.createTempFile("temp_image", ".png");
                    Files.copy(zis, tempFile, StandardCopyOption.REPLACE_EXISTING);
                    Image image = new Image(tempFile.toUri().toString());
                    imagesList.add(image);
                }
                zis.closeEntry();
            }
            if (!imagesList.isEmpty()) {
                currentIndex = 0;
                inputImgView.setImage(imagesList.get(currentIndex));
                resultImgView.setImage(null);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void DetectEdges() {
        // Set the loading GIF before starting processing
        Image gift = new Image(getClass().getResource("/se233/imgdecrop/loading_ps.gif").toExternalForm());
        resultImgView.setImage(gift);

        // Use a background task for image processing
        Task<List<WritableImage>> processingTask = new Task<List<WritableImage>>() {
            @Override
            protected List<WritableImage> call() throws Exception {
                MultiThreadedImageProcessor processor = new MultiThreadedImageProcessor(4);
                double opacity = 0;
                double strength = 0;

                // Get algorithm-specific parameters
                if (Launcher.selectedAlgo == 1) {
                    opacity = opacitySlider.getValue();
                } else if (Launcher.selectedAlgo == 3) {
                    strength = strengthSlider.getValue();
                }

                // Process the images
                List<WritableImage> processedImages = processor.processImages(imagesList, opacity, strength);
                processor.shutdown();  // Shutdown the processor after use
                return processedImages;  // Return the processed images
            }

            @Override
            protected void succeeded() {
                // Update the resultImageList with the processed images
                try {
                    List<WritableImage> resultImages = get();  // Get the result from the task
                    if (!resultImages.isEmpty()) {
                        resultImageList = resultImages;  // Store the processed images
                        currentIndex = 0;  // Set the index to show the first image
                        resultImgView.setImage(resultImageList.get(currentIndex));  // Update the ImageView
                    } else {
                        showAlert("No Images", "No images were processed. Please check your input and try again.");
                        clear();
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();  // Handle exceptions accordingly
                }
            }

            @Override
            protected void failed() {
                // Handle any exceptions that occurred during processing
                Throwable e = getException();
                showAlert("Processing Failed", "Image processing failed: " + e.getMessage());
                e.printStackTrace();  // Or show an alert to the user
            }
        };

        // Run the processing task in a background thread
        new Thread(processingTask).start();
    }

    @FXML
    private void showNextImage() {
        if (currentIndex < imagesList.size() - 1) {
            currentIndex++;
            inputImgView.setImage(imagesList.get(currentIndex));
            if (Launcher.counter == 1) {
                resultImgView.setImage(resultImageList.get(currentIndex));
            }
        } else {
            showAlert("No next image", "This the last image."); //Alert message
        }
    }

    @FXML
    private void showBackImage() {
        if (currentIndex > 0) {
            currentIndex--;
            inputImgView.setImage(imagesList.get(currentIndex));
            if (Launcher.counter == 1) {
                resultImgView.setImage(resultImageList.get(currentIndex));
            }
        } else {
            showAlert("No previous image", "This the first image."); //Alert message
        }
    }

    @FXML
    private void clear() {
        imagesList.clear();
        inputImgView.setImage(null);
        resultImgView.setImage(null);
        resultImageList.clear();
        currentIndex = 0;
        Launcher.counter = 0;
        showAlert("Clear all image", "All image have been cleared."); //Alert message
    }

    @FXML
    private void onSaveImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Image");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Image", "*.png"));
        File file = fileChooser.showSaveDialog(resultImgView.getScene().getWindow());

        if (file != null) {
            try {
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(resultImgView.getImage(), null);
                ImageIO.write(bufferedImage, "png", file);
                // Show a success alert
                showAlert("Image Saved", "Your image was saved successfully!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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

