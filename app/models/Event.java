package models;

import java.util.ArrayList;

public class Event {

	private String id;
	private String title;
	private String date;
	private String tickets;
	private Venue venue;
	private ArrayList<Artist> artists;
	
	/**
	 * Constructor - Creates an Event object and sets the variables.
	 * @param id - A unique event id.
	 * @param name - An event name.
	 */
	public Event(String id, String name){
		this.id = id;
		this.title = name;
		artists = new ArrayList<Artist>();
	}
	
	/**
	 * Constructor - Creates an Event object and sets the variables.
	 * @param id - A unique event id.
	 * @param name - An event name.
	 * @param artists - Artists that participate in this event.
	 */
	public Event(String id, String name, ArrayList<Artist> artists){
		this.id = id;
		this.title = name;
		this.artists = artists;
	}
	
	/**
	 * Constructor - Creates an Event object and sets the variables.
	 * @param id - A unique event id.
	 * @param name - An event name.
	 * @param venue - The venue of the event.
	 * @param artists - Artists that participate in this event.
	 */
	public Event(String id, String name, Venue venue, ArrayList<Artist> artists){
		this.id = id;
		this.title = name;
		this.venue = venue;
		this.artists = artists;		
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getDate() {
		return date;
	}
	
	public void setDate(String date) {
		this.date = date;
	}
	
	public String getTickets() {
		return tickets;
	}
	
	public void setTickets(String tickets) {
		this.tickets = tickets;
	}
	
	public Venue getVenue() {
		return venue;
	}

	public void setVenue(Venue venue) {
		this.venue = venue;
	}

	public ArrayList<Artist> getArtists() {
		return artists;
	}
	
	public void setArtists(ArrayList<Artist> artists) {
		this.artists = artists;
	}
	
	public void addArtist(Artist artist){
		if(!artists.contains(artist)){
			artists.add(artist);
		}
	}
	
	public void deleteArtist(Artist artist){
		if(artists.contains(artist)){
			artists.remove(artist);
		}
	}
	
	public String toJSON(boolean includeVenue){
		StringBuilder builder = new StringBuilder("{")
			.append("\"id\": ")			.append(this.id != null? "\"" + this.id + "\"" : null)				.append(", ")
			.append("\"name\": ")		.append(this.title != null? "\"" + this.title + "\"" : null)			.append(", ")
			.append("\"date\": ")		.append(this.date != null? "\"" + this.date + "\"" : null)			.append(", ")
			.append("\"tickets\": ")	.append(this.tickets != null? "\"" + this.tickets + "\"" : null)	.append(", ")
			.append("\"artists\": [");
		
		for(Artist artist : artists){
			builder.append(artist.toJSON(false))	.append(", ");
		}
		builder.append("]");
		
		if(includeVenue){
			builder.append(", ");
			builder.append("\"venue\": ")		.append(this.venue != null? this.venue.toJSON() : null)				.append(", ");
		}
		
		builder.append("}");
		
		String json = builder.toString();
		json = json.replaceAll(", ]", "]");
		json = json.replaceAll(", }", "}");
		return json;
	}
	
} // End of Class
