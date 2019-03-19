package generators;

import java.util.Map;
import java.util.LinkedHashMap;

public class QDIMACSBuilder {

	protected Map<String, Integer> var;
	protected int nbClause;
	protected int nbVar;
	
	public QDIMACSBuilder() {
		var = new LinkedHashMap<String, Integer>();
		this.nbVar = 0;
		this.nbClause = 0;
	}
	
	public void addVar(String name) {
		this.nbVar ++;
		var.put(name,  new Integer(this.nbVar));
	}
	
	public Integer getVarCode(String name) {
		Integer encoding = var.get(name);
		if(encoding == null) {
			throw new UnsupportedOperationException("Atom of name " + name + " is not in the var list");
		}
		return encoding;
	}
	
	public Map<String, Integer> getVars() {
		return this.var;
	}
	
	public void incClause() {
		this.nbClause++;
	}
	
	public int getNbVar() {
		return this.nbVar;
	}
	
	public int getNbClause() {
		return this.nbClause;
	}
}
