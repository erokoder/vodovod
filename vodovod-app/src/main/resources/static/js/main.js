$(document).ready(function () {
    // Sidebar toggle
    $('#sidebarCollapse').on('click', function () {
        $('#sidebar').toggleClass('active');
        $('#content').toggleClass('active');
    });
    
    // Auto-hide alerts after 5 seconds
    setTimeout(function() {
        $('.alert').fadeOut('slow');
    }, 5000);
    
    // Initialize tooltips
    var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'))
    var tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl)
    });
    
    // Initialize popovers
    var popoverTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'))
    var popoverList = popoverTriggerList.map(function (popoverTriggerEl) {
        return new bootstrap.Popover(popoverTriggerEl)
    });
    
    // Confirm delete actions
    $('.delete-confirm').on('click', function(e) {
        if (!confirm('Jeste li sigurni da želite obrisati ovaj zapis?')) {
            e.preventDefault();
        }
    });
    
    // DataTable initialization (if present)
    if ($.fn.DataTable) {
        $('.data-table').DataTable({
            language: {
                "sEmptyTable": "Nema podataka u tablici",
                "sInfo": "Prikazano _START_ do _END_ od _TOTAL_ rezultata",
                "sInfoEmpty": "Prikazano 0 do 0 od 0 rezultata",
                "sInfoFiltered": "(filtrirano iz _MAX_ ukupnih rezultata)",
                "sInfoPostFix": "",
                "sInfoThousands": ",",
                "sLengthMenu": "Prikaži _MENU_ rezultata po stranici",
                "sLoadingRecords": "Učitavanje...",
                "sProcessing": "Obrađivanje...",
                "sSearch": "Pretraži:",
                "sZeroRecords": "Ništa nije pronađeno",
                "oPaginate": {
                    "sFirst": "Prva",
                    "sPrevious": "Nazad",
                    "sNext": "Naprijed",
                    "sLast": "Zadnja"
                },
                "oAria": {
                    "sSortAscending": ": aktiviraj za rastući poredak",
                    "sSortDescending": ": aktiviraj za padajući poredak"
                }
            }
        });
    }
});