# Order Management System

A Spring Boot-based e-commerce order management system that provides REST APIs for managing customers, products, and orders.

## Features

- Customer Management (CRUD operations)
- Product Management with stock tracking
- Order Management with stock validation
- Search functionality for orders
- OpenAPI/Swagger documentation
- Global exception handling
- Transaction management
- Data validation

## Technologies

- Java 17
- Spring Boot 3.2.3
- Spring Data JPA
- PostgreSQL
- Lombok
- OpenAPI/Swagger
- JUnit & Mockito for testing

## Getting Started

### Prerequisites

- JDK 17 or later
- Maven 3.6 or later
- PostgreSQL 12 or later

### Database Setup

1. Create a PostgreSQL database:
```sql
CREATE DATABASE oms_db;
```

2. Configure database connection in `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/oms_db
spring.datasource.username=postgres
spring.datasource.password=postgres
```

### Building and Running

1. Clone the repository:
```bash
git clone https://github.com/aliabbastatli/order-management-system.git
cd order-management-system
```

2. Build the project:
```bash
mvn clean install
```

3. Run the application:
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## API Documentation

Access the Swagger UI at: `http://localhost:8080/swagger-ui.html`

### API Endpoints

#### Customer Management
- POST `/api/customers` - Create a new customer
- PUT `/api/customers/{id}` - Update an existing customer
- DELETE `/api/customers/{id}` - Delete a customer
- GET `/api/customers/{id}` - Get a customer by ID
- GET `/api/customers` - Get all customers
- GET `/api/customers/email/{email}` - Get a customer by email

#### Product Management
- POST `/api/products` - Create a new product
- PUT `/api/products/{id}` - Update an existing product
- DELETE `/api/products/{id}` - Delete a product
- GET `/api/products/{id}` - Get a product by ID
- GET `/api/products` - Get all products
- PATCH `/api/products/{id}/stock` - Update product stock
- GET `/api/products/in-stock` - Get all products in stock

#### Order Management
- POST `/api/orders` - Create a new order
- GET `/api/orders/{id}` - Get an order by ID
- GET `/api/orders` - Get all orders
- GET `/api/orders/customer/{customerId}` - Get orders by customer
- GET `/api/orders/search` - Search orders by criteria
- DELETE `/api/orders/{id}` - Delete an order

## Example API Requests

### Create a Customer
```json
POST /api/customers
{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "phone": "+90 555 123 4567"
}
```

### Create a Product
```json
POST /api/products
{
    "name": "Smartphone",
    "description": "Latest model smartphone",
    "price": 999.99,
    "stockQuantity": 50
}
```

### Create an Order
```json
POST /api/orders
{
    "customerId": "123e4567-e89b-12d3-a456-426614174000",
    "orderItems": [
        {
            "productId": "123e4567-e89b-12d3-a456-426614174001",
            "quantity": 2
        }
    ]
}
```

## Transaction Management

The application uses Spring's declarative transaction management with the following strategies:

- Pessimistic locking for stock updates to prevent overselling
- Read-only transactions for queries to optimize performance
- Transaction rollback on exceptions

## Error Handling

The application includes a global exception handler that provides consistent error responses:

- 400 Bad Request - For validation errors
- 404 Not Found - For resource not found
- 500 Internal Server Error - For unexpected errors

Error response format:
```json
{
    "status": "BAD_REQUEST",
    "timestamp": "dd-MM-yyyy HH:mm:ss",
    "message": "Error message",
    "debugMessage": "Detailed error message",
    "errors": ["Validation error 1", "Validation error 2"]
}
```

## Testing

Run the tests using:
```bash
mvn test
```

The project includes:
- Unit tests for services
- Integration tests for repositories
- API tests for controllers

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request
