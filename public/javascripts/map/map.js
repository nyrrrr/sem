// center function
jQuery.fn.center = function() {
	this.css("position", "absolute");
	this.css("top", Math.max(0, (($(window).height() - $(this).outerHeight()) / 2) + $(window).scrollTop()) + "px");
	this.css("left", Math.max(0, (($(window).width() - $(this).outerWidth()) / 2) + $(window).scrollLeft()) + "px");
	return this;
}
var map;
// page load animation
$(document).on({
	ajaxStart : function() {
		$('body').addClass("loading");
	},
	ajaxStop : function() {
		$('body').removeClass("loading");
	}
});
// on page ready
$(function() {
	// TODO here needs to be some function that helps to start the application in query mode when someone adds a parameter to the URL
	reorderFromStart = document.URL.indexOf('#') !== -1;
	pinCollection = null;
	// init map
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
				mtype : 'map', // map type (map, sat, hyb); defaults to map
				bestFitMargin : 100, /*margin offset from the map viewport when applying a bestfit on shapes*/
				zoomOnDoubleClick : true	/*zoom in when double-clicking on map*/
			};
			// Construct an instance of MQA.TileMap with the options object
			map = new MQA.TileMap(options);
			// add controls
			MQA.withModule('largezoom', 'viewoptions', 'geolocationcontrol', 'insetmapcontrol', 'mousewheel', function() {
				map.addControl(new MQA.LargeZoom(), new MQA.MapCornerPlacement(MQA.MapCorner.TOP_LEFT, new MQA.Size(5, 5)));
				map.addControl(new MQA.ViewOptions());
				map.addControl(new MQA.GeolocationControl(), new MQA.MapCornerPlacement(MQA.MapCorner.TOP_RIGHT, new MQA.Size(10, 50)));
				// geo search
				var gcontrol = new MQA.GeolocationControl();
				gcontrol.onLocate = function(poi, position) {
					sendRequest("", "location", position.coords.latitude, position.coords.longitude, $('#radius :selected').val());
					var point = new MQA.Poi({
						lat : position.coords.latitude,
						lng : position.coords.longitude
					});
					var icon = new MQA.Icon("http://content.mqcdn.com/open-sdk/js/v7.0.1/images/waving_man_sm.png", 20, 29);
					point.setIcon(icon);
					map.addShape(point);
				};
				map.addControl(gcontrol, new MQA.MapCornerPlacement(MQA.MapCorner.TOP_RIGHT, new MQA.Size(10, 50)));
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
		$('.ui-tooltip').hide("fade", "slow");
		// do not submit form or anything
		event.preventDefault();
		// handle user input only when a radio button has been selected and we are online!
		if ($('#query').val() !== "" && $("input[type=radio]:checked").size() > 0 && (MQA.fake === undefined)) {
			handleUserInput();
		} else {
			createErrorDialog("<b>Incomplete input</b><br/><br/>Please make sure you entered a search term and selected a category.");
		}
		$(document).tooltip("option", "hide");
	});
	// radio buttons
	$('#type').buttonset().click(function(event) {
		$('.ui-tooltip').hide("fade", "fast");
		if (!reorderFromStart)
			$('#submit').show("fade", "slow");
		if ($(type).find(':checked').val() == "location") {
			$('#radius').show().position({
				my : "bottom center+26",
				at : "bottom center",
				of : $('#type label[for="location"]')
			});
		} else {
			$('#radius').hide()
		}
	});
	// slider
	$("#radius").removeClass('ui-slider-horizontal').position({
		my : "bottom center+26",
		at : "bottom center",
		of : $('#type label[for="location"]')
	});

	// // images
	// $('img').on({
	// hover : function() {
	// $(this).css({
	// height: $(this).position().top,
	// width : $(this).position().left
	// });
	// }
	// });

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
	if ($('body').data("moved") === undefined && !reorderFromStart) {
		reorderDomElements();
	}
	var query = $("#query").val() + "";
	var type = $("input[type=radio]:checked").val();
	var lat = "", lon = "";
	var radius = $('#radius :selected').val();
	if (type !== "location")
		sendRequest(query, type, null, null, radius);
	else
		getGeoLocation(query, lat, lon, radius, sendRequest);
}

function sendRequest(query, type, lat, lon, radius, venue) {
	// clear map
	map.removeAllShapes();
	$.ajax({
		url : "/request",
		method : "post",
		async : true,
		dataType : "json",
		timeout : 30000,
		data : {
			query : query,
			type : type,
			lat : lat,
			lon : lon,
			radius : radius,
			venue : venue
		},
		before : function() {
			$("body").css("cursor", "progress");
		},
		success : handleServerResponse(type),
		error : genericErrorMessage(),
		complete : function() {
			$("body").css("cursor", "default");
		}
	});
}

function getGeoLocation(query, lat, lon, radius, callback) {
	$.ajax({
		url : 'http://open.mapquestapi.com/nominatim/v1/search/' + query + '?format=json&addressdetails=1',
		crossDomain : true,
		async : true,
		timeout : 10000,
		success : function(data, status, xhr) {
			if (data.length === 0 || data[0].lat === "" || data[0].lon === "") {
				createErrorDialog("<b>Please try again or redefine your query!</b><br/></br>We could not find any specified location based on your input.");
				return;
			}
			callback(query, "location", data[0].lat, data[0].lon, radius);
		},
		error : genericErrorMessage()
	});
}

function genericErrorMessage() {
	return function(xhr, status, errorThrown) {
		createErrorDialog("<b>ERROR:</b> " + status + "<br/>" + errorThrown);
	};
}

function handleServerResponse(type) {
	return function(data, textStatus, jqXHR) {
		if (data.error === undefined && (data[0] !== undefined ? data[0].error === undefined : true)) {
			var infoContentHTML, venue, info;
			console.log("----- request response retrieved for: " + type);
			pinCollection = new MQA.ShapeCollection();
			if (type === "artist") {
				if (data.events === undefined || jQuery.isEmptyObject(data.events))
					createErrorDialog("<b>It occurs as if the artist is not on tour.</b><br/><br/>Please try another search term.");
				addArtistInformationOnMap(data);
			} else if (type === "location") {
				var info, artist;
				$.each(data, function(i, obj) {
					info = createPOI("info", obj.venue.latitude, obj.venue.longitude);

					artist = (obj.artists.length >= 1 ? obj.artists[0] : {
						name : obj.title,
						lastfm : "http://last.fm/event/" + obj.id,
						genres : []
					})
					infoContentHTML = createInfoContentHtml(obj, artist, obj.venue, false);

					info.setRolloverContent(createInfoContentHtml(obj, artist, obj.venue, true));
					info.setInfoContentHTML(infoContentHTML);
					infoContentHTML = "";
					info.setDeclutterMode(true);
					pinCollection.add(info);
				});
				map.addShapeCollection(pinCollection);
			} else if (type === "venue") {
				// TODO
				var info, artist;
				$.each(data.events, function(i, eventObj) {
					info = createPOI("info", data.latitude, data.longitude);

					artist = (eventObj.artists.length >= 1 ? eventObj.artists[0] : {
						name : eventObj.title,
						lastfm : "http://last.fm/event/" + eventObj.id,
						genres : []
					})
					infoContentHTML = createInfoContentHtml(eventObj, artist, data, false);

					info.setRolloverContent(createInfoContentHtml(eventObj, artist, data, true));
					info.setInfoContentHTML(infoContentHTML);
					infoContentHTML = "";
					info.setDeclutterMode(true);
					pinCollection.add(info);
				});
				map.addShapeCollection(pinCollection);
			} else if (type === "venueSearch") {
				var html = "";
				$.each(data, function(key, json) {
					html += "<input type='checkbox' id='" + json.id + "' name='venue'/>" + json.name + (json.city !== "" ? " " + json.city : "") + (json.country !== "" ? " " + json.country : "") + "<br/>";
				});
				$("#error-panel").html(html).dialog({
					modal : true,
					height : 300,
					width : 350,
					buttons : [{
						text : "Ok",
						click : function() {
							$(this).dialog("close");
							sendRequest($('#error-panel input:checkbox:checked').val(), "venue", "", "", 25, $('#error-panel input:checkbox:checked').attr('id'));
							// venue id
						}
					}, {
						text : "Cancel",
						click : function() {
							$(this).dialog("close");
						}
					}]
				}).css("z-index", "999999").find("input:checkbox").click(function() {
					if ($(this).is(":checked")) {
						var group = "input:checkbox[name='" + $(this).attr("name") + "']";
						$(group).prop("checked", false);
						$(this).prop("checked", true);
					} else {
						$(this).prop("checked", true);
					}
				}).first().prop("checked", true);
				;
			} else {
				createErrorDialog("<b>ERROR:</b> An unknown error occured.");
			}
		} else {
			var html = (data.message !== undefined ? data.message : (data[0].message !== undefined ? data[0].message : "An unknown error occured while querying last.fm."));
			createErrorDialog("<b>Please try again or redefine your query!</b><br/></br>" + html);
		}
		map.bestFit();
	};
}

function addArtistInformationOnMap(data) {
	$.each(data.events, function(eventID, eventObj) {
		infoContentHTML = "";
		venue = eventObj.venue;
		if (venue.latitude !== "" && venue.longitude !== "") {
			info = createPOI("info", venue.latitude, venue.longitude);
		} else {
			nominatimBackUpQuery(eventObj, data);
			return;
		}
		// TODO infoWindow

		// title
		infoContentHTML = createInfoContentHtml(eventObj, data, venue, false);

		if (!info)
			return;

		info.setRolloverContent(createInfoContentHtml(eventObj, data, venue, true));
		info.setInfoContentHTML(infoContentHTML);
		infoContentHTML = "";
		info.setDeclutterMode(true);
		pinCollection.add(info);
	});
	map.addShapeCollection(pinCollection);
}

function createInfoContentHtml(eventObj, data, venue, isRollOverText) {
	var infoContentHTML = "<h3><a href='http://last.fm/event/" + eventObj.id + "'>" + eventObj.title + (eventObj.title == data.name && venue.name !== "" ? " in " + venue.name : "") + "</a></h3>";

	// images
	if (data.img)
		infoContentHTML += "<div class='img'><img src='" + data.img + "' alt='artist_pic' /></div>";
	if (venue.img)
		infoContentHTML += "<div class='img'><img src='" + venue.img + "' alt='venue_pic' /></div>";
	// artists
	if (isRollOverText === false) {
		infoContentHTML += "<br/><b>Who?</b> <br/>";
		if (eventObj.artists.length > 0) {
			var i = 1, names = "";
			$.each(eventObj.artists, function(i, artist) {
				i++;
				if (i == 4) {
					// TODO shorten?
				}
				infoContentHTML += "<a href='http://last.fm/music/" + artist.name + "'>" + artist.name + "</a>" + (i == eventObj.artists.length ? "" : ", ");
			});
			infoContentHTML = infoContentHTML.substr(0, infoContentHTML.lastIndexOf(', ')) + "<br/>";
		} else {
			infoContentHTML += "<a href='" + data.lastfm + "'>" + data.name + "</a><br/>";
		}
		if (data.description) {
			//var description = data.description.substr(0, data.description.lastIndexOf('<a')).replace(/(\n|\\n)/g, "<br/>").replace(/\t/g, "");
			var description = data.description.replace(/(\n|\\n)/g, "<br/>").replace(/\t/g, "");
			infoContentHTML += "<br/><div class='description'>" + description + "</div>"
		};
		// wiki link
		if (data.wiki)
			infoContentHTML += "<a href='" + data.wiki + "' class='moreLink'>more info</a><br/>";
	}
	// date
	infoContentHTML += "<br/><b>When?</b><br/> " + eventObj.date + "<br/><br/>"
	// address
	infoContentHTML += "<b>Where?</b> <br/>";
	if (venue.name)
		infoContentHTML += "<a href='http://last.fm/venue/" + venue.id + "'>" + venue.name + "</a><br/>";
	if (venue.street)
		infoContentHTML += venue.street + "<br/>";
	if (venue.postalCode)
		infoContentHTML += venue.postalCode + " ";
	if (venue.city)
		infoContentHTML += venue.city + "<br/>";
	if (venue.country)
		infoContentHTML += venue.country + "<br/>";
	// tel / homepage
	if (venue.homepage || data.homepage)
		infoContentHTML += "<br/><b>Web:</b> <a href='" + (venue.homepage ? venue.homepage + "'>" + venue.homepage + "</a>" + (data.homepage ? ", <a href='" + data.homepage + "'>" + data.homepage + "</a>" : "") : data.homepage + "'>" + data.homepage + "</a>");
	// tickets
	if (eventObj.tickets)
		infoContentHTML += "<br/><b>Tickets:</b> <a href='" + eventObj.tickets + "'>" + (eventObj.tickets.length > 55 ? eventObj.tickets.substr(0, 55) + "..." : eventObj.tickets) + "</a>";
	// bar
	infoContentHTML += "<hr/>";
	// genre

	if (data.genres.length === 0)
		data.genres = eventObj.tags;
	for (var i = 0; i < data.genres.length; i++) {
		infoContentHTML += "<a href='http://last.fm/tag/" + data.genres[i] + "'>" + data.genres[i] + "</a>";
		if (i !== data.genres.length - 1)
			infoContentHTML += ", ";
	}
	// more details

	return infoContentHTML;
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
				if (nomQuery.indexOf(',') !== -1) {
					console.log("----- retry nominatim request with modified query");
					var retry = nomQuery.substr(nomQuery.indexOf(',') + 1);
					retry = url.replace(nomQuery, retry);
					console.log("debug# nominatim query:");
					console.log(nomQuery);
					console.log(url);
					console.log("debug# Event object:");
					nominatimRequest(retry, "", eventObj, pData, true);
					return;
				}
				once = false;
				return;

			} else if (data.length === 0) {// ignore result
				console.log("debug# nominatim query:");
				console.log(nomQuery);
				console.log(url);
				console.log("debug# Event object:");
				console.log(eventObj);
				createErrorDialog("Some results could not be located on the map due to missing geo information.<br/><br/>If there are no results on the map at all, you will have to modify your query.")
				return;
			}

			console.log("----- nominatim retrieval successful");

			// TODO alles abgefangen?

			var displayName = data[0].display_name;
			if ((eventObj.venue.country !== "" && displayName.indexOf(eventObj.venue.country) !== -1) || (eventObj.venue.city !== "" && displayName.indexOf(eventObj.venue.city) !== -1)) {
				info = createPOI("info", data[0].lat, data[0].lon);
				// TODO infoWindow

				var infoContentHTML = createInfoContentHtml(eventObj, pData, eventObj.venue, false);
				//= "<h4>" + eventObj.title + (((eventObj.title == pData.name) && (eventObj.venue.name !== "")) ? " in " + eventObj.venue.name : " in " + ((displayName.indexOf(',') !== -1) ? (displayName.substr(0, displayName.indexOf(',')) === "undefined" ? (displayName.substr(11, displayName.substr(11).indexOf(',')) !== "undefined" ? displayName.substr(11, displayName.substr(11).indexOf(',')) : "") : "") : displayName)
				//) + "</h4>";
				infoContentHTML += "";

				info.setRolloverContent(createInfoContentHtml(eventObj, pData, eventObj.venue, true));
				info.setInfoContentHTML(infoContentHTML);
				info.setDeclutterMode(true);
				pinCollection.add(info);

				map.addShapeCollection(pinCollection);
				once = false;
			} else {
				createErrorDialog("Some results could not be located on the map due to missing geo information.<br/><br/>If there are no results on the map at all, you will have to modify your query.")
			}
			//poi.setInfoContentHTML(data.display_name);
			//poi.toggleInfoWindow();
			map.bestFit();
		},
		error : genericErrorMessage()
	});
}

function createPOI(info, lat, lon) {
	window[info] = new MQA.Poi({
		lat : lat,
		lng : lon
	});
	MQA.EventManager.addListener(window[info], 'mouseover', function() {
	});
	return window[info];
}

function createErrorDialog(html) {
	$("#error-panel").html(html).dialog({
		modal : true,
		buttons : {
			Ok : function() {
				$(this).dialog("close");
			}
		}
	});
}
