package models;

public class DBPediaSparqlFactory {

	public static final String ENDPOINT = "http://dbpedia.org/sparql/";

	private static DBPediaSparqlFactory instance = null;
	private final String PREFIXES;

	private DBPediaSparqlFactory() {
		StringBuilder builder = new StringBuilder(100);
		builder.append("PREFIX owl: <http://www.w3.org/2002/07/owl#>");
		builder.append("\r\n");
		builder.append("PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>");
		builder.append("\r\n");
		builder.append("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>");
		builder.append("\r\n");
		builder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>");
		builder.append("\r\n");
		builder.append("PREFIX foaf: <http://xmlns.com/foaf/0.1/>");
		builder.append("\r\n");
		builder.append("PREFIX dc: <http://purl.org/dc/elements/1.1/>");
		builder.append("\r\n");
		builder.append("PREFIX : <http://dbpedia.org/resource/>");
		builder.append("\r\n");
		builder.append("PREFIX dbpedia2: <http://dbpedia.org/property/>");
		builder.append("\r\n");
		builder.append("PREFIX db: <http://dbtune.org/musicbrainz/resource/>");
		builder.append("\r\n");
		builder.append("PREFIX skos: <http://www.w3.org/2004/02/skos/core#>");
		builder.append("\r\n");
		builder.append("PREFIX dbo: <http://dbpedia.org/ontology/>");
		builder.append("\r\n");
		builder.append("PREFIX schema: <http://schema.org/>");
		builder.append("\r\n");

		PREFIXES = builder.toString();

	}

	public static DBPediaSparqlFactory getInstance() {
		if (instance == null) {
			instance = new DBPediaSparqlFactory();
		}
		return instance;
	}

	public String getArtistInfo(String artist) {
		return PREFIXES + "SELECT * WHERE {" + " ?artist a schema:MusicGroup." + " ?artist dbpedia2:name \"" + artist
				+ "\"@en." + " OPTIONAL {" + "  ?artist foaf:homepage ?homepage." + " }" + " OPTIONAL {"
				+ "  ?artist dbo:abstract ?abstract." + "  FILTER(lang(?abstract) = \"en\")." + " }" + " OPTIONAL {"
				+ "  ?artist dbo:hometown ?home." + "  ?home a schema:Country." + " }" + " OPTIONAL {"
				+ "  ?artist foaf:isPrimaryTopicOf ?wiki." + " }" + "}" + "LIMIT 1";
	}

	public String getArtistInfoViaSameAs(String sameAs) {
		sameAs = "<" + sameAs + ">";
		return PREFIXES + "SELECT * WHERE {" + sameAs + " a schema:MusicGroup." + " OPTIONAL {" + sameAs
				+ " foaf:homepage ?homepage." + " }" + " OPTIONAL {" + sameAs + " dbo:abstract ?abstract."
				+ "  FILTER(lang(?abstract) = \"en\")." + " }" + " OPTIONAL {" + sameAs + " dbo:hometown ?home."
				+ "  ?home a schema:Country." + " }" + " OPTIONAL {" + sameAs + " foaf:isPrimaryTopicOf ?wiki." + " }"
				+ "}" + "LIMIT 1";
	}

	public String getVenueInfo(String venue) {
		return PREFIXES + "SELECT * WHERE {" + " ?venue a" + "}";
	}

} // End of Class
