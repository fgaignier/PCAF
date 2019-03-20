package generators;

import java.util.Map;
import java.util.LinkedHashMap;

public class QDIMACSBuilder {

	//initial variables of the problem (all quantified with free, forall, exists)
	protected Map<String, Integer> varI;
	// additional variables created
	protected Map<String, Integer> varA;
	protected int nbClause;
	protected int nbVar;
	
	public QDIMACSBuilder() {
		varI = new LinkedHashMap<String, Integer>();
		varA = new LinkedHashMap<String, Integer>();
		this.nbVar = 0;
		this.nbClause = 0;
	}
	
	/**
	 * add a variable by its name. Must specify if it is an additional variable or ont
	 * @param name
	 * @param additional
	 */
	public void addVar(String name, boolean additional) {
		this.nbVar ++;
		if(additional) {
			varA.put(name,  new Integer(this.nbVar));
		} else {
			varI.put(name,  new Integer(this.nbVar));
		}
	}
	
	public Integer getVarCode(String name) {
		Integer encoding = varI.get(name);
		if(encoding == null) {
			encoding = varA.get(name);
		}
		if(encoding == null) {
			throw new UnsupportedOperationException("Atom of name " + name + " is not in the var list");
		}
		return encoding;
	}
	
	public Map<String, Integer> getVars() {
		Map <String, Integer> result = new LinkedHashMap<String, Integer>();
		result.putAll(this.varI);
		result.putAll(this.varA);
		return result;
	}
	
	public Map<String, Integer> getInitialVars() {
		return this.varI;
	}
	
	public Map<String, Integer> getAdditionalVars() {
		return this.varA;
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
