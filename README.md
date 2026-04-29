# Personal Finance Analyzer — Powered by Claude AI

A Java CLI application that integrates with Anthropic's Claude API to produce a **comprehensive, one-shot financial analysis report** from your uploaded statements. Drop your statement files into the `inputs/` folder, run one command, and get a full report.

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

### 2. Set the Environment Variable

**PowerShell (current session):**
```powershell
$env:ANTHROPIC_API_KEY="sk-ant-your-key-here"
```

**Permanent (Windows):**
```powershell
setx ANTHROPIC_API_KEY "sk-ant-your-key-here"
```
Then close and reopen your terminal/IDE.

**IntelliJ IDEA Run Configuration:**
1. `Run` -> `Edit Configurations...`
2. Select your `FinanceCoachApp` configuration
3. In **Environment variables**, add `ANTHROPIC_API_KEY=sk-ant-your-key-here`
4. Apply and Run

**macOS / Linux:**
```bash
export ANTHROPIC_API_KEY='sk-ant-your-key-here'
```
(Add to `~/.bashrc` or `~/.zshrc` to persist.)

### 3. Install Dependencies

```bash
cd Personal-Finance-Tracker
mvn clean install
```

## Folder Layout

```
Personal-Finance-Tracker/
  src/main/java/com/personalfinance/
    inputs/    <-- DROP YOUR STATEMENT FILES HERE  (CSV, TXT, PDF)
    output/    <-- TIMESTAMPED REPORTS LAND HERE
    app/        - FinanceCoachApp (main entry point)
    services/   - DocumentCoach, FinanceCoach, StatefulCoach
    models/     - FinanceData
    utils/      - StatementProcessor (PDF/text extraction)
  pom.xml
```

- **`inputs/`** — drop any number of `.csv`, `.txt`, or `.pdf` bank / credit card / loan statement files here. Both directories are auto-created on first run.
- **`output/`** — each run produces a timestamped file like `report_20260429_143022.txt`.

---

## Usage

### Run the Analyzer

```powershell
mvn exec:java "-Dexec.mainClass=com.personalfinance.app.FinanceCoachApp"
```

The app will:

1. Auto-discover all supported files in `src/main/java/com/personalfinance/inputs/`
2. List them on screen
3. Ask one optional question (extra context — press Enter to skip)
4. Send everything to Claude and print/save the report

## Architecture

### Package Structure

```
com.personalfinance/
+-- app/
|   +-- FinanceCoachApp.java        # Auto-scans inputs/, calls DocumentCoach, writes to output/
+-- services/
|   +-- DocumentCoach.java          # Multi-statement one-shot analysis (core engine)
|   +-- FinanceCoach.java           # Single-turn utility
|   +-- StatefulCoach.java          # Multi-turn conversation helper
+-- models/
|   +-- FinanceData.java            # Data structures
+-- utils/
    +-- StatementProcessor.java     # File reading + PDF text extraction (PDFBox)
```

### Core Classes

1. **FinanceCoachApp** (`app`) — Main CLI
   - Auto-discovers all `.csv`/`.txt`/`.pdf` files in `inputs/`
   - Validates `ANTHROPIC_API_KEY` is set before calling Claude
   - Shows a heartbeat every 5s while Claude responds
   - Saves the final report to `output/report_<timestamp>.txt`

2. **DocumentCoach** (`services`) — Multi-statement analysis engine
   Reads all files, concatenates them into one request, and sends to Claude for a full report.
   Key methods:
   - `analyzeMultipleDocuments(List<String> filePaths, String extraContext)`
   - `buildCombinedPayload(List<String> filePaths, String extraContext)` (no API call; useful for inspection)

3. **FinanceCoach** (`services`) — Simple single-turn utility
   Quick single-question interface with no file handling.

4. **StatefulCoach** (`services`) — Multi-turn conversation helper
   Maintains chat history across calls.

5. **FinanceData** (`models`) — Data structures
   Account, Transaction, RecurringExpense, PayoffPlan models.

6. **StatementProcessor** (`utils`) — File utilities
   Text extraction (UTF-8 read + PDFBox PDF extraction), base64 encoding, MIME type detection.

---

## Privacy & Security

- Never hardcode API keys in source code — always use environment variables
- API keys are read from `ANTHROPIC_API_KEY`, never logged
- No persistent data store — files are read on demand from `inputs/`
- The only artifact written is the report under `output/`

## Further Development

- Add CSV/Excel statement parsing helpers
- Banking API integrations (Plaid, Open Banking)
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
