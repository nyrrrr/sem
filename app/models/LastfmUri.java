package models;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * This class handles the creation of parameters for RESTful requests towards the Last.fm API.
 * @author Ricardo Lüeer
 *
 */
public class LastfmUri {

	public static final String ENDPOINT = "http://ws.audioscrobbler.com/";
	public final String API_KEY = "d4f2b544b1f11f5bd7fbc3ca3c33db26";
	public final String API_SECRET = "5b536824fec9b868fde4d42f5564fc7e";
	
	public final String ARTIST = "artist";
	public final String EVENT = "event";
	public final String LOCATION = "location";
	public final String VENUE = "venue";
	public final String METHOD_ARTIST_GETEVENTS = "artist.getEvents";
	public final String METHOD_EVENT_GETINFO = "event.getInfo";
	public final String METHOD_GEO_GETEVENTS = "geo.getEvents";
	public final String METHOD_VENUE_SEARCH = "venue.search";
	public final String METHOD_VENUE_GETEVENTS = "venue.getEvents";
	
	private boolean isJSON;
	
	private static LastfmUri instance = null;
	
	/**
	 * Private Constructor, necessary for the singleton pattern.
	 */
	private LastfmUri(){
		this.isJSON = true;
	}
	
	/**
	 * Returns the only existing LastfmUri instance. If it does not exist, it will be created and returned afterwards.
	 * @return instance
	 */
	public static LastfmUri getInstance(){
		if(instance == null){
			instance = new LastfmUri();
		}
		return instance;
	}
	
	/**
	 * Returns the whole parameter string that is necessary to request all events of a particular artist via the Last.fm API.
	 * @param artist - Name of an interpret or a band, e.g. 'Volbeat'.
	 * @param festivalsOnly - <code>true</code> if only festivals shall be shown, <code>false</code> if all events shall be retrieved.
	 * @return parameter string for requesting the Last.fm API
	 * @throws UnsupportedEncodingException
	 */
	public String getArtistEvents(String artist, boolean festivalsOnly) throws UnsupportedEncodingException{
		return lastfmURI(ARTIST, METHOD_ARTIST_GETEVENTS, artist, festivalsOnly);
	}
	
	/**
	 * Returns the whole parameter string that is necessary to request event information for an event ID.
	 * @param id - Event ID for a particular event, e.g. '123456'
	 * @return parameter string for requesting the Last.fm API
	 * @throws UnsupportedEncodingException
	 */
	public String getEventInfo(int id) throws UnsupportedEncodingException{
		return lastfmURI(EVENT, METHOD_EVENT_GETINFO, Integer.toString(id), false);
	}
	
	/**
	 * Returns the whole parameter string that is necessary to search a particular venue within Last.fm.
	 * @param venue - The venue's name, e.g. 'SAP Arena'
	 * @param country - The venue's country, e.g. 'Germany'
	 * @return parameter string for requesting the Last.fm API
	 * @throws UnsupportedEncodingException
	 */
	public String getVenueSearch(String venue, String country) throws UnsupportedEncodingException{
		venue = URLEncoder.encode(venue, "UTF-8");
		country = URLEncoder.encode(country, "UTF-8");
		
		StringBuilder uri = new StringBuilder(100);
		uri.append(lastfmURI(VENUE, METHOD_VENUE_SEARCH, venue, false));
		if(country != null){
			uri.append("&country=");
			uri.append(country);
		}
		
		return uri.toString();
	}
	
	/**
	 * Returns the whole parameter string that is necessary to request all events for a particular venue.
	 * @param id - Venue ID for a particular venue, e.g. '654321'.
	 * @param festivalsOnly - <code>true</code> if only festivals shall be retrieved, <code>false</code> if all events shall be retrieved.
	 * @return parameter string for requesting the Last.fm API
	 * @throws UnsupportedEncodingException
	 */
	public String getVenueEvents(int id, boolean festivalsOnly) throws UnsupportedEncodingException{
		return lastfmURI(VENUE, METHOD_VENUE_GETEVENTS, Integer.toString(id), festivalsOnly);
	}
	
	/**
	 * Returns the whole parameter string that is necessary for requesting all events in the area of a particular location.
	 * If no location is handed over, the current position will be taken.
	 * @param location - The desired location, e.g. 'Mannheim'.
	 * @param distance - The radius to search within in kilometers, e.g. '10'.
	 * @param festivalsOnly - <code>true</code> if only festivals shall be retrieved, <code>false</code> if all event types shall be retrieved.
	 * @return parameter string for requesting the Last.fm API
	 * @throws UnsupportedEncodingException
	 */
	public String getGeoEvents(String location, int distance, boolean festivalsOnly) throws UnsupportedEncodingException{
		StringBuilder uri = new StringBuilder(100);
		uri.append("2.0/?api_key=");
		uri.append(API_KEY);
		
		if(location != null){
			location = URLEncoder.encode(location, "UTF-8");
			uri.append("&location=");
			uri.append(location);
		}
		if(distance > 0){
			uri.append("&distance=");
			uri.append(distance);
		}
		if(festivalsOnly){
			uri.append("&festivalsonly=1");
		}
		
		return uri.toString();
	}
	
	/**
	 * Returns a parameter string that is necessary to send RESTful requests via the Last.fm API.
	 * @param methodParent - e.g. 'location' or 'artist'
	 * @param method - The used method, e.g. 'event.getInfo' or 'artist.getEvents'.
	 * @param search - The term to search for, can be either a normal String or an ID (depending on method), e.g. 'Volbeat' or '123456'
	 * @param festivalsOnly - <code>true</code> if only festivals shall be retrieved, <code>false</code> if all event types shall be retrieved.
	 * @return parameter string for requesting the Last.fm API
	 * @throws UnsupportedEncodingException
	 */
	public String lastfmURI(String methodParent, String method, String search, boolean festivalsOnly) throws UnsupportedEncodingException{
		// encode search term
		search = URLEncoder.encode(search, "UTF-8");
		
		//build uri
		StringBuilder uri = new StringBuilder(100);
		uri.append("2.0/?method=");
		uri.append(method);
		uri.append("&");
		uri.append(methodParent);
		uri.append("=");
		uri.append(search);
		uri.append("&api_key=");
		uri.append(API_KEY);
		if(festivalsOnly){
			uri.append("&festivalsonly=1");
		}
		if(isJSON){
			uri.append("&format=json");
		}
	
		return uri.toString();	
	}

	/**
	 * Returns whether the request response will be in XML format or in JSON format.
	 * @return <code>true</code> if JSON format, <code>false</code> if XML.
	 */
	public boolean isJSON() {
		return isJSON;
	}

	/**
	 * Sets whether the request response shall be in XML format or in JSON format.
	 * @param isJSON - <code>true</code> if JSON format, <code>false</code> if XML format.
	 */
	public void setJSON(boolean isJSON) {
		this.isJSON = isJSON;
	}
	
} // End of Class