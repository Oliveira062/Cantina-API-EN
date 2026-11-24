# University Canteen - REST API

Backend for the canteen ordering system, built with Java 21 and Spring Boot 4.2.
It uses PostgreSQL, JPA, Flyway, validation, JWT authentication, Swagger and Docker.

## Run with Docker (simplest way)

```bash
cp .env.example .env
docker compose up -d --build
```

- Web interface: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui.html
- API health: http://localhost:8080/actuator/health
- First administrator: the email and password set in `ADMIN_EMAIL` and `ADMIN_PASSWORD` in `.env`

## Web interface

The interface lives in `src/main/resources/static` and is served by the API itself, at the same address.
It is plain HTML, CSS and JavaScript, with no framework and no build step.

| Role | What it shows |
|---|---|
| Customer | Menu with the order ticket, payment by PIX or card, order history and notices |
| Cashier | Counter (find the customer and build the order, including cash with change), order list and delivery |
| Kitchen | Queue with orders to prepare and in preparation, refreshed every 10 seconds |
| Admin | Dashboard with counters and low stock, all the screens above, products, team and canteen settings |

## Run without Docker (for development)

With JDK 21, Maven 3.9 or newer and a local PostgreSQL (database `canteen`, user `canteen`, password `canteen123`):

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

In the `dev` profile the administrator is `admin@canteen.edu` / `Admin@2026`.

## Tests

```bash
mvn test
```

The end-to-end tests against the running API are in `qa/canteen.yaml` and run with the
`api-tester` tool:

```bash
ADMIN_PASSWORD=ChangeThisPassword1 java -jar api-tester.jar qa/canteen.yaml
```

## Roles and permissions

| Action | Customer | Cashier | Kitchen | Admin |
|---|:-:|:-:|:-:|:-:|
| See the menu | yes | yes | yes | yes |
| Open order, add items, pay | own orders | for customers | no | yes |
| Pay in cash | no | yes | no | yes |
| Start preparing / mark as ready | no | no | yes | yes |
| Confirm delivery | no | yes | no | yes |
| Cancel | before paying | yes | no | yes |
| Products, users, settings | no | no | no | yes |

## Where the original project's patterns are

| Pattern | Where it is |
|---|---|
| Singleton | `CanteenSettings` (a single row in the database) and the Spring beans |
| Factory | `payment/PaymentFactory` |
| Strategy | `payment/PaymentStrategy` and the classes `PixPayment`, `CardPayment`, `CashPayment` |
| Observer | `observer/CustomerObserver` and `observer/KitchenObserver`, notified by Spring events |
