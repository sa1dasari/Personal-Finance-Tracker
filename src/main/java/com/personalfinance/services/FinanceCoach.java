package com.personalfinance.services;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;
import com.anthropic.models.messages.ContentBlock;

/**
 * Basic single-turn Finance Coach using Anthropic's Claude API.
 * For interactive multi-turn conversations, use StatefulCoach instead.
 */
public class FinanceCoach {

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

    public FinanceCoach() {
        this.client = AnthropicOkHttpClient.fromEnv();
    }

    /**
     * Send a single message to the finance coach and get a response.
     */
    public String ask(String userMessage) {
        MessageCreateParams params = MessageCreateParams.builder()
                .model(Model.CLAUDE_OPUS_4_7)
                .maxTokens(4096L)
                .system(SYSTEM_PROMPT)
                .addUserMessage(userMessage)
                .build();

        Message response = client.messages().create(params);

        StringBuilder out = new StringBuilder();
        for (ContentBlock block : response.content()) {
            block.text().ifPresent(t -> out.append(t.text()));
        }
        return out.toString();
    }

    public static void main(String[] args) {
        FinanceCoach coach = new FinanceCoach();
        String reply = coach.ask(
            "I have $4,200/mo income. Credit card A: $3,500 balance, 24% APR, $80 min. " +
            "Card B: $1,200, 19% APR, $35 min. Student loan: $18,000, 6%, $190 min. " +
            "Build me a payoff plan."
        );
        System.out.println(reply);
    }
}

