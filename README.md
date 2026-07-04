# Biblioteca Gestionale

Gestionale full stack per una biblioteca, realizzato come progetto di studio con backend Java/Spring Boot e frontend Angular/TypeScript.

## Stack Tecnologico

Backend:

- Java 17
- Spring Boot
- Spring Web MVC per API REST
- Spring Data JPA per persistenza
- Hibernate come provider JPA
- PostgreSQL come database relazionale
- Liquibase per versionamento schema database
- Maven come build tool
- Maven Wrapper incluso nel modulo backend

Frontend:

- Angular
- TypeScript
- Reactive Forms
- Angular Router
- Angular HttpClient
- SCSS
- npm come package manager

Tooling:

- IntelliJ IDEA
- pgAdmin 4 per PostgreSQL
- HTTP Client `.http` di IntelliJ per test API
- PowerShell per avvio e test manuali

## Struttura Del Progetto

```text
biblioteca-gestionale/
  pom.xml
  README.md
  docker-compose.yml
  .env.example

  backend/
    pom.xml
    mvnw
    mvnw.cmd
    http/
      books-api.http
    src/
      main/
        java/com/raffo/bibliotecabackend/
          BackendApplication.java
          book/
          common/
          config/
        resources/
          application.properties
          db/
            changelog/
              db.changelog-master.xml
            migration/
              001-create-books-table.xml

  frontend/
    package.json
    angular.json
    proxy.conf.json
    src/app/
      core/
      features/
      layout/
      shared/
```

Il `pom.xml` nella root e' un aggregatore Maven. Il backend ha il proprio `pom.xml` reale con dipendenze Spring. Il frontend non usa Maven: usa `package.json`, `angular.json` e npm.

## Backend

Il backend espone API REST sotto:

```text
http://localhost:8080/api
```

Il dominio implementato finora e' `book`, cioe' la gestione dei libri.

Struttura principale:

```text
book/
  Book.java
  BookController.java
  BookService.java
  BookRepository.java
  dto/
    BookRequest.java
    BookResponse.java
```

Responsabilita':

- `Book.java`: entity JPA collegata alla tabella `books`.
- `BookController.java`: riceve le richieste HTTP.
- `BookService.java`: contiene la logica applicativa.
- `BookRepository.java`: accede al database tramite Spring Data JPA.
- `BookRequest.java`: payload in ingresso per create/update.
- `BookResponse.java`: payload restituito al frontend.

API disponibili:

```text
GET    /api/books
GET    /api/books/{id}
POST   /api/books
PUT    /api/books/{id}
DELETE /api/books/{id}
```

## Gestione Errori Backend

La gestione errori e' centralizzata in:

```text
common/exception/
  ApiError.java
  GlobalExceptionHandler.java
  NotFoundException.java
  ConflictException.java
```

Errori gestiti:

- `404 Not Found` quando un libro non esiste.
- `409 Conflict` quando si tenta di usare un ISBN gia' presente.
- `400 Bad Request` quando la validazione del request body fallisce.

## Database

Database usato:

```text
PostgreSQL
```

Database locale:

```text
biblioteca
```

Configurazione principale:

```text
backend/src/main/resources/application.properties
```

Liquibase gestisce lo schema del database. Hibernate non crea tabelle: valida soltanto lo schema.

Configurazione importante:

```properties
spring.jpa.hibernate.ddl-auto=validate
spring.liquibase.enabled=true
spring.liquibase.change-log=classpath:/db/changelog/db.changelog-master.xml
```

Changelog master:

```text
backend/src/main/resources/db/changelog/db.changelog-master.xml
```

Prima migration:

```text
backend/src/main/resources/db/migration/001-create-books-table.xml
```

Tabelle create da Liquibase:

```text
books
databasechangelog
databasechangeloglock
```

## Frontend

Il frontend Angular gira su:

```text
http://localhost:4200
```

La pagina libri si trova in:

```text
frontend/src/app/features/books/
```

Il service HTTP per comunicare col backend e':

```text
frontend/src/app/core/services/book-api.service.ts
```

Il model TypeScript e':

```text
frontend/src/app/core/models/book.model.ts
```

Il proxy Angular e' configurato in:

```text
frontend/proxy.conf.json
```

Serve a inoltrare:

```text
/api
```

verso:

```text
http://localhost:8080
```

In questo modo il frontend chiama `/api/books` senza dover scrivere direttamente l'URL del backend.

## Avvio Del Progetto

Backend:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Frontend:

```powershell
cd frontend
npm install
npm start
```

URL:

```text
Backend:  http://localhost:8080
Frontend: http://localhost:4200
```

## Test API

Le API possono essere testate con:

- IntelliJ HTTP Client
- PowerShell
- Postman
- pgAdmin per verifica dati lato database

File HTTP Client:

```text
backend/http/books-api.http
```

Esempio:

```http
GET http://localhost:8080/api/books
```

## Stato Attuale

Completato:

- struttura monorepo con backend e frontend;
- backend Spring Boot configurato;
- PostgreSQL collegato;
- Liquibase configurato;
- tabella `books` versionata via migration;
- CRUD backend dei libri;
- gestione errori base;
- frontend Angular collegato al backend tramite proxy;
- pagina Angular libri in lavorazione con lista, form, create, update e delete.

Da fare:

- sistemare e rifinire pagina CRUD Angular;
- aggiungere test automatici backend;
- aggiungere autori;
- aggiungere iscritti/utenti biblioteca;
- aggiungere prestiti;
- aggiungere restituzioni;
- aggiungere prenotazioni;
- aggiungere autenticazione/autorizzazione in una fase successiva.
