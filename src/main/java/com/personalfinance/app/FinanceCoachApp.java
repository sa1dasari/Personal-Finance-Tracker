package com.personalfinance.app;

import com.personalfinance.services.DocumentCoach;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class FinanceCoachApp {

    private static final Path PROJECT_ROOT = Paths.get("").toAbsolutePath().normalize();
    private static final Path INPUT_DIR = PROJECT_ROOT.resolve("src").resolve("main").resolve("java")
            .resolve("com").resolve("personalfinance").resolve("inputs");
    private static final Path OUTPUT_DIR = PROJECT_ROOT.resolve("src").resolve("main").resolve("java")
            .resolve("com").resolve("personalfinance").resolve("output");
    private static final DateTimeFormatter REPORT_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public static void main(String[] args) {
        try {
            ensureDirectories();

            List<String> filePaths = collectAllSupportedFiles();
            if (filePaths.isEmpty()) {
                System.out.println("No statement files found in: " + INPUT_DIR);
                System.out.println("Supported formats: .csv, .txt, .pdf");
                return;
            }

            System.out.println("============================================================");
            System.out.println("      Personal Finance Analyzer - Powered by Claude AI");
            System.out.println("============================================================");
            System.out.println("Found " + filePaths.size() + " statement file(s):");
            for (String file : filePaths) {
                System.out.println("  - " + Paths.get(file).getFileName());
            }

            Scanner scanner = new Scanner(System.in);
            System.out.print("\nExtra context (optional, press Enter to skip): ");
            String extraContext = scanner.nextLine().trim();
            if (extraContext.isBlank()) {
                extraContext = null;
            }

            System.out.println("\nAnalyzing " + filePaths.size() + " statement(s)...");
            String apiKey = System.getenv("ANTHROPIC_API_KEY");
            if (apiKey == null || apiKey.isBlank()) {
                System.err.println("ERROR: ANTHROPIC_API_KEY environment variable is not set.");
                System.err.println("Set it (PowerShell): $env:ANTHROPIC_API_KEY=\"sk-ant-...\"");
                return;
            }
            System.out.println("API key detected (length=" + apiKey.length() + ").");
            System.out.println("Sending request to Claude. This usually takes 30-90 seconds...");

            DocumentCoach coach = new DocumentCoach();
            String report = callClaudeWithProgress(coach, filePaths, extraContext);

            System.out.println("\n================ FINANCIAL REPORT ================\n");
            System.out.println(report);

            Path reportPath = saveReport(report);
            System.out.println("\nReport saved to: " + reportPath);
        } catch (IllegalArgumentException | IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static void ensureDirectories() throws IOException {
        Files.createDirectories(INPUT_DIR);
        Files.createDirectories(OUTPUT_DIR);
    }

    private static List<String> collectAllSupportedFiles() throws IOException {
        List<String> files = new ArrayList<>();

        if (!Files.exists(INPUT_DIR)) {
            return files;
        }

        try (var stream = Files.list(INPUT_DIR)) {
            stream.filter(Files::isRegularFile)
                    .map(Path::toString)
                    .filter(path -> isSupportedFile(path))
                    .forEach(files::add);
        }

        return files;
    }

    private static boolean isSupportedFile(String filePath) {
        String lower = filePath.toLowerCase();
        return lower.endsWith(".csv") || lower.endsWith(".txt") || lower.endsWith(".pdf");
    }

    private static Path saveReport(String report) throws IOException {
        String fileName = "report_" + LocalDateTime.now().format(REPORT_TIMESTAMP) + ".txt";
        Path reportPath = OUTPUT_DIR.resolve(fileName);
        Files.writeString(reportPath, report, StandardCharsets.UTF_8);
        return reportPath;
    }

    private static String callClaudeWithProgress(DocumentCoach coach, List<String> filePaths, String extraContext)
            throws IOException {
        ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "claude-call");
            t.setDaemon(true);
            return t;
        });

        long startMs = System.currentTimeMillis();
        Future<String> future = executor.submit(() -> coach.analyzeMultipleDocuments(filePaths, extraContext));

        try {
            while (!future.isDone()) {
                try {
                    String result = future.get(5, TimeUnit.SECONDS);
                    long elapsed = (System.currentTimeMillis() - startMs) / 1000;
                    System.out.println("Claude responded in " + elapsed + "s.");
                    return result;
                } catch (java.util.concurrent.TimeoutException te) {
                    long elapsed = (System.currentTimeMillis() - startMs) / 1000;
                    System.out.println("  ...still waiting on Claude (" + elapsed + "s elapsed)");
                }
            }
            return future.get();
        } catch (Exception e) {
            Throwable cause = (e.getCause() != null) ? e.getCause() : e;
            if (cause instanceof IOException io) {
                throw io;
            }
            if (cause instanceof RuntimeException re) {
                throw re;
            }
            throw new RuntimeException(cause);
        } finally {
            executor.shutdownNow();
        }
    }
}
