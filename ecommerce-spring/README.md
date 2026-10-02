# Ecommerce App — Spring Boot

This is a standalone Java/Spring Boot implementation of the demo store described in the original [`ecommerce/README.md`](https://github.com/MasterAditya/shopping-website/blob/main/ecommerce/README.md). It uses Spring MVC and Thymeleaf for the UI and backend in one application, so there is no separate React development server.

## Requirements

- JDK 21
- Maven 3.8+ (or the Maven support built into your IDE)

## Run in VS Code

1. Open this `ecommerce-spring` folder in VS Code.
2. Install the **Extension Pack for Java** (or at least the Java language support and debugger extensions).
3. Start with the explicit local-development profile:

   ```powershell
   mvn spring-boot:run "-Dspring-boot.run.profiles=local"
   ```

4. Visit <http://localhost:8080>.

For the local profile only, the default administrator login is **admin@store.com** / **storeadmin123**. The account is created in the users table on the first run with a BCrypt-encoded password. These shared demo credentials are public and must never be used on a publicly accessible deployment. The default profile has no admin credential fallback and requires `ADMIN_EMAIL` and `ADMIN_PASSWORD` to be supplied from the environment or deployment secret manager; the password must contain at least 12 characters. If that email already belongs to a regular account, it is promoted and its password is set from `ADMIN_PASSWORD` once; subsequent starts do not overwrite an existing administrator's password. Regularly registered accounts and orders are stored in the file-backed H2 database at `./data/superstyleshop` and remain after application restarts. The H2 console is at <http://localhost:8080/h2-console> (JDBC URL: `jdbc:h2:file:./data/superstyleshop`, user `sa`, blank password).

### Clear local user accounts

To remove all accounts from the local H2 database without deleting products or order history:

1. Open <http://localhost:8080/h2-console> and connect using the JDBC URL, username, and password above.
2. Run these statements separately in the SQL console:

   ```sql
   UPDATE ORDERS SET USER_ID = NULL;
   ```

   ```sql
   DELETE FROM USERS;
   ```

3. Restart the application. The configured administrator account will be created again; newly registered accounts will be stored as usual.

This permanently deletes all account rows. Existing orders are retained but are no longer associated with a user. Do not run this against production data.

### Clear local order history

The admin dashboard can delete individual orders and renumbers the affected account’s remaining orders. To clear all local order history and reset the database order IDs while retaining accounts and products, stop the application, open the H2 console, and run each statement separately:

```sql
DELETE FROM ORDER_ITEMS;
```

```sql
DELETE FROM ORDERS;
```

```sql
ALTER TABLE ORDERS ALTER COLUMN ID RESTART WITH 1;
```

Restart the application afterward. Each account’s next visible order number starts at 1. Deleting history does not restore product stock, because the purchase already consumed that stock. These commands permanently delete order records; do not run them against production data.

Run the test suite with:

```powershell
mvn test
```

## Optional PostgreSQL

Create an `ecommerce` database and set the connection environment variables before starting:

```powershell
$env:SPRING_DATASOURCE_URL = "jdbc:postgresql://localhost:5432/ecommerce"
$env:SPRING_DATASOURCE_USERNAME = "postgres"
$env:SPRING_DATASOURCE_PASSWORD = "your-local-password"
$env:ADMIN_EMAIL = "admin@example.com"
$env:ADMIN_PASSWORD = "your-unique-password-of-at-least-12-characters"
mvn spring-boot:run
```

PostgreSQL is included as a runtime driver; the same JPA entities are used. The default H2 configuration requires no database installation. The PostgreSQL example intentionally uses no local profile, so the application fails to start unless administrator credentials are supplied.

## What is included

- Product catalog seeded with the six demo products, category filtering, case-insensitive name search, and product details.
- Session cart with add, quantity update, remove, and total calculation. A cart works without an account; checkout requires login.
- Signup, login/logout, BCrypt password hashing, and profile editing (name and address). New passwords must be 8–72 characters and contain at least one letter and one digit; symbols are optional. Non-admin accounts must save an address before checkout.
- A database-seeded administrator account configured using `ADMIN_EMAIL` and `ADMIN_PASSWORD`. The admin may check out without an address.
- Separate order numbers and history for each account, with per-order item names, quantities, saved unit prices, category filters, and lifetime purchase totals. Users can delete their own history; order numbers are renumbered afterward.
- Inventory defaults to 99 per product and is decremented transactionally at checkout. Admins can update product prices and stock, edit saved order quantities/prices, delete orders, and remove customer accounts. Deleting a customer also deletes their orders; admin accounts are protected.
- The admin dashboard is available at `/admin` after logging in with an administrator account.
- QR/card demo-payment choices; checkout creates a database order and clears the cart. No payment credentials are requested or processed.
- A generic password-reset confirmation screen. No email is sent and no password is changed.

## Project layout

```text
src/main/java/com/superstyleshop/
  config/       Security configuration and sample catalog
  model/        JPA product, user, order, and order-line entities
  repository/   Spring Data repositories
  service/      Account, session-cart, and checkout logic
  web/          MVC controllers
src/main/resources/
  templates/    Thymeleaf storefront and account screens
  static/css/   Responsive storefront styling
```

This is an educational demo, not a production store. It uses session authentication rather than the original API's JWT flow; password reset and payment are deliberately simulated. The documented demo administrator credentials are public and must never be used on a publicly accessible deployment. For production, require unique administrator credentials from a deployment secret manager/environment, use schema migrations, email verification/reset tokens, payment-provider integration, and deployment-appropriate security and database settings. Product photos are served from Unsplash URLs and therefore need internet access; the rest of the app runs locally.
