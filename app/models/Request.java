package models;

import play.data.validation.Constraints.Required;

public class Request {
	@Required
	public String query;
	@Required
	public String type;
	@Required
	public String lat, lon;
	@Required
	public String radius;
}
