package com.personalfinance.services;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;
import com.personalfinance.utils.StatementProcessor;

import java.io.IOException;

/**
 * Utilities for handling document uploads with Claude's Messages API.
 * Extracts text from statements and sends to Claude for analysis.
 */
public class DocumentCoach {

    private static final String SYSTEM_PROMPT = """
        You are a personal finance coach. Users upload statements from any combination of accounts:
        debit/checking, credit cards, personal loans, student loans, home loans, or any other loan type.
        Parse and unify all data into a single financial picture. Categorize all transactions and identify
        recurring expenses, subscriptions, and spending patterns. For every debt or loan, extract (or ask for)
        the balance, interest rate, and minimum payment. Then:
        (1) Build a month-by-month cash flow summary showing income vs. total obligations.
        (2) Identify specific spending cuts that free up extra cash.
        (3) Present a debt payoff plan using both the avalanche method (highest interest first) and
            snowball method (smallest balance first), with projected payoff dates and total interest saved.
        (4) Layer in loan payoff schedules alongside credit debt so the user sees a unified timeline
            to becoming debt-free.
        (5) Recommend a realistic monthly savings target after obligations.
        Always present numbers in clear tables. Be concise, non-judgmental, and actionable.
        Never retain data beyond the current session.
        """;

    private final AnthropicClient client;

    public DocumentCoach() {
        this.client = AnthropicOkHttpClient.fromEnv();
    }

    /**
     * Send extracted document text to Claude along with a question.
     */
    public String analyzeDocument(String filePath, String question) throws IOException {
        String documentText = StatementProcessor.extractTextFromFile(filePath);

        if (documentText.length() > 20000) {
            documentText = documentText.substring(0, 20000) + "\n\n[Content truncated...]";
        }

        String fullMessage = String.format(
            "Here's a financial statement:\n\n%s\n\n%s",
            documentText,
            question
        );

        MessageCreateParams params = MessageCreateParams.builder()
                .model(Model.CLAUDE_OPUS_4_7)
                .maxTokens(4096L)
                .system(SYSTEM_PROMPT)
                .addUserMessage(fullMessage)
                .build();

        Message response = client.messages().create(params);

        return response.content().stream()
                .map(b -> b.text().map(t -> t.text()).orElse(""))
                .reduce("", String::concat);
    }

    /**
     * Analyze a statement without any additional question.
     */
    public String analyzeStatement(String filePath) throws IOException {
        return analyzeDocument(filePath,
            "Please analyze this financial statement. Extract all accounts, balances, interest rates, " +
            "and minimum payments. Identify all transactions and recurring expenses. Then provide " +
            "a comprehensive financial analysis with debt payoff strategies.");
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java DocumentCoach <path-to-file> [question]");
            System.out.println("Example: java DocumentCoach statement.csv \"What's my debt-to-income ratio?\"");
            return;
        }

        String filePath = args[0];
        String question = args.length > 1 ? args[1] : null;

        try {
            DocumentCoach coach = new DocumentCoach();
            String analysis;

            if (question != null) {
                System.out.println("Analyzing document with question: " + question);
                analysis = coach.analyzeDocument(filePath, question);
            } else {
                System.out.println("Analyzing statement...");
                analysis = coach.analyzeStatement(filePath);
            }

            System.out.println("\nAnalysis:\n");
            System.out.println(analysis);
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

