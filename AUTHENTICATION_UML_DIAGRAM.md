# 🔐 MoodFood Authentication System - UML Diagram

## 📊 **Class Diagram**

```
┌─────────────────────────────────────────────────────────────────┐
│                        MainActivity                            │
├─────────────────────────────────────────────────────────────────┤
│ - authRepo: AuthRepository                                     │
│ - settingsRepo: SettingsRepository                             │
│ + onCreate()                                                   │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                      AppNavHost                               │
├─────────────────────────────────────────────────────────────────┤
│ - navController: NavController                                 │
│ + onAuthSuccess()                                              │
│ + onOnboardingComplete()                                       │
│ + onSignOut()                                                  │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                    LoginScreen                                 │
├─────────────────────────────────────────────────────────────────┤
│ - viewModel: AuthViewModel                                     │
│ - email: String                                                │
│ - password: String                                             │
│ + onSignInSuccess()                                            │
│ + onNavigateToSignup()                                         │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                    SignupScreen                                │
├─────────────────────────────────────────────────────────────────┤
│ - viewModel: AuthViewModel                                     │
│ - email: String                                                │
│ - password: String                                             │
│ - confirmPassword: String                                      │
│ + onSignUpSuccess()                                            │
│ + onNavigateToLogin()                                          │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                    AuthViewModel                               │
├─────────────────────────────────────────────────────────────────┤
│ - authRepository: AuthRepository                               │
│ - _uiState: MutableStateFlow<AuthUiState>                      │
│ + signIn(email: String, password: String)                      │
│ + signUp(email: String, password: String, confirmPassword: String)│
│ + signOut()                                                    │
│ + clearError()                                                 │
│ + clearSuccess()                                               │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                   AuthRepository                               │
├─────────────────────────────────────────────────────────────────┤
│ - context: Context                                             │
│ - userDao: UserDao                                             │
│ + signUp(email: String, password: String): Result<Unit>        │
│ + signIn(email: String, password: String): Result<User>        │
│ + signOut()                                                    │
│ + getCurrentUser(): Flow<User?>                                │
│ + isAuthenticated(): Flow<Boolean>                             │
│ - hashPassword(password: String): String                       │
│ - isValidEmail(email: String): Boolean                         │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                      UserDao                                   │
├─────────────────────────────────────────────────────────────────┤
│ + getUserByEmail(email: String): UserEntity?                   │
│ + insertUser(user: UserEntity)                                 │
│ + updateUser(user: UserEntity)                                 │
│ + getUserById(userId: String): UserEntity?                     │
│ + getCurrentUser(): Flow<UserEntity?>                          │
│ + deleteUser(userId: String)                                   │
│ + deleteAllUsers()                                             │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                    UserEntity                                  │
├─────────────────────────────────────────────────────────────────┤
│ - id: String (Primary Key)                                     │
│ - email: String                                                │
│ - passwordHash: String                                         │
│ - createdAt: Long                                              │
│ - lastLoginAt: Long                                            │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                      User                                      │
├─────────────────────────────────────────────────────────────────┤
│ - id: String                                                   │
│ - email: String                                                │
│ - createdAt: Long                                              │
│ - lastLoginAt: Long                                            │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                   AuthUiState                                  │
├─────────────────────────────────────────────────────────────────┤
│ - isLoading: Boolean                                           │
│ - errorMessage: String?                                        │
│ - isSuccess: Boolean                                           │
└─────────────────────────────────────────────────────────────────┘
```

## 🔄 **Sequence Diagram - Sign Up Flow**

```
User    LoginScreen    AuthViewModel    AuthRepository    UserDao    Database
 │           │              │                │              │           │
 │           │              │                │              │           │
 │───Enter Details─────────▶│                │              │           │
 │           │              │                │              │           │
 │           │───signUp()──▶│                │              │           │
 │           │              │                │              │           │
 │           │              │───signUp()────▶│              │           │
 │           │              │                │              │           │
 │           │              │                │───getUserByEmail()─────▶│
 │           │              │                │              │           │
 │           │              │                │◀──null───────────────────│
 │           │              │                │              │           │
 │           │              │                │───hashPassword()────────▶│
 │           │              │                │              │           │
 │           │              │                │───insertUser()──────────▶│
 │           │              │                │              │           │
 │           │              │                │              │───INSERT─▶│
 │           │              │                │              │           │
 │           │              │                │◀──Success─────────────────│
 │           │              │                │              │           │
 │           │              │                │───saveSession()──────────▶│
 │           │              │                │              │           │
 │           │              │◀──Success──────────────────────────────────│
 │           │              │                │              │           │
 │           │◀──Navigate to Onboarding──────────────────────────────────│
 │           │              │                │              │           │
```

## 🔄 **Sequence Diagram - Sign In Flow**

```
User    LoginScreen    AuthViewModel    AuthRepository    UserDao    Database
 │           │              │                │              │           │
 │           │              │                │              │           │
 │───Enter Credentials────▶│                │              │           │
 │           │              │                │              │           │
 │           │───signIn()──▶│                │              │           │
 │           │              │                │              │           │
 │           │              │───signIn()────▶│              │           │
 │           │              │                │              │           │
 │           │              │                │───getUserByEmail()─────▶│
 │           │              │                │              │           │
 │           │              │                │◀──UserEntity─────────────│
 │           │              │                │              │           │
 │           │              │                │───hashPassword()────────▶│
 │           │              │                │              │           │
 │           │              │                │───verifyPassword()──────▶│
 │           │              │                │              │           │
 │           │              │                │───updateUser()──────────▶│
 │           │              │                │              │           │
 │           │              │                │              │───UPDATE─▶│
 │           │              │                │              │           │
 │           │              │                │◀──Success─────────────────│
 │           │              │                │              │           │
 │           │              │                │───saveSession()──────────▶│
 │           │              │                │              │           │
 │           │              │◀──User Object─────────────────────────────│
 │           │              │                │              │           │
 │           │◀──Navigate to Onboarding──────────────────────────────────│
 │           │              │                │              │           │
```

## 🔄 **Sequence Diagram - Logout Flow**

```
User    ProfileScreen    AuthViewModel    AuthRepository    DataStore
 │           │              │                │                │
 │           │              │                │                │
 │───Tap Logout───────────▶│                │                │
 │           │              │                │                │
 │           │───Show Dialog──────────────────────────────────▶│
 │           │              │                │                │
 │───Confirm Logout───────▶│                │                │
 │           │              │                │                │
 │           │───signOut()──▶│                │                │
 │           │              │                │                │
 │           │              │───signOut()────▶│                │
 │           │              │                │                │
 │           │              │                │───clearSession()─▶│
 │           │              │                │                │
 │           │              │                │◀──Success─────────│
 │           │              │                │                │
 │           │              │◀──Complete─────────────────────────│
 │           │              │                │                │
 │           │◀──Navigate to Login──────────────────────────────│
 │           │              │                │                │
```

## 🏗️ **Component Architecture**

```
┌─────────────────────────────────────────────────────────────────┐
│                        UI Layer                                │
├─────────────────────────────────────────────────────────────────┤
│  LoginScreen    SignupScreen    ProfileScreen                  │
│      │              │               │                          │
│      └──────────────┼───────────────┘                          │
│                     │                                          │
│              AuthViewModel                                      │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Business Layer                              │
├─────────────────────────────────────────────────────────────────┤
│                AuthRepository                                  │
│  - Password Hashing                                            │
│  - Input Validation                                            │
│  - Session Management                                          │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                     Data Layer                                 │
├─────────────────────────────────────────────────────────────────┤
│  UserDao ────────┐    DataStore ────────┐                     │
│      │           │         │             │                     │
│      ▼           │         ▼             │                     │
│  Room Database   │    Preferences        │                     │
│  (UserEntity)    │    (Session)          │                     │
└─────────────────────────────────────────────────────────────────┘
```

## 🔐 **Security Flow**

```
User Input → Validation → Hashing → Storage → Verification
     │           │           │          │           │
     ▼           ▼           ▼          ▼           ▼
  Email/Pass  Format     SHA-256    Database   Compare
   Fields     Check      Hash       Storage    Hashes
```

## 📱 **Navigation Flow**

```
App Launch
    │
    ▼
Check Authentication
    │
    ├─ Not Authenticated → Login Screen
    │                        │
    │                        ├─ Sign In → Onboarding → Home
    │                        │
    │                        └─ Sign Up → Onboarding → Home
    │
    └─ Authenticated → Check Onboarding
                          │
                          ├─ Not Done → Onboarding → Home
                          │
                          └─ Done → Home Screen
                                      │
                                      └─ Profile → Logout → Login
```

This UML diagram shows the complete architecture and flow of the authentication system we implemented! 🎉
