# Banking API

Production-ready Banking REST API built with Spring Boot 4, MySQL, Redis, and JWT authentication.

## Tech Stack

- **Spring Boot 4.0.3** / Java 21
- **Spring Security 7** + JWT (jjwt 0.12.5)
- **MySQL 9** with Flyway migrations (V1–V6)
- **Redis 7** — JWT token blacklisting
- **SpringDoc OpenAPI 2.3** — Swagger UI
- **Lombok**, **HikariCP**, **Testcontainers**

## Quick Start

### Prerequisites
- Java 21, Maven 3.9+
- MySQL 9 running on port 3306
- Redis 7 running on port 6379

### Using Docker Compose (recommended)
```bash
docker-compose up -d        # starts MySQL + Redis
mvn spring-boot:run
```

### Manual
Update `src/main/resources/application.yml` with your DB/Redis credentials, then:
```bash
mvn clean spring-boot:run
```

### Swagger UI
```
http://localhost:8080/api/swagger-ui.html
```

## Default Admin
| Field    | Value                  |
|----------|------------------------|
| Email    | admin@springbank.com   |
| Password | Admin@12345            |

## API Reference

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST | /api/v1/auth/register | Public | Register customer |
| POST | /api/v1/auth/login | Public | Login, get tokens |
| POST | /api/v1/auth/refresh-token | Public | Refresh access token |
| POST | /api/v1/auth/logout | Bearer | Logout |
| GET | /api/v1/users/me | Bearer | Get my profile |
| PUT | /api/v1/users/me | Bearer | Update profile |
| PUT | /api/v1/users/me/password | Bearer | Change password |
| GET | /api/v1/admin/users | Admin | List all users |
| PUT | /api/v1/admin/users/{id}/status | Admin | Enable/disable user |
| POST | /api/v1/accounts | Bearer | Open account |
| GET | /api/v1/accounts | Bearer | My accounts |
| GET | /api/v1/accounts/{id} | Bearer | Account details |
| GET | /api/v1/accounts/{id}/balance | Bearer | Account balance |
| PUT | /api/v1/admin/accounts/{id}/status | Admin | Freeze/unfreeze account |
| DELETE | /api/v1/accounts/{id} | Bearer | Close account |
| POST | /api/v1/transactions/deposit | Bearer | Deposit funds |
| POST | /api/v1/transactions/withdraw | Bearer | Withdraw funds |
| GET | /api/v1/transactions | Bearer | My transactions |
| GET | /api/v1/transactions/{id} | Bearer | Transaction by ID |
| GET | /api/v1/accounts/{id}/transactions | Bearer | Account transaction history |
| POST | /api/v1/transfers | Bearer | Transfer funds |
| GET | /api/v1/transfers | Bearer | My transfers |
| GET | /api/v1/transfers/{reference} | Bearer | Transfer by reference |
| POST | /api/v1/loans/apply | Bearer | Apply for loan |
| GET | /api/v1/loans | Bearer | My loans |
| GET | /api/v1/loans/{id} | Bearer | Loan by ID |
| POST | /api/v1/loans/{id}/repay | Bearer | Repay loan |
| PUT | /api/v1/admin/loans/{id}/approve | Admin | Approve loan |
| PUT | /api/v1/admin/loans/{id}/reject | Admin | Reject loan |
| GET | /api/v1/admin/loans | Admin | All loans |
| GET | /api/v1/reports/statement | Bearer | Account statement |
| GET | /api/v1/admin/reports/summary | Admin | Platform summary |

## Project Structure

```
src/main/java/com/nazir/banking/
├── BankingApiApplication.java
├── config/          SecurityConfig, OpenApiConfig
├── common/          ApiResponse, PagedResponse, Exceptions, GlobalExceptionHandler
├── auth/            JWT auth, register, login, refresh, logout
├── user/            Profile management, admin user control
├── account/         Open/close accounts, freeze (admin)
├── transaction/     Deposit, withdraw, history
├── transfer/        Atomic fund transfers
├── loan/            Apply, approve, reject, repay
└── report/          Account statements, admin summary
```
