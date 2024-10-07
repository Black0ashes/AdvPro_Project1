package se233.imgdecrop.CustomException;

public class ImageProcessingException extends Exception {

    public ImageProcessingException(String message) {
        super(message);
    }

    public ImageProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}

