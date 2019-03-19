package logic.qbf;

import java.util.List;

import logic.pl.Formula;

public class QBFFormula {
	private List<Quantifier> quantifiers ;
	private Formula matrix ;
	
	public QBFFormula(List<Quantifier> quantifiers, Formula matrix) {
		this.quantifiers = quantifiers;
		this.matrix = matrix;
	}
	
	public String toString() {
		StringBuilder build = new StringBuilder("#QCIR-14\n");
		for(int i = 0 ; i < quantifiers.size() ; i++) {
			build.append(quantifiers.get(i).toString() + "\n");
		}
		build.append("output(" + matrix.getName() + ")\n");
		build.append("TRUE = and()\n");
		build.append(matrix.toQCir());
		return build.toString();
	}
	
	public List<Quantifier> getQuantifiers() {
		return this.quantifiers;
	}
	
	public Formula getMatrix() {
		return this.matrix;
	}
	
}
