package com.demo.my_backend_java.service;

import com.demo.my_backend_java.dto.ProcessedImage;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

@Service
public class ImageProcessingService {

    private int maxInputBytes;

    private int maxLongEdge;

    private int maxTargetBytes;

    private int minLongEdge;

    private int maxPixel;

    public ImageProcessingService(
            @Value("${photo.max-input-bytes:10485760}") int maxInputBytes,
            @Value("${photo.max-long-edge:1024}") int maxLongEdge,
            @Value("${photo.max-target-bytes:512000}") int maxTargetBytes,
            @Value("${photo.min-long-edge:320}") int minLongEdge,
            @Value("${photo.max-pixels:25000000}") int maxPixel) {

        this.maxInputBytes = maxInputBytes;
        this.maxLongEdge = maxLongEdge;
        this.maxTargetBytes = maxTargetBytes;
        this.minLongEdge = minLongEdge;
        this.maxPixel = maxPixel;

        validateConfiguration();
    }

    //check if the plugin value is corrupted.
    private void validateConfiguration() {
        if (minLongEdge > maxLongEdge) {
            throw new IllegalStateException("long edge cannot be smaller than max edge");
        }

        if (minLongEdge < 320) {
            throw new IllegalStateException("long edge too small, it needs to be at least 320");
        }

        if (maxInputBytes <= 0) {
            throw new IllegalStateException(" max input must be greater than 0");
        }

        if (maxTargetBytes <= 0) {
            throw new IllegalStateException("max target byte must be greater than 0");
        }

        if (maxPixel <= 0) {
            throw new IllegalStateException("max pixel must be greater than 0");
        }

        if (maxLongEdge <= 0) {
            throw new IllegalStateException(" max long edge must be greater than 0");
        }
    }

    public ProcessedImage processAndCompress(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("PHOTO_EMPTY: Uploaded photo is empty");
        }

        if (file.getSize() > maxInputBytes) {
            throw new IllegalArgumentException("PHOTO_TOO_LARGE: File exceeds configure input limit");
        }

        //split the byte into an array
        byte[] inputBytes = file.getBytes();
        
        //check to see if its a real photo 
        //never trust file.getContentType()
        validateMagicBytes(inputBytes);

        int orientation = extractOrientation(inputBytes);

        BufferedImage originalImage = null;
        BufferedImage orientedImage = null;
        BufferedImage resizedImage = null;
        BufferedImage smallerImage = null;

        try {
            //deocode the image 
            originalImage = ImageIO.read(new ByteArrayInputStream(inputBytes));

            if (originalImage == null) {
                throw new IllegalArgumentException("PHOTO_INVALID: Could not decode image");
            }

            long totalPixels = (long) originalImage.getWidth() * originalImage.getHeight();
            if (totalPixels > maxPixel) {
                //originalImage.flush();
                throw new IllegalArgumentException("PHOTO_TOO_LARGE: Image dimensions are dangerously large");
            }

            orientedImage = applyOrientation(originalImage, orientation);

            //resize 
            resizedImage = resizeProportional(orientedImage, maxLongEdge);

            byte[] compressedData = compressToJpeg(resizedImage);

            int finalWidth = resizedImage.getWidth();
            int finalHeight = resizedImage.getHeight();

            int currentEdge = maxLongEdge;

            while (compressedData.length > maxTargetBytes && currentEdge > minLongEdge) {

                currentEdge = Math.max(minLongEdge, currentEdge - 200);

                if (smallerImage != null) {
                    smallerImage.flush();
                }

                smallerImage = resizeProportional(resizedImage, currentEdge);
                compressedData = compressToJpeg(smallerImage);

                finalHeight = smallerImage.getHeight();
                finalWidth = smallerImage.getWidth();
            }

            if (compressedData.length > maxTargetBytes) {
                throw new IllegalArgumentException("PHOTO_TOO_LARGE: Could not compress the image within the configured limit");
            }

            return new ProcessedImage(
                compressedData,
                "image/jpeg",
                compressedData.length,
                finalWidth,
                finalHeight
            );
        } finally {
            if (originalImage != null) {
                originalImage.flush();
            }
            if (orientedImage != null && orientedImage != originalImage) {
                orientedImage.flush();
            }
            if (resizedImage != null && resizedImage != orientedImage) {
                resizedImage.flush();
            }
            if (smallerImage != null) {
                smallerImage.flush();
            }
        }

        
    }

    //magic byte to verify the file if its a picture or not
    private void validateMagicBytes(byte[] data) {
        if (data.length < 8) {
            throw new IllegalArgumentException("PHOTO_INVALID: File header is too short");
        }

        // JPEG Magic Bytes: FF D8 FF
        boolean isJpeg = (data[0] & 0xFF) == 0xFF &&
                        (data[1] & 0xFF) == 0xD8 &&
                        (data[2] & 0xFF) == 0xFF;

        // PNG Magic Bytes: 89 50 4E 47
        boolean isPng = (data[0] & 0xFF) == 0x89 &&
                        (data[1] & 0xFF) == 0x50 &&
                        (data[2] & 0xFF) == 0x4E &&
                        (data[3] & 0xFF) == 0x47;

        if (!isJpeg && !isPng) {
            throw new IllegalArgumentException("PHOTO_UNSUPPORTED_FORMAT: Only JPEG and PNG are permitted");
        }
    }

    private BufferedImage resizeProportional(BufferedImage source, int maxEdge) {
        int width = source.getWidth();
        int height = source.getHeight();

        // If it's already small enough, just return a clean RGB copy (strips alpha/metadata)
        if (width <= maxEdge && height <= maxEdge) {
            return ensureRgb(source);
        }

        double ratio = (double) width / height;
        if (ratio > 1.0) { // Landscape
            width = maxEdge;
            height = (int) Math.round(maxEdge / ratio);
        } else { // Portrait
            height = maxEdge;
            width = (int) Math.round(maxEdge * ratio);
        }

        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = output.createGraphics();
        
        // Use Bilinear interpolation for smooth downscaling
        //RenderingHints.VALUE_INTERPOLATION_BILINEAR mathematically blend the color
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(source, 0, 0, width, height, null);
        g2d.dispose(); // Always dispose Graphics2D to prevent memory leaks
        
        return output;
    }

    private BufferedImage ensureRgb(BufferedImage source) {
        if (source.getType() == BufferedImage.TYPE_INT_RGB) return source;
        BufferedImage rgbImage = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = rgbImage.createGraphics();
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return rgbImage;
    }

    private byte[] compressToJpeg(BufferedImage image) throws IOException {
        float currentQuality = 0.80f; // Start at 80% quality
        byte[] bytes;

        do {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
            if (!writers.hasNext()) throw new IllegalStateException("No JPEG ImageWriter found");

            ImageWriter writer = writers.next();
            try (ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
                writer.setOutput(ios);
                ImageWriteParam param = writer.getDefaultWriteParam();
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(currentQuality);
                writer.write(null, new IIOImage(image, null, null), param);
            } finally {
                writer.dispose();
            }

            bytes = baos.toByteArray();
            currentQuality -= 0.10f; // Drop quality by 10% if it's still too big
            
        } while (bytes.length > maxTargetBytes && currentQuality >= 0.40f); // Stop at 40% to prevent garbage images

        return bytes;
    }

    private int extractOrientation(byte[] inputBytes) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(new ByteArrayInputStream(inputBytes));
            ExifIFD0Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);

            if (directory != null && directory.containsTag(ExifIFD0Directory.TAG_ORIENTATION)) {
                return directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
            }
        } catch (Exception ingored){

        }
        return 1;
    }

    private BufferedImage applyOrientation(BufferedImage image, int orientation) {
        if (orientation <= 1) {
            return image;
        }

        int w = image.getWidth();
        int h = image.getHeight();

        AffineTransform t = new AffineTransform();

        switch (orientation) {
            case 2 -> { t.scale(-1.0, 1.0); t.translate(-w, 0); } 
            case 3 -> { t.translate(w, h); t.rotate(Math.PI); } 
            case 4 -> { t.scale(1.0, -1.0); t.translate(0, -h); } 
            case 5 -> { t.scale(-1.0, 1.0); t.translate(-h, 0); t.translate(h, 0); t.rotate(Math.PI / 2); int temp = w; w = h; h = temp; } 
            case 6 -> { t.translate(h, 0); t.rotate(Math.PI / 2); int temp = w; w = h; h = temp; } 
            case 7 -> { t.scale(-1.0, 1.0); t.translate(-w, 0); t.translate(0, w); t.rotate(-Math.PI / 2); int temp = w; w = h; h = temp; } 
            case 8 -> { t.translate(0, w); t.rotate(-Math.PI / 2); int temp = w; w = h; h = temp; } 
            default -> { return image; }
        }

        BufferedImage transformed = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = transformed.createGraphics();
        g.drawImage(image, t, null);
        g.dispose();
        return transformed;   
    }
}