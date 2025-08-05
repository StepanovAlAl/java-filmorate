# Filmorate

## Структура Базы Данных
```
+----------------+       +-------------------+       +----------------+
|     FILMS      |       |   FILM_GENRES     |       |     GENRES     |
+----------------+       +-------------------+       +----------------+
| PK: id         |------>| PK: id            |       | PK: id         |
|    name        |  |    | FK: film_id       |<------|    name        |
|    description |  |    | FK: genre_id      |       +----------------+
|    release_date|  |    +-------------------+
|    duration    |  |                         
|    mpa_id      |--|    +-------------------+
+----------------+  |    |       MPA         |
                    |    +-------------------+
+----------------+  |    | PK: id            |
|     USERS      |  |    |    name           |
+----------------+  |    |    description    |
| PK: id         |  |    +-------------------+
|    email       |  |                         
|    login       |  |    +-------------------+
|    name        |  |    |    FRIENDSHIP     |
|    birthday    |  |    +-------------------+
+----------------+  |    | PK: id            |
                    |    | FK: user_id       |
                    |----| FK: friend_id     |
                         |    status         |
                         |    (confirmed)    |
                         +-------------------+
```




## Структура каталогов
```
├── 📁 src
│   ├── 📁 main
│   │   ├── 📁 java
│   │   │   └── 📁 ru
│   │   │       └── 📁 yandex
│   │   │           └── 📁 practicum
│   │   │               └── 📁 filmorate
│   │   │                   ├── 📁 controller
│   │   │                   │   ├── FilmController.java
│   │   │                   │   ├── MpaController.java
│   │   │                   │   └── UserController.java
│   │   │                   │
│   │   │                   ├── 📁 exception
│   │   │                   │   ├── GlobalExceptionHandler.java
│   │   │                   │   ├── NotFoundException.java
│   │   │                   │   └── ValidationException.java
│   │   │                   │
│   │   │                   ├── 📁 model
│   │   │                   │   ├── Film.java
│   │   │                   │   ├── Genre.java
│   │   │                   │   ├── Mpa.java
│   │   │                   │   └── User.java
│   │   │                   │
│   │   │                   ├── 📁 service
│   │   │                   │   └── UserService.java
│   │   │                   │
│   │   │                   ├── 📁 storage
│   │   │                   │   ├── 📁 film
│   │   │                   │   │   ├── FilmDbStorage.java
│   │   │                   │   │   ├── FilmStorage.java
│   │   │                   │   │   └── InMemoryFilmStorage.java
│   │   │                   │   │
│   │   │                   │   ├── 📁 user
│   │   │                   │   │   ├── InMemoryUserStorage.java
│   │   │                   │   │   ├── UserDbStorage.java
│   │   │                   │   │   ├── UserStorage.java
│   │   │                   │   │   └── UserRowMapper.java
│   │   │                   │   │
│   │   │                   │   └── 📁 mappers
│   │   │                   │       └── FilmRowMapper.java
│   │   │                   │
│   │   │                   └── 📁 validation
│   │   │                       ├── MinReleaseDate.java
│   │   │                       ├── MinReleaseDateValidator.java
│   │   │                       └── ValidationGroups.java
│   │   │
│   │   └── 📁 resources
│   │       ├── application.properties
│   │       ├── application-dev.properties
│   │       ├── lombok.config
│   │       └── schema.sql
│   │
│   └── 📁 test
│       └── 📁 java
│           └── 📁 ru
│               └── 📁 yandex
│                   └── 📁 practicum
│                       └── 📁 filmorate
│                           └── 📁 storage
│                               ├── 📁 film
│                               │   └── FilmDbStorageTest.java
│                               └── 📁 user
│                                   └── UserDbStorageTest.java
│
├── 📄 lombok.config
├── 📄 pom.xml
├── 📄 checkstyle.xml
├── 📄 README.md
```