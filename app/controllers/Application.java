package controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.impl.conn.tsccm.RefQueueHandler;

import models.Artist;
import models.Event;
import models.HttpRequestManager;
import models.LastfmUri;
import models.MusicbrainzSparqlFactory;
import models.Request;
import models.SparqlQueryManager;
import models.Venue;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class Application extends Controller {

	// framerwork vars
	static Form<Request> requestForm = Form.form(Request.class);
	static Form<Request> filledForm;

	// ref vars
	static HttpRequestManager request = HttpRequestManager.getInstance();
	static SparqlQueryManager sparql = SparqlQueryManager.getInstance();
	static MusicbrainzSparqlFactory musicbrainz = MusicbrainzSparqlFactory.getInstance();
	static LastfmUri lastfm = LastfmUri.getInstance();

	// deprecated but still in routes..
	public static Result index() {
		return controllers.Application.displayMap();
	}

	// default map rendering
	public static Result displayMap() {
		if (filledForm != null) {
			// TODO some detailed error handling - either here or in html
			Form<Request> tmp = filledForm;
			filledForm = null;
			return badRequest(map.render("Map", tmp));
		}
		return ok(map.render("Map", requestForm));
	}

	// handle input on map page
	public static Result processQuery() {
		String queryString = "";
		Request req = null;
		JsonNode result = null;
		filledForm = requestForm.bindFromRequest();
		
		if (filledForm.hasErrors()) {
			return controllers.Application.displayMap();
		} 
		
		req = filledForm.get();
		queryString = req.type.toString();
		
		// select query
		if (queryString.equals("artist")) {
			try {
				result = getArtistEvents(req.query, false);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// TODO error handling depending on result object
		}
		else if (queryString.equals("location")) {
			try {
				ArrayNode a = JsonNodeFactory.instance.arrayNode();
				// TODO remove 25 (def value) and implement selector on UI
				a.addAll(getLocalEvents(req.lon, req.lat, 25, false)); 
				result = a;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if (queryString.equals("venue")) {}
		else {
			// TODO error handling
		}
		
		// TODO trigger some kind of request that will return a JSON
		//result = Json.parse("{\"1\":1,\"2\" : {\"child\" : [1,2,3,4]} }");
		return ok(result);
	}

	/**
	 * Send geo.getEvents request to Last.fm. Parse response responses and store
	 * the information in the respective objects - Artist, Event, Venue.
	 * 
	 * @param longitude
	 *            - The longitude of a geo location.
	 * @param latitude
	 *            - The latitude of a geo location.
	 * @param radius
	 *            - A maximum radius to search for events with provided geo
	 *            location (longitude and latitude) as center.
	 * @param festivalsOnly
	 *            - <code>true</code> if only festivals shall be retrieved,
	 *            <code>false</code> if all events shall be displayed.
	 * @return eventNodes - A list of all events stored as JsonNode objects.
	 * @throws IOException
	 */
	public static ArrayList<JsonNode> getLocalEvents(String longitude, String latitude, int radius, boolean festivalsOnly) throws IOException {
		ArrayList<JsonNode> eventNodes = new ArrayList<JsonNode>();

		// create parameter for Last.fm query to retrieve local event info
		String params = LastfmUri.getInstance().getGeoEvents(longitude, latitude, radius, festivalsOnly);
		JsonNode jsonGeoEvents = request.sendRequest("GET", LastfmUri.ENDPOINT, params);
		jsonGeoEvents = jsonGeoEvents.get("events");

		// iterate through each event, create object and set variables
		if (jsonGeoEvents.get("event") != null) {
			for (JsonNode event : jsonGeoEvents.get("event")) {
				Event e = new Event(event.get("id").toString().replaceAll("\"", ""), event.get("title").toString().replaceAll("\"", ""));
				e.setDate(event.get("startDate").toString().replaceAll("\"", ""));

				if (event.get("artists") != null) {
					for (JsonNode artist : event.get("artists").get("artist")) {
						Artist a = new Artist(artist.toString().replaceAll("\"", ""));
						e.addArtist(a);
					}
				}

				JsonNode jsonVenue = event.get("venue");
				if (jsonVenue != null) {
					Venue v = new Venue(jsonVenue.get("id").toString().replaceAll("\"", ""), jsonVenue.get("name").toString().replaceAll("\"", ""));
					v.setHomepage(jsonVenue.get("website").toString().replaceAll("\"", ""));
					v.setPhone(jsonVenue.get("phonenumber").toString().replaceAll("\"", ""));

					if (jsonVenue.get("location") != null) {
						if (jsonVenue.get("location").get("geo:point") != null) {
							v.setLatitude(jsonVenue.get("location").get("geo:point").get("geo:lat").toString().replaceAll("\"", ""));
							v.setLongitude(jsonVenue.get("location").get("geo:point").get("geo:long").toString().replaceAll("\"", ""));
						}
						v.setCity(jsonVenue.get("location").get("city").toString().replaceAll("\"", ""));
						v.setCountry(jsonVenue.get("location").get("country").toString().replaceAll("\"", ""));
						v.setStreet(jsonVenue.get("location").get("street").toString().replaceAll("\"", ""));
						v.setPostalCode(jsonVenue.get("location").get("postalcode").toString().replaceAll("\"", ""));
					}

					if (event.get("image") != null) {
						for (JsonNode image : event.get("image")) {
							if (image.get("size").toString().replaceAll("\"", "").equals("large")) {
								v.setImg(image.get("#text").toString().replaceAll("\"", ""));
							}
						}
					}

					// convert event to JsonNode and store it in eventNodes
					// list.
					eventNodes.add(Json.toJson(e));
				}

			}
		}

		return eventNodes;
	}

	/**
	 * Send artist.getEvents request to Last.fm, and send SPARQL query to get
	 * artist information. Parse all responses and store the information in the
	 * respective objects - Artist, Event, Venue.
	 * 
	 * @param artist
	 *            - The artist to search for.
	 * @param festivalsOnly
	 *            - <code>true</code> if only festivals shall be displayed,
	 *            <code>false</code> if all events shall be displayed.
	 * @return a JsonNode instance containing all artist and event information.
	 * @throws IOException 
	 */
	public static JsonNode getArtistEvents(String artist, boolean festivalsOnly) throws IOException {

		// create parameter for Last.fm query to retrieve artist info
		// (especially for mbid)
		String params = LastfmUri.getInstance().getArtistInfo(artist);
		JsonNode jsonArtistInfo = request.sendRequest("GET", LastfmUri.ENDPOINT, params);
		jsonArtistInfo = jsonArtistInfo.get("artist");

		if (jsonArtistInfo != null) {
			String mbid = jsonArtistInfo.get("mbid").toString().replaceAll("\"", "");
			String name = jsonArtistInfo.get("name").toString().replaceAll("\"", "");

			// create artist with name and mbid (if existing)
			Artist a = new Artist(mbid, name);

			// get whether the artist is on tour or not
			if (jsonArtistInfo.get("ontour").toString().replaceAll("\"", "").equals("1")) {
				a.setOnTour(true);
			}

			// get image link
			if (jsonArtistInfo.get("image") != null) {
				for (JsonNode image : jsonArtistInfo.get("image")) {
					if (image.get("size").toString().replaceAll("\"", "").equals("large"))
						;
					a.setImg(image.get("#text").toString().replaceAll("\"", ""));
				}
			}

			// Create parameter for Last.fm query to retrieve all events.
			// Send request to Last.fm and store response in JsonNode.
			if (a.getMbid() != null) {
				params = LastfmUri.getInstance().getArtistEventsViaMbid(a.getMbid(), festivalsOnly);
			} else {
				params = LastfmUri.getInstance().getArtistEvents(artist, festivalsOnly);
			}
			JsonNode jsonArtistEvents = request.sendRequest("GET", LastfmUri.ENDPOINT, params);

			// Extract all information from result JsonNode and store them
			// in respective objects.
			if (jsonArtistEvents.get("events") != null) {
				JsonNode events = jsonArtistEvents.get("events").get("event");
				HashMap<String, Event> eventList = new HashMap<String, Event>();
				if (events != null) {
					for (JsonNode jEvent : events) {
						Event event = new Event(jEvent.get("id").toString().replaceAll("\"", ""), jEvent.get("title").toString().replaceAll("\"", ""));
						event.setDate(jEvent.get("startDate").toString().replaceAll("\"", ""));
						event.setTickets(jEvent.get("website").toString().replaceAll("\"", ""));

						if (jEvent.get("artists") != null) {
							for (JsonNode jArtist : jEvent.get("artists").get("artist")) {
								event.addArtist(new Artist(jArtist.toString().replaceAll("\"", "")));
							}
						}

						// extract venue information and set its variables
						JsonNode jVenue = jEvent.get("venue");
						if (jVenue != null) {
							Venue venue = new Venue(jVenue.get("id").toString().replaceAll("\"", ""), jVenue.get("name").toString().replaceAll("\"", ""));
							venue.setHomepage(jVenue.get("website").toString().replaceAll("\"", ""));
							venue.setPhone(jVenue.get("phonenumber").toString().replaceAll("\"", ""));

							if (jVenue.get("location") != null) {
								if (jVenue.get("location").get("geo:point") != null) {
									venue.setLatitude(jVenue.get("location").get("geo:point").get("geo:lat").toString().replaceAll("\"", ""));
									venue.setLongitude(jVenue.get("location").get("geo:point").get("geo:long").toString().replaceAll("\"", ""));
								}
								venue.setCity(jVenue.get("location").get("city").toString().replaceAll("\"", ""));
								venue.setCountry(jVenue.get("location").get("country").toString().replaceAll("\"", ""));
								venue.setStreet(jVenue.get("location").get("street").toString().replaceAll("\"", ""));
								venue.setPostalCode(jVenue.get("location").get("postalcode").toString().replaceAll("\"", ""));
							}
							
							if(jVenue.get("image") != null){
								for(JsonNode image : jVenue.get("image")){
									if(image.get("size").toString().replaceAll("\"", "").equals("large")){
										venue.setImg(image.get("#text").toString().replaceAll("\"", ""));
									}
								}
							}
							
							// set venue and store Event in event list.
							event.setVenue(venue);
							eventList.put(event.getId(), event);
						}
					}
				}

				// Store the all events in artist's object.
				a.setEvents(eventList);
			}

			// query Musicbrainz via SPARQL in order to get further artist
			// info.
			String query;
			if (a.getMbid() != null) {
				query = musicbrainz.getArtistInfoViaMbid(a.getMbid());
			} else {
				query = musicbrainz.getArtistInfo(artist);
			}

			// extract info from SPARQL response
			HashMap<RDFNode, RDFNode> nodes = sparql.sendQuery(MusicbrainzSparqlFactory.ENDPOINT, query);
			for (RDFNode key : nodes.keySet()) {
				System.out.println("Key: " + key.toString());
				System.out.println("Value: " + nodes.get(key).toString());
				System.out.println("");
			}

			return Json.toJson(a);
		}

		return null;
	}

	/**
	 * Send venue.getEvents request to Last.fm to event information. Parse
	 * response and store the information in the respective objects - Artist,
	 * Event, Venue.
	 * 
	 * @param id
	 *            - A unique venue id.
	 * @param festivalsOnly
	 *            - <code>true</code> if only festivals shall be displayed,
	 *            <code>false</code> if all events shall be displayed.
	 * @return a JsonNode instance containing venue and event information.
	 * @throws IOException 
	 */
	public JsonNode getVenueEvents(String id, boolean festivalsOnly) throws IOException {

		// create parameter for Last.fm query to retrieve venue info
		// (especially events)
		String params = LastfmUri.getInstance().getVenueEvents(id, festivalsOnly);
		JsonNode jsonVenueEvents = request.sendRequest("GET", LastfmUri.ENDPOINT, params);
		jsonVenueEvents = jsonVenueEvents.get("events");

		// iterate through each event, create object, set variables and add
		// it to venue object
		Venue venue = null;
		boolean isFirst = true;
		if (jsonVenueEvents != null) {
			for (JsonNode event : jsonVenueEvents.get("event")) {

				// for the first element in the list, extract venue
				// information (stored in all events, but only needed once)
				if (isFirst) {
					isFirst = false; // ensures that this if clause is only
										// entered once

					// extract venue information and store it in Venue
					// instance
					JsonNode jsonVenue = event.get("venue");
					if (jsonVenue != null) {
						venue = new Venue(jsonVenue.get("id").toString().replaceAll("\"", ""), jsonVenue.get("name").toString().replaceAll("\"", ""));
						venue.setHomepage(jsonVenue.get("website").toString().replaceAll("\"", ""));
						venue.setPhone(jsonVenue.get("phonenumber").toString().replaceAll("\"", ""));

						if (jsonVenue.get("location") != null) {
							if (jsonVenue.get("location").get("geo:point") != null) {
								venue.setLatitude(jsonVenue.get("location").get("geo:point").get("geo:lat").toString().replaceAll("\"", ""));
								venue.setLongitude(jsonVenue.get("location").get("geo:point").get("geo:long").toString().replaceAll("\"", ""));
							}
							venue.setCity(jsonVenue.get("location").get("city").toString().replaceAll("\"", ""));
							venue.setCountry(jsonVenue.get("location").get("country").toString().replaceAll("\"", ""));
							venue.setStreet(jsonVenue.get("location").get("street").toString().replaceAll("\"", ""));
							venue.setPostalCode(jsonVenue.get("location").get("postalcode").toString().replaceAll("\"", ""));
						}

						// retrieve venue picture link with size 'large'
						for (JsonNode image : jsonVenue.get("image")) {
							if (image.get("size").toString().replaceAll("\"", "").equals("large")) {
								venue.setImg(image.get("#text").toString().replaceAll("\"", ""));
							}
						}
					}

				}

				// extract event information, store it in Event instance and
				// add it to the Venue instance
				Event e = new Event(event.get("id").toString().replaceAll("\"", ""), event.get("title").toString().replaceAll("\"", ""));
				e.setDate(event.get("startDate").toString().replaceAll("\"", ""));
				e.setTickets(event.get("website").toString().replaceAll("\"", ""));
				ArrayList<Artist> artists = new ArrayList<Artist>();
				if (event.get("artists") != null) {
					for (JsonNode artist : event.get("artists").get("artist")) {
						Artist a = new Artist(artist.toString().replaceAll("\"", ""));
						artists.add(a);
					}
				}
				e.setArtists(artists);

				if (venue != null) {
					venue.addEvent(e);
				}

			}
		}

		if (venue != null) {
			return Json.toJson(venue);
		} else {
			return null;
		}

	}

//	public static void main(String[] args) throws IOException {
//		Application app = new Application();
//
//		System.out.println(app.getArtistEvents("Boyz II Men", false).toString());
//		System.out.println(app.getLocalEvents(null, null, 20, false));
//		System.out.println(app.getVenueEvents("8908030", false));
//
//	}

}
