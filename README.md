# Ecommerce App - Spring Boot

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


## What is included

- Product catalog seeded with the six demo products, category filtering, case-insensitive name search, and product details.
- Session cart with add, quantity update, remove, and total calculation. A cart works without an account; checkout requires login.
- Signup, login/logout, BCrypt password hashing, and profile editing (name and address). New passwords must be 8–72 characters and contain at least one letter and one digit; symbols are optional. Non-admin accounts must save an address before checkout.
- A database-seeded administrator account configured using `ADMIN_EMAIL` and `ADMIN_PASSWORD`. The admin may check out without an address.
- Separate order numbers and history for each account, with per-order item names, quantities, saved unit prices, category filters, and lifetime purchase totals. Users can delete their own history; order numbers are renumbered afterward.
- Inventory defaults to 99 per product and is decremented transactionally at checkout. Admins can update product prices and stock, edit saved order quantities/prices, delete orders, and remove customer accounts. Deleting a customer also deletes their orders; admin accounts are protected.
- The admin dashboard is available at `/admin` after logging in with an administrator account.
- QR/card demo-payment choices; checkout creates a database order and clears the cart. No payment credentials are requested or processed.
- A generic password-reset confirmation screen. No email is sent, and no password is changed.


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
