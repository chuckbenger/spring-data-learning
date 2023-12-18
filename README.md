# spring-data-learning

### Introduction

The goal of this to project is to document some learnings about Spring Data, Hibernate and JPA

### Start Here

[Spring Data](https://spring.io/projects/spring-data) is a huge ecosystem of different components.
One of those is Spring Data JPA (Java Persistence API). Spring provides an awesome guide to get started with
JPA [here](https://docs.spring.io/spring-data/jpa/reference/jpa/getting-started.html) using both Java and Kotlin
examples.

While you don't need to read all of this right off the bat, I would read the following sections
on the getting started page before diving into this learning.

- Core Concepts
- Defining Repository Interfaces
- Configuration
- Persisting Entities
- Defining Query Methods

### Running the Application

#### Pre-requisites

- Docker installed and running

The repo is using `spring-boot-docker-compose` to sping up a postgres database to test against

#### Sections

Under the tests folder there are several test classes that can be run to see the different aspects JPA/Hibernate

- [EntityManagementTest](src/test/kotlin/com/example/wsspringdatalearn/jpa/EntityManagementTests.kt)
- [InsertionPerformanceTest](src/test/kotlin/com/example/wsspringdatalearn/jpa/InsertionPerformanceTests.kt)
- [ImplicitFlushingBehaviorTest](src/test/kotlin/com/example/wsspringdatalearn/jpa/ImplicitFlushingBehaviourTests.kt)

#### Configuration

Updating

If you want to see the SQL that Hibernate is generating you can enable the following property in the
`application.yml` file

```yaml
logging:
  level:
    org:
      hibernate:
        SQL: trace
        type:
          descriptor:
            sql:
              BasicBinder: trace
```

test  4