package controllers;

import java.io.IOException;
import java.util.HashMap;

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
import com.hp.hpl.jena.rdf.model.RDFNode;

public class Application extends Controller {

	static Form<Request> requestForm = Form.form(Request.class);
	static Form<Request> filledForm;

	HttpRequestManager request;
	SparqlQueryManager sparql;
	MusicbrainzSparqlFactory musicbrainz;
	LastfmUri lastfm;
	
	public Application(){
		request = HttpRequestManager.getInstance();
		sparql = SparqlQueryManager.getInstance();
		musicbrainz = MusicbrainzSparqlFactory.getInstance();
		lastfm = LastfmUri.getInstance();
	}
	
	public static Result index() {
		return controllers.Application.displayMap();
	}

	// default map rendering
	public static Result displayMap() {
		if (filledForm != null) {
			Form<Request> tmp = filledForm;
			filledForm = null;
			return badRequest(map.render("Map", tmp));
		}
		return ok(map.render("Map", requestForm));
	}

	// handle input on map page
	public static Result processQuery() {
		Request req;
		filledForm = requestForm.bindFromRequest();
		if (filledForm.hasErrors()) {
			return controllers.Application.displayMap();
		} else {
			req = filledForm.get(); // queryType
		}
		//JsonNode v = Json.toJson(req);
		//String jsonString = v.toString();
		// TODO trigger some kind of request that will return a JSON
		JsonNode result = Json.parse("{\"1\":1,\"2\" : {\"child\" : [1,2,3,4]} }");
		return ok(result);
	}
	
	public String getLocalEvents(String longitude, String latitude, int radius){
		return null;
	}
	
	/**
	 * Send artist.getEvents request to Last.fm, send SPARQL query to get artist information, and send SPARQL query to FlickrWrappr
	 * for pictures. Parse all responses and store the information in the respective objects - Artist, Event, Venue.
	 * @param artist - The artist to search for.
	 * @param festivalsOnly - <code>true</code> if only festivals shall be displayed, <code>false</code> if all events shall be displayed.
	 * @return an Artist instance containing all events and artist information.
	 * @throws IOException
	 */
	public JsonNode getArtistEvents(String artist, boolean festivalsOnly) throws IOException{		
		
		// create parameter for Last.fm query to retrieve artist info (especially for mbid)
		String params = LastfmUri.getInstance().getArtistInfo(artist);
		JsonNode jsonArtistInfo = request.sendRequest("GET", LastfmUri.ENDPOINT, params);
		jsonArtistInfo = jsonArtistInfo.get("artist");
		String mbid = jsonArtistInfo.get("mbid").toString().replaceAll("\"", "");
		String name = jsonArtistInfo.get("name").toString().replaceAll("\"", "");
		
		// create artist with name and mbid (if existing)
		Artist a;
		if(mbid != null){
			a = new Artist(mbid, name);
		} else {
			a = new Artist(name);
		}
		
		// get whether the artist is on tour or not
		if(jsonArtistInfo.get("ontour").toString().replaceAll("\"", "").equals("1")){
			a.setOnTour(true);
		}
		
		// get image link
		for(JsonNode image : jsonArtistInfo.get("image")){
			if(image.get("size").toString().replaceAll("\"", "").equals("large"));
			a.setImg(image.get("#text").toString().replaceAll("\"", ""));
		}
		
		
		// Create parameter for Last.fm query to retrieve all events. Send request to Last.fm and store response in JsonNode.
		if(a.getMbid() != null){
			params = LastfmUri.getInstance().getArtistEventsViaMbid(a.getMbid(), festivalsOnly);
		} else {
			params = LastfmUri.getInstance().getArtistEvents(artist, festivalsOnly);
		}
		JsonNode jsonArtistEvents = request.sendRequest("GET", LastfmUri.ENDPOINT, params);
		
		// Extract all information from result JsonNode and store them in respective objects.
		JsonNode events = jsonArtistEvents.get("events").get("event");
		HashMap<String, Event> eventList = new HashMap<String, Event>();
		for(JsonNode jEvent : events){
			Event event = new Event(jEvent.get("id").toString().replaceAll("\"", ""), jEvent.get("title").toString().replaceAll("\"", ""));
			
			for(JsonNode jArtist : jEvent.get("artists").get("artist")){
				event.addArtist(new Artist(jArtist.toString().replaceAll("\"", "")));
			}
			
			event.setDate(jEvent.get("startDate").toString().replaceAll("\"", ""));
			event.setTickets(jEvent.get("website").toString().replaceAll("\"", ""));
			
			JsonNode jVenue = jEvent.get("venue");
			Venue venue = new Venue(
					jVenue.get("id").toString().replaceAll("\"", ""),
					jVenue.get("name").toString().replaceAll("\"", ""));
			venue.setLatitude(jVenue.get("location").get("geo:point").get("geo:lat").toString().replaceAll("\"", ""));
			venue.setLongitude(jVenue.get("location").get("geo:point").get("geo:long").toString().replaceAll("\"", ""));
			venue.setCity(jVenue.get("location").get("city").toString().replaceAll("\"", ""));
			venue.setCountry(jVenue.get("location").get("country").toString().replaceAll("\"", ""));
			venue.setStreet(jVenue.get("location").get("street").toString().replaceAll("\"", ""));
			venue.setPostalCode(jVenue.get("location").get("postalcode").toString().replaceAll("\"", ""));
			venue.setHomepage(jVenue.get("website").toString().replaceAll("\"", ""));
			venue.setPhone(jVenue.get("phonenumber").toString().replaceAll("\"", ""));
			event.setVenue(venue);
			
			// store Event in event list.
			eventList.put(event.getId(), event);
		}
		
		// Store the all events in artist's object.
		a.setEvents(eventList);
		
		// query Musicbrainz via SPARQL in order to get further artist info.
		String query;
		if(a.getMbid() != null){
			query = musicbrainz.getArtistInfoViaMbid(a.getMbid());
		} else {
			query = musicbrainz.getArtistInfo(artist);
		}
		
		// extract info from SPARQL response
		HashMap<RDFNode, RDFNode> nodes = sparql.sendQuery(MusicbrainzSparqlFactory.ENDPOINT, query);
		for(RDFNode key : nodes.keySet()){
			System.out.println("Key: " + key.toString());
			System.out.println("Value: " + nodes.get(key).toString());
			System.out.println("");
		}		
		
		return Json.toJson(a);
	}
	
	public String getVenueEvents(String venue, String country){
		return null;
	}
	
}
