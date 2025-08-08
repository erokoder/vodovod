# Vodovod Aplikacija

Moderna web aplikacija za upravljanje vodovodnim uslugama, razvijena u Python Flask frameworku.

## 🚰 Funkcionalnosti

- **Upravljanje korisnicima** - dodavanje, uređivanje i praćenje korisnika vodovodnih usluga
- **Očitanja vodomjera** - unos i praćenje potrošnje vode za sve korisnike
- **Upravljanje računima** - automatsko generiranje računa na temelju potrošnje
- **Praćenje plaćanja** - označavanje računa kao plaćenih
- **Dashboard s statistikama** - pregled ključnih pokazatelja
- **Korisnički sustav** - sigurna prijava i upravljanje operatorima

## 🛠️ Tehnologije

- **Backend**: Python Flask
- **Frontend**: HTML5, CSS3, JavaScript, Bootstrap 5
- **Baza podataka**: SQLite
- **Autentifikacija**: Flask-Login
- **Forme**: Flask-WTF

## 📦 Instalacija

### Preduvjeti
- Python 3.7+
- pip (Python package manager)

### Pokretanje aplikacije

1. **Klonirajte ili preuzmite projekt**
   ```bash
   git clone <repository-url>
   cd vodovod-app
   ```

2. **Instalirajte potrebne pakete**
   ```bash
   pip install -r requirements.txt
   ```

3. **Pokrenite aplikaciju**
   ```bash
   python app.py
   ```

4. **Otvorite web preglednik**
   ```
   http://localhost:5000
   ```

## 👤 Prijava

**Admin pristup:**
- Korisničko ime: `admin`
- Lozinka: `admin123`

## 📱 Korištenje

### 1. Dashboard
- Pregled ukupnog broja korisnika
- Broj neplaćenih računa
- Ukupan iznos dugovanja
- Brze akcije za dodavanje korisnika i očitanja

### 2. Upravljanje korisnicima
- Dodavanje novih korisnika s osnovnim podacima
- Pregled svih aktivnih korisnika
- Uređivanje postojećih korisnika

### 3. Očitanja vodomjera
- Unos novih očitanja vodomjera
- Automatski izračun potrošnje
- Pregled povijesti očitanja

### 4. Računi
- Generiranje računa za određeni period
- Označavanje računa kao plaćenih
- Pregled svih računa s filtriranjem

## 🗄️ Struktura baze podataka

### Tablice:
- **User** - korisnički sustav (operatori/administratori)
- **Korisnik** - korisnici vodovodnih usluga
- **Ocitanje** - očitanja vodomjera
- **Racun** - računi za vodu

## 🔧 Konfiguracija

Aplikacija koristi sljedeće zadane postavke:
- **SQLite baza**: `vodovod.db`
- **Cijena po m³**: 2.50 kn (može se mijenjati pri generiranju računa)
- **Port**: 5000

## 📝 Značajke

### Sigurnost
- Hashiranje lozinki
- Autentifikacija potrebna za sve operacije
- Zaštićene rute

### Korisnost
- Responzivni dizajn (Bootstrap 5)
- Automatsko izračunavanje potrošnje
- Validacija formi
- Flash poruke za povratne informacije

### Performanse
- SQLite baza za brzo pokretanje
- Optimizirani SQL upiti
- Minimalni broj dependency-ja

## 🎨 Dizajn

Aplikacija koristi moderan i čist dizajn s:
- Bootstrap 5 komponente
- Font Awesome ikone
- Responzivni layout
- Intuitivna navigacija
- Gradijenti i animacije

## 🚀 Produkcija

Za produkciju preporučuje se:
1. Promjena `SECRET_KEY` u sigurniju vrijednost
2. Korištenje PostgreSQL ili MySQL baze
3. Postavljanje HTTPS-a
4. Korištenje production WSGI servera (gunicorn)
5. Implementacija backup strategije

## 📞 Podrška

Za pitanja i podršku kontaktirajte razvojni tim.

## 📄 Licenca

Ovaj projekt je razvijen za potrebe upravljanja vodovodnim uslugama.