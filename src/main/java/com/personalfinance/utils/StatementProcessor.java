package com.personalfinance.utils;

import org.apache.commons.codec.binary.Base64;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Processes financial statements and prepares them for Claude API consumption.
 * Handles file reading and base64 encoding for the Messages API.
 */
public class StatementProcessor {

    /**
     * Read text from a file (CSV, TXT, or other text-based statements).
     */
    public static String extractTextFromFile(String filePath) throws IOException {
        Path path = new File(filePath).toPath();
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("File not found: " + filePath);
        }

        if (filePath.toLowerCase().endsWith(".pdf")) {
            return extractTextFromPdf(path);
        }

        return Files.readString(path, StandardCharsets.UTF_8);
    }

    private static String extractTextFromPdf(Path path) throws IOException {
        try (PDDocument document = Loader.loadPDF(path.toFile())) {
            PDFTextStripper textStripper = new PDFTextStripper();
            return textStripper.getText(document);
        }
    }

    /**
     * Convert a file to base64 encoding.
     */
    public static String encodeFileToBase64(String filePath) throws IOException {
        Path path = new File(filePath).toPath();
        byte[] fileBytes = Files.readAllBytes(path);
        return Base64.encodeBase64String(fileBytes);
    }

    /**
     * Get the MIME type for a file based on extension.
     */
    public static String getMimeType(String filePath) {
        String lower = filePath.toLowerCase();
        if (lower.endsWith(".pdf")) {
            return "application/pdf";
        } else if (lower.endsWith(".csv")) {
            return "text/csv";
        } else if (lower.endsWith(".txt")) {
            return "text/plain";
        } else if (lower.endsWith(".xlsx")) {
            return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        } else if (lower.endsWith(".xls")) {
            return "application/vnd.ms-excel";
        }
        return "application/octet-stream";
    }

    /**
     * Prepare a file for upload to Claude API.
     */
    public static class FileContent {
        public final String base64Content;
        public final String mimeType;

        public FileContent(String base64Content, String mimeType) {
            this.base64Content = base64Content;
            this.mimeType = mimeType;
        }
    }

    public static FileContent prepareFileContent(String filePath) throws IOException {
        String base64 = encodeFileToBase64(filePath);
        String mimeType = getMimeType(filePath);
        return new FileContent(base64, mimeType);
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java StatementProcessor <path-to-file>");
            System.out.println("Supports: PDF, CSV, TXT, XLSX, XLS");
            return;
        }

        String filePath = args[0];

        try {
            if (filePath.toLowerCase().endsWith(".csv") || filePath.toLowerCase().endsWith(".txt")) {
                System.out.println("Reading file: " + filePath);
                String text = extractTextFromFile(filePath);
                System.out.println("File contents (first 500 chars):");
                System.out.println(text.substring(0, Math.min(500, text.length())));
            }

            FileContent content = prepareFileContent(filePath);
            System.out.println("\nFile prepared for API:");
            System.out.println("MIME Type: " + content.mimeType);
            System.out.println("Base64 length: " + content.base64Content.length());
        } catch (IOException e) {
            System.err.println("Error processing file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

