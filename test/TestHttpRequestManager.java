import java.io.IOException;

import models.MusicbrainzSparqlFactory;
import models.SparqlQueryManager;

import org.junit.Test;

import com.hp.hpl.jena.rdf.model.RDFNode;


public class TestHttpRequestManager {

	@Test
	public void test() throws IOException {
//		HttpRequestManager request = HttpRequestManager.getInstance();
//		LastfmUri lastfm = LastfmUri.getInstance();
		MusicbrainzSparqlFactory musicbrainz = MusicbrainzSparqlFactory.getInstance();
		SparqlQueryManager sparqlManager = SparqlQueryManager.getInstance();
		
//		String params = LastfmUri.getInstance().getArtistEvents("Volbeat", false);
//		String params = LastfmUri.getInstance().getEventInfo(3669169);
//		request.sendRequest("GET", LastfmUri.getInstance().ENDPOINT, params);
		
//		HttpRequestManager.getInstance().sendRequest("GET", "artist", "Enter Shikari", "artist.getEvents");
//		HttpRequestManager.getInstance().sendRequest("GET", "artist", "Enter Shikari", "artist.getInfo");
//		HttpRequestManager.getInstance().sendRequest("GET", "artist", "Enter Shikari", "artist.search");
//		HttpRequestManager.getInstance().sendRequest("GET", "venue", "8788926", "venue.getEvents");
//		HttpRequestManager.getInstance().sendRequest("GET", "location", "Ludwigshafen am Rhein", "geo.getEvents");
		
		String query = musicbrainz.getArtistInfo("Volbeat");
//		String query = musicbrainz.getResource("http://dbtune.org/musicbrainz/resource/artist/4753fcb7-9270-493a-974d-8daca4e49125");
//		RDFNode node = sparqlManager.sendQuery(musicbrainz.ENDPOINT, query).get(0);
		sparqlManager.sendQuery(musicbrainz.ENDPOINT, query);
		
	}

}
