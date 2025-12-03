# Appointment Authentication Service
- This is the service responsible for managing:
  - User Registration
  - Password Recovery
  - Generate access tokens
- It is build on top of [Spring Authorization Server](https://spring.io/projects/spring-authorization-server) making use of user details persisted on the managed db.
  - Spring Authorization Server allows users to be persisted to any dbms. All it needs it the ``UserDetailsService`` to authenticate users and ``PasswordEncoder`` to salt passwords on the bean.
- The tables for OAuth2 Server Authorization clients are persisted to db using [Flyway](https://www.red-gate.com/products/flyway/)
  - The public client is preloaded on application start for UI.
- Services that require data from this service must be clients that make requests using [gRPC](https://grpc.io/) protocol.

### Existing public Docker Image
- There is an already existing public image you can use without building the new one if you not making code changes:
  - Image - ```docker.io/menelismthembu12/appointment-auth-server```
  - Tag - ```1.0.5```
- The service allows config to be externalized using config-server.
  - The seeded admin user for testing if admin can ``CONFIRM`` or ``CANCEL`` appointment. Customers can register via the system
```json
{
  "firstName": "user",
  "lastName": "admin",
  "email": "user@admin.com",
  "contactNo": "1234569875",
  "password": "useradmin$$$1234"
}
```
```yaml
infrastructure:
  env: dev
  DB:
    USERNAME: {db_username}
    PASSWORD: {db_username}

spring:
  application:
    name: auth-server
  cloud:
    config:
      enabled: false
  datasource:
    url: jdbc:postgresql://localhost:5432/{auth_db}
    username: ${infrastructure.DB.USERNAME}
    password: ${infrastructure.DB.PASSWORD}
  flyway:
    schemas: public
    baseline-version: 2.0.0 # The increment is based on existing tables
    baseline-description: Base Migration
    enabled: true
    baseline-on-migrate: true
    ignore-migration-patterns:
      - "*:missing"
    clean-disabled: false
  jpa:
    hibernate:
      ddl-auto: update
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        enable_lazy_load_no_trans: true
  kafka:
    bootstrap-servers:
      - localhost:9092
      - localhost:9093
      - localhost:9094
      - localhost:9095
      - localhost:9096
      - localhost:9097
    producer:
      acks: -1
      batch-size: 100
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      compression-type: zstd
springdoc:
  api-docs:
    path: /auth-service/v3/api-docs
  swagger-ui:
    path: /auth-service/swagger-ui.html
    operations-sorter: method
    doc-expansion: none

app:
  email-template:
    verify-email-template: Hi %s,<br/><br/> Welcome to Appointment System.To complete registration process click <a href='%s'>here</a> to verify your account.<br/></br/><br/><br/> %s
    reset-password-email-template: Hi %s,<br/><br/> We have received your request to reset your password.Please click <a href='%s'>here</a> to reset your password.<br/></br/><br/> %s
  cors:
    allowed-origins:
      - http://ui:4200
    allowed-methods:
      - POST
      - GET
      - PUT
      - OPTIONS
      - DELETE
      - PATCH
    allowed-headers:
      - "*"
    max-age: 3600
  white-list:
    - "/auth-service/v3/api-docs/**"
    - "/auth-service/swagger-ui/**"
    - "/api/v1/auth/**"
  custom-exposed-endpoints:
    - "/api/v1/auth/**"
  open-api:
    info:
      title: Appointment Auth Service {infrastructure.env}
      description: Appointment Authentication Service
      version: 1.0.0
  verification-token:
    expiration-ms: 86400000
  kafka:
    notification-topic: {infrastructure.env}-appointment-notifications
  encryption-key: "{encryption key}"
  registered-clients:
    -
      # Public Client for UI
      client-id: {infrastructure.env}-appointment-ui-client
      client-authentication-methods:
        - "none"
      authorization-grant-types:
        - "authorization_code"
        - "refresh_token"
      redirect-uris:
        - "http://ui:4200/callback"
      post-logout-redirect-uris:
        - "http://ui:4200/account/sign-out"
      scopes:
        - "openid"
        - "profile"
        - "email"
      client-settings:
        require-proof-of-key: true
      token-setting:
        access-token-time-to-live: 120 # in minutes
        refresh-token-time-to-live: 1 # in days
  client-url: http://ui:4200/# #The hash represent hashLocationStrategy used by angular
# gRPC Server Config
grpc:
  server:
    port: 9090
```

