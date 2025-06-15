# Wallet Service

This project is a proof-of-concept for a wallet management service built with Spring Boot. It allows users to query their balance and top-up their wallets using a credit card, demonstrating key principles of modern backend development, including event-driven architecture, security, and concurrency control.

## Key Features & Design Decisions

This application incorporates several important design patterns and features:

### 1. Event-Driven & Asynchronous Architecture

The top-up process is fully asynchronous to ensure the API remains responsive.

* **Initial Request:** When a user requests a top-up via the `POST /v1/wallets/{walletId}/top-up` endpoint, the `WalletService` does not immediately process the payment. Instead, it validates the request and publishes a `TopUpRequestedEvent`. 
* **Payment Processing:** The `PaymentListener` listens for this event and, in a separate thread (`@Async`), calls the `StripeService` to handle the charge.
* **Outcome Events:** Based on the Stripe service's response, the `PaymentListener` publishes either a `PaymentCompletedEvent` or a `PaymentFailedEvent`.
* **Updating the Wallet:** The `WalletListener` listens for `PaymentCompletedEvent` and updates the wallet's balance within a new transaction.
* **Notifications:** The `NotificationListener` listens for `PaymentFailedEvent` and sends a notification to the user (not implemented in this demo, but could be an email or a message to a messaging service)

This decoupled, event-driven approach improves fault tolerance and makes the system more scalable.

### 2. Security with JWT

The application is secured using Spring Security and JSON Web Tokens (JWT).

* **Authentication:** The `POST /v1/auth/login` endpoint authenticates users and provides a JWT.
* **Authorization:** The `JwtTokenFilter` intercepts all other requests to protected endpoints, validates the JWT, and sets the user's security context.
* **Configuration:** The `SecurityConfig` class defines separate, ordered security filter chains: one for the public login endpoint and another that applies the `JwtTokenFilter` to all private wallet endpoints. This prevents security filters from being incorrectly applied to public endpoints.

### 3. Concurrency Control (Optimistic Locking)

To prevent data corruption from simultaneous updates (e.g., two top-ups at the same time), the `Wallet` entity uses optimistic locking.

* **`@Version` Annotation:** The `Wallet` entity has a `version` field annotated with `@Version`, which Hibernate uses to manage concurrent updates.
* **Locking Mechanism:** When Hibernate saves an updated wallet, it checks if the `version` number in the database matches the `version` of the object in memory. If they don't match, it means another transaction has already updated the record, and it throws an `OptimisticLockException`.
* **Error Handling:** The `WalletListener` includes a `try-catch` block for this exception, indicating where a retry policy or reconciliation logic could be implemented.

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
```
mvn clean install
```

### 2. Run the Application
The application will start, create a demo wallet, and print its ID to the console.
```
java -jar target/exercise-wallet-0.0.2.jar
```
**Copy the `Wallet ID`** from the console output.

### 3. Access the H2 Database Console
* Navigate to <http://localhost:8090/h2-console> in your browser.
* Use the JDBC URL: `jdbc:h2:mem:testdb`
* Click **Connect**.

### 4. Test with Postman
* Import the provided Postman collection and the "Local" environment.
* Select the "Wallet Service - Local" environment in Postman.
* Set the `walletId` variable in your Postman environment to the ID you copied from the console.
* Run the requests in the collection, starting with "Login and Get Token".

## API Endpoints
* `POST /v1/auth/login`: Authenticates a user and returns a JWT.
* `GET /v1/wallets/{walletId}`: Retrieves the balance for a specific wallet.
* `POST /v1/wallets/{walletId}/top-up`: Initiates a top-up for a specific wallet.
