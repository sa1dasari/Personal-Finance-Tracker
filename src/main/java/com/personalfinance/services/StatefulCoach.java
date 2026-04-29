package com.personalfinance.services;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;
import com.anthropic.models.messages.MessageParam;

import java.util.ArrayList;
import java.util.List;

/**
 * Stateful Finance Coach that maintains conversation history across multiple turns.
 * Claude is stateless, so we accumulate all messages and send the full history with each request.
 */
public class StatefulCoach {

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
    private final List<MessageParam> history;

    public StatefulCoach() {
        this.client = AnthropicOkHttpClient.fromEnv();
        this.history = new ArrayList<>();
    }

    /**
     * Send a message and maintain conversation history.
     * All previous messages are included with each API call.
     */
    public String ask(String userMessage) {
        history.add(MessageParam.builder()
                .role(MessageParam.Role.USER)
                .content(userMessage)
                .build());

        MessageCreateParams.Builder params = MessageCreateParams.builder()
                .model(Model.CLAUDE_OPUS_4_7)
                .maxTokens(4096L)
                .system(SYSTEM_PROMPT);

        for (MessageParam m : history) {
            params.addMessage(m);
        }

        Message response = client.messages().create(params.build());

        String assistantText = response.content().stream()
                .map(b -> b.text().map(t -> t.text()).orElse(""))
                .reduce("", String::concat);

        history.add(MessageParam.builder()
                .role(MessageParam.Role.ASSISTANT)
                .content(assistantText)
                .build());

        return assistantText;
    }

    /**
     * Clear conversation history.
     * Honors the "never retain data beyond session" privacy principle.
     */
    public void reset() {
        history.clear();
    }

    /**
     * Get the current conversation history size.
     */
    public int getHistorySize() {
        return history.size();
    }

    public static void main(String[] args) {
        StatefulCoach coach = new StatefulCoach();

        String reply1 = coach.ask(
            "I have $4,200/mo income. Credit card A: $3,500 balance, 24% APR, $80 min. " +
            "Card B: $1,200, 19% APR, $35 min. Student loan: $18,000, 6%, $190 min. " +
            "Build me a payoff plan."
        );
        System.out.println("Coach: " + reply1);
        System.out.println("\n---\n");

        String reply2 = coach.ask(
            "What if I could cut $500/month in spending? How would that change my payoff timeline?"
        );
        System.out.println("Coach: " + reply2);
        System.out.println("\n---\n");

        String reply3 = coach.ask(
            "Can you focus on the snowball method for psychological wins?"
        );
        System.out.println("Coach: " + reply3);

        coach.reset();
    }
}

