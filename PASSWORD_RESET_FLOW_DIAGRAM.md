# Password Reset Flow Diagram

## User Journey

```
┌─────────────────┐
│  LoginScreen    │
│  ┌───────────┐  │
│  │  Email    │  │
│  ├───────────┤  │
│  │ Password  │  │
│  ├───────────┤  │
│  │ Sign In   │  │
│  └───────────┘  │
│                 │
│  [Forgot Pass?] │ ◄── Click here to start reset
└────────┬────────┘
         │
         ▼
┌────────────────────────────┐
│ ForgotPasswordScreen       │
│ Step 1: Email Entry        │
│ ══════════════════════     │  ← Progress: 33%
│                            │
│  Enter Your Email          │
│  ┌──────────────────────┐  │
│  │ user@example.com     │  │
│  └──────────────────────┘  │
│                            │
│  [Continue]                │
└────────┬───────────────────┘
         │
         ▼
┌────────────────────────────┐
│ ForgotPasswordScreen       │
│ Step 2: Security Questions │
│ ══════════════════════════ │  ← Progress: 66%
│                            │
│  ╔══════════════════════╗  │
│  ║ Question 1           ║  │
│  ║ What city were you   ║  │
│  ║ born in?             ║  │
│  ║ ┌──────────────────┐ ║  │
│  ║ │ San Francisco    │ ║  │
│  ║ └──────────────────┘ ║  │
│  ╚══════════════════════╝  │
│                            │
│  ╔══════════════════════╗  │
│  ║ Question 2           ║  │
│  ║ What was your first  ║  │
│  ║ pet's name?          ║  │
│  ║ ┌──────────────────┐ ║  │
│  ║ │ Max              │ ║  │
│  ║ └──────────────────┘ ║  │
│  ╚══════════════════════╝  │
│                            │
│  [Back]    [Verify]        │
└────────┬───────────────────┘
         │
         ▼
┌────────────────────────────┐
│ ForgotPasswordScreen       │
│ Step 3: New Password       │
│ ══════════════════════════ │  ← Progress: 100%
│                            │
│  Choose a strong password  │
│  ┌──────────────────────┐  │
│  │ ••••••••             │  │
│  └──────────────────────┘  │
│  New Password              │
│                            │
│  ┌──────────────────────┐  │
│  │ ••••••••             │  │
│  └──────────────────────┘  │
│  Confirm Password          │
│                            │
│  [Back]  [Reset Password]  │
└────────┬───────────────────┘
         │
         ▼
┌─────────────────┐
│  LoginScreen    │
│                 │
│  ✅ Password   │
│  reset         │
│  successfully! │
└─────────────────┘
```

## Signup Flow with Security Questions

```
┌─────────────────────────────┐
│  SignupScreen               │
│  ┌───────────────────────┐  │
│  │  Email                │  │
│  ├───────────────────────┤  │
│  │  Password             │  │
│  ├───────────────────────┤  │
│  │  Confirm Password     │  │
│  └───────────────────────┘  │
│                             │
│  ┌───────────────────────┐  │
│  │ Security Questions  ▼ │  │ ◄── Click to expand
│  │ Required for recovery │  │
│  └───────────────────────┘  │
│                             │
│  [Create Account]           │
└─────────────────────────────┘
         │ (expanded)
         ▼
┌─────────────────────────────┐
│  SignupScreen               │
│  ... (email, passwords)     │
│                             │
│  ┌───────────────────────┐  │
│  │ Security Questions  ▲ │  │ ◄── Expanded
│  │ Required for recovery │  │
│  └───────────────────────┘  │
│                             │
│  Security Question 1 ▼      │
│  ┌───────────────────────┐  │
│  │ What city were you    │  │
│  │ born in?              │  │
│  └───────────────────────┘  │
│  Your Answer                │
│  ┌───────────────────────┐  │
│  │ San Francisco         │  │
│  └───────────────────────┘  │
│                             │
│  Security Question 2 ▼      │
│  ┌───────────────────────┐  │
│  │ What was your first   │  │
│  │ pet's name?           │  │
│  └───────────────────────┘  │
│  Your Answer                │
│  ┌───────────────────────┐  │
│  │ Max                   │  │
│  └───────────────────────┘  │
│                             │
│  [Create Account]           │
└─────────────────────────────┘
```

## Security Architecture

```
┌──────────────────────────────────────────────┐
│           User Input                         │
│  Question: "What city were you born in?"     │
│  Answer: "San Francisco"                     │
└──────────────┬───────────────────────────────┘
               │
               ▼
┌──────────────────────────────────────────────┐
│           Normalization                      │
│  1. Convert to lowercase: "san francisco"    │
│  2. Trim whitespace                          │
└──────────────┬───────────────────────────────┘
               │
               ▼
┌──────────────────────────────────────────────┐
│           Salt Generation                    │
│  SecureRandom().nextBytes(16)                │
│  Base64 encode: "aB3dEf9Gh2=="               │
└──────────────┬───────────────────────────────┘
               │
               ▼
┌──────────────────────────────────────────────┐
│           Hashing (SHA-256)                  │
│  Hash("san francisco" + "aB3dEf9Gh2==")      │
│  Result: "7c3f2a9b8e1d..."                   │
└──────────────┬───────────────────────────────┘
               │
               ▼
┌──────────────────────────────────────────────┐
│           Storage Format                     │
│  "7c3f2a9b8e1d...:aB3dEf9Gh2=="             │
│  ─────┬──────────  ──────┬──────             │
│       │                  │                   │
│    Hash (64 chars)    Salt (24 chars)        │
└──────────────────────────────────────────────┘
```

## Verification Process

```
┌──────────────────────────────────────────────┐
│  User enters answer: "SAN FRANCISCO"         │
└──────────────┬───────────────────────────────┘
               │
               ▼
┌──────────────────────────────────────────────┐
│  Normalize input: "san francisco"            │
└──────────────┬───────────────────────────────┘
               │
               ▼
┌──────────────────────────────────────────────┐
│  Retrieve from database:                     │
│  "7c3f2a9b8e1d...:aB3dEf9Gh2=="             │
└──────────────┬───────────────────────────────┘
               │
               ▼
┌──────────────────────────────────────────────┐
│  Split by ":" to get:                        │
│  storedHash = "7c3f2a9b8e1d..."             │
│  salt = "aB3dEf9Gh2=="                       │
└──────────────┬───────────────────────────────┘
               │
               ▼
┌──────────────────────────────────────────────┐
│  Hash input with stored salt:                │
│  Hash("san francisco" + "aB3dEf9Gh2==")      │
│  inputHash = "7c3f2a9b8e1d..."              │
└──────────────┬───────────────────────────────┘
               │
               ▼
┌──────────────────────────────────────────────┐
│  Compare:                                    │
│  storedHash == inputHash                     │
│  ✅ Match! → Allow password reset            │
└──────────────────────────────────────────────┘
```

## Component Communication

```
┌──────────────────┐
│ ForgotPassword   │
│    Screen        │
└────────┬─────────┘
         │
         │ viewModel.getSecurityQuestions()
         ▼
┌──────────────────┐
│   AuthViewModel  │
└────────┬─────────┘
         │
         │ authRepository.getUserSecurityQuestions()
         ▼
┌──────────────────┐
│  AuthRepository  │
└────────┬─────────┘
         │
         │ userDao.getUserByEmail()
         ▼
┌──────────────────┐
│     UserDao      │
└────────┬─────────┘
         │
         │ Room SQL Query
         ▼
┌──────────────────┐
│   UserEntity     │
│   (Database)     │
└──────────────────┘

     ─────────────────────────
     Data flows back up chain:
     UserEntity → UserDao → 
     Repository → ViewModel → 
     Screen UI
```
