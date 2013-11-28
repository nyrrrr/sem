// center
jQuery.fn.center = function() {
	this.css("position", "absolute");
	this.css("top", Math.max(0, (($(window).height() - $(this).outerHeight()) / 2) + $(window).scrollTop()) + "px");
	this.css("left", Math.max(0, (($(window).width() - $(this).outerWidth()) / 2) + $(window).scrollLeft()) + "px");
	return this;
}
// on page ready
$(function() {
	// style
	generalStylingAndSetup();
	if (MQA.fake === undefined) {
		// resize map to screen width
		resizeMapOnStart();
		// on page resize resize map as well
		resizeMapOnWindowResize();
	}
});
// some basic css adjustments and simple input event handling
function generalStylingAndSetup() {
	// tooltips
	var tooltips = $(document).tooltip({
		position : {
			my : "left bottom-5",
			at : "left top",
			collision : "flipfit"
		}
	});
	// center overlay
	if (!('WebkitTransform' in document.body.style || 'MozTransform' in document.body.style || 'OTransform' in document.body.style || 'transform' in document.body.style)) {
		$('.center').center();
	}
	// input field focus
	$('#query').on({
		input : function() {
			$('.ui-tooltip-13').hide("fade", "fast");
			$('#type').show("fade", "slow");
		}
	});
	// submit button
	$('#submit').button().click(function(event) {
		event.preventDefault();
		if ($("input[type=radio]:checked").size() > 0 && (MQA.fake === undefined)) {
			handleUserInput();
		} else {
			// TODO error handling
		}
	});
	// radio buttons
	$('#type').buttonset().click(function(event) {
		$('#submit').show("fade", "slow");
	});
}

// resize map to screen width
function resizeMapOnStart() {
	$('#map').css({
		width : window.innerWidth + "px",
		height : (window.innerHeight + "px")// - overlayHeight) + "px",
	});
}

// on page resize resize map as well
function resizeMapOnWindowResize() {
	var wdw = $(window);
	wdw.resize(function() {
		window.map.setSize(new MQA.Size(window.innerWidth + "px", (window.innerHeight - $('#overlay.header').height())+"px"));
	});
}

function reorderVisuals() {
	//
	$('#overlay').prepend($("#dpanel h1")).show("fade", "slow").removeClass("center", 1000).addClass("header", 1000).dequeue();
	$("#dpanel").remove();
	$('body').data("moved", "true"); 
	// make map visible and move
	$('#map').show("fade", "slow");
	$(window).trigger('resize');
}

function handleUserInput() {
	if (true) {
		if ($('body').data("moved") === undefined) reorderVisuals();
		$(function() {
			$('#submit').click(function(evt) {
				evt.preventDefault()
				$.ajax({
					url : "/request",
					method : "post",
					async : true,
					dataType : "json",
					data : {
						query : $("#query").val() + "",
						type : $("input[type=radio]:checked").val()
					},
					success : function(a, b, c) {
						console.log("success");
						console.table(a);
					},
					error : function(a, b) {
						console.log("fail");
						console.table(a)
					}
				});
			});
		});
	} else {
		// TODO error handling?
	}

}

/*
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

*/
// }