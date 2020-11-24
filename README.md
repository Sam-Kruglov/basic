# Base
This is a base/template for any app that requires user-role-based security.
It is built on top of Spring Boot 2.4 using the following tech:
- SQL database (H2)
- schema versioning (Flyway)
- ORM (Hibernate)
- cache (Hazelcast)
- servlets (Spring MVC)
- JWT security (Spring Security and a couple of Spring OAuth2 dependencies)
- automatic OpenAPI v3 documentation (SpringDoc) + UI (Swagger) + generated API Client (openapi-generator)
- integration tests (nothing mocked) on the API level using the generated client