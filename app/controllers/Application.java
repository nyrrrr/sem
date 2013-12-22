package controllers;
import java.io.IOException;
import models.Request;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.map;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
public class Application extends Controller {
	// framerwork vars
	static Form<Request> requestForm = Form.form(Request.class);
	static Form<Request> filledForm;
	// deprecated but still in routes..
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
		String queryType = "";
		Request req = null;
		JsonNode result = null;
		filledForm = requestForm.bindFromRequest();
		if (filledForm.hasErrors()) {
			return controllers.Application.displayMap();
		}
		req = filledForm.get();
		queryType = req.type.toString();
		// select query
		if (queryType.equals("artist")) {
			try {
				result = RequestController.getArtistEvents(req.query, false);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (queryType.equals("location")) {
			try {
				ArrayNode a = JsonNodeFactory.instance.arrayNode();
				// here we need to flip lat and lon... don't ask me why...
				a.addAll(RequestController.getLocalEvents(req.lat, req.lon, Integer.parseInt(req.radius), false));
				result = a;
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (queryType.equals("venueSearch")) {
			ArrayNode a = JsonNodeFactory.instance.arrayNode();
			try {
				result = a.addAll(RequestController.searchVenue(req.query, null));
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (queryType.equals("venue")) {
			return TODO; // TODO
		} else {
			result = Json.parse("{error: 1337, message : 'It looks like no category was selected.'}");
		}
		return ok(result);
	}
}
