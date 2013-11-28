package models;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Venue {

	private String id;
	private String name;
	private String img;
	private String city;
	private String street;
	private String postalCode;
	private String country;
	private String latitude;
	private String longitude;
	private String homepage;
	private String phone;	
	private HashMap<String, Event> events;
	
	public Venue(String id, String name){
		this.id = id;
		this.name = name;
		events = new HashMap<String, Event>();
	}
	
	public Venue(String id, String name, HashMap<String, Event> events){
		this.id = id;
		this.name = name;
		this.events = events;
	}
	
	public Venue(String id, String name, String img, String city, String street, 
			String postalCode, String country, String longitude, String latitude,
			String homepage, String phone){
		this.id = id;
		this.name = name;
		this.img = img;
		this.city = city;
		this.street = street;
		this.postalCode = postalCode;
		this.latitude = latitude;
		this.longitude = longitude;
		this.homepage = homepage;
		this.phone = phone;
		events = new HashMap<String, Event>();
	}
	
	public Venue(String id, String name, String img, String city, String street, 
			String postalCode, String country, String longitude, String latitude,
			String homepage, String phone, HashMap<String, Event> events){
		this.id = id;
		this.name = name;
		this.img = img;
		this.city = city;
		this.street = street;
		this.postalCode = postalCode;
		this.latitude = latitude;
		this.longitude = longitude;
		this.homepage = homepage;
		this.phone = phone;
		this.events = events;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getImg() {
		return img;
	}
	
	public void setImg(String img) {
		this.img = img;
	}
	
	public String getCity() {
		return city;
	}
	
	public void setCity(String city) {
		this.city = city;
	}
	
	public String getStreet() {
		return street;
	}
	
	public void setStreet(String street) {
		this.street = street;
	}
	
	public String getPostalCode() {
		return postalCode;
	}
	
	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}
	
	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getLatitude() {
		return latitude;
	}
	
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	
	public String getLongitude() {
		return longitude;
	}
	
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
	
	public String getHomepage() {
		return homepage;
	}
	
	public void setHomepage(String homepage) {
		this.homepage = homepage;
	}
	
	public String getPhone() {
		return phone;
	}
	
	public void setPhone(String phone) {
		this.phone = phone;
	}

	public HashMap<String, Event> getEvents() {
		return events;
	}

	public void setEvents(HashMap<String, Event> events) {
		this.events = events;
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
	
	public String toJSON(){
		StringBuilder builder = new StringBuilder("{")
			.append("\"id\": ")			.append(this.id != null? "\"" + this.id + "\"" : null)					.append(", ")
			.append("\"name\": ")		.append(this.name != null? "\"" + this.name + "\"" : null)				.append(", ")
			.append("\"img\": ")		.append(this.img != null? "\"" + this.img + "\"" : null)				.append(", ")
			.append("\"city\": ")		.append(this.city != null? "\"" + this.city + "\"" : null)				.append(", ")
			.append("\"street\": ")		.append(this.street != null? "\"" + this.street + "\"" : null)			.append(", ")
			.append("\"postalCode\": ")	.append(this.postalCode != null? "\"" + this.postalCode + "\"" : null)	.append(", ")
			.append("\"country\": ")	.append(this.country != null? "\"" + this.country + "\"" : null)		.append(", ")
			.append("\"latitude\": ")	.append(this.latitude != null? "\"" + this.latitude + "\"" : null)		.append(", ")
			.append("\"longitude\": ")	.append(this.longitude != null? "\"" + this.longitude + "\"" : null)	.append(", ")
			.append("\"homepage\": ")	.append(this.homepage != null? "\"" + this.homepage + "\"" : null)		.append(", ")
			.append("\"phone\": ")		.append(this.phone != null? "\"" + this.phone + "\"" : null)			.append(", ")
			.append("\"events\": [");			
		for(String key : this.events.keySet()){
			builder.append(this.events.get(key).toJSON())	.append(", ");
		}
		builder.append("]}");
		
		
		String json = builder.toString();
		json = json.replaceAll(", ]", "]");
		json = json.replaceAll(", }", "}");
		return json;
	}
	
	public void toJSON(File file){
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("id", this.id);
		map.put("name", this.name);
		map.put("img", this.img);
		map.put("city", this.city);
		map.put("street", this.street);
		map.put("postalCode", this.postalCode);
		map.put("country", this.country);
		map.put("latitude", this.latitude);
		map.put("longitude", this.longitude);
		map.put("homepage", this.homepage);
		map.put("phone", this.phone);
		map.put("events", this.events);
	
		System.out.println(map.toString());
		
//		ObjectMapper mapper = new ObjectMapper();
//		try{
//			mapper.writeValue(file, map);
//		} catch(JsonGenerationException e){
//			e.printStackTrace();
//		} catch(JsonMappingException e){
//			e.printStackTrace();
//		} catch(IOException e){
//			e.printStackTrace();
//		}
	}
	
	public static void main(String[] args){
		Artist artist1 = new Artist("Volbeat");
		Artist artist2 = new Artist("Enter Shikari");
		ArrayList<Artist> artists = new ArrayList<Artist>(Arrays.asList(artist1, artist2));
		Event e1 = new Event("654321", "Event Arena", artists);
		Venue venue = new Venue("123456", "TestVenue", "http://www.venuepics.de/venue.png", "Gotham City", "Bat Cave 1", "00001", "Utopia", "47.00000", "8.00000", "www.test.com", "123-456-789");
		venue.addEvent(e1);
		System.out.println(venue.toJSON());
		
	}
	
} // End of Class
