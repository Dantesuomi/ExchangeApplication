# Exchange Application
This application is written in Java 21 using Spring Boot and provides a RESTful API for managing currency exchange rates. 
It supports basic CRUD operations for exchange rates and integrates with MySQL for data storage and Redis for caching and resilience.
MySQL and Redis are integrated using Docker Compose, allowing for easy setup and deployment of the application.

## Cloning the Application
```
git clone https://github.com/Dantesuomi/ExchangeApplication.git
cd ExchangeApplication
```

## Running the Application
To run the application ```mysql``` and ```redis``` are required. You can use the provided `docker-compose.yaml` file to start these services which are already preconfigured.  

To build run the application, use the following command:  
```
docker compose --profile application up -d
```

Alternatively you can build the app with Maven and run with Java.  
Java OpenJDK (or other Java distribution) version 21 and maven must be present on system.  
Compose to start the application dependencies
```
docker compose up -d 
mvn clean install
java -jar target/ExchangeApplication-0.0.1-SNAPSHOT.jar 
```
## Testing
To execute the unit tests run
```
mvn test
```
## Application usage

__All operations require authentication__, except for client registration.  
__Authentication is done using Basic Auth with credentials provided during registration.__  
Example endpoint usage with curl:
```
curl -X GET 'http://localhost:8080/api/client/' -u johnsmith:RandomPassword12345
```

### 1. Register new Client. 
To register the new client, use POST request with following body:  
```POST``` ```/api/client/register```  
#### Body: 
```
{
  "email": "johnsmith@example.com",
  "password": "RandomPassword12345",
  "name": "John Smith",
  "username": "johnsmith"
}
```
Email must be unique and follow standard email format.  

Username must be unique and can contain only alphanumeric characters and underscores.

Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, one digit, and one special character.  

When the existing client tries to register with the same email or username, the application will return 409 Conflict status code.
If body is invalid, the application will return 400 Bad Request status code.  

When the client is successfully registered, the application will return 201 Created status code with the following body: 
#### Response:  
```
{
    "id": "7365f635-0c79-42c7-8c45-b6efbc76aa83",
    "email": "johnsmith@example.com",
    "name": "John Smith",
    "username": "johnsmith",
    "createdAt": "2025-06-15T14:49:07.140+00:00",
    "authorities": null,
    "credentialsNonExpired": true,
    "accountNonExpired": true,
    "accountNonLocked": true,
    "enabled": true
}
```
### 2. Get information about logged in Client.  

```GET``` ```/api/client/```
#### Body: None

#### Response:  
```
{
    "id": "7365f635-0c79-42c7-8c45-b6efbc76aa83",
    "email": "johnsmith@example.com",
    "name": "John Smith",
    "username": "johnsmith",
    "createdAt": "2025-06-15T14:49:07.140+00:00",
    "authorities": [
        {
            "authority": "ROLE_USER"
        }
    ],
    "credentialsNonExpired": true,
    "accountNonExpired": true,
    "accountNonLocked": true,
    "enabled": true
}
```
### 3. Create a new Account:  
To create new account, use POST request with following body:  
```POST``` ```/api/account/create```

#### Body:
```
{
    "currency": "CAD"
}
```  
Creates account, generates IBAN and returns the account information.  
Body must contain the currency code in ISO 4217 format. App supports 163 currencies, including but not limited to: AUD, CAD, EUR, GBP, JPY, USD.  

#### Response:
```
{
    "id": "8a19ba06-19d8-417c-bfa8-6401e4fe07e2",
    "currency": "CAD",
    "balance": 0,
    "clientId": "a93234ba-9ff8-4b73-a727-b04f3e5bbafa",
    "clientName": "John Smith",
    "iban": "LV36HABAJO2TKVAYM894E"
}
```  
If body is invalid, the application will return 400 Bad Request status code.

### 4. Get all Accounts of the Client:
```GET``` ```/api/account/{clientId}```
#### Body: None 
Valid clientId must be provided in the path.
#### Response:
```
[
    {
        "id": "17124279-f8ea-4b4e-9f3e-d50b0d3f3a5b",
        "currency": "GBP",
        "balance": 100.00,
        "clientId": "a93234ba-9ff8-4b73-a727-b04f3e5bbafa",
        "clientName": "John Smith",
        "iban": "LV83HABA397NC9TA6ERJS"
    },
    {
        "id": "39597665-1eda-4e2f-8530-9073ae84d6c2",
        "currency": "EUR",
        "balance": 1166.79,
        "clientId": "a93234ba-9ff8-4b73-a727-b04f3e5bbafa",
        "clientName": "John Smith",
        "iban": "LV87HABAPJHGX1SWFCRJU"
    }
]
```
When attempting to get accounts that does not belong to the logged in client, the application will return 403 Forbidden status code.  

### 5. Perform deposit for Account: 
To perform deposit, use POST request with following body:  
```POST``` ```/api/transaction/deposit```
#### Body:
```
{
    "accountIban": "LV23HABASAXMQ749DHCA1",
    "amount": 100
}
```
#### Response:
```
{
    "id": "a8459094-3bd6-4c88-970a-970c05620001",
    "currency": "EUR",
    "balance": 193.99,
    "clientId": "a93234ba-9ff8-4b73-a727-b04f3e5bbafa",
    "clientName": "John Smith",
    "iban": "LV23HABASAXMQ749DHCA1"
}
```
When attempting to deposit account that does not belong to the logged in client, the application will return 403 Forbidden status code.
#### Error Response:
```
{
    "httpStatus": "FORBIDDEN",
    "error": "You are not authorized perform action on this account"
}
```
### 6. Perform withdrawal for Account:
To perform withdrawal, use POST request with following body:  
```POST``` ```/api/transaction/withdraw```
#### Body:
```
{
    "accountIban": "LV23HABASAXMQ749DHCA1",
    "amount": 150
}
```
#### Response:
```
{
    "id": "a8459094-3bd6-4c88-970a-970c05620001",
    "currency": "EUR",
    "balance": 93.99,
    "clientId": "a93234ba-9ff8-4b73-a727-b04f3e5bbafa",
    "clientName": "John Smith",
    "iban": "LV23HABASAXMQ749DHCA1"
}
```
When attempting to deposit account that does not belong to the logged in client, the application will return 403 Forbidden status code.
If client's balance is not sufficient to perform the withdrawal, the application will return 400 Bad Request status code with the message "Insufficient balance".
#### Error Responses:
```
{
    "httpStatus": "BAD_REQUEST",
    "error": "Insufficient balance"
}
```  
```
{
    "httpStatus": "FORBIDDEN",
    "error": "You are not authorized perform action on this account"
}
```
### 7. Perform transfer between accounts:
To perform transfer between accounts, use POST request with following body:  
```POST``` ```/api/transaction/transfer```
#### Body:
```
{
  "sourceAccountNumber": "LV23HABASAXMQ749DHCA1",
  "destinationAccountNumber": "LV83HABA397NC9TA6ERJS",
  "amount": 100,
  "description": "For the pizza",
  "destinationCurrency": "GBP"
}
```
#### Response:
```
{
    "transferStatus": "SUCCESSFUL",
    "message": "Transfer Performed Successfully"
}
```
If source account does not belong to the logged in client, the application will return 403 Forbidden status code.  
In addition, if source or destination account does not exist, the application will return 404 Not Found status code.  

Description must not be null.  

If source account balance is not sufficient to perform the transfer, the application will return 400 Bad Request status code with the message "Insufficient balance".  

On the condition that source and destination accounts are identical, the application will return 400 Bad Request status code with the message "Source and destination accounts are the same".  

In case of mismatch between source and destination currencies, the application will return 400 Bad Request status code with the message "Source and destination currencies do not match".

#### Error Responses:
```
{
    "transferStatus": "FAILED",
    "message": "Source account not found"
}
```
```
{
    "transferStatus": "FAILED",
    "message": "Destination account not found"
}
```
```
{
    "transferStatus": "FAILED",
    "message": "Insufficient balance"
}
```  
```
{
    "transferStatus": "FAILED",
    "message": "You are not authorized perform action on this account"
}
```
```
{
    "transferStatus": "FAILED",
    "message": "Source and destination account are identical"
}
```
```
{
    "transferStatus": "FAILED",
    "message": "The currency of funds in the transfer operation must match the receiver's account currency"
}
```
If source account currency does not match the destination account currency, the application will perform currency conversion using the latest exchange rate from third-party API and return the transfer status.  

The exchange rate is cached in Redis for 60 minutes to reduce the number of requests to the third-party API and make rates available even if API is not reachable.

### 8. Get all Transactions of the Account:
```GET``` ```/api/transaction/{accountId}?limit=10&offset=0```
#### Body: None
#### Parameters: limit, offset
Valid accountId must be provided in the path.  

Endpoint supports pagination with limit and offset parameters, which are optional. 

Limit defaults to 10 and stands for the maximum number of transactions to return, while offset defaults to 0 and indicates the starting point for the transactions to return.
#### Response:
```
{
    "content": [
        {
            "id": "788b9f58-0f9b-4ded-80fb-baffd03b4c91",
            "timestamp": "2025-06-15T18:45:33.689748",
            "description": "For the pizza",
            "sourceAccount": {
                "id": "a8459094-3bd6-4c88-970a-970c05620001",
                "iban": "LV23HABASAXMQ749DHCA1"
            },
            "destinationAccount": {
                "id": "17124279-f8ea-4b4e-9f3e-d50b0d3f3a5b",
                "iban": "LV83HABA397NC9TA6ERJS"
            },
            "sourceCurrencyCode": "EUR",
            "transactionOperation": "TRANSFER",
            "destinationCurrencyCode": "GBP",
            "sourceAmountDebited": 1.18,
            "destinationAmountCredited": 1.00,
            "transferType": "SENT"
        },
        {
            "id": "d793c5a9-f64f-4020-9e42-0e75c9f3136e",
            "timestamp": "2025-06-15T18:16:04.406556",
            "description": null,
            "sourceAccount": null,
            "destinationAccount": {
                "id": "a8459094-3bd6-4c88-970a-970c05620001",
                "iban": "LV23HABASAXMQ749DHCA1"
            },
            "sourceCurrencyCode": null,
            "transactionOperation": "DEPOSIT",
            "destinationCurrencyCode": "EUR",
            "sourceAmountDebited": 0.00,
            "destinationAmountCredited": 100.00,
            "transferType": "RECEIVED"
        }
    ],
    "pageable": {
        "pageNumber": 0,
        "pageSize": 2,
        "sort": {
            "empty": false,
            "sorted": true,
            "unsorted": false
        },
        "offset": 0,
        "paged": true,
        "unpaged": false
    },
    "last": false,
    "totalPages": 14,
    "totalElements": 27,
    "size": 2,
    "number": 0,
    "sort": {
        "empty": false,
        "sorted": true,
        "unsorted": false
    },
    "numberOfElements": 2,
    "first": true,
    "empty": false
}
```
When attempting to get transactions of the account that does not belong to the logged in client, the application will return 403 Forbidden status code.

In addition, if account does not exist, the application will return 404 Not Found status code.
