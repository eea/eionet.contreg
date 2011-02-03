package eionet.cr.web.sparqlClient.helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

/**
 * 
 * @author altnyris
 *
 */
public class QueryResult{
	
	/** */
	private List<String> variables;
	private ArrayList<HashMap<String,ResultValue>> rows;
	private ArrayList<Map<String,Object>> cols;

	/**
	 * 
	 * @param rs
	 */
	public QueryResult(ResultSet rs){
		
		if (rs!=null && rs.hasNext()){
			
			this.variables = rs.getResultVars();
			addCols();
			while (rs.hasNext()){
				add(rs.next());
			}
		}
	}

	/**
	 * 
	 * @param querySolution
	 */
	private void add(QuerySolution querySolution){
		
		if (querySolution==null || variables==null || variables.isEmpty()){
			return;
		}
		
		HashMap<String,ResultValue> map = new HashMap<String, ResultValue>();
		for (String variable : variables){
			
			ResultValue resultValue = null;
			RDFNode rdfNode = querySolution.get(variable);
			if (rdfNode!=null){
				if (rdfNode.isLiteral()){
					resultValue = new ResultValue(rdfNode.asLiteral().getString(), true);
				}
				else if (rdfNode.isResource()){
					resultValue = new ResultValue(rdfNode.asResource().toString(), false);
				}
			}
			
			map.put(variable, resultValue);
		}
		
		if (rows==null){
			rows = new ArrayList<HashMap<String,ResultValue>>();
		}
		rows.add(map);
	}
	
	private void addCols(){
		
		if (variables==null || variables.isEmpty()){
			return;
		}

		for (String variable : variables){
			
			Map<String, Object> col = new HashMap<String, Object>();
			col.put("property", variable);
			col.put("title", variable);
			col.put("sortable", Boolean.TRUE);
			
			if (cols == null){
				cols = new ArrayList<Map<String,Object>>();
			}
			cols.add(col);
		}
	}

	/**
	 * @return the variables
	 */
	public List<String> getVariables() {
		return variables;
	}

	/**
	 * @return the rows
	 */
	public ArrayList<HashMap<String, ResultValue>> getRows() {
		return rows;
	}
	
	/**
	 * @return the cols
	 */
	public ArrayList<Map<String, Object>> getCols() {
		return cols;
	}
}
