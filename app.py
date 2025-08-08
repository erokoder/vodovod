from flask import Flask, render_template, request, redirect, url_for, flash, jsonify
from flask_sqlalchemy import SQLAlchemy
from flask_login import LoginManager, UserMixin, login_user, logout_user, login_required, current_user
from werkzeug.security import generate_password_hash, check_password_hash
from datetime import datetime, date
import os

app = Flask(__name__)
app.config['SECRET_KEY'] = 'vodovod-secret-key-2024'
app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///vodovod.db'
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False

db = SQLAlchemy(app)
login_manager = LoginManager()
login_manager.init_app(app)
login_manager.login_view = 'login'

# Modeli baze podataka
class User(UserMixin, db.Model):
    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(80), unique=True, nullable=False)
    email = db.Column(db.String(120), unique=True, nullable=False)
    password_hash = db.Column(db.String(120), nullable=False)
    role = db.Column(db.String(20), default='operator')  # admin, operator

class Korisnik(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    ime = db.Column(db.String(100), nullable=False)
    prezime = db.Column(db.String(100), nullable=False)
    adresa = db.Column(db.String(200), nullable=False)
    telefon = db.Column(db.String(20))
    email = db.Column(db.String(120))
    broj_vodomjera = db.Column(db.String(50), unique=True, nullable=False)
    datum_prijave = db.Column(db.Date, default=date.today)
    aktivan = db.Column(db.Boolean, default=True)
    
    ocitanja = db.relationship('Ocitanje', backref='korisnik', lazy=True)
    racuni = db.relationship('Racun', backref='korisnik', lazy=True)

class Ocitanje(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    korisnik_id = db.Column(db.Integer, db.ForeignKey('korisnik.id'), nullable=False)
    datum_ocitanja = db.Column(db.Date, nullable=False)
    stanje_vodomjera = db.Column(db.Float, nullable=False)
    potrosnja = db.Column(db.Float)
    
class Racun(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    korisnik_id = db.Column(db.Integer, db.ForeignKey('korisnik.id'), nullable=False)
    datum_racuna = db.Column(db.Date, default=date.today)
    period_od = db.Column(db.Date, nullable=False)
    period_do = db.Column(db.Date, nullable=False)
    potrosnja = db.Column(db.Float, nullable=False)
    cijena_po_kubiku = db.Column(db.Float, default=2.50)
    ukupan_iznos = db.Column(db.Float, nullable=False)
    placen = db.Column(db.Boolean, default=False)
    datum_placanja = db.Column(db.Date)

@login_manager.user_loader
def load_user(user_id):
    return User.query.get(int(user_id))

# Rute
@app.route('/')
def index():
    if current_user.is_authenticated:
        return redirect(url_for('dashboard'))
    return render_template('index.html')

@app.route('/login', methods=['GET', 'POST'])
def login():
    if request.method == 'POST':
        username = request.form['username']
        password = request.form['password']
        user = User.query.filter_by(username=username).first()
        
        if user and check_password_hash(user.password_hash, password):
            login_user(user)
            return redirect(url_for('dashboard'))
        else:
            flash('Neispravno korisničko ime ili lozinka')
    
    return render_template('login.html')

@app.route('/logout')
@login_required
def logout():
    logout_user()
    return redirect(url_for('index'))

@app.route('/dashboard')
@login_required
def dashboard():
    ukupno_korisnika = Korisnik.query.filter_by(aktivan=True).count()
    neplaceni_racuni = Racun.query.filter_by(placen=False).count()
    ukupan_dug = db.session.query(db.func.sum(Racun.ukupan_iznos)).filter_by(placen=False).scalar() or 0
    
    return render_template('dashboard.html', 
                         ukupno_korisnika=ukupno_korisnika,
                         neplaceni_racuni=neplaceni_racuni,
                         ukupan_dug=ukupan_dug)

@app.route('/korisnici')
@login_required
def korisnici():
    korisnici = Korisnik.query.filter_by(aktivan=True).all()
    return render_template('korisnici.html', korisnici=korisnici)

@app.route('/korisnici/novi', methods=['GET', 'POST'])
@login_required
def novi_korisnik():
    if request.method == 'POST':
        korisnik = Korisnik(
            ime=request.form['ime'],
            prezime=request.form['prezime'],
            adresa=request.form['adresa'],
            telefon=request.form['telefon'],
            email=request.form['email'],
            broj_vodomjera=request.form['broj_vodomjera']
        )
        db.session.add(korisnik)
        db.session.commit()
        flash('Korisnik je uspješno dodan')
        return redirect(url_for('korisnici'))
    
    return render_template('novi_korisnik.html')

@app.route('/ocitanja')
@login_required
def ocitanja():
    ocitanja = db.session.query(Ocitanje, Korisnik).join(Korisnik).order_by(Ocitanje.datum_ocitanja.desc()).all()
    return render_template('ocitanja.html', ocitanja=ocitanja)

@app.route('/ocitanja/novo', methods=['GET', 'POST'])
@login_required
def novo_ocitanje():
    if request.method == 'POST':
        korisnik_id = request.form['korisnik_id']
        stanje = float(request.form['stanje_vodomjera'])
        
        # Pronađi zadnje očitanje za izračun potrošnje
        zadnje_ocitanje = Ocitanje.query.filter_by(korisnik_id=korisnik_id).order_by(Ocitanje.datum_ocitanja.desc()).first()
        potrosnja = stanje - zadnje_ocitanje.stanje_vodomjera if zadnje_ocitanje else 0
        
        ocitanje = Ocitanje(
            korisnik_id=korisnik_id,
            datum_ocitanja=datetime.strptime(request.form['datum_ocitanja'], '%Y-%m-%d').date(),
            stanje_vodomjera=stanje,
            potrosnja=potrosnja
        )
        db.session.add(ocitanje)
        db.session.commit()
        flash('Očitanje je uspješno dodano')
        return redirect(url_for('ocitanja'))
    
    korisnici = Korisnik.query.filter_by(aktivan=True).all()
    return render_template('novo_ocitanje.html', korisnici=korisnici)

@app.route('/racuni')
@login_required
def racuni():
    racuni = db.session.query(Racun, Korisnik).join(Korisnik).order_by(Racun.datum_racuna.desc()).all()
    return render_template('racuni.html', racuni=racuni)

@app.route('/racuni/generiraj', methods=['POST'])
@login_required
def generiraj_racune():
    korisnici = Korisnik.query.filter_by(aktivan=True).all()
    period_od = datetime.strptime(request.form['period_od'], '%Y-%m-%d').date()
    period_do = datetime.strptime(request.form['period_do'], '%Y-%m-%d').date()
    cijena = float(request.form['cijena_po_kubiku'])
    
    generirani = 0
    for korisnik in korisnici:
        # Pronađi očitanja u periodu
        ocitanja = Ocitanje.query.filter(
            Ocitanje.korisnik_id == korisnik.id,
            Ocitanje.datum_ocitanja >= period_od,
            Ocitanje.datum_ocitanja <= period_do
        ).all()
        
        if ocitanja:
            ukupna_potrosnja = sum(o.potrosnja for o in ocitanja if o.potrosnja)
            iznos = ukupna_potrosnja * cijena
            
            racun = Racun(
                korisnik_id=korisnik.id,
                period_od=period_od,
                period_do=period_do,
                potrosnja=ukupna_potrosnja,
                cijena_po_kubiku=cijena,
                ukupan_iznos=iznos
            )
            db.session.add(racun)
            generirani += 1
    
    db.session.commit()
    flash(f'Generirano je {generirani} računa')
    return redirect(url_for('racuni'))

@app.route('/racun/<int:racun_id>/plati', methods=['POST'])
@login_required
def plati_racun(racun_id):
    racun = Racun.query.get_or_404(racun_id)
    racun.placen = True
    racun.datum_placanja = date.today()
    db.session.commit()
    flash('Račun je označen kao plaćen')
    return redirect(url_for('racuni'))

def create_admin_user():
    """Stvori admin korisnika ako ne postoji"""
    admin = User.query.filter_by(username='admin').first()
    if not admin:
        admin = User(
            username='admin',
            email='admin@vodovod.hr',
            password_hash=generate_password_hash('admin123'),
            role='admin'
        )
        db.session.add(admin)
        db.session.commit()

if __name__ == '__main__':
    with app.app_context():
        db.create_all()
        create_admin_user()
    app.run(debug=True, host='0.0.0.0')