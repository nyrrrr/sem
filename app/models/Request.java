package models;

import play.data.validation.Constraints.Required;

public class Request {
	public String query;
	public String type;
	public String lat, lon;
}
