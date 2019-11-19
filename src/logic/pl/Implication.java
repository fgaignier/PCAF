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
		String encodeCSign = " ";
		String encodeCNeg = " ";
		if(this.formulaIf instanceof Negation) {
			Negation neg = (Negation)formulaIf;
			encodeC = build.getVarCode(neg.getAtomName());
			encodeCSign = " -";
		} else {
			encodeC = build.getVarCode(this.formulaIf.getName());
			encodeCNeg = " -";
		}
		Integer encodeT = null;
		String encodeTSign = " ";
		String encodeTNeg = " ";
		if(this.formulaThen instanceof Negation) {
			Negation neg = (Negation)formulaThen;
			encodeT = build.getVarCode(neg.getAtomName());
			encodeTSign = " -";
		} else {
			encodeT = build.getVarCode(this.formulaThen.getName());
			encodeTNeg = " -";
		}
		
		result.append(encodeCNeg + encodeC.toString() + encodeTSign + encodeT.toString() + " -" + encodeV.toString() + " 0\n");
		build.incClause();
		result.append(encodeCSign + encodeC.toString() + " " + encodeV.toString() + " 0\n");
		build.incClause();
		result.append(encodeTNeg + encodeT.toString() + " " + encodeV.toString() +  " 0\n");
		build.incClause();
		
		return result.toString();
	}
}
