package generators;

import java.util.List;
import java.util.Map;
import java.util.Set;

import logic.pl.Atom;
import logic.qbf.Exists;
import logic.qbf.Free;
import logic.qbf.QBFFormula;
import logic.qbf.Quantifier;

public class QDIMACSConverter {

	protected QBFFormula qbf;
	protected QDIMACSBuilder build;
	
	public QDIMACSConverter(QBFFormula qbf) {
		this.qbf = qbf;
		this.build = new QDIMACSBuilder();
		
	}
	
	public String toQDimacs() {
		StringBuilder result = new StringBuilder();
		StringBuilder comment = new StringBuilder();
		StringBuilder header = new StringBuilder();
		StringBuilder quantifs = new StringBuilder();
		StringBuilder encoding = new StringBuilder();
		
		List<Quantifier> quantifiers = qbf.getQuantifiers();
		for(Quantifier q: quantifiers) {
			quantifs.append(quantifierToQDIMACS(q));
		}
		
		// first we encode the matrix (to determine all other variables and number of clauses)
		encoding.append(qbf.getMatrix().toQDIMACS(build));
		
		header.append(this.getHeaderQDIMACS());
		comment.append(this.getCommentQDIMACS());
		result.append(comment.toString());
		result.append(header.toString());
		result.append(quantifs.toString());
		result.append(encoding.toString());
		
		return result.toString();
	}
	
	public String getHeaderQDIMACS() {
		return "p cnf " + this.build.getNbVar() + " " + this.build.getNbClause() + "\n";
	}
	
	public String quantifierToQDIMACS(Quantifier q) {
		StringBuilder result = new StringBuilder();
		Set<Atom> variables = q.getVariables();
		if(q instanceof Free) {
			for(Atom a: variables) {
				build.addVar(a.getName());
			}
		} else if(q instanceof Exists) {
			result.append("e ");
			for(Atom a: variables) {
				build.addVar(a.getName());
				result.append(build.getNbVar() + " ");
			}
			result.append("0\n");
		} else {
			result.append("a ");
			for(Atom a: variables) {
				build.addVar(a.getName());
				result.append(build.getNbVar() + " ");
			}
			result.append("0\n");
		}
		return result.toString();
	}
	
	public String getCommentQDIMACS() {
		StringBuilder result = new StringBuilder();
		Map<String, Integer> vars = build.getVars();
		for(String s : vars.keySet()) {
			result.append("c VarName " + vars.get(s).toString() + "	: " + s + "\n");
		}
		
		return result.toString();
	}
	
}
