# Personal Finance Tracker - Finance Coach Powered by Claude AI

A Java application that integrates with Anthropic's Claude API to provide intelligent personal finance coaching. Upload financial statements (checking, credit cards, loans) and get categorized spending analysis, debt payoff strategies, and budget recommendations.

## Features

- **Multi-turn conversations** - Ask follow-up questions about your finances  
- **Document analysis** - Upload statements for automatic analysis  
- **Debt payoff planning** - Both avalanche (highest interest) and snowball (smallest balance) methods  
- **Cash flow analysis** - Month-by-month income vs. obligations  
- **Spending categorization** - Automatic transaction categorization  
- **Recurring expense detection** - Identify subscriptions and regular payments  
- **Privacy-first** - No data retained beyond the current session  
- **Interactive CLI** - Easy command-line interface

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

