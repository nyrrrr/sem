package models;

import java.util.ArrayList;
import java.util.HashMap;

public class Artist {

	private String mbid;
	private String lastfm;
	private String name;
	private String description;
	private String img;
	private String country;
	private String homepage;
	private String wiki;
	private boolean isOnTour;
	private ArrayList<String> genres;
	private ArrayList<String> members;
	private HashMap<String, Event> events;
	
	/**
	 * Constructor - Creates an Artist object and sets the variables.
	 * @param name - The artist's name.
	 */
	public Artist(String name){
		this.name = name;
		this.genres = new ArrayList<String>();
		this.members = new ArrayList<String>();
		this.events = new HashMap<String, Event>();
	}
	
	/**
	 * Constructor - Creates an Artist object and sets the variables.
	 * @param mbid - A unique Musicbrainz identifier for the artist.
	 * @param name - The artist's name.
	 */
	public Artist(String mbid, String name){
		this.mbid = mbid;
		this.name = name;
		this.genres = new ArrayList<String>();
		this.members = new ArrayList<String>();
		this.events = new HashMap<String, Event>();
	}
	
	/**
	 * Constructor - Creates an Artist object and sets the variables.
	 * @param name - The artist's name.
	 * @param events - All events of the artist.
	 */
	public Artist(String name, HashMap<String, Event> events){
		this.name = name;
		this.events = events;
	}
	
	/**
	 * Constructor - Creates an Artist object and sets the variables.
	 * @param mbid - A unique Musicbrainz identifier for the artist.
	 * @param name - The artist's name.
	 * @param events - All events of the artist.
	 */
	public Artist(String mbid, String name, HashMap<String, Event> events){
		this.mbid = mbid;
		this.name = name;
		this.events = events;
	}
	
	public String getMbid() {
		return mbid;
	}
	
	public void setMbid(String mbid) {
		this.mbid = mbid;
	}
	
	public String getLastfm() {
		return lastfm;
	}

	public void setLastfm(String lastfm) {
		this.lastfm = lastfm;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getImg() {
		return img;
	}
	
	public void setImg(String img) {
		this.img = img;
	}
	
	public String getCountry() {
		return country;
	}
	
	public void setCountry(String country) {
		this.country = country;
	}
	
	public ArrayList<String> getGenres() {
		return genres;
	}
	
	public void setGenres(ArrayList<String> genres) {
		this.genres = genres;
	}
	
	public String getHomepage() {
		return homepage;
	}
	
	public void setHomepage(String homepage) {
		this.homepage = homepage;
	}
	
	public ArrayList<String> getMembers() {
		return members;
	}
	
	public void setMembers(ArrayList<String> members) {
		this.members = members;
	}
	
	public String getWiki() {
		return wiki;
	}
	
	public void setWiki(String wiki) {
		this.wiki = wiki;
	}
	
	public boolean isOnTour() {
		return isOnTour;
	}

	public void setOnTour(boolean isOnTour) {
		this.isOnTour = isOnTour;
	}

	public HashMap<String, Event> getEvents() {
		return events;
	}
	
	public void setEvents(HashMap<String, Event> events) {
		this.events = events;
	}
	
	public void addGenre(String genre){
		if(!genres.contains(genre)){
			genres.add(genre);
		}
	}
	
	public void deleteGenre(String genre){
		if(genres.contains(genre)){
			genres.remove(genre);
		}
	}
	
	public void addMember(String member){
		if(!members.contains(member)){
			members.add(member);
		}
	}
	
	public void deleteMember(String member){
		if(members.contains(member)){
			members.remove(member);
		}
	}
	
	public void addEvent(Event event){
		if(!events.containsKey(event.getId())){
			events.put(event.getId(), event);
		}
	}
	
	public void deleteEvent(Event event){
		if(events.containsKey(event.getId())){
			events.remove(event);
		}
	}
	
	public String toJSON(boolean includeEvents){
		StringBuilder builder = new StringBuilder("{")
			.append("\"mbid\": ")			.append(this.mbid != null? "\"" + this.mbid + "\"" : null)				.append(", ")
			.append("\"lastfm\": ")			.append(this.lastfm != null? "\"" + this.lastfm + "\"" : null)			.append(", ")
			.append("\"name\": ")			.append(this.name != null? "\"" + this.name + "\"" : null)				.append(", ")
			.append("\"description\"")		.append(this.description != null? "\"" + this.description + "\"" : null).append(", ")
			.append("\"img\": ")			.append(this.img != null? "\"" + this.img + "\"" : null)				.append(", ")
			.append("\"country\": ")		.append(this.country != null? "\"" + this.country + "\"" : null)		.append(", ")
			.append("\"homepage\": ")		.append(this.homepage != null? "\"" + this.homepage + "\"" : null)		.append(", ")
			.append("\"wiki\": ")			.append(this.wiki != null? "\"" + this.wiki + "\"" : null)				.append(", ")
			.append("\"onTour\": ")			.append(this.isOnTour)													.append(", ")
			.append("\"genres\": [");
		for(String genre : genres){
			builder.append("\"" + genre + "\", ");
		}
			builder.append("], ");
			builder.append("\"members\": [");			
		for(String member : members){
			builder.append("\"" + member + "\", ");	
		}
		builder.append("], ");
		
		if(includeEvents){
			builder.append("\"events\": [");
			for(String key : this.events.keySet()){
				builder.append(events.get(key).toJSON(true) + ", ");
			}
			builder.append("]");
		}
		
		builder.append("}");
		
		String json = builder.toString();
		json = json.replaceAll(", ]", "]");
		json = json.replaceAll(", }", "}");
	
		return json;
		
	}
	
} // End of Class
