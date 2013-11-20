package controllers;

import java.util.Map;

import models.Request;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;
import views.html.map;

import com.fasterxml.jackson.databind.JsonNode;

public class Application extends Controller {

	static Form<Request> requestForm = Form.form(Request.class);

	public static Result index() {
		return ok(index.render("Semantic Event Map"));
	}

	// default map rendering
	public static Result displayMap() {
		return ok(map.render("Map", requestForm));
	}

	// handle input on map page
	public static Result processQuery() {
		Request req;
		Form<Request> filledForm = requestForm.bindFromRequest();
		if (filledForm.hasErrors()) {
			if (filledForm.hasErrors()) {
				return badRequest(views.html.map.render("Map", filledForm));
			}
		} else {
			req = filledForm.get();
		}
		return TODO;
	}
}
