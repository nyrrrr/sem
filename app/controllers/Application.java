package controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.impl.conn.tsccm.RefQueueHandler;

import models.Artist;
import models.Event;
import models.HttpRequestManager;
import models.LastfmUri;
import models.MusicbrainzSparqlFactory;
import models.Request;
import models.SparqlQueryManager;
import models.Venue;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;

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
			// TODO some detailed error handling - either here or in html
			Form<Request> tmp = filledForm;
			filledForm = null;
			return badRequest(map.render("Map", tmp));
		}
		return ok(map.render("Map", requestForm));
	}

	// handle input on map page
	public static Result processQuery() {
		String queryString = "";
		Request req = null;
		JsonNode result = null;
		filledForm = requestForm.bindFromRequest();

		if (filledForm.hasErrors()) {
			return controllers.Application.displayMap();
		}

		req = filledForm.get();
		queryString = req.type.toString();

		// select query
		if (queryString.equals("artist")) {
			try {
				result = RequestController.getArtistEvents(req.query, false);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// TODO error handling depending on result object
		} else if (queryString.equals("location")) {
			try {
				ArrayNode a = JsonNodeFactory.instance.arrayNode();
				// TODO remove 25 (def value) and implement selector on UI
				a.addAll(RequestController.getLocalEvents(req.lon, req.lat, Integer.parseInt(req.radius), false));
				result = a;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (queryString.equals("venue")) {
		} else {
			// TODO error handling
		}

		// TODO trigger some kind of request that will return a JSON
		// result = Json.parse("{\"1\":1,\"2\" : {\"child\" : [1,2,3,4]} }");
		return ok(result);
	}

}
