package models;

import java.util.HashMap;
import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class SparqlQueryManager {

	private static SparqlQueryManager instance = null;
	
	
	
	private SparqlQueryManager(){

	}
	
	public static SparqlQueryManager getInstance(){
		if(instance == null){
			instance = new SparqlQueryManager();
		}
		return instance;
	}
	
	public HashMap<RDFNode, RDFNode> sendQuery(String endpoint, String queryString){

		Query query = QueryFactory.create(queryString);
		ARQ.getContext().setTrue(ARQ.useSAX);
		QueryExecution execution = QueryExecutionFactory.sparqlService(endpoint, query);
		HashMap<RDFNode, RDFNode> rdfNodes = new HashMap<RDFNode, RDFNode>();
		
		try{
			ResultSet results = execution.execSelect();
			while(results.hasNext()){
				QuerySolution solution = results.nextSolution();
				rdfNodes.put(solution.get("?p"), solution.get("?o"));
				System.out.println(solution.get("?s") + "   " + solution.get("?p") + "   " + solution.get("?o"));
			}
		}
		finally{
			execution.close();
		}
		
		return rdfNodes;
	}
	
} // End of Class
