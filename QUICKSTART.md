# Quick Start Guide

A one-shot Personal Finance Analyzer that reads all statement files from your `inputs/` folder, sends them to Claude in a single request, and writes a full financial report to `output/`.

---

## 1. Get Your API Key (1 minute)

1. Visit https://console.anthropic.com
2. Sign up or log in
3. Create an API key (starts with `sk-ant-`)
4. Copy it somewhere safe

## 2. Set the Environment Variable

### Windows PowerShell (current session)

```powershell
$env:ANTHROPIC_API_KEY="sk-ant-your-key-here"

# Verify it is set
echo $env:ANTHROPIC_API_KEY
```

### Windows (permanent, all future sessions)

```powershell
setx ANTHROPIC_API_KEY "sk-ant-your-key-here"
```

Then **close and reopen** your terminal / IDE.

### IntelliJ IDEA Run Configuration

1. `Run` -> `Edit Configurations...`
2. Select your `FinanceCoachApp` configuration
3. In **Environment variables**, add:
   - `ANTHROPIC_API_KEY=sk-ant-your-key-here`
4. Apply and Run

## 3. Drop Your Statement Files Into `inputs/`

Place any `.csv`, `.txt`, or `.pdf` financial statements into:

```
src/main/java/com/personalfinance/inputs/
```

The app will **auto-discover all supported files** in this folder. No paths to enter.

Examples that work:
- `chase_checking_april.pdf`
- `amex_card_statement.pdf`
- `student_loan.csv`

## 4. Run the Application

```powershell
cd "C:\Users\sawan\Downloads\Personal-Finance-Tracker"
mvn exec:java "-Dexec.mainClass=com.personalfinance.app.FinanceCoachApp"
```

You will be asked one optional question:

```
Extra context (optional, press Enter to skip):
```

You can type things like `My monthly take-home pay is $4,200` or just press Enter to skip.

## Troubleshooting

### `ANTHROPIC_API_KEY environment variable is not set`
You must set it in the **same** terminal you are running `mvn` from:
```powershell
$env:ANTHROPIC_API_KEY="sk-ant-your-key"
mvn exec:java "-Dexec.mainClass=com.personalfinance.app.FinanceCoachApp"
```

### `401 ... x-api-key header is required`
The key wasn't picked up by the process. Most common causes:
- Set the key in a different terminal than where you ran the app
- IntelliJ run config is missing the env var (see step 2)
- Used `setx` but didn't restart the terminal/IDE

### `No statement files found in: ...inputs`
- Make sure files are inside `src/main/java/com/personalfinance/inputs/`
- Allowed extensions: `.csv`, `.txt`, `.pdf`

### "Is the app frozen?"
While Claude is processing you'll see a heartbeat every ~5s:
```
  ...still waiting on Claude (5s elapsed)
```
If you see those, the app is alive. Typical analysis takes 30-90 seconds.

---

## What the Report Includes

1. **Account Summary** - all accounts, balances, interest rates, min payments
2. **Income & Cash Flow** - month-by-month
3. **Spending Breakdown** - categorized transactions and recurring subs
4. **Spending Optimizations** - actionable cuts with $ savings estimates
5. **Debt Payoff Plans** - Avalanche and Snowball, side by side
6. **Unified Debt-Free Timeline**
7. **Savings Recommendation**
8. **Consolidation / Balance-Transfer Opportunities**

---

## Project Structure

```
src/main/java/com/personalfinance/
+-- app/
|   +-- FinanceCoachApp.java           - Main entry point (auto-reads inputs/, writes output/)
+-- services/
|   +-- DocumentCoach.java             - Builds combined payload + calls Claude
|   +-- FinanceCoach.java              - Simple single-turn helper
|   +-- StatefulCoach.java             - Multi-turn conversation helper
+-- models/
|   +-- FinanceData.java               - Data structures
+-- utils/
|   +-- StatementProcessor.java        - File reading + PDF text extraction
+-- inputs/                            - DROP STATEMENT FILES HERE
+-- output/                            - GENERATED REPORTS LAND HERE
```

---

## Quick Recap

1. Set API key:
   ```powershell
   $env:ANTHROPIC_API_KEY="sk-ant-your-key"
   ```
2. Drop PDFs/CSVs/TXTs into `src/main/java/com/personalfinance/inputs/`
3. Run:
   ```powershell
   mvn exec:java "-Dexec.mainClass=com.personalfinance.app.FinanceCoachApp"
   ```
4. Read your report from `src/main/java/com/personalfinance/output/`

Happy budgeting!
