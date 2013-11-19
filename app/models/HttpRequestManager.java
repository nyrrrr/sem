package models;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.HttpURLConnection;

public class HttpRequestManager {


	public final String USER_AGENT = "Semantic Event Map / 1.0 (Development Phase)";
	
	private static HttpRequestManager instance = null;
	
	private HttpRequestManager(){
		
	}
	
	public static HttpRequestManager getInstance(){
		if(instance == null){
			instance = new HttpRequestManager();
		}
		return instance;
	}
	
	public void sendRequest(String requestMethod, String endpoint, String params) throws IOException{
		
		URL url = new URL(endpoint + params);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		
		connection.setRequestMethod(requestMethod);
		connection.setRequestProperty("User-Agent", USER_AGENT);
		
		connection.setDoOutput(true);
		DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
		dos.writeBytes(params);
		dos.flush();
		dos.close();
		
		int responseCode = connection.getResponseCode();
		System.out.println("Sending '" + requestMethod + "' request to URL: " + url.toString());
		System.out.println("URL: " + endpoint);
		System.out.println("Params: " + params);
		System.out.println("Response Code: " + responseCode + " " + connection.getResponseMessage() + "\r\n\r\n");
		
		BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		StringBuilder stringResponse = new StringBuilder();
		String line = "";
		while((line = rd.readLine()) != null){
			System.out.println(line);
			stringResponse.append(line);
		}
		rd.close();
		
	}
	
} // End of Class
