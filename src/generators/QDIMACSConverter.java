package generators;

import java.util.Collection;
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
		StringBuilder output = new StringBuilder();
		
		// first we encode the quantifiers (to get the quantified variables)
		List<Quantifier> quantifiers = qbf.getQuantifiers();
		for(Quantifier q: quantifiers) {
			quantifs.append(quantifierToQDIMACS(q));
		}
		
		// then we encode the matrix (to determine all other variables and number of clauses)
		encoding.append(qbf.getMatrix().toQDIMACS(build));
		
		//here we must finish the quantifier encoding to add all additional variables added
		// remove the last two characters and encode
		quantifs.delete(quantifs.length()-2, quantifs.length());
		quantifs.append(additionalQuantifierQDIMACS());
		
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
		result.append(quantifs.toString());
		result.append(output.toString());
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
				build.addVar(a.getName(), false);
			}
		} else if(q instanceof Exists) {
			result.append("e ");
			for(Atom a: variables) {
				build.addVar(a.getName(), false);
				result.append(build.getNbVar() + " ");
			}
			result.append("0\n");
		} else {
			result.append("a ");
			for(Atom a: variables) {
				build.addVar(a.getName(), false);
				result.append(build.getNbVar() + " ");
			}
			result.append("0\n");
		}
		return result.toString();
	}
	
	public String additionalQuantifierQDIMACS() {
		StringBuilder result = new StringBuilder();
		Collection<Integer> vars = build.getAdditionalVars().values();
		for(Integer i: vars) {
			result.append(i.toString() + " ");
		}
		result.append("0\n");
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
