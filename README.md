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
- Migrazioni database con Liquibase
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
Prerequisiti
Installa sul tuo ambiente:
•
Java 17
•
Node.js
•
npm
•
PostgreSQL
•
Git
Configurazione ambiente
Copia il file .env.example in .env:
Copy-Item .env.example .env
Poi modifica .env con i tuoi valori locali.
Esempio:
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
Per generare una secret JWT valida da PowerShell:
$bytes = New-Object byte[] 32
[System.Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
[Convert]::ToBase64String($bytes)
Database
Il backend usa PostgreSQL.
Crea un database chiamato:
biblioteca
Le tabelle vengono create automaticamente da Liquibase all’avvio del backend.
Migrazioni principali:
•
001-create-books-table.xml
•
002-create-app-users-table.xml
Avvio Backend
Dalla root del progetto:
cd backend
.\mvnw.cmd spring-boot:run
Il backend sarà disponibile su:
http://localhost:8080
Avvio Frontend
In un secondo terminale:
cd frontend
npm install
npm start
Il frontend sarà disponibile su:
http://localhost:4200
Il frontend usa proxy.conf.json per inoltrare le chiamate /api al backend su localhost:8080.
Endpoint principali
Auth
POST /api/auth/register
POST /api/auth/login
Esempio registrazione:
{
  "username": "utente",
  "password": "password123",
  "confirmPassword": "password123"
}
Esempio login:
{
  "username": "utente",
  "password": "password123"
}
Risposta:
{
  "token": "jwt_token",
  "username": "utente",
  "role": "ROLE_USER"
}
Books
GET /api/books
GET /api/books/{id}
POST /api/books
PUT /api/books/{id}
DELETE /api/books/{id}
Sicurezza
Le password non vengono salvate in chiaro nel database.
Durante la registrazione:
1.
l’utente invia username e password
2.
il backend valida i dati
3.
la password viene trasformata in hash con BCrypt
4.
viene salvato solo l’hash nel database
L’autenticazione usa JWT.
Dopo login o registrazione, il frontend salva il token e lo invia nelle chiamate successive con header:
Authorization: Bearer <token>
Build
Backend
cd backend
.\mvnw.cmd test
Frontend
cd frontend
npm run build
Note di sviluppo
File sensibili da non committare:
•
.env
•
password reali
•
secret JWT reali
•
credenziali database reali
Il file .env.example deve contenere solo valori di esempio.
Stato del progetto
Il progetto è in fase di sviluppo e ha attualmente:
•
gestione libri
•
login/register
•
base di autenticazione JWT
Prossimi possibili miglioramenti:
•
protezione completa degli endpoint libri lato backend
•
refresh token
•
ruoli admin/user
•
gestione prestiti
•
test unitari e integration test
•
validazioni frontend più complete