package generators;

import java.util.Map;
import java.util.LinkedHashMap;

/**
 * stores information for the QDIMACSConverter
 * an additional variable is a variable that is not part of the quantifiers (free, exists, forall)
 * These are separated since they are added to an exists quantifier at the end
 */
public class QDIMACSBuilder {

	//initial variables of the problem (all quantified with free, forall, exists)
	protected Map<String, Integer> varI;
	// additional variables created
	protected Map<String, Integer> varA;
	
	//reverse Maps to get the name from its int value (to decode the solution)
	protected Map<Integer, String> varIrev;
	protected Map<Integer, String> varArev;
	
	protected int nbClause;
	protected int nbVar;
	
	/**
	 * need to be able to associate a var name to its int value and vice versa
	 */
	public QDIMACSBuilder() {
		varI = new LinkedHashMap<String, Integer>();
		varA = new LinkedHashMap<String, Integer>();
		// reverse structures
		varIrev = new LinkedHashMap<Integer, String>();
		varArev = new LinkedHashMap<Integer, String>();
		
		this.nbVar = 0;
		this.nbClause = 0;
	}
	
	
	/**
	 * add a variable by its name. Must specify if it is an additional variable or not
	 * additional variable is a variable that is not part of the quantifiers (free, exists, forall)
	 * @param name name of variable
	 * @param additional additional variable or not 
	 */
	public void addVar(String name, boolean additional) {
		this.nbVar ++;
		Integer value = new Integer(this.nbVar);
		if(additional) {
			varA.put(name,  value);
			varArev.put(value, name);
			
		} else {
			varI.put(name,  value);
			varIrev.put(value, name);
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
	
	/**
	 * returns the name of the variable
	 * could be an original variable or an additional one
	 * if not found throws an exception
	 * @param var
	 * @return
	 */
	public String getVarName(Integer var) {
		String name = varIrev.get(var);
		if(name == null) {
			name = varArev.get(var);
		}
		if(name == null) {
			throw new UnsupportedOperationException("Atom of Id " + var.toString() + " is not in the var list");
		}
		return name;
	}
	
	/**
	 * returns the name of the corresponding variable.
	 * if it is not an original variable, returns null
	 * @param var
	 * @return
	 */
	public String getOriginalVarName(Integer var) {
		return varIrev.get(var);
	}
}
