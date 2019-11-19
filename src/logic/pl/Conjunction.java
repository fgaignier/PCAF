package logic.pl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import generators.QDIMACSBuilder;
import logic.Utils;

public class Conjunction extends Formula {
	private List<Formula> subformulas;

	public Conjunction(String name) {
		this.name = name;
		subformulas = new ArrayList<Formula>();
	}

	public Conjunction(String name, List<Formula> subf) {
		this.name = name;
		subformulas = new ArrayList<Formula>(subf);
	}

	public void addSubformula(Formula f) {
		subformulas.add(f);
	}

	public String toString() {
		if(subformulas.isEmpty()) {
			System.out.println("and clause with no operandes: " + name);
		}
		return name + " = and(" + Utils.toVarList(subformulas) + ")";
	}

	@Override
	public Set<Atom> getVariables() {
		Set<Atom> res = new HashSet<Atom>();
		for (Formula f : subformulas) {
			if (f instanceof Atom)
				res.add((Atom) f);
			else
				res.addAll(f.getVariables());
		}
		return res;
	}

	@Override
	public String toQCir() {
		StringBuilder build = new StringBuilder();
		for(Formula f : subformulas) {
			if(!(f instanceof Atom || f instanceof Negation)) {
				build.append(f.toQCir());
			}
		}
		build.append(this.toString() + "\n");
		return build.toString();
	}
	
	public String toQDIMACS(QDIMACSBuilder build) {
		StringBuilder result = new StringBuilder();
		
		build.addVar(this.getName(), true);
		for(Formula f : subformulas) {
			if(!(f instanceof Atom || f instanceof Negation)) {
				result.append(f.toQDIMACS(build));
			}
		}
		// debug only
		//result.append(this.toString() + "\n");
		result.append(this.andQDIMACS(build));
		//result.append("current number of clauses: " + build.getNbClause() + "\n");
		return result.toString();
	}
	

	/**
	 * (f <==> (x1 & x2 & x3)) expands to
     * (~x1 | ~x2 | ~x3 | f)  &  (~f | x1)  &  (~f | x2)  &  (~f | x3)
	 * @param build
	 * @return
	 */
	public String andQDIMACS(QDIMACSBuilder build) {
		StringBuilder result = new StringBuilder();
		StringBuilder individual = new StringBuilder();
		StringBuilder global = new StringBuilder();
		Integer encode = build.getVarCode(this.getName());
		global.append(encode.toString());
		for(Formula f: this.subformulas) {
			Integer subEncode = null;
			String subEncodeSign = " ";
			String subEncodeNeg = " ";
			if(f instanceof Negation) {
				Negation neg = (Negation)f;
				subEncode = build.getVarCode(neg.getAtomName());
				subEncodeSign = " -";
			} else {
				subEncode = build.getVarCode(f.getName());
				subEncodeNeg = " -";
			}
			build.incClause();
			individual.append("-" + encode.toString() + subEncodeSign + subEncode.toString() + " 0\n");
			global.append(subEncodeNeg + subEncode.toString());
		}
		build.incClause();
		global.append(" 0\n");
		result.append(individual.toString());
		result.append(global.toString());
		return result.toString();
	}
}
