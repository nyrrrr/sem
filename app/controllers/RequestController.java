package controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import play.libs.Json;

import com.fasterxml.jackson.databind.JsonNode;
import com.hp.hpl.jena.rdf.model.RDFNode;

import models.Artist;
import models.DBPediaSparqlFactory;
import models.Event;
import models.HttpRequestManager;
import models.LastfmUri;
import models.MusicbrainzSparqlFactory;
import models.SparqlQueryManager;
import models.Venue;

public class RequestController {
	// ref vars
	static HttpRequestManager request = HttpRequestManager.getInstance();
	static SparqlQueryManager sparql = SparqlQueryManager.getInstance();
	static MusicbrainzSparqlFactory musicbrainz = MusicbrainzSparqlFactory.getInstance();
	static DBPediaSparqlFactory dbpedia = DBPediaSparqlFactory.getInstance();
	static LastfmUri lastfm = LastfmUri.getInstance();

	private RequestController() {
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
	public static ArrayList<JsonNode> getLocalEvents(String latitude, String longitude, int radius, boolean festivalsOnly) throws IOException {
		ArrayList<JsonNode> eventNodes = new ArrayList<JsonNode>();

		// create parameter for Last.fm query to retrieve local event info
		String params = LastfmUri.getInstance().getGeoEvents(latitude, longitude, radius, festivalsOnly);
		JsonNode jsonGeoEvents = request.sendRequest("GET", LastfmUri.ENDPOINT, params);
		if (jsonGeoEvents.get("error") != null) {
			eventNodes.add(jsonGeoEvents);
			return eventNodes;
		} else {
			jsonGeoEvents = jsonGeoEvents.get("events");

			// iterate through each event, create object and set variables
			if (jsonGeoEvents != null && jsonGeoEvents.get("event") != null) {
				if(jsonGeoEvents.get("event").getNodeType().name().equals("ARRAY")){
					jsonGeoEvents = jsonGeoEvents.get("event");
				}
				
				for (JsonNode event : jsonGeoEvents) {
					if(event.get("id") != null && event.get("title") != null){
						Event e = new Event(event.get("id").toString().replaceAll("\"", ""), event.get("title").toString().replaceAll("\"", ""));
						e.setDate(event.get("startDate").toString().replaceAll("\"", ""));

						if (event.get("artists") != null) {
							for (JsonNode artist : event.get("artists").get("artist")) {
								Artist a = new Artist(artist.toString().replaceAll("\"", ""));
								e.addArtist(a);
							}
						}

						if (event.get("tags") != null) {
							for (JsonNode tag : event.get("tags").get("tag")) {
								e.addTag(tag.toString().replaceAll("\"", ""));
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
							e.setVenue(v);
							eventNodes.add(Json.toJson(e));
						}

					}
					
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
		if (jsonArtistInfo.get("error") != null) {
			return jsonArtistInfo;
		} else {
			jsonArtistInfo = jsonArtistInfo.get("artist");

			if (jsonArtistInfo != null) {
				String mbid = jsonArtistInfo.get("mbid").toString().replaceAll("\"", "");
				String name = jsonArtistInfo.get("name").toString().replaceAll("\"", "");

				// create artist with name and mbid (if existing)
				Artist a = new Artist(mbid, name);
				a.setLastfm(jsonArtistInfo.get("url").toString().replaceAll("\"", ""));

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

				// get genres
				if (jsonArtistInfo.get("tags") != null && jsonArtistInfo.get("tags").get("tag") != null) {
					for (JsonNode genre : jsonArtistInfo.get("tags").get("tag")) {
						a.addGenre(genre.get("name").toString().replaceAll("\"", ""));
					}
				}
				
				// get artist summary
				if(jsonArtistInfo.get("bio") != null && jsonArtistInfo.get("bio").get("summary") != null){
					a.setDescription(jsonArtistInfo.get("bio").get("summary").toString().replaceAll("\"", ""));
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
				if (jsonArtistEvents.get("events") != null && jsonArtistEvents.get("events").get("event") != null) {
					JsonNode events = jsonArtistEvents.get("events");
					HashMap<String, Event> eventList = new HashMap<String, Event>();
					
					if(events.get("event").getNodeType().name().equals("ARRAY")){
						events = events.get("event");
					}
					for (JsonNode jEvent : events) {
						if(jEvent.get("id") != null && jEvent.get("title") != null){
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

								if (jVenue.get("image") != null) {
									for (JsonNode image : jVenue.get("image")) {
										if (image.get("size").toString().replaceAll("\"", "").equals("large")) {
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
					query = musicbrainz.getArtistDbpediaViaMbid(a.getMbid());
				} else {
					query = musicbrainz.getArtistDbpedia(artist);
				}

				// extract info from SPARQL response
				RDFNode sameAs = sparql.sendMusicbrainzQuery(MusicbrainzSparqlFactory.ENDPOINT, query);
				if(sameAs != null){
					query = dbpedia.getArtistInfoViaSameAs(sameAs.toString());
//					query = dbpedia.getArtistInfo(a.getName());
				} else {
					query = dbpedia.getArtistInfo(a.getName());
				}
				RDFNode[] nodes = sparql.sendDbpediaQuery(DBPediaSparqlFactory.ENDPOINT, query);
				if(sameAs != null && nodes == null){
					query = dbpedia.getArtistInfo(a.getName());
					nodes = sparql.sendDbpediaQuery(DBPediaSparqlFactory.ENDPOINT, query);
				}
				
				if(nodes != null){
					if(nodes[0] != null){
						a.setHomepage(nodes[0].toString());
					}
					if(nodes[1] != null){
						String description = nodes[1].toString().replaceAll("@en", "");;
						a.setDescription(description);
					}
					if(nodes[2] != null){
						a.setCountry(nodes[2].toString());
					}
					if(nodes[3] != null){
						a.setWiki(nodes[3].toString());
					}
				}
				
				return Json.toJson(a);
			}
		}

		return null;
	}

	/**
	 * Allows to search for a venue via string (not ID). Returns a JsonNode list
	 * containing all search matches. This is needed if a venue's events shall
	 * be retrieved. As no events can be searched via string, it is necessary to
	 * search the venue and extract its ID. With this ID all events can be
	 * displayed.
	 * 
	 * @param name
	 *            - The venue's name.
	 * @param country
	 *            - The country where to search at (NOTE: if this is null, the
	 *            country parameter will not be set as this is optional).
	 * @return venueList - An ArrayList with all Venue objects stored as
	 *         JsonNodes.
	 * @throws IOException
	 */
	public static ArrayList<JsonNode> searchVenue(String name, String country) throws IOException {
		ArrayList<JsonNode> venueList = new ArrayList<JsonNode>();

		// create parameter for Last.fm query to retrieve venues
		String params = LastfmUri.getInstance().getVenueSearch(name, country);
		JsonNode jsonVenues = request.sendRequest("GET", LastfmUri.ENDPOINT, params);
		jsonVenues = jsonVenues.get("results");

		// iterate through all matches and extract venue information
		if (jsonVenues != null && jsonVenues.get("venuematches") != null) {
			jsonVenues = jsonVenues.get("venuematches");
			for (JsonNode venue : jsonVenues.get("venue")) {
				Venue v = new Venue(venue.get("id").toString().replaceAll("\"", ""), venue.get("name").toString().replaceAll("\"", ""));
				v.setHomepage(venue.get("website").toString().replaceAll("\"", ""));
				v.setPhone(venue.get("phonenumber").toString().replaceAll("\"", ""));

				if (venue.get("location") != null) {
					if (venue.get("location").get("geo:point") != null) {
						v.setLatitude(venue.get("location").get("geo:point").get("geo:lat").toString().replaceAll("\"", ""));
						v.setLongitude(venue.get("location").get("geo:point").get("geo:long").toString().replaceAll("\"", ""));
					}
					v.setCity(venue.get("location").get("city").toString().replaceAll("\"", ""));
					v.setCountry(venue.get("location").get("country").toString().replaceAll("\"", ""));
					v.setStreet(venue.get("location").get("street").toString().replaceAll("\"", ""));
					v.setPostalCode(venue.get("location").get("postalcode").toString().replaceAll("\"", ""));

				}

				if (venue.get("image") != null) {
					for (JsonNode image : venue.get("image")) {
						if (image.get("size").toString().replaceAll("\"", "").equals("large")) {
							v.setImg(image.get("#text").toString().replaceAll("\"", ""));
						}
					}
				}

				// convert Venue to JsonNode and store it in the list.
				venueList.add(Json.toJson(v));
			}
		}

		return venueList;
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
	public static JsonNode getVenueEvents(String id, boolean festivalsOnly) throws IOException {

		// create parameter for Last.fm query to retrieve venue info
		// (especially events)
		String params = LastfmUri.getInstance().getVenueEvents(id, festivalsOnly);
		JsonNode jsonVenueEvents = request.sendRequest("GET", LastfmUri.ENDPOINT, params);
		jsonVenueEvents = jsonVenueEvents.get("events");
		
		// iterate through each event, create object, set variables and add
		// it to venue object
		Venue venue = null;
		boolean isFirst = true;
		if (jsonVenueEvents != null && jsonVenueEvents.get("event") != null) {
			for (JsonNode event : jsonVenueEvents.get("event")) {
				if(event.get("id") != null && event.get("title") != null){
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

					if (event.get("tags") != null) {
						for (JsonNode tag : event.get("tags").get("tag")) {
							e.addTag(tag.toString().replaceAll("\"", ""));
						}
					}

					
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

					if (venue != null) {
						venue.addEvent(e);
					}
				}


			}
		}

		if (venue != null) {
			return Json.toJson(venue);
		} else {
			return null;
		}

	}

	public static void main(String[] args) throws IOException {

//		System.out.println(getLocalEvents(null, null, 20, false));
//		System.out.println(getLocalEvents("49.29180", "8.264116", 20, false));
//		System.out.println(getLocalEvents("49.4058478", "8.7150993861622", 5, false));
//		System.out.println(getVenueEvents("8908030", false));
//		System.out.println(getVenueEvents("8908030", true));
		System.out.println(getArtistEvents("Enter Shikari", false).toString());
//		System.out.println(getArtistEvents("Volbeat", false).toString());
//		System.out.println(getArtistEvents("Söhne Mannheims", false).toString());
//		System.out.println(getArtistEvents("Max Herre", false).toString()); // NullPointerException for whatever reason
//		System.out.println(getArtistEvents("Alesana", false).toString()); // NullPointerException for whatever reason
//		System.out.println(searchVenue("SAP Arena", "Germany").toString());

		
		
	}

}
