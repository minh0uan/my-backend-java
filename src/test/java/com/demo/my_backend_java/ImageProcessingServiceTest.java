package com.demo.my_backend_java;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import com.demo.my_backend_java.dto.ProcessedImage;
import com.demo.my_backend_java.service.ImageProcessingService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class ImageProcessingServiceTest {

    private final ImageProcessingService service =
    new ImageProcessingService(
        10 * 1024 * 1024,
        1024,
        512000,
        320,
        25_000_000
    );

    @Test
    void orientation1ProcessesSuccessfully() throws Exception {
        assertFixtureProcesses("landscape_1.jpg");
    }

    @Test
    void orientation2ProcessesSuccessfully() throws Exception {
        assertFixtureProcesses("landscape_2.jpg");
    }

    @Test
    void orientation3ProcessesSuccessfully() throws Exception {
        assertFixtureProcesses("landscape_3.jpg");
    }

    @Test
    void orientation4ProcessesSuccessfully() throws Exception {
        assertFixtureProcesses("landscape_4.jpg");
    }

    @Test
    void orientation5ProcessesSuccessfully() throws Exception {
        assertFixtureProcesses("landscape_5.jpg");
    }

    @Test
    void orientation6ProcessesSuccessfully() throws Exception {
        assertFixtureProcesses("landscape_6.jpg");
    }

    @Test
    void orientation7ProcessesSuccessfully() throws Exception {
        assertFixtureProcesses("landscape_7.jpg");
    }

    @Test
    void orientation8ProcessesSuccessfully() throws Exception {
        assertFixtureProcesses("landscape_8.jpg");
    }

    private void assertFixtureProcesses(String filename) throws Exception {
        ProcessedImage result = processFixture(filename);

        assertNotNull(result);
        assertEquals("image/jpeg", result.contentType());
        assertEquals(result.data().length, result.sizeBytes());
        assertTrue(result.width() > 0);
        assertTrue(result.height() > 0);
        assertEquals(result.width(), readOutputWidth(result));
        assertEquals(result.height(), readOutputHeight(result));

        System.out.printf(
            "%s -> %d x %d, %d bytes%n",
            filename,
            result.width(),
            result.height(),
            result.sizeBytes()
        );

        saveOutput(filename, result);
    }

    private void saveOutput(String filename, ProcessedImage result)
            throws IOException {

        Path outputDirectory = Paths.get("target", "test-output", "exif");
        Files.createDirectories(outputDirectory);

        String outputName = filename.replace(".jpg", "_processed.jpg");

        Files.write(
            outputDirectory.resolve(outputName),
            result.data()
        );
    }


    private ProcessedImage processFixture(String filename) throws IOException {
        try (InputStream input =
                    getClass().getResourceAsStream("/exif/" + filename)) {

            if (input == null) {
                throw new IllegalStateException("Missing fixture: " + filename);
            }

            byte[] bytes = input.readAllBytes();

            MockMultipartFile file = new MockMultipartFile(
                "photo",
                filename,
                "image/jpeg",
                bytes
            );

            return service.processAndCompress(file);
        }
    }

    private int readOutputWidth(ProcessedImage result) throws IOException {
        try (InputStream input =
                        new java.io.ByteArrayInputStream(result.data())) {
            return ImageIO.read(input).getWidth();
        }
    }

    private int readOutputHeight(ProcessedImage result) throws IOException {
        try (InputStream input =
                    new java.io.ByteArrayInputStream(result.data())) {
            return ImageIO.read(input).getHeight();
        }
    }
}

