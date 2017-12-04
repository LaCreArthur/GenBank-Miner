// fonction appelée au clique sur démarrer
function startStats() {
    var select = myForm.elements['select'];
    var options = select && select.options;

    var kingdoms = [];
    for (var i=0; i<options.length; i++) {
        if (options[i].selected)
            kingdoms.push(options[i].text);
    }

    if (kingdoms.length == 0) {
        alert("Veuillez sélectionner au moins 1 royaume.");
        // TODO ne pas lancer l'etape 2 + utiliser une alerte bootstrap
        return;
    }

    var dl_full = myForm.elements['cb_dl_full'].checked;
    var dl_valid = myForm.elements['cb_dl_valid'].checked;
    var dl_archiv = myForm.elements['cb_dl_archiv'].checked;

    IO.startHandler(kingdoms, dl_full, dl_valid, dl_archiv);
}


function pauseRun() {
    IO.pause();
    if ($("#btnStop").html() == "Stopper") {
        $("#btnStop").attr("class","btn btn-warning disabled");
        $("#btnStop").text("Reprendre");
        $("#wait").html("\<img src= 'loading.gif' height='32'\> Suspension de l'opération ");
    }
    else {
        $("#btnStop").attr("class","btn btn-warning");
        $("#btnStop").text("Stopper");
        $("#wait").text(" ");
    }
}

function exit() {
    if (confirm("Êtes-vous sûr de quitter ?") == true) {
        IO.exit();
    }
}

function startArbo() {
    IO.callDrawTree();
}

function openXlsx(id) {
    var el = document.getElementById(id);
    var path = "";
    while (el.parentNode) {
        path += "/" + el.id;
        el = el.parentNode;
        if (el.className === "tree")
            break;
    }
    path = path.split('/').reverse().join('/');
    path = path.split("//").join("/");
    IO.openXlsx(path);
}
// Ouvre la modale de la mise a jour
function launchFC() {
    var path = IO.getLocalSrc();
    if (path != null) {
        document.getElementsByName("pathText")[0].value=path;
    }
}

function reload() {
    IO.reload();
    document.getElementById("load").style.color = "";
    $("#buttonHolder").html("");
    $("#load").html("<img src='loading.gif' height='32'> Attente de récupération de la taille des organismes  ...");
}

// fonction utilisee pour ecrire dans la page, appelee par des methodes java
function writeTerm (s, i, t) {
    if (t==1) var term = $("#terminal1")
    else if (t==2) var term = $("#terminal2")
    else alert("writeTerm Error : wrong terminal number, must be 1 or 2");

    var old = term.html();
    switch(i) {
        case 0:
            term.html(old + "<p class='terminal--output'>" + s + "</p>");
            break;
        case 1:
            term.html(old + "<p class='terminal--output is-console'>" + s + "</p>");
            break;
        case 2:
            term.html(old + "<p class='terminal--exception'>" + s + "</p>");
            break;
        default:
            break;
    }

    term.scrollTop(term[0].scrollHeight);
    if(term.children().length > 200) {
        term.find('p').first().remove();
    }
}

// ecrire le temps restant
function writeEstimateTime (str) {
    var label = document.getElementById("estim");
    label.innerHTML ="Temps restant : "+str;
    
}

function checkSelectedKingdoms(){
    var selected = $('.selectpicker option:selected');
    if(selected.length == 0) {
        $("#btnConfirm").attr("class","btn btn-success disabled");
        $('.selectpicker').popover('show')
        $('.selectpicker')
    }
    else {
        $("#btnConfirm").attr("class","btn btn-success");
        $('.selectpicker').popover('hide')
    }
}

$(document).ready(function(){
    $('.selectpicker').on('change', function(){
        checkSelectedKingdoms();
    });
});
