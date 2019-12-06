package generators;


import java.util.Map;
import java.util.ArrayList;
import java.util.List;
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
	
	public int getNbClause() {
		if(this.build == null) {
			throw new UnsupportedOperationException("need to convert first");
		}
		return this.build.getNbClause();
	}
	
	public int getNbVar() {
		if(this.build == null) {
			throw new UnsupportedOperationException("need to convert first");
		}
		return this.build.getNbVar();
	}
	
	/**
	 * returns the QDIMACS encoding in String format
	 * used for solvers that only accept files as imput
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
	
	public String toQDimacsList() {
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
		List<List<Integer>> listClauses = formula.getMatrix().toQDIMACSList(build);
		encoding.append(this.convertQDIMACSList(listClauses));
		
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
	
	private String convertQDIMACSList(List<List<Integer>> listClauses) {
		StringBuilder result = new StringBuilder();
		for(List<Integer> clause : listClauses) {
			for(Integer var : clause) {
				result.append(var.toString());
				result.append(" ");
			}
			result.append("0\n");
		}
		
		return result.toString();
	}
	
	private List<int[]> vectorConversion(List<List<Integer>> listClauses) {
		List<int[]> result = new ArrayList<int[]>();
		for(List<Integer> clause : listClauses) {
			int[] clauseInt = new int[clause.size()];
			for(int i=0; i<clause.size();i++) {
				clauseInt[i] = clause.get(i);
			}
			result.add(clauseInt);
		}
		return result;
	}
	
	public List<int[]> convertToListOfClauses() {
		// creates the builder
		this.build = new QDIMACSBuilder();
		// adds variables to the builder
		this.addVariablesToBuilder();
		// then we encode the matrix (to determine all other variables and number of clauses)
		List<List<Integer>> listClauses = formula.getMatrix().toQDIMACSList(build);
		// output must be true
		Integer main = build.getVarCode("main");
		build.incClause();
		List<Integer> mainClause = new ArrayList<Integer>();
		mainClause.add(main);
		listClauses.add(mainClause);
		
		return this.vectorConversion(listClauses);
	}
	
	/**
	 * returns a list of variables (original variables) with value 1
	 * all the others will have value 0
	 * @param model
	 * @return
	 */
	public List<String> decodeModel(int[] model) {
		List<String> result = new ArrayList<>();
		for(int i =0; i<model.length; i++) {
			// if negative will not be found
			String name = build.getOriginalVarName(model[i]);
			if(name != null) {
				result.add(name);
			}
		}
		return result;
	}
}
