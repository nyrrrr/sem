package models;

public class DataManager {

	private static DataManager instance = null;
	
	private DataManager(){
		
	}
	
	public static DataManager getInstance(){
		if(instance == null){
			instance = new DataManager();
		}
		return instance;
	}

} // End of Class
