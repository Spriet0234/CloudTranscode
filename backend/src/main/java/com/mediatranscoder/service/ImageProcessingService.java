package com.mediatranscoder.service;

import com.mediatranscoder.model.Job;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.awt.color.ColorSpace;
import java.awt.image.ColorConvertOp;

@Slf4j
@Service
public class ImageProcessingService {

    public void processImage(File inputFile, File outputFile, Job job) throws IOException {
        BufferedImage inputImage = null;
        try {
            inputImage = ImageIO.read(inputFile);
            if (inputImage == null) {
                throw new IOException("Failed to read input image: " + inputFile.getName());
            }
        } catch (Throwable e) {
            log.warn("Primary image read failed: {}. Attempting fallback to RGB. Exception: {}", e.getClass().getName(), e.getMessage());
            // Try to read and convert to RGB colorspace if possible
            try {
                BufferedImage tmp = ImageIO.read(inputFile);
                if (tmp != null) {
                    BufferedImage rgbImage = new BufferedImage(
                        tmp.getWidth(), tmp.getHeight(), BufferedImage.TYPE_INT_RGB);
                    ColorConvertOp op = new ColorConvertOp(
                        tmp.getColorModel().getColorSpace(),
                        ColorSpace.getInstance(ColorSpace.CS_sRGB), null);
                    op.filter(tmp, rgbImage);
                    inputImage = rgbImage;
                }
            } catch (Throwable ex) {
                log.error("Fallback image read also failed: {}: {}", ex.getClass().getName(), ex.getMessage());
                throw new IOException("Unsupported or corrupt image file: " + inputFile.getName(), ex);
            }
        }
        if (inputImage == null) {
            throw new IOException("Failed to read input image: " + inputFile.getName());
        }

        // Apply resize if specified
        if ("true".equalsIgnoreCase(job.getSettings().get("resize"))) {
            String width = (String) job.getSettings().get("width");
            String height = (String) job.getSettings().get("height");
            if (width != null && !width.isEmpty() && height != null && !height.isEmpty()) {
                inputImage = resizeImage(inputImage, Integer.parseInt(width), Integer.parseInt(height));
            }
        }

        // Get the output format
        String format = job.getOutputFormat().toLowerCase();
        if (!format.equals("jpg") && !format.equals("png") && !format.equals("webp")) {
            format = "jpg"; // Default to JPG if format not supported
        }

        // Get quality setting
        float quality = switch (job.getOutputQuality().toLowerCase()) {
            case "high" -> 0.9f;
            case "medium" -> 0.75f;
            case "low" -> 0.6f;
            default -> 0.75f;
        };

        // Ensure the image is in TYPE_INT_RGB before writing as JPEG
        if (inputImage.getType() != BufferedImage.TYPE_INT_RGB) {
            BufferedImage rgbImage = new BufferedImage(
                inputImage.getWidth(),
                inputImage.getHeight(),
                BufferedImage.TYPE_INT_RGB
            );
            Graphics2D g = rgbImage.createGraphics();
            g.drawImage(inputImage, 0, 0, null);
            g.dispose();
            inputImage = rgbImage;
        }

        // Write the output image
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            if (format.equals("jpg")) {
                writeJpegImage(inputImage, fos, quality);
            } else {
                ImageIO.write(inputImage, format, fos);
            }
        }

        log.info("Successfully processed image: {} to {}", inputFile.getName(), outputFile.getName());
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, originalImage.getType());
        Graphics2D graphics2D = resizedImage.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        graphics2D.dispose();
        return resizedImage;
    }

    private void writeJpegImage(BufferedImage image, FileOutputStream outputStream, float quality) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        if (!writers.hasNext()) {
            throw new IOException("No JPEG writer available");
        }

        ImageWriter writer = writers.next();
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(outputStream)) {
            writer.setOutput(ios);
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality);
            writer.write(null, new IIOImage(image, null, null), param);
        } finally {
            writer.dispose();
        }
    }

    public void getImageInfo(File imageFile) throws IOException {
        BufferedImage image = ImageIO.read(imageFile);
        if (image == null) {
            throw new IOException("Failed to read image: " + imageFile.getName());
        }

        log.info("Image info for {}: Width={}, Height={}, Type={}, Size={} bytes",
                imageFile.getName(),
                image.getWidth(),
                image.getHeight(),
                image.getType(),
                imageFile.length());
    }
} 