// listener
$(document).on('click', '#mapbutton', handleInput);

// handle user input and invoke appropriate methods
function handleInput() {
	
	// wipe map
	map.removeShapeCollection();
	
	var userInput = $('#index-search').val();
	// input field
	var eventFirstMode;
	// determines query mode; should be set by some input fields

	if (isValidInput(userInput)) {
		eventFirstMode = determineQueryMode(userInput, eventFirstMode);
		executeQueries(eventFirstMode, userInput);
	} else {
		// handle invalid input somehow (play framework?)
	}
}

// TODO sparql and stuff
function executeQueries(eventFirstMode, userInput) {
	if (eventFirstMode) {
		// TODO sparql
		// TODO map

		// dummy request
		MQA.withModule('nominatim', function() {

			// search and add points on map
			map.nominatimSearchAndAddLocation(userInput + ", Germany", function (response) {
			//map.nominatimSearchAndAddLocation('Arena, Germany', function (response) {
				// debug
				console.log(response);
				
				// TODO detailled erorr handling
				if(response.length === 0) alert("found not hits in Germany");
				
				// $.each(response, function (i, result) {
					// window.pois = MQA.ShapeCollection();
					// $.each(pois, function (n, poi) {
						// poi.setRolloverContent(i);
 						// poi.setInfoContentHTML(i);
					// });
				// });
			});
			
			
			
			// zooms out (or in) so all points can be seen
			map.bestFit();

		});

	} else {
		// TODO
	}
}

// this method will determine which query mode is needed
function determineQueryMode(userInput) {
	// TODO determine mode according to the radio buttons
	return true;
}

// creates a URI for querying the sparql service
function createQueryUri(userInput) {
	return "";
	// TODO
}

// check whether the user's input is valid
function isValidInput(userInput) {
	if (userInput === "") {
		// TODO handle missing input
		return false;
	} else {
		// TODO handle invalid input
		//return false;
	}
	return true;
	// TODO
}

// function addPoi() {
// var info = new MQA.Poi({
// lat : 51.227741,
// lng : 6.773456
// });
// window.map.addShape(info);
// 	window.map.bestFit();
// }