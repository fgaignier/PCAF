package logic.pl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import generators.QDIMACSBuilder;
import logic.Utils;

public class Disjunction extends Formula {
	private List<Formula> subformulas;

	public Disjunction(String name) {
		this.name = name;
		subformulas = new ArrayList<Formula>();
	}

	public Disjunction(String name, List<Formula> subf) {
		this.name = name;
		subformulas = new ArrayList<Formula>(subf);
	}

	public void addSubformula(Formula f) {
		subformulas.add(f);
	}

	public String toString() {
		return name + " = or(" + Utils.toVarList(subformulas) + ")";
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
	
	public List<List<Integer>> toQDIMACSList(QDIMACSBuilder build) {
		List<List<Integer>> result = new ArrayList<List<Integer>>();

		build.addVar(this.getName(), true);
		for(Formula f : subformulas) {
			if(!(f instanceof Atom || f instanceof Negation)) {
				result.addAll(f.toQDIMACSList(build));
			}
		}
		result.addAll(this.orQDIMACSList(build));

		return result;
	}
	
	public String toQDIMACS(QDIMACSBuilder build) {
		StringBuilder result = new StringBuilder();
		build.addVar(this.getName(), true);
		//build.incClause();
		for(Formula f : subformulas) {
			if(!(f instanceof Atom || f instanceof Negation)) {
				result.append(f.toQDIMACS(build));
			}
		}
		
		// debug only
		//result.append(this.toString() +"\n");	
		result.append(this.orQDIMACS(build));
		//result.append("current number of clauses: " + build.getNbClause() + "\n");
		return result.toString();

	}

	// 
	/**
	 * (f <==> (x1 | x2 | x3)) expands to
     * (~f | x1 | x2 | x3)   &   (~x1 | f)  &  (~x2 | f)  &  (~x3 | f)
	 * @param build
	 * @return
	 */
	public String orQDIMACS(QDIMACSBuilder build) {
		StringBuilder result = new StringBuilder();
		StringBuilder individual = new StringBuilder();
		StringBuilder global = new StringBuilder();
		Integer encode = build.getVarCode(this.getName());
		global.append("-" + encode.toString() + " ");
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
			individual.append(encode.toString() + subEncodeNeg + subEncode.toString() + " 0\n");
			global.append(subEncodeSign + subEncode.toString() + " ");
		}
		build.incClause();
		global.append(" 0\n");
		result.append(individual.toString());
		result.append(global.toString());
		return result.toString();
	}
	
	public List<List<Integer>> orQDIMACSList(QDIMACSBuilder build) {
		//StringBuilder result = new StringBuilder();
		//StringBuilder individual = new StringBuilder();
		//StringBuilder global = new StringBuilder();
		List<List<Integer>> result = new ArrayList<List<Integer>>();
		List<Integer> individual = null;
		List<Integer> global = new ArrayList<Integer>();
		
		Integer encode = build.getVarCode(this.getName());
		//global.append("-" + encode.toString() + " ");
		global.add(-1*encode);
		for(Formula f: this.subformulas) {
			Integer subEncode = null;
			int subEncodeSign = 1;
			int subEncodeNeg = 1;
			if(f instanceof Negation) {
				Negation neg = (Negation)f;
				subEncode = build.getVarCode(neg.getAtomName());
				subEncodeSign = -1;
			} else {
				subEncode = build.getVarCode(f.getName());
				subEncodeNeg = -1;
			}
			build.incClause();
			individual = new ArrayList<Integer>();
			individual.add(encode);
			individual.add(subEncodeNeg*subEncode);
			result.add(individual);
			global.add(subEncodeSign*subEncode);
			//individual.append(encode.toString() + subEncodeNeg + subEncode.toString() + " 0\n");
			//global.append(subEncodeSign + subEncode.toString() + " ");
		}
		build.incClause();
		result.add(global);
//		global.append(" 0\n");
//		result.append(individual.toString());
//		result.append(global.toString());
		return result;
	}
}
