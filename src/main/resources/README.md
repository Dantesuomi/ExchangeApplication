# Exchange Application

## Running the Application
To run the application ```mysql``` and ```redis``` are required. You can use the provided `docker-compose.yaml` file to start these services which are already preconfigured.  

To build run the application, use the following command:  
```docker compose up -d```

Alternatively you can build the app with Maven and run with Java  
Compose to start the application dependencies
```
docker compose up -d
mvn clean install
java -jar target/ExchangeApplication-0.0.1-SNAPSHOT.jar --server.port=8081
```