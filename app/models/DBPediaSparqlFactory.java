package models;

public class DBPediaSparqlFactory {

	public static final String ENDPOINT = "";
	
	private static DBPediaSparqlFactory instance = null;
	
	private DBPediaSparqlFactory(){
		
	}
	
	public static DBPediaSparqlFactory getInstance(){
		if(instance == null){
			instance = new DBPediaSparqlFactory();
		}
		return instance;
	}
	
} // End of Class
