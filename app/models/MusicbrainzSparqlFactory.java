package models;

import java.io.UnsupportedEncodingException;

public class MusicbrainzSparqlFactory {

	public final String ENDPOINT = "http://dbtune.org/musicbrainz/sparql";
	
	private static MusicbrainzSparqlFactory instance = null;
	private final String PREFIXES;
	
	private MusicbrainzSparqlFactory(){
		StringBuilder builder = new StringBuilder(100);
		builder.append("PREFIX map: <file:/home/moustaki/work/motools/musicbrainz/d2r-server-0.4/mbz_mapping_raw.n3#>");
		builder.append("\r\n");
		builder.append("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>");
		builder.append("\r\n");
		builder.append("PREFIX owl: <http://www.w3.org/2002/07/owl#>");
		builder.append("\r\n");
		builder.append("PREFIX event: <http://purl.org/NET/c4dm/event.owl#>");
		builder.append("\r\n");
		builder.append("PREFIX rel: <http://purl.org/vocab/relationship/>");
		builder.append("\r\n");
		builder.append("PREFIX lingvoj: <http://www.lingvoj.org/ontology#>");
		builder.append("\r\n");
		builder.append("PREFIX foaf: <http://xmlns.com/foaf/0.1/>");
		builder.append("\r\n");
		builder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>");
		builder.append("\r\n");
		builder.append("PREFIX tags: <http://www.holygoat.co.uk/owl/redwood/0.1/tags/>");
		builder.append("\r\n");
		builder.append("PREFIX db: <http://dbtune.org/musicbrainz/resource/>");
		builder.append("\r\n");
		builder.append("PREFIX geo: <http://www.geonames.org/ontology#>");
		builder.append("\r\n");
		builder.append("PREFIX dc: <http://purl.org/dc/elements/1.1/>");
		builder.append("\r\n");
		builder.append("PREFIX bio: <http://purl.org/vocab/bio/0.1/>");
		builder.append("\r\n");
		builder.append("PREFIX mo: <http://purl.org/ontology/mo/>");
		builder.append("\r\n");
		builder.append("PREFIX vocab: <http://dbtune.org/musicbrainz/resource/vocab/>");
		builder.append("\r\n");
		builder.append("PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>");
		builder.append("\r\n");
		builder.append("PREFIX mbz: <http://purl.org/ontology/mbz#>");
		builder.append("\r\n");
		
		PREFIXES = builder.toString();
		
	}
	
	public static MusicbrainzSparqlFactory getInstance(){
		if(instance == null){
			instance = new MusicbrainzSparqlFactory();
		}
		return instance;
	}
	
	public String getArtistInfo(String artist) throws UnsupportedEncodingException{
		return PREFIXES + 
				"SELECT * WHERE {" + 
			    " ?s foaf:name \"" + artist + "\"." +
				" ?s ?p ?o." +
			    "}";
	}
	
	public String getResource(String resourceUri){
		return PREFIXES + 
				"SELECT * WHERE {" + 
				" ?s a <" + resourceUri + ">" +
				" ?s ?p ?o." + 
				"}";
	}
	
} // End of Class
