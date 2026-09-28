package com.demo.my_backend_java.service;

import com.demo.my_backend_java.dto.ProcessedImage;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;

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

    private static final int MAX_INPUT_BYTES = 10 * 1024 * 1024; // 10MB
    private static final int MAX_LONG_EDGE = 1024;
    private static final long MAX_TARGET_BYTES = 500 * 1024; // 500KB

    public ProcessedImage processAndCompress(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("PHOTO_EMPTY: Uploaded photo is empty");
        }

        if (file.getSize() > MAX_INPUT_BYTES) {
            throw new IllegalArgumentException("PHOTO_TOO_LARGE: File exceeds 10MB limit");
        }

        //split the byte into an array
        byte[] inputBytes = file.getBytes();
        
        //check to see if its a real photo 
        //never trust file.getContentType()
        validateMagicBytes(inputBytes);

        int orientation = extractOrientation(inputBytes);

        //deocode the image 
        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(inputBytes));
        if (originalImage == null) {
            throw new IllegalArgumentException("PHOTO_INVALID: Could not decode image");
        }

        BufferedImage orientedImage = applyOrientation(originalImage, orientation);

        //resize 
        BufferedImage resizedImage = resizeProportional(orientedImage, MAX_LONG_EDGE);

        byte[] compressedData = compressToJpeg(resizedImage);

        return new ProcessedImage(
            compressedData,
            "image/jpeg",
            compressedData.length,
            resizedImage.getWidth(),
            resizedImage.getHeight()
        );
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
            
        } while (bytes.length > MAX_TARGET_BYTES && currentQuality >= 0.40f); // Stop at 40% to prevent garbage images

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
            case 3 -> {
                t.translate(w, h);
                t.rotate(Math.PI); //rotate 180
            }
            case 6 -> {
                t.translate(h, 0);
                t.rotate(Math.PI / 2);
                int temp = w; w = h; h = temp; //90
            } 
            case 8 -> {
                t.translate(0, w);
                t.rotate(-Math.PI / 2); //90
                int temp = w; w = h; h = temp;
            }
            default -> {
                return image;
            }
        }

        BufferedImage transformed = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = transformed.createGraphics();
        g.drawImage(image, t, null);
        g.dispose();
        return transformed;   
    }
}