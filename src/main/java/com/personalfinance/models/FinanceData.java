package com.personalfinance.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data structures for representing parsed financial information.
 */
public class FinanceData {

    /**
     * Represents a single financial account (checking, credit card, loan, etc.)
     */
    public static class Account {
        public String accountType;
        public String accountName;
        public double currentBalance;
        public double minPayment;
        public Double interestRate;
        public LocalDate lastUpdated;

        public Account(String accountType, String accountName, double currentBalance) {
            this.accountType = accountType;
            this.accountName = accountName;
            this.currentBalance = currentBalance;
            this.lastUpdated = LocalDate.now();
        }

        @Override
        public String toString() {
            return String.format("%s (%s): $%.2f balance, $%.2f min payment, %.2f%% APR",
                    accountName, accountType, currentBalance, minPayment,
                    interestRate != null ? interestRate : 0.0);
        }
    }

    /**
     * Represents a single transaction
     */
    public static class Transaction {
        public LocalDate date;
        public String description;
        public double amount;
        public String category;
        public String accountName;

        public Transaction(LocalDate date, String description, double amount, String category, String accountName) {
            this.date = date;
            this.description = description;
            this.amount = amount;
            this.category = category;
            this.accountName = accountName;
        }

        @Override
        public String toString() {
            return String.format("%s | %s | $%.2f [%s] from %s", date, description, amount, category, accountName);
        }
    }

    /**
     * Represents recurring spending (subscriptions, utilities, etc.)
     */
    public static class RecurringExpense {
        public String name;
        public double monthlyAmount;
        public String category;
        public LocalDate startDate;
        public String accountName;

        public RecurringExpense(String name, double monthlyAmount, String category, String accountName) {
            this.name = name;
            this.monthlyAmount = monthlyAmount;
            this.category = category;
            this.startDate = LocalDate.now();
            this.accountName = accountName;
        }

        @Override
        public String toString() {
            return String.format("%s: $%.2f/month [%s]", name, monthlyAmount, category);
        }
    }

    /**
     * Represents a complete financial snapshot for a user
     */
    public static class FinancialProfile {
        public String userId;
        public double monthlyIncome;
        public List<Account> accounts;
        public List<Transaction> transactions;
        public List<RecurringExpense> recurringExpenses;
        public LocalDate createdAt;

        public FinancialProfile(String userId, double monthlyIncome) {
            this.userId = userId;
            this.monthlyIncome = monthlyIncome;
            this.accounts = new ArrayList<>();
            this.transactions = new ArrayList<>();
            this.recurringExpenses = new ArrayList<>();
            this.createdAt = LocalDate.now();
        }

        public void addAccount(Account account) {
            this.accounts.add(account);
        }

        public void addTransaction(Transaction transaction) {
            this.transactions.add(transaction);
        }

        public void addRecurringExpense(RecurringExpense expense) {
            this.recurringExpenses.add(expense);
        }

        public double getTotalDebt() {
            return accounts.stream()
                    .filter(a -> a.currentBalance > 0)
                    .mapToDouble(a -> a.currentBalance)
                    .sum();
        }

        public double getTotalMonthlyObligations() {
            double minPayments = accounts.stream()
                    .mapToDouble(a -> a.minPayment)
                    .sum();

            double recurringTotal = recurringExpenses.stream()
                    .mapToDouble(e -> e.monthlyAmount)
                    .sum();

            return minPayments + recurringTotal;
        }

        public double getMonthlyBudgetGap() {
            return monthlyIncome - getTotalMonthlyObligations();
        }

        public String getSummary() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== Financial Profile Summary ===\n");
            sb.append(String.format("Monthly Income: $%.2f\n", monthlyIncome));
            sb.append(String.format("Total Debt: $%.2f\n", getTotalDebt()));
            sb.append(String.format("Monthly Obligations: $%.2f\n", getTotalMonthlyObligations()));
            sb.append(String.format("Monthly Budget Gap: $%.2f\n", getMonthlyBudgetGap()));
            sb.append(String.format("\nAccounts (%d):\n", accounts.size()));
            for (Account a : accounts) {
                sb.append("  ").append(a).append("\n");
            }
            sb.append(String.format("\nRecurring Expenses (%d):\n", recurringExpenses.size()));
            for (RecurringExpense e : recurringExpenses) {
                sb.append("  ").append(e).append("\n");
            }
            return sb.toString();
        }
    }

    /**
     * Represents a debt payoff plan
     */
    public static class PayoffPlan {
        public enum Strategy {
            AVALANCHE,
            SNOWBALL
        }

        public Strategy strategy;
        public List<PayoffStep> steps;
        public double totalInterestPaid;
        public int monthsToPayoff;

        public PayoffPlan(Strategy strategy) {
            this.strategy = strategy;
            this.steps = new ArrayList<>();
            this.totalInterestPaid = 0;
            this.monthsToPayoff = 0;
        }

        public void addStep(PayoffStep step) {
            this.steps.add(step);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== ").append(strategy.name()).append(" Payoff Plan ===\n");
            sb.append(String.format("Months to Payoff: %d\n", monthsToPayoff));
            sb.append(String.format("Total Interest Paid: $%.2f\n\n", totalInterestPaid));
            for (PayoffStep step : steps) {
                sb.append(step).append("\n");
            }
            return sb.toString();
        }
    }

    /**
     * Represents a single step in a payoff plan
     */
    public static class PayoffStep {
        public String debtName;
        public double balance;
        public double interestRate;
        public double paymentAmount;
        public int monthsUntilPayoff;
        public double totalInterestOnThisDebt;

        public PayoffStep(String debtName, double balance, double interestRate) {
            this.debtName = debtName;
            this.balance = balance;
            this.interestRate = interestRate;
        }

        @Override
        public String toString() {
            return String.format("%s: $%.2f @ %.2f%% | Pay $%.2f/month | Payoff in %d months | Interest: $%.2f",
                    debtName, balance, interestRate, paymentAmount, monthsUntilPayoff, totalInterestOnThisDebt);
        }
    }
}

