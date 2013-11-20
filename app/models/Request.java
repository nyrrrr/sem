package models;

import play.data.validation.Constraints.Required;

public class Request {
	@Required
	public String query;
	public String type;
}
