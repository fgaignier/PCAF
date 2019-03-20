package logic.pl;

import java.util.HashSet;
import java.util.Set;

import generators.QDIMACSBuilder;

public class Implication extends Formula {
	private Formula formulaIf;
	private Formula formulaThen;

	public Implication(String name, Formula formulaIf, Formula formulaThen) {
		this.formulaIf = formulaIf;
		this.formulaThen = formulaThen;
		this.name = name;
	}

	public String toString() {
		return name + " = ite(" + formulaIf.getName() + ", " + formulaThen.getName() + ", TRUE)";
	}

	@Override
	public Set<Atom> getVariables() {
		Set<Atom> res = new HashSet<Atom>();
		res.addAll(formulaIf.getVariables());
		res.addAll(formulaThen.getVariables());
		return res;
	}
	
	@Override
	public String toQCir() {
		StringBuilder build = new StringBuilder();

		if (!(formulaIf instanceof Atom || formulaIf instanceof Negation)) {
			build.append(formulaIf.toQCir());
		}
		
		if (!(formulaThen instanceof Atom || formulaThen instanceof Negation)) {
			build.append(formulaThen.toQCir());
		}

		build.append(this.toString() + "\n");
		return build.toString();
	}
	
	public String toQDIMACS(QDIMACSBuilder build) {
		StringBuilder result = new StringBuilder();

		build.addVar(this.getName(), true);
		
		
		if (!(formulaIf instanceof Atom || formulaIf instanceof Negation)) {
			result.append(formulaIf.toQDIMACS(build));
		}
		
		if (!(formulaThen instanceof Atom || formulaThen instanceof Negation)) {
			result.append(formulaThen.toQDIMACS(build));
		}

		// debug only
		//result.append(this.toString() + "\n");		
		result.append(this.implQDIMACS(build));
		//result.append("current number of clauses: " + build.getNbClause() + "\n");
		return result.toString();
	}
	
	 
	/**
	 * v <==> (c => t) expands to
	 * (~c | t | ~v) & (c | v) & (~t | v)
	 * @param build
	 * @return
	 */
	public String implQDIMACS(QDIMACSBuilder build) {
		StringBuilder result = new StringBuilder();
		Integer encodeV = build.getVarCode(this.getName());
		Integer encodeC = null;
		if(this.formulaIf instanceof Negation) {
			Negation neg = (Negation)formulaIf;
			encodeC = build.getVarCode(neg.getAtomName());
		} else {
			encodeC = build.getVarCode(this.formulaIf.getName());
		}
		Integer encodeT = null;
		if(this.formulaThen instanceof Negation) {
			Negation neg = (Negation)formulaThen;
			encodeT = build.getVarCode(neg.getAtomName());
		} else {
			encodeT = build.getVarCode(this.formulaThen.getName());
		}
		
		result.append("-" + encodeC.toString() + " " + encodeT.toString() + " -" + encodeV.toString() + " 0\n");
		build.incClause();
		result.append(encodeC.toString() + " " + encodeV.toString() + " 0\n");
		build.incClause();
		result.append("-" + encodeT.toString() + " " + encodeV.toString() +  " 0\n");
		build.incClause();
		
		return result.toString();
	}
}
