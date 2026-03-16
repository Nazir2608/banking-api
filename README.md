# 🏦 Banking API

A production-ready RESTful Banking API built with **Spring Boot 4**, **MySQL**, **Redis**, and **JWT** authentication. Fully containerised with Docker and deployable via GitHub Actions CI/CD.

---

## 📋 Table of Contents

- [About the Project](#about-the-project)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Database Schema](#database-schema)
- [API Reference](#api-reference)
- [Application Flow](#application-flow)
- [Architecture Diagram](#architecture-diagram)
- [Getting Started](#getting-started)
- [CI/CD Pipeline](#cicd-pipeline)
- [Environment Variables](#environment-variables)

---

## About the Project

Banking API simulates a core banking system with the following capabilities:

- **Authentication** — JWT-based register, login, token refresh and logout with Redis blacklisting
- **Account Management** — Open savings or current accounts, freeze, close
- **Transactions** — Deposit and withdraw with full ledger history
- **Transfers** — Atomic fund transfers between accounts with reference numbers
- **Loans** — Apply, approve/reject, disburse and repay with EMI calculation
- **Reports** — Account statements with date-range filtering

All balance-modifying operations use **pessimistic locking** (`SELECT ... FOR UPDATE`) to prevent race conditions under concurrent load.

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 21 |
| Framework | Spring Boot 4.0.3 |
| Security | Spring Security 7 + JWT (jjwt 0.12.5) |
| Persistence | Spring Data JPA + Hibernate 7 |
| Database | MySQL 9.5 |
| Migrations | Flyway 11 |
| Cache / Token Store | Redis 7 |
| API Docs | SpringDoc OpenAPI 2.3 (Swagger UI) |
| Build Tool | Maven 3.9 |
| Containerisation | Docker + Docker Compose |
| CI/CD | GitHub Actions + Qodana |
| Testing | JUnit 5 + Mockito + Testcontainers |

---

## Project Structure

```
banking-api/
├── .github/
│   └── workflows/
│       └── ci.yml                  # CI/CD pipeline
├── src/
│   └── main/
│       ├── java/com/nazir/banking/
│       │   ├── BankingApiApplication.java
│       │   ├── config/
│       │   │   ├── SecurityConfig.java
│       │   │   └── OpenApiConfig.java
│       │   ├── common/
│       │   │   ├── dto/            # ApiResponse, PagedResponse
│       │   │   └── exception/      # GlobalExceptionHandler, custom exceptions
│       │   ├── auth/               # JWT auth — register, login, refresh, logout
│       │   ├── user/               # Profile management, admin user control
│       │   ├── account/            # Open/close/freeze accounts
│       │   ├── transaction/        # Deposit, withdraw, history
│       │   ├── transfer/           # Atomic fund transfers
│       │   ├── loan/               # Apply, approve, reject, repay
│       │   └── report/             # Account statements
│       └── resources/
│           ├── application.yml
│           ├── application-dev.yml
│           ├── application-prod.yml
│           └── db/migration/
│               ├── V1__create_users_table.sql
│               ├── V2__seed_admin_user.sql
│               ├── V3__create_accounts_table.sql
│               ├── V4__create_transactions_table.sql
│               ├── V5__create_transfers_table.sql
│               └── V6__create_loans_table.sql
├── Dockerfile
├── docker-compose.yml
└── pom.xml
```

---

## Database Schema

### Entity Relationship Diagram

```
┌──────────────────────────────────────────────────────────────────┐
│                            USERS                                  │
│  id (PK, CHAR 36)  first_name  last_name  email (UQ)             │
│  password  phone (UQ)  role (CUSTOMER|ADMIN)  is_active          │
│  created_at  updated_at                                           │
└───────────────────────────┬──────────────────────────────────────┘
                            │ 1
                            │
              ┌─────────────┼──────────────────┐
              │ N           │ N                │ N
              ▼             ▼                  ▼
┌─────────────────┐  ┌──────────────────┐  ┌──────────────────────┐
│    ACCOUNTS     │  │      LOANS       │  │                      │
│  id (PK)        │  │  id (PK)         │  │                      │
│  account_number │  │  user_id (FK)    │  │                      │
│  account_type   │  │  account_id (FK) │  │                      │
│  status         │  │  amount          │  │                      │
│  balance        │  │  interest_rate   │  │                      │
│  user_id (FK)   │  │  term_months     │  │                      │
│  created_at     │  │  emi_amount      │  │                      │
│  updated_at     │  │  outstanding     │  │                      │
└────────┬────────┘  │  total_repaid    │  │                      │
         │           │  status          │  │                      │
         │ 1         │  approved_by     │  │                      │
         │           │  approved_at     │  │                      │
   ┌─────┴────┐      └──────────────────┘  └──────────────────────┘
   │          │
   │ N        │ N (from/to)
   ▼          ▼
┌──────────────────────┐     ┌─────────────────────────────────┐
│     TRANSACTIONS     │     │           TRANSFERS             │
│  id (PK)             │     │  id (PK)                        │
│  account_id (FK)     │     │  reference_number (UQ)          │
│  type (DEP|WITH)     │     │  from_account_id (FK)           │
│  amount              │     │  to_account_id (FK)             │
│  balance_before      │     │  amount                         │
│  balance_after       │     │  status (PENDING|COMPLETED|     │
│  description         │     │          FAILED)                │
│  created_at          │     │  description                    │
└──────────────────────┘     │  created_at                     │
                             └─────────────────────────────────┘
```

### Table Summary

| Table | Purpose | Key Constraints |
|-------|---------|----------------|
| `users` | All system users (customers + admins) | email UNIQUE, phone UNIQUE |
| `accounts` | Bank accounts per user | account_number UNIQUE, FK → users |
| `transactions` | Individual debit/credit ledger entries | FK → accounts |
| `transfers` | Fund transfers between accounts | reference_number UNIQUE, 2x FK → accounts |
| `loans` | Loan applications and repayment tracking | FK → users, FK → accounts |

---

## API Reference

Base URL: `http://localhost:8080/api`

Swagger UI: `http://localhost:8080/api/swagger-ui.html`

### Authentication

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/v1/auth/register` | Public | Register new customer account |
| `POST` | `/v1/auth/login` | Public | Login and receive access + refresh tokens |
| `POST` | `/v1/auth/refresh-token` | Public | Exchange refresh token for new access token |
| `POST` | `/v1/auth/logout` | Bearer | Blacklist tokens and invalidate session |

### Users

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `GET` | `/v1/users/me` | Bearer | Get current user profile |
| `PUT` | `/v1/users/me` | Bearer | Update name and phone |
| `PUT` | `/v1/users/me/password` | Bearer | Change password |
| `GET` | `/v1/admin/users` | Admin | List all users (paginated, filterable by active status) |
| `PUT` | `/v1/admin/users/{id}/status` | Admin | Enable or disable a user account |

### Accounts

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/v1/accounts` | Bearer | Open a new SAVINGS or CURRENT account |
| `GET` | `/v1/accounts` | Bearer | List all my accounts |
| `GET` | `/v1/accounts/{id}` | Bearer | Get account details |
| `GET` | `/v1/accounts/{id}/balance` | Bearer | Get current balance |
| `PUT` | `/v1/admin/accounts/{id}/status` | Admin | Set account status (ACTIVE / FROZEN / CLOSED) |
| `DELETE` | `/v1/accounts/{id}` | Bearer | Close account (balance must be zero) |

### Transactions

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/v1/transactions/deposit` | Bearer | Deposit funds into account |
| `POST` | `/v1/transactions/withdraw` | Bearer | Withdraw funds from account |
| `GET` | `/v1/transactions` | Bearer | Get all my transactions (paginated) |
| `GET` | `/v1/transactions/{id}` | Bearer | Get transaction by ID |
| `GET` | `/v1/accounts/{id}/transactions` | Bearer | Get transactions for a specific account |

### Transfers

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/v1/transfers` | Bearer | Transfer funds to another account by account number |
| `GET` | `/v1/transfers` | Bearer | Get all my transfer history (paginated) |
| `GET` | `/v1/transfers/{reference}` | Bearer | Get transfer by reference number |

### Loans

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/v1/loans/apply` | Bearer | Submit a loan application |
| `GET` | `/v1/loans` | Bearer | Get all my loans (paginated) |
| `GET` | `/v1/loans/{id}` | Bearer | Get loan details |
| `POST` | `/v1/loans/{id}/repay` | Bearer | Make a loan repayment |
| `PUT` | `/v1/admin/loans/{id}/approve` | Admin | Approve loan and disburse funds |
| `PUT` | `/v1/admin/loans/{id}/reject` | Admin | Reject loan application |
| `GET` | `/v1/admin/loans` | Admin | List all loans with optional status filter |

### Reports

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `GET` | `/v1/reports/statement` | Bearer | Account statement for a date range |
| `GET` | `/v1/admin/reports/summary` | Admin | Platform-wide transaction summary |

---

## Application Flow

### Auth Flow

```
Client                    API                       Redis                  MySQL
  │                        │                          │                      │
  │── POST /auth/register ─►│                          │                      │
  │                        │── check email/phone ─────────────────────────► │
  │                        │◄─ not exists ────────────────────────────────── │
  │                        │── save user ──────────────────────────────────► │
  │                        │── store refresh token ──► │                      │
  │◄─ {accessToken, ────── │                          │                      │
  │    refreshToken}        │                          │                      │
  │                        │                          │                      │
  │── POST /auth/logout ──► │                          │                      │
  │   Bearer: <token>       │── blacklist:token ──────► │ (TTL = 15min)       │
  │                        │── delete refresh:userId ─► │                     │
  │◄─ 200 OK ─────────────── │                          │                      │
```

### Transfer Flow

```
Client                    API                        MySQL
  │                        │                           │
  │── POST /v1/transfers ─► │                           │
  │  { fromAccountId,       │                           │
  │    toAccountNumber,     │── SELECT ... FOR UPDATE ─► │ (lock from account)
  │    amount }             │◄─ fromAccount locked ───── │
  │                        │── SELECT toAccount ───────► │
  │                        │◄─ toAccount ──────────────── │
  │                        │                           │
  │                        │── validate both ACTIVE     │
  │                        │── validate sufficient balance
  │                        │                           │
  │                        │── UPDATE from: balance - amount ──► │
  │                        │── UPDATE to:   balance + amount ───► │
  │                        │── INSERT transaction (WITHDRAWAL) ──► │
  │                        │── INSERT transaction (DEPOSIT) ─────► │
  │                        │── INSERT transfer record ───────────► │
  │                        │── COMMIT ─────────────────────────► │
  │◄─ TransferResponse ─── │                           │
  │   {referenceNumber}     │                           │
```

### Loan Lifecycle

```
Customer applies ──► PENDING ──► Admin approves ──► ACTIVE ──► Customer repays ──► CLOSED
                         │
                         └──► Admin rejects ──► REJECTED

On APPROVE: funds disbursed to customer's account via DEPOSIT transaction
On REPAY:   funds deducted from customer's account via WITHDRAWAL transaction
            outstanding reduced; if outstanding ≤ 0 → status set to CLOSED
```

---

## Architecture Diagram

```
                        ┌─────────────────────────────────────────┐
                        │              CLIENT                      │
                        │  (Postman / Browser / Mobile App)        │
                        └────────────────┬────────────────────────┘
                                         │ HTTPS
                                         ▼
                        ┌─────────────────────────────────────────┐
                        │         Spring Boot Application          │
                        │                                          │
                        │  ┌────────────┐   ┌──────────────────┐  │
                        │  │ JWT Auth   │   │  SecurityConfig  │  │
                        │  │ Filter     │──►│  (STATELESS)     │  │
                        │  └────────────┘   └──────────────────┘  │
                        │                                          │
                        │  ┌──────────────────────────────────┐   │
                        │  │         REST Controllers          │   │
                        │  │  Auth | User | Account | Txn      │   │
                        │  │  Transfer | Loan | Report         │   │
                        │  └──────────────┬───────────────────┘   │
                        │                 │                        │
                        │  ┌──────────────▼───────────────────┐   │
                        │  │           Services                │   │
                        │  │  Business logic + validation      │   │
                        │  │  @Transactional boundaries        │   │
                        │  └───────┬──────────────┬───────────┘   │
                        │          │              │               │
                        │  ┌───────▼──────┐  ┌───▼────────────┐  │
                        │  │ JPA Repos    │  │ StringRedis    │  │
                        │  │ (Hibernate)  │  │ Template       │  │
                        │  └───────┬──────┘  └───┬────────────┘  │
                        └──────────┼─────────────┼───────────────┘
                                   │             │
                    ┌──────────────▼──┐    ┌─────▼──────────┐
                    │   MySQL 9.5      │    │   Redis 7       │
                    │                 │    │                 │
                    │  users          │    │  blacklist:*    │
                    │  accounts       │    │  (JWT tokens)   │
                    │  transactions   │    │                 │
                    │  transfers      │    │  refresh:userId │
                    │  loans          │    │  (refresh tokens│
                    │                 │    │   with TTL)     │
                    └─────────────────┘    └─────────────────┘
```

### Docker Compose Topology

```
┌──────────────────────────────────────────────────────────────────┐
│                     banking-network (bridge)                      │
│                                                                   │
│  ┌──────────────────┐    ┌──────────────────┐   ┌─────────────┐  │
│  │   banking-api    │    │  banking-mysql   │   │banking-redis│  │
│  │  :8080 → :8080   │───►│  :3307 → :3306   │   │ :6379→:6379 │  │
│  │  profile: prod   │    │  banking_db      │   │             │  │
│  │                  │───────────────────────────►             │  │
│  └──────────────────┘    └──────────────────┘   └─────────────┘  │
│                                                                   │
└──────────────────────────────────────────────────────────────────┘
```

---

## Getting Started

### Prerequisites

- Java 21
- Maven 3.9+
- Docker & Docker Compose

### Run with Docker (recommended)

```bash
# Clone the repo
git clone https://github.com/Nazir2608/banking-api.git
cd banking-api

# Start all services
docker compose up --build

# API is available at
http://localhost:8080/api/swagger-ui.html
```

### Run locally (MySQL + Redis must be running)

```bash
# Update application.yml with your local DB and Redis credentials
mvn spring-boot:run
```

### Run tests

```bash
mvn test
```

### Default Admin Credentials

| Field | Value |
|-------|-------|
| Email | admin@springbank.com |
| Password | Admin@12345 |

---

## CI/CD Pipeline

```
push to main / develop
        │
        ▼
  ┌─────────────┐
  │  validate   │  compile + unit tests
  └──────┬──────┘
         │
         ▼
  ┌─────────────┐
  │   qodana    │  static code analysis (blocks deploy on failure)
  └──────┬──────┘
         │
         ▼
  ┌─────────────┐
  │    build    │  package JAR → verify migrations → build & push Docker image
  └──────┬──────┘
         │ (main branch only)
         ▼
  ┌─────────────┐
  │   deploy    │  SSH → docker pull → rolling restart → health check
  └─────────────┘
```

---

## Environment Variables

### GitHub Secrets required for CI/CD

| Secret | Description |
|--------|-------------|
| `QODANA_TOKEN` | JetBrains Qodana project token |
| `DEPLOY_HOST` | Production server IP or domain |
| `DEPLOY_USER` | SSH username on the server |
| `DEPLOY_SSH_KEY` | Private SSH key for server access |
| `DEPLOY_PORT` | SSH port (default: 22) |
| `DEPLOY_APP_DIR` | App directory on server (default: `/opt/banking-api`) |
| `GHCR_USER` | GitHub username for container registry |
| `GHCR_TOKEN` | GitHub PAT with `read:packages` scope |

### Application Config

| Property | Default | Description |
|----------|---------|-------------|
| `app.jwt.secret` | — | HS256 signing key (hex encoded) |
| `app.jwt.access-token-expiration-ms` | `900000` | Access token TTL (15 min) |
| `app.jwt.refresh-token-expiration-ms` | `604800000` | Refresh token TTL (7 days) |
| `spring.datasource.url` | — | MySQL JDBC URL |
| `spring.data.redis.host` | `localhost` | Redis host |

---

## License

This project is for portfolio and educational purposes.