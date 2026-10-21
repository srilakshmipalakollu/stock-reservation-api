Stock Reservation API-Initial Plan

1. Problem and Acceptance criteria:
- Build a Java REST API for warehouse inventory. Customers can view stock, reserve multiple products, retrieve a reservation, confirm a reservation, or cancel it.
- The application must use a persistent SQL database. Inventory changes and reservation state changes must remain consistent when requests fail or execute concurrently.

Acceptance Critieria:
- List products with total, reserved, and available stock.
- Create an ACTIVE reservation containing one or more products.
- Reject invalid quantities, unknown products, and insufficient stock.
- Ensure a multi-product reservation succeeds completely or leaves no changes.
- Allow only ACTIVE → CONFIRMED and ACTIVE → CANCELLED transitions.
- Ensure repeated confirmation or cancellation never changes inventory twice.
- Prevent concurrent reservations from overselling stock.
- Persist data across application restarts.
- Provide automated database-backed tests and reproducible setup instructions.

2. Assumptions and scope
- Use Java 21, Spring Boot, Maven, Spring Data JPA, and MySQL.
- Use integer quantities and reject zero or negative quantities.
- Each reservation request is independent; request deduplication is not required.
- Reject duplicate product IDs within one request rather than silently combining them.
- Reservations do not expire automatically.
- Authentication, frontend development, payments, and deployment are out of scope.

3. Initial API Design
GET/api/products -- list inventory
POST/api/reservations -- create a reservation
GET/api/reservations/{id} -- retrieve a reservation
POST/api/reservations/{id}/confirm -- confirm a reservation
POST/api/reservations/{id}/cancel -- cancel a reservation
Use JSON request and response bodies. Return consistent error responses with suitable HTTP status codes.

4. Initial database design

products
id: primary key
sku: unique, not null (stock keeping unit)
name: not null
total_stock: non-negative integer
reserved_stock: non-negative integer, never greater than total_stock
Available stock is derived as total_stock - reserved_stock.

reservations
id: primary key
status: ACTIVE, CONFIRMED, or CANCELLED
created_at: not null
updated_at: not null

reservation_items
reservation_id: foreign key to reservations
product_id: foreign key to products
quantity: positive integer
primary key: (reservation_id, product_id)

5. Architecture and consistency

Use a simple layered application:
Controller: HTTP requests and responses.
Service: business rules and transaction boundaries.
Repository: database queries.
Entity: persisted data.
DTO: API request and response data.
Exception handler: consistent error responses.
- Use database transactions for reservation creation, confirmation, and cancellation. Lock affected product rows in a consistent order while checking and updating stock, so competing reservations cannot both consume the same available units.
- If a business rule fails, ensure all database changes from the operation roll back. Verify the locking and rollback behaviour with integration tests.

6. Implementation sequence
- Generate the Spring Boot project and commit this initial plan.
- Configure MySQL and create the schema and reproducible seed data.
- Implement entities, repositories, and inventory retrieval.
- Implement reservation creation and validation.
- Implement retrieval, confirmation, cancellation, and error handling.
- Add database-backed integration tests, including rollback and concurrency     scenarios.
- Run the application and demonstrate the API with curl.
- Complete README.md, AI_USAGE.md, and the retrospective.

7. Test plan
- Successful multi-product reservation lifecycle.
- Cancellation releases reserved stock exactly once.
- An unavailable product causes the entire request to fail.
- Invalid quantities, unknown products, and missing reservations are rejected.
- Invalid state transitions do not change inventory.
- Repeated confirmation and cancellation do not change stock twice.
- Records remain available after an application restart.
- Competing reservations cannot oversell a product.
- Tests use a real SQL database rather than mocked repositories for persistence and transaction behaviour.

8. Initial risks and trade-offs
- Incorrect transaction boundaries could leave partial updates.
- Unprotected stock checks could allow concurrent overselling.
- Database-specific locking behaviour must be verified against the selected MySQL version.
- A separate test database is needed to prevent tests from damaging sample data.

After implementation, I will record differences from this plan, remaining limitations, actual time spent, and possible improvements.