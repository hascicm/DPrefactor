function executePathFinder() {
	var searchCodes = [];
	var table = document.getElementById("searchTable");
	var length = table.rows.length;
	for(var i = length; i > 1 ; --i) {
		if(table.rows[i - 1].cells[0].getElementsByTagName("input")[0].checked) {
			searchCodes.push(table.rows[i - 1].cells[1].innerHTML);
		}
	}
	
	var methodSelect = document.getElementById("pathFindingMethod");
	var repo = document.getElementById("repo-pathfiner").value;
	$( '<div class="pathFinderAlert" id= "'+document.getElementById("repo-pathfiner").value+
		'"> <div class=\"spinner\"></div> <div class="text"> '+document.getElementById("repo-pathfiner").value+
		' </div> </div>' ).appendTo( "#pathFinderAlertContainer").hide().fadeIn(1000);






	var method = methodSelect.options[methodSelect.selectedIndex].value;
	//alert(method)
	var request = {
		repo : document.getElementById("repo-pathfiner").value,
		name : document.getElementById("name-pathfiner").value,
		password : document.getElementById("password-pathfiner").value,
		searchBranch : document.getElementById("searchBranch-pathfiner").value,
		explanationSearch : document.getElementById("explanationSearch-pathfiner").checked,
		selectedMethod : method,
		utilizeclustering : document.getElementById("utilizeclustering").checked,
//		createrepairrecord : document.getElementById("createrepairrecord-pathfiner").checked,
isSonarEnabled : document.getElementById("sonarQubeAnalyze-pathfiner").checked,
sonarHost: document.getElementById("sonarHost-pathfiner").value,
sonarLogin: document.getElementById("sonarLogin-pathfiner").value,
sonarPassword: document.getElementById("sonarPassword-pathfiner").value,
searchCodes : searchCodes,
}

$.ajax({
	type: "PUT",
	url: "http://localhost:8080/refactor/executePathFinder/",
	data: JSON.stringify(request),
	contentType:"application/json; charset=utf-8",
	dataType: "json",
	success: function(response) {
		var string = "Boli nájdené nasledujúce problémy: \n\n";
		for(var i in response) {
			string += i + ":		" + response[i] + "\n";
		}
		$("#" + $.escapeSelector(repo)).addClass("finished");
		$("#" + $.escapeSelector(repo)).delay(5000).fadeOut(1000);
		setTimeout(function() {	$("#" + $.escapeSelector(repo)).delay(5000).fadeOut(1000);}, 2500);
			//window.alert(string);
		}
	});
}



function pathFinder(){
	document.getElementById("home").hidden = true;
	document.getElementById("pathFinder").hidden = false;
	document.getElementById("pathFinderResultsConcrete").hidden = true;
	document.getElementById("search").hidden = true;
	document.getElementById("refactor").hidden = true;
	document.getElementById("rules").hidden = true;
	document.getElementById("preferences").hidden = true;
	document.getElementById("about").hidden = true;
	document.getElementById("repairEdit").hidden = true;
	document.getElementById("searchEdit").hidden = true;
	document.getElementById("SmellDatabase").hidden = true;
	document.getElementById("records").hidden = true;
	document.getElementById("recordDetail").hidden = true;
	document.getElementById("pathFinderResults").hidden = true;    
	buttonSelection("javascript:pathFinder();");
	buttonSelectionSubnav("javascript:pathFinder();");


}


function pathFinderResults(){
	document.getElementById("home").hidden = true;
	document.getElementById("pathFinder").hidden = true;
	document.getElementById("pathFinderResultsConcrete").hidden = true;
	document.getElementById("search").hidden = true;
	document.getElementById("refactor").hidden = true;
	document.getElementById("rules").hidden = true;
	document.getElementById("preferences").hidden = true;
	document.getElementById("about").hidden = true;
	document.getElementById("repairEdit").hidden = true;
	document.getElementById("searchEdit").hidden = true;
	document.getElementById("SmellDatabase").hidden = true;
	document.getElementById("records").hidden = true;
	document.getElementById("recordDetail").hidden = true;
	document.getElementById("pathFinderResults").hidden = false;
	buttonSelectionSubnav("javascript:pathFinderResults();");


	jQuery.get("http://localhost:8080/refactor/pathfinerresults", function(response) {
		var table = document.getElementById("tabletbodyPathFinderResultsAnalys");
		var length = table.rows.length
		for(var i = length; i > 0 ; --i) {
			table.deleteRow(i - 1);
		}

		response.forEach(function(value) {
			//var row = table.insertRow(i);
			$(table).append("<tr><td>"+value.id+"</td><td>"+value.git+"</td><td>"+value.gituser+"</td><td>"
				+value.time+"</td><td>"+
				'<a href="javascript:PathFinderAnalysisDetail(' + value.id + ');"><span>podrobnosti</span></a>'+"</td></tr>");

		})
	});


}


var currentPathfinderClusterNumber = 1;
var currentPathfinderClusterCount = 5; 
var currentPathfinderCluster

function pathFinderResultsPreviousCluster(){

	currentPathfinderClusterNumber--;

	if (currentPathfinderClusterNumber <= 1){
		currentPathfinderClusterNumber = 1;
		document.getElementById("pathFinderResultsPreviousCluster").disabled = true;
	}
	if (currentPathfinderClusterCount>1){
		document.getElementById("pathFinderResultsNextCluster").disabled = false;
	}
	document.getElementById("pathFinderResultsClusterID").value = currentPathfinderClusterNumber;	
	currentPathfinderRepairNumber = 1;

	jQuery.get("http://localhost:8080/refactor/PathFinderAnalysisCluster/"+ currentAnalysis + "/" + currentPathfinderClusterNumber  ,function(response){
		currentPathfinderCluster = response.clusterid;

		var table = document.getElementById("pathFinderResultSmellTable");
		var length = table.rows.length
		for(var i = length; i > 0 ; --i) {
			table.deleteRow(i - 1);
		}
		response.smells.forEach(function(value) {
			$(table).append("<tr><td>"+value.id+"</td><td>"+value.smellname+"</td><td>"+value.description+"</td></tr>");

		})
		getClusterInfo(currentPathfinderCluster);		

	})

}

function pathFinderResultsNextCluster(){

	currentPathfinderClusterNumber++;

	if (currentPathfinderClusterNumber >= currentPathfinderClusterCount){
		currentPathfinderClusterNumber = currentPathfinderClusterCount;
		document.getElementById("pathFinderResultsNextCluster").disabled = true;
	}
	if (currentPathfinderClusterCount>1){
		document.getElementById("pathFinderResultsPreviousCluster").disabled = false;
	}
	document.getElementById("pathFinderResultsClusterID").value = currentPathfinderClusterNumber;	
	currentPathfinderRepairNumber = 1;

	jQuery.get("http://localhost:8080/refactor/PathFinderAnalysisCluster/"+ currentAnalysis + "/" + currentPathfinderClusterNumber  ,function(response){
		currentPathfinderCluster = response.clusterid;
		var table = document.getElementById("pathFinderResultSmellTable");
		var length = table.rows.length
		for(var i = length; i > 0 ; --i) {
			table.deleteRow(i - 1);
		}
		response.smells.forEach(function(value) {
			$(table).append("<tr><td>"+value.id+"</td><td>"+value.smellname+"</td><td>"+value.description+"</td></tr>");

		})
		getClusterInfo(currentPathfinderCluster);
	})

}

function getClusterInfo(clusterid){
	currentPathfinderRepairNumber = 1;
	jQuery.get("http://localhost:8080/refactor/PathFinderClusterInfo/" + clusterid , function(response){
		currentPathfinderrRepairCount = response.repaircount;

		document.getElementById("pathFinderResultsPreviousRepair").disabled = true;
		if (currentPathfinderrRepairCount>1){
			document.getElementById("pathFinderResultsNextRepair").disabled = false;
		}else {
			document.getElementById("pathFinderResultsNextRepair").disabled = true;
		}
		document.getElementById("pathFinderResultsSmell").value = "";
		document.getElementById("pathFinderResultsSmellPosition").value = "TODO";
		document.getElementById("pathFinderResultsRecRepair").value = "";
		document.getElementById("pathFinderResultsOrder").value = "";
		if (currentPathfinderrRepairCount>0){
			jQuery.get("http://localhost:8080/refactor/getPathFinderRepair/"+ currentPathfinderCluster + "/1" , function(response){
			getAndSetRepairInfo(response);		
			getAndSetSmellOccPosition(response.soid);

			})
		}

	})

	
}



var currentPathfinderRepairNumber = 1;
var currentPathfinderrRepairCount = 4; 

function pathFinderResultsPreviousRepair(){

	currentPathfinderRepairNumber--;

	if (currentPathfinderRepairNumber <= 1){
		currentPathfinderRepairNumber = 1;
		document.getElementById("pathFinderResultsPreviousRepair").disabled = true;

	}
	if (currentPathfinderrRepairCount>1){
		document.getElementById("pathFinderResultsNextRepair").disabled = false;
	}

	jQuery.get("http://localhost:8080/refactor/getPathFinderRepair/"+ currentPathfinderCluster + "/" +  currentPathfinderRepairNumber , function(response){
		getAndSetRepairInfo(response);
		getAndSetSmellOccPosition(response.soid);
		
	})
}

function pathFinderResultsNextRepair(){

	currentPathfinderRepairNumber++;

	if (currentPathfinderRepairNumber >= currentPathfinderrRepairCount){
		currentPathfinderRepairNumber = currentPathfinderrRepairCount;
		document.getElementById("pathFinderResultsNextRepair").disabled = true;
	}
	if (currentPathfinderrRepairCount>1){
		document.getElementById("pathFinderResultsPreviousRepair").disabled = false;
	}


	jQuery.get("http://localhost:8080/refactor/getPathFinderRepair/"+ currentPathfinderCluster + "/" + currentPathfinderRepairNumber  , function(response){
		getAndSetRepairInfo(response);
		getAndSetSmellOccPosition(response.soid);
	})
}

function getAndSetSmellOccPosition(soid){
	console.log("position ");
		jQuery.get("http://localhost:8080/refactor/getSmellOccPosition/"+ soid, function(response){
			console.log(response);
			var pos = "";
			var x = 1;
			response.forEach(function(value) {
				pos += "Poloha " + x + "\n";
				pos += "balík "  + value.package + "\n";
				pos += "trieda " + value.class + "\n";
				pos += "metoda " + value.method + "\n";
				x++;

			})
			console.log(" is \n " + pos );

			document.getElementById("pathFinderResultsSmellPosition").value = pos;
	})
}


var currentPathfinderRepairID; 

function getAndSetRepairInfo(response){
	currentPathfinderRepairID = response.concrepid;

	document.getElementById("pathFinderResultsSmell").value = response.smell;
	document.getElementById("pathFinderResultsSmellPosition").value = "TODO";
	document.getElementById("pathFinderResultsRecRepair").value = response.repair;
	document.getElementById("pathFinderResultsOrder").value = response.order;
	console.log(response.isdone);
	if (response.isdone == true){
		document.getElementById("pathFinderResultsIsdone").value = "Nie"
		document.getElementById("pathFinderResultsRepairComplete").innerHTML = "označiť ako dokončené";
	}else {
		document.getElementById("pathFinderResultsRepairComplete").innerHTML = "označiť ako nedokončené";
		document.getElementById("pathFinderResultsIsdone").value = "Áno"		
	}



}


function pathFinderResultsRepairComplete(){
	if (document.getElementById("pathFinderResultsIsdone").value == "Nie"){
		document.getElementById("pathFinderResultsIsdone").value = "Áno";
		document.getElementById("pathFinderResultsRepairComplete").innerHTML = "označiť ako nedokončené";
		$.ajax({
			type: "PUT",
			url: "http://localhost:8080/refactor/updatePathfinderRepairStatus/",
			data: JSON.stringify({"isdone" : true, "id" : currentPathfinderRepairID}),
			contentType:"application/json; charset=utf-8",
			dataType: "json"
		});
	}else if (document.getElementById("pathFinderResultsIsdone").value == "Áno"){
		document.getElementById("pathFinderResultsIsdone").value = "Nie";		
		document.getElementById("pathFinderResultsRepairComplete").innerHTML = "označiť ako dokončené";
		$.ajax({
			type: "PUT",
			url: "http://localhost:8080/refactor/updatePathfinderRepairStatus/",
			data: JSON.stringify({"isdone" : false, "id" : currentPathfinderRepairID}),
			contentType:"application/json; charset=utf-8",
			dataType: "json"
		});
	}
}


var currentAnalysis;

function PathFinderAnalysisDetail(i){
	document.getElementById("home").hidden = true;
	document.getElementById("pathFinder").hidden = true;
	document.getElementById("pathFinderResultsConcrete").hidden = false;
	document.getElementById("search").hidden = true;
	document.getElementById("refactor").hidden = true;
	document.getElementById("rules").hidden = true;
	document.getElementById("preferences").hidden = true;
	document.getElementById("about").hidden = true;
	document.getElementById("repairEdit").hidden = true;
	document.getElementById("searchEdit").hidden = true;
	document.getElementById("SmellDatabase").hidden = true;
	document.getElementById("records").hidden = true;
	document.getElementById("recordDetail").hidden = true;
	document.getElementById("pathFinderResults").hidden = true;

	currentAnalysis = i;

//	document.getElementById("pathFinderResultsPreviousCluster").disabled = true;
//	document.getElementById("pathFinderResultsNextCluster").disabled = true;


jQuery.get("http://localhost:8080/refactor/PathFinderAnalysisDetail/" + i, function(response) {
	document.getElementById("pathFinderResultsRepo").value = response.git
	document.getElementById("pathFinderResultsUser").value = response.gituser
	document.getElementById("pathFinderResultsTime").value = response.time
	document.getElementById("pathFinderResultsClusterID").value = currentPathfinderClusterNumber;	


	jQuery.get("http://localhost:8080/refactor/PathFinderAnalysisCluster/"+ i + "/1"  ,function(response){
		currentPathfinderCluster = response.clusterid;
		var table = document.getElementById("pathFinderResultSmellTable");
		response.smells.forEach(function(value) {
			//var row = table.insertRow(i);
			$(table).append("<tr><td>"+value.id+"</td><td>"+value.smellname+"</td><td>"+value.description+"</td></tr>");

		})
		jQuery.get("http://localhost:8080/refactor/PathFinderAnalysisInfo/" + i , function(response){

			currentPathfinderClusterCount = response.clustecount;

			if (currentPathfinderClusterCount <2){
				var element = document.getElementById("clusterselectiondiv");
				element.style.display = 'none';
			} else {
				var element = document.getElementById("clusterselectiondiv");
				element.style.display = 'block';
			}
		})

		getClusterInfo(currentPathfinderCluster);

	});
});
}


function pathFinderResultsBackr(){
	pathFinderResults();
}



function pathFinderResultsConcrete(){
	document.getElementById("home").hidden = true;
	document.getElementById("pathFinder").hidden = true;
	document.getElementById("pathFinderResultsConcrete").hidden = false;
	document.getElementById("search").hidden = true;
	document.getElementById("refactor").hidden = true;
	document.getElementById("rules").hidden = true;
	document.getElementById("preferences").hidden = true;
	document.getElementById("about").hidden = true;
	document.getElementById("repairEdit").hidden = true;
	document.getElementById("searchEdit").hidden = true;
	document.getElementById("SmellDatabase").hidden = true;
	document.getElementById("records").hidden = true;
	document.getElementById("recordDetail").hidden = true;
	document.getElementById("pathFinderResults").hidden = true;


}
