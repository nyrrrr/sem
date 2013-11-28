package controllers;

import models.Request;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.map;

import com.fasterxml.jackson.databind.JsonNode;

public class Application extends Controller {

	static Form<Request> requestForm = Form.form(Request.class);
	static Form<Request> filledForm;

	public static Result index() {
		return controllers.Application.displayMap();
	}

	// default map rendering
	public static Result displayMap() {
		if (filledForm != null) {
			Form<Request> tmp = filledForm;
			filledForm = null;
			return badRequest(map.render("Map", tmp));
		}
		return ok(map.render("Map", requestForm));
	}

	// handle input on map page
	public static Result processQuery() {
		Request req;
		filledForm = requestForm.bindFromRequest();
		if (filledForm.hasErrors()) {
			return controllers.Application.displayMap();
		} else {
			req = filledForm.get(); // queryType
		}
		//JsonNode v = Json.toJson(req);
		//String jsonString = v.toString();
		// TODO trigger some kind of request that will return a JSON
		JsonNode result = Json.parse("{\"1\":1,\"2\" : {\"child\" : [1,2,3,4]} }");
		return ok(result);
	}
}
