# Store Management Service

Summary of Work Done

1. Added the service layer to handle all business logic and remove tight coupling between controllers and repositories
2. Refactored controllers to remove business logic and keep controllers lean and clean
3. Added some basic validation logic
4. Added some paging on Customer repository and caching on the Customer service to take care of perfomance issues
5. Updated entities to introduce the product enntity
6. Updated database schema(schema.sql) to align with the updated entities
7. Updated data scripts(data.sql) to create some data for the new tables
8. Added scripts to reset sequences as creating new records on the DB was failing
9. Introduced some exceptions handling and also the GlobalExceptionHandler to handle all application exceptions
10. Added some comprehensive unit tests
11. Added jacoco configuration to enforce test coverage checks
12. Added some basic logging
13. Added Swagger Configuration for API Documentation 
14. For the API documentation go to http://localhost:8080/swagger-ui/index.html#
15. Under the resources/postman folder there's a postman collection that you can use to test all the endpoints

## Tools Used

| Tool               | Purpose                          | 
|--------------------|----------------------------------|
| Intellij           | IDE                              |
| Gradle             | Build Tool                       |
| Git                | Version control system           |
| Java 17            | Java version used                |
| Mapstruct          | Mapping/conversion of DTOs       |
| Liquibase          | DB SQL Scripts Migration Tool    |
| Postgres           | Database Engine                  |
| JPA/JPL            | ORM Tool                         |
| Lombok             | Reduce boilerplate code          |
| SpringDoc Open API | API Documentation                |
| JUNIT Jupiter      | Unit testing                     |
| Mockito            | Mocking framework for unit tests |
| Jacoco             | Test coverage checks             |

Future considerations
1. Security(Authentication and Authorization)
2. Auditing(Tracking who creates, deletes and update records in the DB and the timestamps it all happens)
3. Reporting capabilities
