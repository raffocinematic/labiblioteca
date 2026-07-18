# Biblioteca Gestionale

Applicazione full stack per la gestione di una biblioteca.

Il progetto è diviso in:

- `backend`: API REST sviluppate con Spring Boot
- `frontend`: interfaccia web sviluppata con Angular

## Funzionalità

- CRUD libri
- Registrazione utente
- Login utente
- Password salvate con hash BCrypt
- Autenticazione tramite JWT
- Protezione backend degli endpoint privati con Spring Security
- Migrazioni database con Liquibase
- Documentazione API con Swagger/OpenAPI
- Frontend Angular con pagine Login, Register e gestione libri

## Stack Tecnologico

### Backend

- Java 17
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Security
- PostgreSQL
- Liquibase
- springdoc-openapi / Swagger UI
- Maven

### Frontend

- Angular
- TypeScript
- Reactive Forms
- Angular Router
- Angular HTTP Client

## Struttura del progetto

```text
biblioteca-gestionale/
├── backend/
│   ├── src/main/java/com/raffo/bibliotecabackend/
│   │   ├── auth/
│   │   ├── book/
│   │   ├── common/
│   │   ├── config/
│   │   └── user/
│   └── src/main/resources/db/
├── frontend/
│   └── src/app/
│       ├── core/
│       ├── features/
│       ├── layout/
│       └── shared/
├── docker-compose.yml
├── .env.example
└── README.md
```

## Prerequisiti

Installa sul tuo ambiente:

- Java 17
- Node.js
- npm
- PostgreSQL
- Git

## Configurazione ambiente

Copia il file `.env.example` in `.env`:

```powershell
Copy-Item .env.example .env
```

Poi modifica `.env` con i tuoi valori locali.

Esempio:

```env
POSTGRES_DB=biblioteca
POSTGRES_USER=postgres
POSTGRES_PASSWORD=change_me
POSTGRES_PORT=5432

SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/biblioteca
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=change_me

APP_JWT_SECRET=base64_secret_di_almeno_32_byte
APP_JWT_EXPIRATION_MINUTES=60

FRONTEND_PORT=4200
BACKEND_PORT=8080
```

Per generare una secret JWT valida da PowerShell:

```powershell
$bytes = New-Object byte[] 32
[System.Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
[Convert]::ToBase64String($bytes)
```

## Database

Il backend usa PostgreSQL.

Crea un database chiamato:

```text
biblioteca
```

Le tabelle vengono create automaticamente da Liquibase all'avvio del backend.

Migrazioni principali:

- `001-create-books-table.xml`
- `002-create-app-users-table.xml`

## Avvio Backend

Dalla root del progetto:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Il backend sarà disponibile su:

```text
http://localhost:8080
```

## Avvio Frontend

In un secondo terminale:

```powershell
cd frontend
npm install
npm start
```

Il frontend sarà disponibile su:

```text
http://localhost:4200
```

Il frontend usa `proxy.conf.json` per inoltrare le chiamate `/api` al backend su `localhost:8080`.

## Swagger / OpenAPI

Il backend espone automaticamente la documentazione degli endpoint tramite `springdoc-openapi`.

Con il backend avviato, Swagger UI è disponibile su:

```text
http://localhost:8080/swagger-ui.html
```

La specifica OpenAPI in formato JSON è disponibile su:

```text
http://localhost:8080/v3/api-docs
```

La specifica OpenAPI in formato YAML è disponibile su:

```text
http://localhost:8080/v3/api-docs.yaml
```

Swagger UI e la specifica OpenAPI sono endpoint pubblici configurati in Spring Security.
Gli endpoint applicativi privati, come le API dei libri, richiedono invece un JWT valido.

La dipendenza usata nel backend è:

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>3.0.3</version>
</dependency>
```

## Endpoint principali

### Auth

```text
POST /api/auth/register
POST /api/auth/login
```

Esempio registrazione:

```json
{
  "username": "utente",
  "password": "password123",
  "confirmPassword": "password123"
}
```

Esempio login:

```json
{
  "username": "utente",
  "password": "password123"
}
```

Risposta:

```json
{
  "token": "jwt_token",
  "username": "utente",
  "role": "ROLE_USER"
}
```

### Books

```text
GET    /api/catalog/books
GET    /api/catalog/books/{id}
POST   /api/catalog/books
PUT    /api/catalog/books/{id}
DELETE /api/catalog/books/{id}
```

Gli endpoint books richiedono l'header:

```http
Authorization: Bearer <token>
```

## Sicurezza

Le password non vengono salvate in chiaro nel database.

Durante la registrazione:

1. l'utente invia username e password
2. il backend valida i dati
3. la password viene trasformata in hash con BCrypt
4. viene salvato solo l'hash nel database

L'autenticazione usa JWT.

Dopo login o registrazione, il frontend salva il token e lo invia nelle chiamate successive con header:

```http
Authorization: Bearer <token>
```

Il backend valida il JWT con un filtro Spring Security dedicato.

Flusso lato backend:

1. `JwtAuthenticationFilter` legge l'header `Authorization`
2. estrae il token Bearer
3. `JwtService` valida firma e scadenza del token
4. dal token viene letto il `subject`, cioe' lo username
5. `AppUserDetailsService` carica l'utente dal database
6. il filtro crea un `UsernamePasswordAuthenticationToken`
7. il `SecurityContext` viene popolato per la request corrente
8. Spring Security permette l'accesso solo agli endpoint configurati come `authenticated()`

Endpoint pubblici:

```text
/api/auth/**
/swagger-ui.html
/swagger-ui/**
/v3/api-docs/**
```

Endpoint protetti:

```text
/api/catalog/books/**
anyRequest().authenticated()
```

Se il token manca, e' scaduto, e' malformato o non e' valido, il backend risponde con `401 Unauthorized` in formato JSON tramite `JwtAuthenticationEntryPoint`.

## Build

### Backend

```powershell
cd backend
.\mvnw.cmd test
```

### Frontend

```powershell
cd frontend
npm run build
```

## Note di sviluppo

File sensibili da non committare:

- `.env`
- password reali
- secret JWT reali
- credenziali database reali

Il file `.env.example` deve contenere solo valori di esempio.

## Stato del progetto

Il progetto è in fase di sviluppo e ha attualmente:

- gestione libri
- login/register
- autenticazione JWT
- protezione backend degli endpoint libri tramite Spring Security
- documentazione Swagger/OpenAPI

Prossimi possibili miglioramenti:

- refresh token
- ruoli admin/user
- gestione prestiti
- test unitari e integration test
- validazioni frontend più complete
