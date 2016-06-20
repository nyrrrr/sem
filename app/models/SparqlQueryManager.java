package models;

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

	private SparqlQueryManager() {

	}

	public static SparqlQueryManager getInstance() {
		if (instance == null) {
			instance = new SparqlQueryManager();
		}
		return instance;
	}

	public RDFNode sendMusicbrainzQuery(String endpoint, String queryString) {

		Query query = QueryFactory.create(queryString);
		ARQ.getContext().setTrue(ARQ.useSAX);
		QueryExecution execution = QueryExecutionFactory.sparqlService(endpoint, query);
		RDFNode sameAs = null;

		try {
			ResultSet results = execution.execSelect();
			while (results.hasNext()) {
				QuerySolution solution = results.nextSolution();
				sameAs = solution.get("?same");
				System.out.println(sameAs);
			}
		} finally {
			execution.close();
		}

		return sameAs;
	}

	public RDFNode[] sendDbpediaQuery(String endpoint, String queryString) {
		Query query = QueryFactory.create(queryString);
		ARQ.getContext().setTrue(ARQ.useSAX);
		QueryExecution execution = QueryExecutionFactory.sparqlService(endpoint, query);

		RDFNode homepage = null;
		RDFNode description = null;
		RDFNode hometown = null;
		RDFNode wiki = null;

		try {
			ResultSet results = execution.execSelect();
			while (results.hasNext()) {
				QuerySolution solution = results.nextSolution();
				homepage = solution.get("?homepage");
				description = solution.get("?abstract");
				hometown = solution.get("?home");
				wiki = solution.get("?wiki");
			}
		} finally {
			execution.close();
		}

		RDFNode[] nodes = { homepage, description, hometown, wiki };
		return nodes;
	}

} // End of Class
