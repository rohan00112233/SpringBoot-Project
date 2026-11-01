# Sentinel Ledger

**Real-Time Fintech Transaction Engine with Fraud Detection and Double-Entry Ledger**

Sentinel Ledger is a backend fintech application built using Java and Spring Boot. It provides REST APIs for bank account management, fund transfers, transaction history, and double-entry ledger records. It also includes a rule-based fraud detection mechanism that rejects transactions exceeding a configured value threshold.

## Features

- **Account Management:** Create, retrieve, update, and delete accounts.
- **Transaction Processing:** Transfer funds between accounts with validation.
- **Balance Management:** Automatically debit the sender and credit the receiver.
- **Double-Entry Ledger:** Record a DEBIT entry for the sender and a CREDIT entry for the receiver.
- **Fraud Detection:** Reject transactions exceeding ₹50,000.
- **Transaction History:** Retrieve all transactions, individual transactions, or transactions associated with an account.
- **Ledger History:** Retrieve ledger entries by transaction or account.
- **Response DTOs:** Return structured transaction and ledger responses.
- **Exception Handling:** Handle invalid requests and missing resources.
- **Automated Testing:** Unit tests for fraud detection and Spring Boot application context testing.

## Technology Stack

| Technology | Purpose |
|---|---|
| Java 21 | Programming language |
| Spring Boot 4.1.1 | Backend framework |
| Spring WebMVC | REST API development |
| Spring Data JPA | Database operations |
| Hibernate | ORM |
| PostgreSQL | Relational database |
| Maven | Dependency management and build |
| Jakarta Validation | Request validation |
| JUnit 5 | Automated testing |
| Eclipse IDE | Development environment |
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
│   │   │   ├── repository/
│   │   │   ├── service/
│   │   │   │   ├── AccountService.java
│   │   │   │   ├── TransactionService.java
│   │   │   │   ├── LedgerEntryService.java
│   │   │   │   └── FraudDetectionService.java
│   │   │   ├── exception/
│   │   │   └── SentinelLedgerApplication.java
│   │   └── resources/
│   └── test/
│       └── java/
│           └── service/
│               └── FraudDetectionServiceTest.java
├── .gitignore
├── mvnw
├── mvnw.cmd
└── pom.xml
```

## Database Configuration

The application uses PostgreSQL.

Create a PostgreSQL database named `sentinel_ledger`.

Configure the local database connection in:

`src/main/resources/application.properties`

Example configuration:

```properties
spring.application.name=sentinel-ledger

spring.datasource.url=jdbc:postgresql://localhost:5432/sentinel_ledger
spring.datasource.username=YOUR_DB_USERNAME
spring.datasource.password=YOUR_DB_PASSWORD

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

server.port=8080
```

Replace the database username and password with your local credentials. Do not commit database credentials to GitHub.

## Running the Application

### Prerequisites

- Java JDK 21
- PostgreSQL
- Maven (or the included Maven Wrapper)
- Git

### 1. Clone the repository

```bash
git clone https://github.com/rohan00112233/SpringBoot-Project.git
```

### 2. Navigate to the project

```bash
cd SpringBoot-Project
```

### 3. Configure PostgreSQL

Create the database and configure `application.properties`.

### 4. Run the application

On Windows:

```bat
mvnw.cmd spring-boot:run
```

The application starts at:

`http://localhost:8080`

## REST API Documentation

All endpoints use the base URL:

`http://localhost:8080`

### Health Check

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/health` | Check application health |

### Account APIs

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/accounts` | Create an account |
| GET | `/api/accounts` | Retrieve all accounts |
| GET | `/api/accounts/{id}` | Retrieve an account by ID |
| PUT | `/api/accounts/{id}` | Update an account |
| DELETE | `/api/accounts/{id}` | Delete an account |

Example account creation request:

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
| GET | `/api/transactions/{id}` | Retrieve a transaction by ID |
| GET | `/api/transactions/account/{accountId}` | Retrieve transactions associated with an account |

Example transaction request:

```json
{
  "senderAccountId": 2,
  "receiverAccountId": 3,
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

1. **DEBIT:** Records the amount deducted from the sender's account.
2. **CREDIT:** Records the amount added to the receiver's account.

For example, a transfer of ₹100 from Account 2 to Account 3 generates:

| Account | Entry Type | Amount |
|---|---|---:|
| Account 2 | DEBIT | ₹100 |
| Account 3 | CREDIT | ₹100 |

Both entries reference the same transaction.

## Fraud Detection

The application implements a basic rule-based fraud detection mechanism.

**Configured threshold: ₹50,000**

- Transactions above ₹50,000 are rejected.
- Transactions of ₹50,000 or less are not flagged by this rule.
- Invalid or non-positive amounts are rejected by transaction validation.
- A rejected high-value transaction does not update account balances or create ledger entries.

Example:

```json
{
  "senderAccountId": 2,
  "receiverAccountId": 3,
  "amount": 60000.00,
  "transactionType": "TRANSFER"
}
```

Expected result: HTTP 400 Bad Request.

The current implementation rejects high-value transfers. It does not yet persist fraud alerts or maintain a separate fraud-review queue.

## Automated Testing

The project uses JUnit 5 and Spring Boot testing support.

Run all tests:

```bat
mvnw.cmd clean test
```

The fraud detection unit tests cover:

- Amount below the threshold.
- Amount equal to the threshold.
- Amount above the threshold.
- Fraud reason generation.
- Normal transaction reason.
- Null amount.
- Zero and negative amounts.

The previously verified test run completed with **8 tests, 0 failures, and 0 errors**.

## Error Handling

The application includes centralized exception handling for:

- Resource not found.
- Invalid transaction requests.
- Insufficient account balance.
- Request validation errors.

## Future Enhancements

Potential improvements include:

- Persistent fraud alert records and review workflow.
- More fraud detection rules.
- Transaction concurrency and account locking.
- Integration and API tests.
- API documentation using OpenAPI/Swagger.
- Authentication and authorization.

## Author

**Rohan Ramesh Shinde**

GitHub: https://github.com/rohan00112233
