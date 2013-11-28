package models;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Event {

	private String id;
	private String title;
	private String date;
	private String tickets;
	private Venue venue;
	private ArrayList<Artist> artists;
	
	public Event(String id, String name){
		this.id = id;
		this.title = name;
		artists = new ArrayList<Artist>();
	}
	
	public Event(String id, String name, String date, String tickets){
		this.id = id;
		this.title = name;
		this.date = date;
		this.tickets = tickets;
		artists = new ArrayList<Artist>();
	}
	
	public Event(String id, String name, ArrayList<Artist> artists){
		this.id = id;
		this.title = name;
		this.artists = artists;
	}
	
	public Event(String id, String name, String date, String tickets, Venue venue, ArrayList<Artist> artists){
		this.id = id;
		this.title = name;
		this.date = date;
		this.tickets = tickets;
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
	
	public String toJSON(){
		StringBuilder builder = new StringBuilder("{")
			.append("\"id\": ")			.append(this.id != null? "\"" + this.id + "\"" : null)				.append(", ")
			.append("\"name\": ")		.append(this.title != null? "\"" + this.title + "\"" : null)			.append(", ")
			.append("\"date\": ")		.append(this.date != null? "\"" + this.date + "\"" : null)			.append(", ")
			.append("\"tickets\": ")	.append(this.tickets != null? "\"" + this.tickets + "\"" : null)	.append(", ")
			.append("\"venue\": ")		.append(this.venue != null? this.venue.toJSON() : null)				.append(", ")
			.append("\"artists\": [");
		
		for(Artist artist : artists){
			builder.append(artist.toJSON(false))	.append(", ");
		}
		builder.append("]");
		
		builder.append("}");
		
		String json = builder.toString();
		json = json.replaceAll(", ]", "]");
		json = json.replaceAll(", }", "}");
		return json;
	}
	
	public void toJSON(File file){
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("id", this.id);
		map.put("title", this.title);
		map.put("date", date);
		map.put("tickets", this.tickets);
		map.put("venue", this.venue);
		map.put("artists", this.artists);
	
		ObjectMapper mapper = new ObjectMapper();
		try{
			mapper.writeValue(file, map);
		} catch(JsonGenerationException e){
			e.printStackTrace();
		} catch(JsonMappingException e){
			e.printStackTrace();
		} catch(IOException e){
			e.printStackTrace();
		}
	}
	
} // End of Class
