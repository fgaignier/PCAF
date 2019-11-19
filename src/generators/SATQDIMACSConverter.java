package generators;


import java.util.Map;
import logic.pl.SatFormula;

/**
 * Class to convert a qbf formula to QDIMACS
 * Requires a QDIMACSBuilder since the construction is not sequential
 * Some metrics must be stored during construction and result assembly 
 * can only happen at the end (nb var and nb clauses for example)
 * @author Fabrice
 *
 */
public class SATQDIMACSConverter {

	protected SatFormula formula;
	protected QDIMACSBuilder build;
	
	public SATQDIMACSConverter(SatFormula formula) {
		this.formula = formula;
		this.build = new QDIMACSBuilder();
	}
	
	/**
	 * returns the QDIMACS encoding
	 * @return
	 */
	public String toQDimacs() {
		StringBuilder result = new StringBuilder();
		StringBuilder comment = new StringBuilder();
		StringBuilder header = new StringBuilder();
		StringBuilder encoding = new StringBuilder();
		StringBuilder output = new StringBuilder();

		// must reset the builder
		this.build = new QDIMACSBuilder();

		// adds variables to the builder
		this.addVariablesToBuilder();
		
		// then we encode the matrix (to determine all other variables and number of clauses)
		encoding.append(formula.getMatrix().toQDIMACS(build));
		
		// output must be true
		Integer main = build.getVarCode("main");
		build.incClause();
		output.append(main.toString() + " 0\n");
		
		// p cnf nbVar nbClause
		header.append(this.getHeaderQDIMACS());
		
		// correspondence between variable name and encoding number for debugging purposes
		comment.append(this.getCommentQDIMACS());
		
		result.append(comment.toString());
		result.append(header.toString());
		result.append(output.toString());
		result.append(encoding.toString());
		
		return result.toString();
	}
	
	private void addVariablesToBuilder() {
		for(String var : formula.getVariables()) {
			this.build.addVar(var, false);
		}
	}
	
	private String getHeaderQDIMACS() {
		return "p cnf " + this.build.getNbVar() + " " + this.build.getNbClause() + "\n";
	}
	
	private String getCommentQDIMACS() {
		StringBuilder result = new StringBuilder();
		Map<String, Integer> vars = build.getVars();
		for(String s : vars.keySet()) {
			result.append("c VarName " + vars.get(s).toString() + "	: " + s + "\n");
		}
		
		return result.toString();
	}
	
}
