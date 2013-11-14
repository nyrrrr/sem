package controllers;

import play.*;
import play.mvc.*;
import views.html.*;

public class Application extends Controller {
	
	
    public static Result index() {
        return ok(index.render("Semantic Event Map"));
    }
    
    public static Result displayMap() {
    	return ok(map.render("Map"));
    }
    
}
