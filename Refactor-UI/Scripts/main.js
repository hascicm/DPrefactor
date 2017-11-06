var add = false;

window.onload = function() {
	init();
	
	search();
    refactor();
    home();
}

function init(){
	
	$("#sonarHost").prop("disabled", true);
	$("#sonarLogin").prop("disabled", true);
	$("#sonarPassword").prop("disabled", true);
	
	$("#sonarQubeAnalyze").change(function() {
	    if(this.checked) {
	        $("#sonarHost").prop("disabled", false);
	        $("#sonarLogin").prop("disabled", false);
	    	$("#sonarPassword").prop("disabled", false);
	    }else{
	    	$("#sonarHost").prop("disabled", true);
	    	$("#sonarLogin").prop("disabled", true);
	    	$("#sonarPassword").prop("disabled", true);
	    }
	});
}

function execute() {
	var searchCodes = [];
	var table = document.getElementById("searchTable");
	var length = table.rows.length;
	for(var i = length; i > 1 ; --i) {
		if(table.rows[i - 1].cells[0].getElementsByTagName("input")[0].checked) {
			searchCodes.push(table.rows[i - 1].cells[1].innerHTML);
		}
	}
	
	var repairCodes = [];
	
	var table = document.getElementById("repairTable");
	var length = table.rows.length;
	for(var i = length; i > 1 ; --i) {
		if(table.rows[i - 1].cells[0].getElementsByTagName("input")[0].checked) {
			repairCodes.push(table.rows[i - 1].cells[1].innerHTML);
		}
	}
	
	var prioritization = [];
	var values = document.getElementById("priorityList").getElementsByTagName('li');
	for(var i = 0; i < values.length; ++i) {
		prioritization.push(values[i].innerHTML);
	}
	
	var request = {
		repo : document.getElementById("repo").value,
		name : document.getElementById("name").value,
		password : document.getElementById("password").value,
		searchBranch : document.getElementById("searchBranch").value,
		repairBranch : document.getElementById("repairBranch").value,
		explanationSearch : document.getElementById("explanationSearch").checked,
		isSonarEnabled : document.getElementById("sonarQubeAnalyze").checked,
		sonarHost: document.getElementById("sonarHost").value,
		sonarLogin: document.getElementById("sonarLogin").value,
		sonarPassword: document.getElementById("sonarPassword").value,
		searchCodes : searchCodes,
		repairCodes : repairCodes,
		prioritization : prioritization
	}
	
	$.ajax({
		type: "PUT",
		url: "http://localhost:8080/refactor/execute/",
		data: JSON.stringify(request),
		contentType:"application/json; charset=utf-8",
		dataType: "json",
		success: function(response) {
			var string = "Boli nájdené nasledujúce problémy: \n\n";
			for(var i in response) {
				string += i + ":		" + response[i] + "\n";
			}
			
			window.alert(string);
		}
	});
}

function home() {
	document.getElementById("home").hidden = false;
    document.getElementById("search").hidden = true;
    document.getElementById("refactor").hidden = true;
    document.getElementById("rules").hidden = true;
    document.getElementById("preferences").hidden = true;
    document.getElementById("about").hidden = true;
    document.getElementById("repairEdit").hidden = true;
    document.getElementById("searchEdit").hidden = true;
    document.getElementById("SmellDatabase").hidden = true;
}

function search() {
	jQuery.get("http://localhost:8080/refactor/search", function(response) {
		var table = document.getElementById("searchTable");
		var length = table.rows.length
		for(var i = length; i > 1 ; --i) {
			table.deleteRow(i - 1);
		}

		var i = 1;
		response.forEach(function(value) {
			var row = table.insertRow(i);
			row.insertCell(0).innerHTML = '<input type="checkbox" name="active" checked="checked"/>';
			row.insertCell(1).innerHTML = value.code;
			row.insertCell(2).innerHTML = value.name;
			row.insertCell(3).innerHTML = '<a href="javascript:searchEdit(' + i + ');"><span>edit</span></a>';
			i++;
		})
	});
	
    document.getElementById("home").hidden = true;
    document.getElementById("search").hidden = false;
    document.getElementById("refactor").hidden = true;
    document.getElementById("rules").hidden = true;
    document.getElementById("preferences").hidden = true;
    document.getElementById("about").hidden = true;
    document.getElementById("repairEdit").hidden = true;
    document.getElementById("searchEdit").hidden = true;
    document.getElementById("SmellDatabase").hidden = true;

}

function refactor() {
	jQuery.get("http://localhost:8080/refactor/repair", function(response) {
		var table = document.getElementById("repairTable");
		var length = table.rows.length
		for(var i = length; i > 1 ; --i) {
			table.deleteRow(i - 1);
		}

		var i = 1;
		response.forEach(function(value) {
			var row = table.insertRow(i);
			row.insertCell(0).innerHTML = '<input type="checkbox" name="active" checked="checked"/>';
			row.insertCell(1).innerHTML = value.code;
			row.insertCell(2).innerHTML = value.name;
			row.insertCell(3).innerHTML = '<a href="javascript:repairEdit(' + i + ');"><span>edit</span></a>';
			i++;
		})
	});
	
    document.getElementById("home").hidden = true;
    document.getElementById("search").hidden = true;
    document.getElementById("refactor").hidden = false;
    document.getElementById("rules").hidden = true;
    document.getElementById("preferences").hidden = true;
    document.getElementById("about").hidden = true;
    document.getElementById("repairEdit").hidden = true;
    document.getElementById("searchEdit").hidden = true;
    document.getElementById("SmellDatabase").hidden = true;

}

function preferences() {
    document.getElementById("home").hidden = true;
    document.getElementById("search").hidden = true;
    document.getElementById("refactor").hidden = true;
    document.getElementById("rules").hidden = true;
    document.getElementById("preferences").hidden = false;
    document.getElementById("about").hidden = true;
    document.getElementById("repairEdit").hidden = true;
    document.getElementById("searchEdit").hidden = true;
    document.getElementById("SmellDatabase").hidden = true;

    prioritization();
}

function rules() {
	jQuery.get("http://localhost:8080/refactor/rulesdefinition", function(response) { 
		document.getElementById("ruleDef").value = atob(response.value);
	});
	
    document.getElementById("home").hidden = true;
    document.getElementById("search").hidden = true;
    document.getElementById("refactor").hidden = true;
    document.getElementById("rules").hidden = false;
    document.getElementById("preferences").hidden = true;
    document.getElementById("about").hidden = true;
    document.getElementById("repairEdit").hidden = true;
    document.getElementById("searchEdit").hidden = true;
    document.getElementById("SmellDatabase").hidden = true;

}

function about() {
    document.getElementById("home").hidden = true;
    document.getElementById("search").hidden = true;
    document.getElementById("refactor").hidden = true;
    document.getElementById("rules").hidden = true;
    document.getElementById("preferences").hidden = true;
    document.getElementById("about").hidden = false;
    document.getElementById("repairEdit").hidden = true;
    document.getElementById("searchEdit").hidden = true;
    document.getElementById("SmellDatabase").hidden = true;

}

function smells() {
    document.getElementById("home").hidden = true;
    document.getElementById("search").hidden = true;
    document.getElementById("refactor").hidden = true;
    document.getElementById("rules").hidden = true;
    document.getElementById("preferences").hidden = true;
    document.getElementById("about").hidden = true;
    document.getElementById("repairEdit").hidden = true;
    document.getElementById("searchEdit").hidden = true;
    document.getElementById("SmellDatabase").hidden = false;
}

function repairEdit(i) {
	add = false;
	var table = document.getElementById("repairTable");
	var code = table.rows[i].cells[1].innerHTML;
	var name = table.rows[i].cells[2].innerHTML;
	
	jQuery.get("http://localhost:8080/refactor/repair/" + code , function(response) { 
		document.getElementById("rEditScript").value = atob(response.value);
	});
	
    document.getElementById("home").hidden = true;
    document.getElementById("search").hidden = true;
    document.getElementById("refactor").hidden = true;
    document.getElementById("rules").hidden = true;
    document.getElementById("preferences").hidden = true;
    document.getElementById("about").hidden = true;
    document.getElementById("repairEdit").hidden = false;
    document.getElementById("searchEdit").hidden = true;
    
    document.getElementById("rEditName").value = name;
    document.getElementById("rEditCode").value = code;
}

function searchEdit(i) {
	add = false;
	var table = document.getElementById("searchTable");
	var code = table.rows[i].cells[1].innerHTML;
	var name = table.rows[i].cells[2].innerHTML;
	
	jQuery.get("http://localhost:8080/refactor/search/" + code , function(response) { 
		document.getElementById("sEditScript").value = atob(response.value);
	});
	
    document.getElementById("home").hidden = true;
    document.getElementById("search").hidden = true;
    document.getElementById("refactor").hidden = true;
    document.getElementById("rules").hidden = true;
    document.getElementById("preferences").hidden = true;
    document.getElementById("about").hidden = true;
    document.getElementById("repairEdit").hidden = true;
    document.getElementById("searchEdit").hidden = false;
    document.getElementById("SmellDatabase").hidden = true;

    
    document.getElementById("sEditName").value = name;
    document.getElementById("sEditCode").value = code;
}

function saveRules() {
	var value = document.getElementById("ruleDef").value;
	var resultJson = {"value" : value};
	
	$.ajax({
		  type: "POST",
		  url: "http://localhost:8080/refactor/rulesdefinition",
		  data: JSON.stringify(resultJson),
		  contentType:"application/json; charset=utf-8",
		  dataType: "json"
	});
}

function saveRepair() {
	var script = document.getElementById("rEditScript").value;
	var code = document.getElementById("rEditCode").value;
	var name = document.getElementById("rEditName").value;
	
	if(add) {
		var resultJson = {"code" : code, "name" : name, "script" : script};
		$.ajax({
			type: "PUT",
			url: "http://localhost:8080/refactor/repair/",
			data: JSON.stringify(resultJson),
			contentType:"application/json; charset=utf-8",
			dataType: "json"
		});
	} else {
		var resultJson = {"script" : script};
		$.ajax({
			type: "POST",
			url: "http://localhost:8080/refactor/repair/" + code,
			data: JSON.stringify(resultJson),
			contentType:"application/json; charset=utf-8",
			dataType: "json"
		});
	}

	home();
}

function saveSearch() {
	var script = document.getElementById("sEditScript").value;
	var code = document.getElementById("sEditCode").value;
	var name = document.getElementById("sEditName").value;

	if(add) {
		var resultJson = {"code" : code, "name" : name, "script" : script};
		$.ajax({
			type: "PUT",
			url: "http://localhost:8080/refactor/search/",
			data: JSON.stringify(resultJson),
			contentType:"application/json; charset=utf-8",
			dataType: "json"
		});
	} else {
		var resultJson = {"script" : script};
		$.ajax({
			type: "POST",
			url: "http://localhost:8080/refactor/search/" + code,
			data: JSON.stringify(resultJson),
			contentType:"application/json; charset=utf-8",
			dataType: "json"
		});
	}
	
	home();
}

function addSearch() {
	add = true;
	
	document.getElementById("home").hidden = true;
    document.getElementById("search").hidden = true;
    document.getElementById("refactor").hidden = true;
    document.getElementById("rules").hidden = true;
    document.getElementById("preferences").hidden = true;
    document.getElementById("about").hidden = true;
    document.getElementById("repairEdit").hidden = true;
    document.getElementById("searchEdit").hidden = false;
    document.getElementById("SmellDatabase").hidden = true;

    
    document.getElementById("sEditScript").value = "";
    document.getElementById("sEditName").value = "";
    document.getElementById("sEditCode").value = "";
}

function addRepair() {
	add = true;
	
	document.getElementById("home").hidden = true;
    document.getElementById("search").hidden = true;
    document.getElementById("refactor").hidden = true;
    document.getElementById("rules").hidden = true;
    document.getElementById("preferences").hidden = true;
    document.getElementById("about").hidden = true;
    document.getElementById("repairEdit").hidden = false;
    document.getElementById("searchEdit").hidden = true;
    document.getElementById("SmellDatabase").hidden = true;

    
    document.getElementById("rEditScript").value = "";
    document.getElementById("rEditName").value = "";
    document.getElementById("rEditCode").value = "";
}

function prioritization() {
	Sortable.create(document.getElementById('priorityList'));
}