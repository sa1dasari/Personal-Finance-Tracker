# Quick Start Guide

## 1. Get Your API Key (1 minute)

1. Visit https://console.anthropic.com
2. Sign up or log in
3. Create an API key (starts with `sk-ant-`)
4. Copy it somewhere safe

## 2. Set Environment Variable (Windows PowerShell)

```powershell
# Temporary (current session only)
$env:ANTHROPIC_API_KEY='sk-ant-your-key-here'

# Verify it's set
echo $env:ANTHROPIC_API_KEY
```

Or set it permanently:
1. Press `Win + X`
2. Type `sysdm.cpl` and press Enter
3. Click "Environment Variables" button
4. New User Variable:
   - Name: `ANTHROPIC_API_KEY`
   - Value: `sk-ant-your-key-here`
5. OK, restart PowerShell

## 3. Run the Application

```powershell
cd "C:\Users\sawan\Downloads\Personal-Finance-Tracker"

# Interactive chat (recommended)
mvn clean compile exec:java -Dexec.mainClass="com.personalfinance.app.FinanceCoachApp"
```

## 4. Use the Finance Coach

Once started, try these commands:

```
> ask I have $4,200/month income, $3,500 credit card debt at 24% APR, and $1,200 at 19% APR
> ask What's my debt-to-income ratio?
> ask How long to pay off if I cut spending by $500/month?
> ask Show me the snowball method for my debts
> exit
```

## Example Session

```
------------------------------------------------------------
      Personal Finance Coach - Powered by Claude AI
------------------------------------------------------------

Commands:
  'ask <question>'  - Ask the finance coach a question
  'upload <path>'   - Upload a financial statement (CSV/TXT)
  'reset'           - Clear conversation history
  'exit'            - Exit the application

> ask I have $4,200/month income and need a debt payoff plan
Coach is analyzing...

Coach: Based on your $4,200 monthly income, here's your financial picture...
[Full analysis with tables and recommendations]

> ask What if I could save an extra $300/month?
Coach is analyzing...

Coach: With an additional $300/month, your payoff timeline improves significantly...
```

## Troubleshooting

### "API key not set"
```powershell
$env:ANTHROPIC_API_KEY='sk-ant-your-key'
mvn clean compile exec:java -Dexec.mainClass="com.personalfinance.app.FinanceCoachApp"
```

### "Maven not found"
Download from https://maven.apache.org/download.cgi and add bin folder to PATH

### "Java not found"
Download Java 15+ from https://www.oracle.com/java/technologies/downloads/

## What You Can Do

- Ask questions about debt payoff strategy  
- Get budget recommendations  
- Analyze spending patterns  
- Compare avalanche vs snowball methods  
- Calculate payoff timelines  
- Get savings targets  

## File Structure

```
src/main/java/com/personalfinance/
+-- app/
|   +-- FinanceCoachApp.java           - Interactive CLI (Main)
+-- services/
|   +-- FinanceCoach.java              - Basic single-turn chat
|   +-- StatefulCoach.java             - Multi-turn conversation
|   +-- DocumentCoach.java             - Analyze statement files
+-- models/
|   +-- FinanceData.java               - Data structures
+-- utils/
    +-- StatementProcessor.java        - File handling utilities
```

## Next Steps

1. Set API key
2. Run `mvn clean install`
3. Launch `FinanceCoachApp`
4. Start asking questions

## Learn More

- **README.md** - Full documentation
- **Anthropic Docs** - https://platform.anthropic.com/docs/

**Ready?** Run this command and start improving your finances:

```powershell
mvn clean compile exec:java -Dexec.mainClass="com.personalfinance.app.FinanceCoachApp"
```

---

Happy budgeting!

