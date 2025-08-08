# Vodovod Management System

Aplikacija za upravljanje vodovodom izgrađena u Spring Boot-u s Thymeleaf frontend-om.

## Funkcionalnosti

### Za administratore:
- **Dashboard** - pregled statistika (broj korisnika, računa, prihoda)
- **Upravljanje korisnicima** - dodavanje, uređivanje, pregled korisnika vodovoda
- **Očitanja vodomjera** - unos novih očitanja s validacijom
- **Računi** - generiranje i upravljanje računima, PDF export
- **Uplate** - knjiženje uplata po računima
- **Admin panel** - postavke sustava (cijene, fiksne naknade, broj računa)

### Za korisnike:
- **Moji računi** - pregled svojih računa
- **Profil** - pregled osobnih podataka

## Pokretanje aplikacije

### Preduvjeti
- Java 17+
- Maven 3.6+

### Instalacija i pokretanje

1. **Kloniraj projekt:**
   ```bash
   git clone <repository-url>
   cd vodovod-management
   ```

2. **Kompajiraj aplikaciju:**
   ```bash
   mvn clean package
   ```

3. **Pokreni aplikaciju:**
   ```bash
   java -jar target/vodovod-management-0.0.1-SNAPSHOT.jar
   ```

4. **Pristupi aplikaciji:**
   Aplikacija je dostupna na: `http://localhost:8080`

## Početni korisnici

Aplikacija automatski kreira početne korisnike:

### Administrator
- **Korisničko ime:** `admin`
- **Lozinka:** `admin123`
- **Rola:** Administrator (potpun pristup sustavu)

### Test korisnik
- **Korisničko ime:** `user1`
- **Lozinka:** `user123`
- **Rola:** Korisnik (može pregledavati svoje račune)
- **Broj vodomjera:** VM001

## Konfiguracija baze podataka

Aplikacija koristi H2 in-memory bazu podataka za svilacke potrebe. 

Za produkciju možete konfigurirati vanjsku bazu (PostgreSQL, MySQL) u `application.properties`:

```properties
# Za PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/vodovod
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

## H2 Database Console

Za pregled baze podataka tijekom razvoja:
- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:file:./data/vodovod`
- Username: `sa`
- Password: `password`

## Arhitektura

### Backend
- **Spring Boot 3.2.0** - glavný framework
- **Spring Security** - autentifikacija i autorizacija
- **Spring Data JPA** - ORM i baza podataka
- **H2 Database** - baza podataka
- **iText PDF** - generiranje PDF računa

### Frontend
- **Thymeleaf** - template engine
- **Bootstrap 5** - CSS framework
- **Bootstrap Icons** - ikone

### Model podataka
- **User** - korisnici sustava (admin i korisnici vodovoda)
- **MeterReading** - očitanja vodomjera
- **Bill** - računi za vodu
- **Payment** - uplate
- **SystemSettings** - sistemske postavke

## API Endpoints

### Javni
- `GET /login` - stranica za prijavu
- `POST /login` - prijava korisnika

### Za administratore
- `GET /dashboard` - početna stranica
- `GET /users` - lista korisnika
- `GET /readings` - očitanja
- `GET /bills` - računi
- `GET /payments` - uplate
- `GET /settings` - postavke

### Za korisnike
- `GET /my-bills` - moji računi
- `GET /my-account` - moj profil

## Sigurnost

- Lozinke su enkriptirane pomoću BCrypt
- Session-based autentifikacija
- Role-based pristupna kontrola
- CSRF zaštita

## Razvojni workflow

1. **Dodavanje novog korisnika** (Admin)
   - Admin kreira korisnika s osnovnim podacima
   - Može se dodijeliti broj vodomjera
   - Generirat će se automatska lozinka ili poslati na email

2. **Unos očitanja** (Admin)
   - Odabire se korisnik
   - Unosi se novo očitanje s datumom
   - Sustav validira da očitanje nije manje od prethodnog
   - Računa se potrošnja

3. **Generiranje računa** (Admin)
   - Može se generirati pojedinačno ili grupno
   - Račun uključuje potrošnju i paušalnu naknadu
   - PDF eksport dostupan

4. **Knjiženje uplate** (Admin)
   - Unosi se iznos uplate za određeni račun
   - Sustav automatski ažurira status računa

## Buduće značajke

- Email obavještenja
- Automatsko generiranje računa
- Izvještaji i statistike
- Import/export podataka
- Mobile responsive design

## Podrška

Za pitanja i podršku kontaktirajte razvojni tim.

---

**Verzija:** 1.0.0  
**Zadnja ažuriranja:** Prosinac 2024