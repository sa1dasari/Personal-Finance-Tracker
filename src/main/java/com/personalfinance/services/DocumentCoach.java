package com.personalfinance.services;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;
import com.personalfinance.utils.StatementProcessor;

import java.io.IOException;
import java.util.List;

/**
 * Utilities for handling document uploads with Claude's Messages API.
 * Extracts text from statements and sends to Claude for analysis.
 */
public class DocumentCoach {

    private static final String SYSTEM_PROMPT = """
        You are a personal finance coach. The user has uploaded one or more financial statements
        (debit/checking, credit cards, personal loans, student loans, home loans, or any other account type).
        Your job is to produce a single, comprehensive, one-shot financial analysis report — do NOT ask
        follow-up questions. Work with whatever data is provided.

        Step-by-step analysis to include in your report:
        (1) ACCOUNT SUMMARY – List every account found: type, current balance, interest rate (if applicable),
            and minimum payment. Present as a table.
        (2) INCOME & CASH FLOW – Identify income deposits and build a month-by-month cash flow summary
            (income vs. total obligations). Present as a table.
        (3) SPENDING BREAKDOWN – Categorize all transactions (groceries, dining, utilities, subscriptions,
            entertainment, transport, etc.). Identify recurring expenses and subscriptions. Present as a table
            with category totals and percentages.
        (4) SPENDING OPTIMIZATIONS – List specific, actionable spending cuts that would free up extra cash.
            Estimate monthly savings per suggestion.
        (5) DEBT PAYOFF PLANS – For every debt/loan, present two strategies side-by-side with projected
            payoff dates and total interest paid:
            • Avalanche Method (highest interest rate first)
            • Snowball Method (smallest balance first)
        (6) UNIFIED DEBT-FREE TIMELINE – A single merged timeline showing when each debt is eliminated under
            each strategy, so the user can see the overall picture.
        (7) SAVINGS RECOMMENDATION – Based on freed-up cash after debt payments, recommend a realistic
            monthly savings target and suggest how to allocate it (emergency fund, investments, etc.).
        (8) CONSOLIDATION OPTIONS – If applicable, flag any debt consolidation or balance-transfer
            opportunities that could reduce interest costs.

        Formatting rules:
        • Use clear section headers.
        • Always present numbers in formatted tables with aligned columns.
        • Be concise, direct, and non-judgmental.
        • Never ask the user for more information — make reasonable assumptions and state them clearly.
        • Never retain data beyond the current session.
        """;

    private final AnthropicClient client;

    public DocumentCoach() {
        this.client = AnthropicOkHttpClient.fromEnv();
    }

    /**
     * Analyze multiple financial statement files together as one combined report.
     *
     * @param filePaths     list of paths to statement files (CSV, TXT, etc.)
     * @param extraContext  optional free-text context the user wants to add (income, notes, etc.)
     * @return comprehensive financial analysis report
     */
    public String analyzeMultipleDocuments(List<String> filePaths, String extraContext) throws IOException {
        String combinedPayload = buildCombinedPayload(filePaths, extraContext);

        MessageCreateParams params = MessageCreateParams.builder()
                .model(Model.CLAUDE_OPUS_4_7)
                .maxTokens(8192L)
                .system(SYSTEM_PROMPT)
                .addUserMessage(combinedPayload)
                .build();

        Message response = client.messages().create(params);

        return response.content().stream()
                .map(b -> b.text().map(t -> t.text()).orElse(""))
                .reduce("", String::concat);
    }

    /**
     * Convenience overload with no extra context.
     */
    public String analyzeMultipleDocuments(List<String> filePaths) throws IOException {
        return analyzeMultipleDocuments(filePaths, null);
    }

    /**
     * Build the exact user payload that would be sent to Claude.
     * Useful for local validation/dry-runs without making an API call.
     */
    public static String buildCombinedPayload(List<String> filePaths, String extraContext) throws IOException {
        StringBuilder combined = new StringBuilder();

        for (int i = 0; i < filePaths.size(); i++) {
            String path = filePaths.get(i).trim();
            String text = StatementProcessor.extractTextFromFile(path);
            if (text.length() > 30000) {
                text = text.substring(0, 30000) + "\n\n[Content truncated due to length...]";
            }
            combined.append(String.format("=== STATEMENT %d: %s ===\n\n", i + 1, path));
            combined.append(text);
            combined.append("\n\n");
        }

        if (extraContext != null && !extraContext.isBlank()) {
            combined.append("=== ADDITIONAL CONTEXT PROVIDED BY USER ===\n\n");
            combined.append(extraContext.trim());
            combined.append("\n\n");
        }

        combined.append(
                "Please analyze ALL of the above statements together and produce the full financial " +
                        "analysis report described in your instructions."
        );

        return combined.toString();
    }

    /**
     * Analyze a single statement file.
     */
    public String analyzeStatement(String filePath) throws IOException {
        return analyzeMultipleDocuments(List.of(filePath), null);
    }

    /**
     * Send extracted document text to Claude along with a specific question.
     * Kept for backward compatibility.
     */
    public String analyzeDocument(String filePath, String question) throws IOException {
        return analyzeMultipleDocuments(List.of(filePath), question);
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java DocumentCoach <file1> [file2 ...] [\"extra context\"]");
            System.out.println("Example: java DocumentCoach checking.csv credit_card.csv");
            return;
        }

        try {
            DocumentCoach coach = new DocumentCoach();
            System.out.println("Analyzing " + args.length + " statement(s)...");
            String analysis = coach.analyzeMultipleDocuments(List.of(args));
            System.out.println("\n" + analysis);
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

