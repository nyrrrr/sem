import static org.junit.Assert.*;

import java.io.IOException;

import models.HttpRequestManager;
import models.LastfmUri;

import org.junit.Test;


public class TestHttpRequestManager {

	@Test
	public void test() throws IOException {
		HttpRequestManager request = HttpRequestManager.getInstance();
//		String params = LastfmUri.getInstance().getArtistEvents("Volbeat", false);
		String params = LastfmUri.getInstance().getEventInfo(3669169);
		request.sendRequest("GET", LastfmUri.getInstance().ENDPOINT, params);
		
//		HttpRequestManager.getInstance().sendRequest("GET", "artist", "Enter Shikari", "artist.getEvents");
//		HttpRequestManager.getInstance().sendRequest("GET", "artist", "Enter Shikari", "artist.getInfo");
//		HttpRequestManager.getInstance().sendRequest("GET", "artist", "Enter Shikari", "artist.search");
//		HttpRequestManager.getInstance().sendRequest("GET", "venue", "8788926", "venue.getEvents");
//		HttpRequestManager.getInstance().sendRequest("GET", "location", "Ludwigshafen am Rhein", "geo.getEvents");
	}

}
