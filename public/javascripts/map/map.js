// center function
jQuery.fn.center = function() {
	this.css("position", "absolute");
	this.css("top", Math.max(0, (($(window).height() - $(this).outerHeight()) / 2) + $(window).scrollTop()) + "px");
	this.css("left", Math.max(0, (($(window).width() - $(this).outerWidth()) / 2) + $(window).scrollLeft()) + "px");
	return this;
}
// on page ready
$(function() {

	// TODO here needs to be some function that helps to start the application in query mode when someone adds a parameter to the URL
	// TODO auto select radio button
	reorderFromStart = document.URL.indexOf('#') !== -1;
	pinCollection = null;

	init();
	// style
	generalStylingAndSetup();
	if (MQA.fake === undefined) {
		// resize map to screen width
		resizeMapOnStart();
		// on page resize resize map as well
		resizeMapOnWindowResize();
	}
});

function init() {
	if (window.MQA !== undefined) {
		MQA.EventUtil.observe(window, 'load', function() {

			// Create an object for options
			var options = {
				elt : document.getElementById('map'), /*ID of element on the page where you want the map added*/
				zoom : 13,
				// initial zoom level of the map
				latLng : {
					lat : 49.48703,
					lng : 8.46625
				}, /*center of map in latitude/longitude */
				mtype : 'osm', /*map type (osm)*/
				bestFitMargin : 5, /*margin offset from the map viewport when applying a bestfit on shapes*/
				zoomOnDoubleClick : true	/*zoom in when double-clicking on map*/
			};

			// Construct an instance of MQA.TileMap with the options object
			map = new MQA.TileMap(options);

			// add controls
			MQA.withModule('largezoom', 'viewoptions', 'geolocationcontrol', 'insetmapcontrol', 'mousewheel', function() {
				map.addControl(new MQA.LargeZoom(), new MQA.MapCornerPlacement(MQA.MapCorner.TOP_LEFT, new MQA.Size(5, 5)));
				map.addControl(new MQA.ViewOptions());
				map.addControl(new MQA.GeolocationControl(), new MQA.MapCornerPlacement(MQA.MapCorner.TOP_RIGHT, new MQA.Size(10, 50)));
				// Map Control options
				var options = {
					size : {
						width : 150,
						height : 125
					},
					zoom : 3,
					mapType : 'osmsat',
					minimized : false
				};
				map.addControl(new MQA.InsetMapControl(options), new MQA.MapCornerPlacement(MQA.MapCorner.BOTTOM_RIGHT));
				map.enableMouseWheelZoom();
			});

			console.log("----- map initialized");
		});
		$(function() {
			$("#dpanel").html("<h1>SEM</h1><h3>Semantic Event Map</h3>").addClass("maxi");
		});
	} else {
		MQA = new Object();
		MQA.fake = true;
		// show error
		$(function() {
			$("#dpanel").html("<b>#ERROR: Map could not be loaded.</b><br />Your internet connection might not be working correctly.");
			$("#dpanel").css({
				"position" : "relative",
				"top" : "20px",
				"width" : "30%",
				"left" : "30%",
				"font-size" : "25px",
				"padding" : "5%"
			}).show("fade", "slow").addClass("error", 1000);
		});
	}
}

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
		// this will wait for the code to be executed when ALL all elements finished loading (after page.ready())
		$(window).bind("load", function() {
			// adjust overlay
			var h1 = $('div#dpanel.maxi h1');
			$('#overlay').prepend(h1);
			// trigger map resize
			$(window).trigger("resize");
		});
		$('#overlay').show().removeClass("center").addClass("header").dequeue();
		// remove panel
		$("#dpanel").hide();
		// save state
		$('body').data("moved", "true");
		$('#map').show().css('top', "52px");
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
		// clear map
		map.removeAllShapes();
		// handle user input only when a radio button has been selected and we are online!
		if ($("input[type=radio]:checked").size() > 0 && (MQA.fake === undefined)) {
			handleUserInput();
		} else {
			// TODO error handling
		}
		$(document).tooltip("option", "hide");
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
	if ($('body').data("moved") === undefined && !reorderFromStart) {
		reorderDomElements();
	}

	var query = $("#query").val() + "";
	var type = $("input[type=radio]:checked").val();

	$.ajax({
		url : "/request",
		method : "post",
		async : true,
		dataType : "json",
		timeout : 20000,
		data : {
			// TODO we really need some input validation here... like a jQuery UI dialog or something
			query : query,
			type : type
		},
		before : function() {
			$("body").css("cursor", "progress");
		},
		success : handleServerResponse(type),
		error : function(jqXHR, textStatus, errorThrown) {
			// debug
			console.log("request failed");
			console.log(errorThrown);
		},
		complete : function() {
			$("body").css("cursor", "default");
		}
	});
}

function handleServerResponse(type) {
	return function(data, textStatus, jqXHR) {
		var infoContentHTML, venue, info;
		console.log("----- request response retrieved for: " + type);
		pinCollection = new MQA.ShapeCollection();
		if (type === "artist") {
			addArtistInformationOnMap(data);
		} else if (type === "location") {
		} else if (type === "venue") {
		} else {
			// TODO
		}
		map.bestFit();
	}
}

function addArtistInformationOnMap(data) {
	$.each(data.events, function(eventID, eventObj) {
		infoContentHTML = "";
		venue = eventObj.venue;
		if (venue.latitude !== "" && venue.longitude !== "") {
			info = new MQA.Poi({
				lat : venue.latitude,
				lng : venue.longitude
			});
		} else {
			nominatimBackUpQuery(eventObj, data);
			return;
		}
		//info.setRolloverContent('ROLLOVER TODO');
		// TODO several artists
		infoContentHTML += "<h4>" + eventObj.title + (eventObj.title == data.name && venue.name !== "" ? " in " + venue.name : "") + "</h4>";

		if (!info)
			return;
		// TODO handle error

		info.setInfoContentHTML(infoContentHTML);
		info.setDeclutterMode(true);
		pinCollection.add(info);
	});
	map.addShapeCollection(pinCollection);
}

// perform nominatim query when not enough geo info is provided
function nominatimBackUpQuery(eventObj, data) {
	var nomQuery = "", venue = eventObj.venue;
	if (venue.street !== "") {
		nomQuery = venue.street;
	} else if (venue.name) {
		nomQuery = venue.name;
	}
	if (venue.postalCode !== "" && nomQuery !== "")
		nomQuery += "," + venue.postalCode;
	if (venue.city !== "" && nomQuery !== "")
		nomQuery += "," + venue.city;
	if (venue.country !== "" && nomQuery !== "")
		nomQuery += "," + venue.country;

	var url = 'http://open.mapquestapi.com/nominatim/v1/search/' + nomQuery + '?format=json&addressdetails=1';
	nominatimRequest(url, nomQuery, eventObj, data, false);
}

function nominatimRequest(url, nomQuery, eventObj, pData, once) {
	$.ajax({
		url : url,
		dataType : 'json',
		crossDomain : true,
		timeout : 10000,
		async : true,
		success : function(data, textStatus, jqXHR) {
			if (data.length === 0 && !once) {
				once = true;
				if (nomQuery.indexOf(',') !== -1) {
					console.log("----- retry nominatim request with modified query");
					var retry = nomQuery.substr(nomQuery.indexOf(',') + 1);
					retry = url.replace(nomQuery, retry);

					nominatimRequest(retry, "", eventObj, pData, once);
					return;
				}
				return;
				// TODO error handling
			}
			// TODO handle result
			console.log("----- nominatim retrieval successful");
			var displayName = data[0].display_name;
			if ((eventObj.venue.country !== "" && displayName.indexOf(eventObj.venue.country) !== -1) || (eventObj.venue.city !== "" && displayName.indexOf(eventObj.venue.city) !== -1)) {
				info = new MQA.Poi({
					lat : data[0].lat,
					lng : data[0].lon
				});

				var infoContentHTML = "<h4>" + eventObj.title + (((eventObj.title == pData.name) && (eventObj.venue.name !== "")) ? " in " + eventObj.venue.name : " in " + ((displayName.indexOf(',') !== -1) ? (displayName.substr(0, displayName.indexOf(',')) === "undefined" ? (displayName.substr(11, displayName.substr(11).indexOf(',')) !== "undefined" ? displayName.substr(11, displayName.substr(11).indexOf(',')) : "") : "") : displayName)
				) + "</h4>";

				info.setInfoContentHTML(infoContentHTML);
				info.setDeclutterMode(true);
				pinCollection.add(info);

				map.addShapeCollection(pinCollection);
			}
			//poi.setInfoContentHTML(data.display_name);
			//poi.toggleInfoWindow();
		},
		error : function(jqXHR, textStatus, errorThrown) {
			console.log(errorThrown);
			return null;
			// TODO error handling
		}
	});
}