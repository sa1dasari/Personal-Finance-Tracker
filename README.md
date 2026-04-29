# Personal Finance Analyzer — Powered by Claude AI

A Java CLI application that integrates with Anthropic's Claude API to produce a **comprehensive, one-shot financial analysis report** from your uploaded statements. No chat, no follow-up questions — just upload your files and get a full report.

## What the Report Covers

1. **Account & Balance Summary** — every account, balance, interest rate, and minimum payment  
2. **Income & Cash Flow** — month-by-month income vs. total obligations  
3. **Spending Breakdown** — transactions categorized (groceries, dining, subscriptions, transport, etc.)  
4. **Actionable Spending Cuts** — specific recommendations with estimated monthly savings  
5. **Debt Payoff Plans** — Avalanche (highest-interest-first) *and* Snowball (smallest-balance-first) side by side, with projected payoff dates and total interest saved  
6. **Unified Debt-Free Timeline** — one merged timeline across all debts  
7. **Debt Consolidation Opportunities** — balance-transfer or consolidation flags where applicable  
8. **Monthly Savings Recommendation** — realistic target with allocation suggestions

## Setup

### 1. Get an API Key

1. Go to https://console.anthropic.com
2. Sign up for a free account if needed
3. Create an API key
4. Copy your key (starts with `sk-ant-`)

### 2. Set Environment Variable

**PowerShell (Windows):**
```powershell
$env:ANTHROPIC_API_KEY='sk-ant-your-key-here'
```

**Git Bash / Standard Shell:**
```bash
export ANTHROPIC_API_KEY='sk-ant-your-key-here'
```

**Permanent (add to your system environment):**
- Windows: System Properties -> Environment Variables -> New User Variable
- Linux/Mac: Add to `~/.bashrc` or `~/.zshrc`

### 3. Install Dependencies

```bash
cd Personal-Finance-Tracker
mvn clean install
```

## Folder Layout

```
Personal-Finance-Tracker/
  input/    <-- PUT YOUR STATEMENT FILES HERE  (CSV, TXT)
  output/   <-- REPORTS ARE SAVED HERE automatically
  src/
  pom.xml
  ...
```

- **`input/`** — drop any number of `.csv` or `.txt` bank / credit card / loan statement files here before running the app.  
- **`output/`** — each run produces a timestamped file like `report_20260429_143022.txt` containing the full analysis report.

Both folders are created automatically the first time you run the app if they don't exist yet.

---

## Usage

### Run the Analyzer (recommended)

```powershell
mvn clean compile exec:java -Dexec.mainClass="com.personalfinance.app.FinanceCoachApp"
```

The app will prompt you interactively:

```
============================================================
         Personal Finance Analyzer — Powered by Claude AI
============================================================

  Statement 1 path (or blank to finish): C:\Users\you\Downloads\checking.csv
  ✓  Added: checking.csv
  Statement 2 path (or blank to finish): C:\Users\you\Downloads\credit_card.csv
  ✓  Added: credit_card.csv
  Statement 3 path (or blank to finish):

  Extra context: Monthly take-home pay is $4,200

  Processing 2 statement(s) — this may take a moment...
  [Full report printed here]
```

### Command-line shortcut (multiple files at once)

```powershell
mvn compile exec:java -Dexec.mainClass="com.personalfinance.services.DocumentCoach" `
  -Dexec.args="checking.csv credit_card.csv loan.csv"
```

### From Java code

```java
import com.personalfinance.services.DocumentCoach;
import java.util.List;

DocumentCoach coach = new DocumentCoach();

// Analyze multiple statements in one shot
String report = coach.analyzeMultipleDocuments(
    List.of("checking.csv", "credit_card.csv", "loan.csv"),
    "My monthly take-home pay is $4,200."  // optional extra context
);
System.out.println(report);
```

## Architecture

### Package Structure

```
com.personalfinance/
+-- app/
|   +-- FinanceCoachApp.java         # Interactive CLI (main entry point)
+-- services/
|   +-- DocumentCoach.java           # Multi-statement one-shot analysis (core engine)
|   +-- FinanceCoach.java            # Single-turn utility
+-- models/
|   +-- FinanceData.java             # Data structures
+-- utils/
    +-- StatementProcessor.java      # File reading utilities
```

### Core Classes

1. **FinanceCoachApp** (`app`) — Interactive CLI  
   Prompts for file paths + optional context, then calls DocumentCoach and prints the report.

2. **DocumentCoach** (`services`) — Multi-statement analysis engine  
   Reads all files, concatenates them into one request, and sends to Claude for a full report.  
   Key method: `analyzeMultipleDocuments(List<String> filePaths, String extraContext)`

3. **FinanceCoach** (`services`) — Simple single-turn utility  
   Quick single-question interface with no file handling.

4. **FinanceData** (`models`) — Data structures  
   Account, Transaction, RecurringExpense, PayoffPlan models.

5. **StatementProcessor** (`utils`) — File utilities  
   Text extraction and base64 encoding, MIME type detection.

## Example Workflows

### Quick single-file analysis
```java
DocumentCoach coach = new DocumentCoach();
System.out.println(coach.analyzeStatement("bank_statement.csv"));
```

### Multiple files with extra context
```java
DocumentCoach coach = new DocumentCoach();
String report = coach.analyzeMultipleDocuments(
    List.of("checking.csv", "visa_card.csv", "student_loan.csv"),
    "Monthly take-home is $4,200. The student loan rate is 6.5%."
);
System.out.println(report);
```

## Dependency Details

- **anthropic-java:2.26.0** - Claude API SDK
- **commons-codec:1.15** - Base64 encoding
- **gson:2.10.1** - JSON processing
- **slf4j** - Logging framework

## System Prompt

The finance coach uses this core system prompt:

```
You are a personal finance coach...
Parse and unify all data into a single financial picture.
Categorize all transactions and identify recurring expenses.
For every debt or loan, extract the balance, interest rate, and minimum payment.
Then:
(1) Build month-by-month cash flow summary
(2) Identify spending cuts
(3) Present avalanche & snowball payoff plans
(4) Layer in loan schedules
(5) Recommend monthly savings target
Always present numbers in clear tables.
Never retain data beyond the current session.
```

## Privacy & Security

**Important:**
- Never hardcode API keys in source code
- Always use environment variables
- API keys are never logged or stored
- Conversation history is cleared on session end
- No data is persisted beyond the current conversation

## Troubleshooting

### "ANTHROPIC_API_KEY not set"
```powershell
$env:ANTHROPIC_API_KEY='sk-ant-...'
mvn clean compile exec:java ...
```

### "API Rate Limited"
- Standard free tier has rate limits
- Wait before retrying or upgrade account

### "File not processing"
- Verify the file path is correct and accessible
- Use supported text-based statement files such as CSV or TXT

### Maven Build Issues
```bash
mvn clean install -U  # Force update dependencies
```

## Building for Production

Create an executable JAR:
```bash
mvn clean package
java -jar target/personal-finance-tracker-1.0.0-shaded.jar
```

## Further Development

- Add CSV/Excel statement parsing
- Implement banking API integrations (Plaid, Open Banking)
- Web UI with Spring Boot
- Database persistence (SQLite, PostgreSQL)
- Automated statement imports
- Email alerts for debt milestones
- Savings goal tracking

## License

MIT License - See LICENSE file for details

## Support

- Anthropic Docs: https://platform.claude.com/docs/en/intro
- Java SDK: https://github.com/anthropics/anthropic-sdk-java
- API Status: https://status.anthropic.com

---

Built with Claude API by Anthropic

