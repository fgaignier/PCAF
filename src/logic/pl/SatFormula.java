package logic.pl;

import java.util.List;


public class SatFormula {
	private List<String> variables ;
	private Formula matrix ;
	
	public SatFormula(List<String> variables, Formula matrix) {
		this.variables = variables;
		this.matrix = matrix;
	}
	
	public List<String> getVariables() {
		return this.variables;
	}
	
	public Formula getMatrix() {
		return this.matrix;
	}
	
	/**
	 * returns the QCIR encoding of the formula
	 * @return String QCIR encoding 
	 */
	public String toQCIR() {
		StringBuilder build = new StringBuilder("#QCIR-14\n");
		build.append("output(" + matrix.getName() + ")\n");
		build.append("TRUE = and()\n");
		build.append(matrix.toQCir());
		return build.toString();
	}
}
