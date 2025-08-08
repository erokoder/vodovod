// Glavna JavaScript funkcionalnost za Vodovod aplikaciju

document.addEventListener('DOMContentLoaded', function() {
    // Inicijalizacija tooltipa
    var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    var tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });

    // Auto-hide alerts nakon 5 sekundi
    setTimeout(function() {
        var alerts = document.querySelectorAll('.alert-dismissible');
        alerts.forEach(function(alert) {
            var bsAlert = new bootstrap.Alert(alert);
            bsAlert.close();
        });
    }, 5000);

    // Potvrda brisanja
    document.querySelectorAll('.btn-danger').forEach(function(btn) {
        btn.addEventListener('click', function(e) {
            if (!confirm('Jeste li sigurni da želite obrisati ovaj element?')) {
                e.preventDefault();
            }
        });
    });

    // Loading state za forme
    document.querySelectorAll('form').forEach(function(form) {
        form.addEventListener('submit', function() {
            var submitBtn = form.querySelector('button[type="submit"]');
            if (submitBtn) {
                submitBtn.classList.add('loading');
                submitBtn.disabled = true;
            }
        });
    });

    // Formatiranje brojeva u tablicama
    formatNumbers();

    // Validacija formi
    setupFormValidation();

    // Postavljanje datuma
    setupDateFields();
});

// Formatiranje brojeva
function formatNumbers() {
    document.querySelectorAll('.format-number').forEach(function(element) {
        var number = parseFloat(element.textContent);
        if (!isNaN(number)) {
            element.textContent = number.toLocaleString('hr-HR', {
                minimumFractionDigits: 2,
                maximumFractionDigits: 2
            });
        }
    });
}

// Postavljanje validacije formi
function setupFormValidation() {
    var forms = document.querySelectorAll('.needs-validation');
    forms.forEach(function(form) {
        form.addEventListener('submit', function(event) {
            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
            }
            form.classList.add('was-validated');
        });
    });
}

// Postavljanje datumskih polja
function setupDateFields() {
    // Postavi današnji datum kao zadanu vrijednost gdje je potrebno
    var today = new Date().toISOString().split('T')[0];
    document.querySelectorAll('input[type="date"][data-default="today"]').forEach(function(input) {
        if (!input.value) {
            input.value = today;
        }
    });

    // Ograniči datume u budućnosti
    document.querySelectorAll('input[type="date"][data-max="today"]').forEach(function(input) {
        input.max = today;
    });
}

// Funkcija za pretraživanje u tablicama
function searchTable(inputId, tableId) {
    var input = document.getElementById(inputId);
    var table = document.getElementById(tableId);
    var rows = table.querySelectorAll('tbody tr');

    input.addEventListener('keyup', function() {
        var filter = input.value.toLowerCase();
        
        rows.forEach(function(row) {
            var text = row.textContent.toLowerCase();
            if (text.indexOf(filter) > -1) {
                row.style.display = '';
            } else {
                row.style.display = 'none';
            }
        });
    });
}

// Funkcija za sortiranje tablice
function sortTable(columnIndex, tableId) {
    var table = document.getElementById(tableId);
    var rows = Array.from(table.querySelectorAll('tbody tr'));
    var isNumeric = true;
    
    // Provjeri je li kolona numerička
    rows.forEach(function(row) {
        var cell = row.cells[columnIndex];
        if (cell && isNaN(parseFloat(cell.textContent))) {
            isNumeric = false;
        }
    });
    
    rows.sort(function(a, b) {
        var aVal = a.cells[columnIndex].textContent.trim();
        var bVal = b.cells[columnIndex].textContent.trim();
        
        if (isNumeric) {
            return parseFloat(aVal) - parseFloat(bVal);
        } else {
            return aVal.localeCompare(bVal, 'hr');
        }
    });
    
    var tbody = table.querySelector('tbody');
    rows.forEach(function(row) {
        tbody.appendChild(row);
    });
}

// Funkcija za izvoz tablice u CSV
function exportTableToCSV(tableId, filename) {
    var table = document.getElementById(tableId);
    var csv = [];
    var rows = table.querySelectorAll('tr');
    
    rows.forEach(function(row) {
        var cols = row.querySelectorAll('td, th');
        var csvRow = [];
        cols.forEach(function(col) {
            csvRow.push('"' + col.textContent.replace(/"/g, '""') + '"');
        });
        csv.push(csvRow.join(','));
    });
    
    var csvContent = csv.join('\n');
    var blob = new Blob([csvContent], { type: 'text/csv' });
    var url = window.URL.createObjectURL(blob);
    var a = document.createElement('a');
    a.href = url;
    a.download = filename + '.csv';
    a.click();
    window.URL.revokeObjectURL(url);
}

// Funkcija za prikaz/skrivanje dodatnih informacija
function toggleDetails(elementId) {
    var element = document.getElementById(elementId);
    if (element.style.display === 'none' || element.style.display === '') {
        element.style.display = 'block';
    } else {
        element.style.display = 'none';
    }
}

// Funkcija za automatsko osvježavanje stranice
function enableAutoRefresh(intervalMinutes) {
    setInterval(function() {
        location.reload();
    }, intervalMinutes * 60 * 1000);
}

// Funkcija za provjeru veze sa serverom
function checkServerConnection() {
    fetch('/dashboard')
        .then(response => {
            if (response.ok) {
                document.body.classList.remove('server-disconnected');
            } else {
                document.body.classList.add('server-disconnected');
            }
        })
        .catch(() => {
            document.body.classList.add('server-disconnected');
        });
}