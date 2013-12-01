var reorderFromStart = document.URL.indexOf('#') !== -1;

// center function
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
	// tooltips init
	var tooltips = $(document).tooltip({
		position : {
			my : "left bottom-5",
			at : "left top",
			collision : "flipfit"
		}
	});

	// this is for "savable" URLs...
	if (reorderFromStart) {
		// adjust overlay
		$('#overlay').prepend($("#dpanel h1")).show().removeClass("center").addClass("header").dequeue();
		// remove panel
		$("#dpanel h3").remove();
		// save state
		$('body').data("moved", "true");
		// trigger map resize
		$(window).trigger("resize");
		$('#map').show()
		// submit button
		$('#submit').show();
		// radio buttons
		$('#type').show();
	}

	// center overlay
	if (!reorderFromStart && !('WebkitTransform' in document.body.style || 'MozTransform' in document.body.style || 'OTransform' in document.body.style || 'transform' in document.body.style)) {
		$('.center').center();
	}
	// input field focus
	$('#query').on({
		// when user types show next input elements
		input : function() {
			//$('.ui-tooltip-13').hide("fade", "fast");
			if (!reorderFromStart)
				$('#type').show("fade", "slow");
		}
	}).keydown(function(event) {
		if (event.keyCode == 13) {
			$('#submit').trigger('click');
		}
	});
	// submit button
	$('#submit').button().click(function(event) {
		// do not submit form or anything
		event.preventDefault();
		// handle user input only when a radio button has been selected and we are online!
		if ($("input[type=radio]:checked").size() > 0 && (MQA.fake === undefined)) {
			handleUserInput();
		} else {
			// TODO error handling
		}
	});
	// radio buttons
	$('#type').buttonset().click(function(event) {
		if (!reorderFromStart)
			$('#submit').show("fade", "slow");
	});
}

// resize map to screen width
function resizeMapOnStart() {
	$('#map').css({
		width : window.innerWidth + "px",
		height : (window.innerHeight + "px"),
	});
}

// resize map on window resize
function resizeMapOnWindowResize() {
	var wdw = $(window);
	wdw.resize(function() {
		window.map.setSize(new MQA.Size(window.innerWidth + "px", (window.innerHeight - 52) + "px"));
		$('#map').css('top', "52px");
	});
}

// guess what...
function reorderDomElements() {
	// adjust overlay
	$('#overlay').prepend($("#dpanel h1")).show("fade", "fast").removeClass("center", "slow").addClass("header", "slow").dequeue();
	// remove panel
	$("#dpanel").remove();
	// save state
	$('body').data("moved", "true");
	// make map visible
	//$('#map').css('top', $('.header').height() + "px");;
	//$('#map div').css('overflow', 'visible');
	// trigger map resize
	var int = setInterval(function() {
		if (!($('#map').is(":animated"))) {
			clearInterval(int);
			$(window).trigger("resize");
			$('#map').effect("slide", "slow");
		}
	}, 100);
}

// guess again...
function handleUserInput() {
	// TODO input validation
	if (true) {
		// only reorder if it has not already be done
		if ($('body').data("moved") === undefined && !reorderFromStart)
			reorderDomElements();

		$.ajax({
			url : "/request",
			method : "post",
			async : true,
			dataType : "json",
			data : {
				// TODO we really need some input validation here... like a jQuery UI dialog or something
				query : $("#query").val() + "",
				type : $("input[type=radio]:checked").val()
			},
			success : function(a, b, c) {
				// debug
				console.log("success");
				console.log(a);
			},
			error : function(a, b) {
				// debug
				console.log("fail");
				console.log(a)
			}
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