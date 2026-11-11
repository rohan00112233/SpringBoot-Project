
# Sentinel Ledger

**Real-Time Fintech Transaction Engine with Fraud Detection and Double-Entry Ledger**

Sentinel Ledger is a backend fintech application built using Java, Spring Boot, and PostgreSQL. It provides REST APIs for account management, fund transfers, transaction history, and double-entry ledger records. It also implements rule-based fraud detection and concurrency control to protect financial transactions.

## Features

- **Account Management:** Create, retrieve, update, and delete accounts.
- **Transaction Processing:** Transfer funds between accounts with validation.
- **Balance Management:** Automatically debit the sender and credit the receiver.
- **Double-Entry Ledger:** Generate corresponding DEBIT and CREDIT entries for every successful transfer.
- **Fraud Detection:** Reject transactions exceeding the configured ₹50,000 threshold.
- **Concurrency Control:** Use pessimistic database locking to prevent concurrent overspending.
- **Transaction History:** Retrieve all transactions, individual transactions, or transactions associated with an account.
- **Ledger History:** Retrieve ledger entries by transaction or account.
- **Request Validation:** Validate incoming API requests.
- **Centralized Exception Handling:** Return structured responses for validation failures and missing resources.
- **Automated Testing:** Unit and integration tests for fraud detection and transaction processing.

## Technology Stack

| Technology | Purpose |
|---|---|
| Java 21 | Programming language |
| Spring Boot 4.1.1 | Backend framework |
| Spring WebMVC | REST API development |
| Spring Data JPA | Database operations |
| Hibernate | ORM |
| PostgreSQL | Relational database |
| Maven | Build and dependency management |
| Jakarta Validation | Request validation |
| JUnit 5 | Automated testing |
| Git and GitHub | Version control |

## Project Structure

```text
sentinel-ledger/
├── src/
│   ├── main/
│   │   ├── java/com/sentinelledger/
│   │   │   ├── controller/
│   │   │   │   ├── AccountController.java
│   │   │   │   ├── TransactionController.java
│   │   │   │   ├── LedgerEntryController.java
│   │   │   │   └── HealthController.java
│   │   │   ├── dto/
│   │   │   │   ├── TransactionResponse.java
│   │   │   │   └── LedgerEntryResponse.java
│   │   │   ├── entity/
│   │   │   │   ├── Account.java
│   │   │   │   ├── Transaction.java
│   │   │   │   └── LedgerEntry.java
│   │   │   ├── exception/
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   └── ResourceNotFoundException.java
│   │   │   ├── repository/
│   │   │   ├── service/
│   │   │   │   ├── AccountService.java
│   │   │   │   ├── TransactionService.java
│   │   │   │   ├── LedgerEntryService.java
│   │   │   │   └── FraudDetectionService.java
│   │   │   └── SentinelLedgerApplication.java
│   │   └── resources/
│   └── test/
│       ├── java/
│       │   ├── com/sentinelledger/service/
│       │   │   └── TransactionServiceIntegrationTest.java
│       │   └── service/
│       │       └── FraudDetectionServiceTest.java
│       └── resources/
├── .gitignore
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md
```

## Database Configuration

The application uses PostgreSQL.

Create a database named:

```sql
CREATE DATABASE sentinel_ledger;
```

Configure your local database connection in:

`src/main/resources/application.properties`

Example:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/sentinel_ledger
spring.datasource.username=YOUR_DB_USERNAME
spring.datasource.password=YOUR_DB_PASSWORD

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
```

Use your own local database credentials. Never commit passwords or local configuration files to GitHub.

## Running the Application

### Prerequisites

- Java JDK 21
- PostgreSQL
- Maven or the included Maven Wrapper
- Git

### 1. Clone the repository

```bash
git clone https://github.com/rohan00112233/SpringBoot-Project.git
```

### 2. Navigate to the project directory

```bash
cd SpringBoot-Project
```

### 3. Configure PostgreSQL

Create the `sentinel_ledger` database and configure your local `application.properties`.

### 4. Run the application

On Windows:

```bat
mvnw.cmd spring-boot:run
```

The application runs at:

`http://localhost:8080`

## REST API Endpoints

Base URL: `http://localhost:8080`

### Health Check

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/health` | Check application health |

### Account APIs

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/accounts` | Create an account |
| GET | `/api/accounts` | Retrieve all accounts |
| GET | `/api/accounts/{id}` | Retrieve an account |
| PUT | `/api/accounts/{id}` | Update account details |
| DELETE | `/api/accounts/{id}` | Delete an account |

Example account request:

```json
{
  "accountHolderName": "Rohan Shinde",
  "accountNumber": "ACC001",
  "balance": 8000.00
}
```

### Transaction APIs

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/transactions` | Create a fund transfer |
| GET | `/api/transactions` | Retrieve all transactions |
| GET | `/api/transactions/{id}` | Retrieve a transaction |
| GET | `/api/transactions/account/{accountId}` | Retrieve account transactions |

Example transfer request:

```json
{
  "senderAccountId": 1,
  "receiverAccountId": 2,
  "amount": 100.00,
  "transactionType": "TRANSFER"
}
```

A successful transfer updates both account balances and creates corresponding ledger entries.

### Ledger APIs

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/ledger/transaction/{transactionId}` | Retrieve ledger entries for a transaction |
| GET | `/api/ledger/account/{accountId}` | Retrieve ledger entries for an account |

## Double-Entry Ledger

Every successful transfer generates two ledger entries:

1. **DEBIT:** Records the amount deducted from the sender.
2. **CREDIT:** Records the amount added to the receiver.

Example: A transfer of ₹100 from Account 1 to Account 2.

| Account | Entry Type | Amount |
|---|---|---:|
| Account 1 | DEBIT | ₹100 |
| Account 2 | CREDIT | ₹100 |

Both entries reference the same transaction.

## Fraud Detection

The application implements a basic rule-based fraud detection mechanism.

**Configured threshold: ₹50,000**

- Transactions exceeding ₹50,000 are rejected.
- Transactions at or below ₹50,000 are not flagged by this rule.
- Invalid or non-positive amounts are rejected.
- Rejected transactions do not update balances or create ledger entries.

The current implementation rejects high-value transfers. It does not maintain a persistent fraud-alert review workflow.

## Transaction Concurrency

The transaction service uses pessimistic database locking to protect account balances during concurrent transfers.

- Accounts are locked before balance verification and modification.
- Accounts are locked in ascending ID order to reduce deadlock risk.
- Balance updates, transaction records, and ledger entries are processed within a database transaction.

A concurrent transfer scenario was manually verified: when two simultaneous ₹6,000 transfers competed for a sender balance of ₹9,000, only one succeeded.

## Automated Testing

The project uses JUnit 5 and Spring Boot testing support.

Run the complete test suite:

```bat
mvnw.cmd clean test
```

**Latest verified test result: 11 tests passed, 0 failures, 0 errors.**

Coverage includes:

- Fraud detection threshold checks.
- Invalid transaction amounts.
- Successful fund transfers.
- Insufficient-balance rejection.
- Account balance consistency.
- Ledger entry creation.
- Concurrent overspending prevention.

Integration tests use a separate test database configuration to avoid modifying the main development database.

## Error Handling

Centralized exception handling covers:

- Missing accounts and transactions: HTTP 404.
- Invalid requests and validation failures: HTTP 400.
- Multiple validation errors returned in a single response.

## Future Enhancements

- OpenAPI/Swagger API documentation.
- Authentication and authorization.
- Persistent fraud alerts and review workflow.
- Additional fraud detection rules.

## Author

**Rohan Ramesh Shinde**

GitHub: [rohan00112233](https://github.com/rohan00112233)

Project Repository: [SpringBoot-Project](https://github.com/rohan00112233/SpringBoot-Project)
