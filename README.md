# Wallet Service
This project is a proof-of-concept for a wallet management service built with Spring Boot. 
It allows users to query their balance, top-up their wallets and view their transaction history including event-driven architecture, security, and concurrency control.

## Key Features & Design Decisions
### 1. Event-Driven & Asynchronous Architecture
The top-up process is fully asynchronous to ensure the API remains responsive.
This decoupled, event-driven approach improves fault tolerance and makes the system more scalable.
* **Initial Request:** When a user requests a top-up via the `POST /v1/wallets/{walletId}/top-up` endpoint, the `WalletService` does not immediately process the payment. Instead, it validates the request and publishes a `TopUpRequestedEvent`.
* **Payment Processing:** The `PaymentListener` listens for this event and, in a separate thread (`@Async`), calls the appropriate payment service (e.g., `StripeService`) to handle the charge.
* **Outcome Events:** Based on the payment service's response, the `PaymentListener` publishes either a `PaymentCompletedEvent` or a `PaymentFailedEvent`.
* **Updating the Wallet:** The `WalletListener` listens for `PaymentCompletedEvent` and delegates to the `WalletUpdateService`, which updates the wallet's balance and creates a `WalletTransaction` record within a new, retry-enabled transaction.
* **Notifications:** The `NotificationListener` listens for `PaymentFailedEvent` and sends a notification to the user (simulated with a log message).


### 2. Security with JWT
The application is secured using Spring Security and JSON Web Tokens (JWT).
* **Authentication:** The `POST /v1/auth/login` endpoint authenticates users and provides a JWT.
* **Authorization:** The `JwtTokenFilter` intercepts all other requests to protected endpoints, validates the JWT, and sets the user's security context.

### 3. Concurrency Control and Idempotency
To prevent data corruption from simultaneous updates or network retries, the application uses a robust concurrency and idempotency strategy.

* **Optimistic Locking with `@Version`:** The `Wallet` entity has a `version` field, which Hibernate uses to manage concurrent updates. If two transactions attempt to update the same wallet, the second one will fail with an `ObjectOptimisticLockingFailureException`.
* **Automatic Retries:** The `WalletUpdateService` is annotated with `@Retryable`. If an `ObjectOptimisticLockingFailureException` occurs, Spring will automatically retry the operation up to 3 times before failing.
* **Idempotency Key:** The `top-up` endpoint is idempotent. Clients must provide a unique `idempotencyKey` (UUID) in the request body. The system checks if a `WalletTransaction` with this key already exists. If it does, the request is acknowledged as successful without being re-processed. A unique constraint in the database on `(wallet_id, idempotencyKey)` provides a final guarantee against race conditions.
* **Reconciliation:** If all retry attempts fail, the service publishes a `WalletReconciliationEvent`, signaling that this transaction requires manual review.

### 4. Comprehensive Testing Strategy
The project includes a multi-layered testing approach to ensure code quality:
* **`@DataJpaTest`:** For testing the `WalletRepository`, including the optimistic locking feature.
* **`@WebMvcTest`:** For unit testing the `WalletController` and `AuthController`, mocking the service layer to isolate the web layer.
* **`@RestClientTest`:** For testing the `StripeService`'s interaction with the external payment simulator.
* **`@SpringBootTest`:** For full integration tests (`WalletApplicationIT`) that test the end-to-end flow of the application, from the API endpoint to the event listeners.

## How to Run and Test
### Prerequisites
* Java 17 or higher
* Apache Maven
* Postman

### 1. Build the Project
```bash
mvn clean install
```

### 2. Run the Application
The application will start, and you can begin testing with Postman.

```bash
java -jar target/exercise-wallet-0.0.2.jar
```

### 3. Access the H2 Database Console
You can inspect the in-memory database while the application is running.
* Navigate to <http://localhost:8090/h2-console> in your browser.
* Use the JDBC URL: `jdbc:h2:mem:testdb`
* Click **Connect**. You will need to query the `WALLET` table to find a valid `id` to use in your Postman tests.

### 4. Test with Postman
* Import the provided Postman collection and the "Local" environment.
* Select the "Wallet Service - Local" environment in Postman.
* **Manually** set the **`walletId` variable** in your Postman environment to a UUID you found in the H2 console.
* Run the requests in the collection, starting with "1. Login and Get Token". This will automatically save the JWT for subsequent requests.

## API Endpoints
* `POST /v1/auth/login`: Authenticates a user and returns a JWT.
* `GET /v1/wallets/{walletId}`: Retrieves the balance for a specific wallet.
* `POST /v1/wallets/{walletId}/top-up`: Initiates a top-up for a specific wallet.
* `GET /v1/wallets/{walletId}/transactions`: Retrieves the transaction history for a specific wallet.

