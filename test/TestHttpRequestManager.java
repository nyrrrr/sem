import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import models.Artist;
import models.Event;
import models.HttpRequestManager;
import models.LastfmUri;
import models.MusicbrainzSparqlFactory;
import models.SparqlQueryManager;
import models.Venue;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.hp.hpl.jena.rdf.model.RDFNode;


public class TestHttpRequestManager {

	@Test
	public void test() throws IOException {
		String artist = "Volbeat";
		boolean festivalsOnly = false;
		
		HttpRequestManager request = HttpRequestManager.getInstance();
		LastfmUri lastfm = LastfmUri.getInstance();
		MusicbrainzSparqlFactory musicbrainz = MusicbrainzSparqlFactory.getInstance();
		SparqlQueryManager sparqlManager = SparqlQueryManager.getInstance();
		
		String params = LastfmUri.getInstance().getArtistEvents(artist, festivalsOnly);
//		String params = LastfmUri.getInstance().getEventInfo(3669169);
		JsonNode jsonLastfm = request.sendRequest("GET", LastfmUri.getInstance().ENDPOINT, params);
		JsonNode events = jsonLastfm.get("events").get("event");
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
			
			eventList.put(event.getId(), event);
		}
		Artist a = new Artist(artist);
		a.setEvents(eventList);
		
//		HttpRequestManager.getInstance().sendRequest("GET", "artist", "Enter Shikari", "artist.getEvents");
//		HttpRequestManager.getInstance().sendRequest("GET", "artist", "Enter Shikari", "artist.getInfo");
//		HttpRequestManager.getInstance().sendRequest("GET", "artist", "Enter Shikari", "artist.search");
//		HttpRequestManager.getInstance().sendRequest("GET", "venue", "8788926", "venue.getEvents");
//		HttpRequestManager.getInstance().sendRequest("GET", "location", "Ludwigshafen am Rhein", "geo.getEvents");
		
		System.out.println("----------");
		
		String query = musicbrainz.getArtistInfo(artist);
//		String query = musicbrainz.getResource("http://dbtune.org/musicbrainz/resource/artist/4753fcb7-9270-493a-974d-8daca4e49125");
//		RDFNode node = sparqlManager.sendQuery(musicbrainz.ENDPOINT, query).get(0);
		HashMap<RDFNode, RDFNode> nodes = sparqlManager.sendQuery(musicbrainz.ENDPOINT, query);
		
		System.out.println("----------");
		System.out.println("----------");
		params = LastfmUri.getInstance().getArtistEvents("Enter Shikari", false);
		JsonNode json2 = request.sendRequest("GET", LastfmUri.getInstance().ENDPOINT, params);
		System.out.println("----------");
		query = musicbrainz.getArtistInfo("Enter Shikari");
		sparqlManager.sendQuery(musicbrainz.ENDPOINT, query);
	}

}
