package logic.pl;

import java.util.HashSet;
import java.util.Set;

import generators.QDIMACSBuilder;

public class Equivalence extends Formula {
	private Formula left;
	private Formula right;

	public Equivalence(String name, Formula left, Formula right) {
		this.name = name;
		this.left = left;
		this.right = right;
	}

	public String toString() {
		return name + " = xor(-" + left.getName() + ", " + right.getName() + ")";
	}

	@Override
	public Set<Atom> getVariables() {
		Set<Atom> res = new HashSet<Atom>();
		res.addAll(right.getVariables());
		res.addAll(left.getVariables());
		return res;
	}

	@Override
	public String toQCir() {
		StringBuilder build = new StringBuilder();

		if (!(left instanceof Atom || left instanceof Negation)) {
			build.append(left.toQCir());
		}
		
		if (!(right instanceof Atom || right instanceof Negation)) {
			build.append(right.toQCir());
		}

		build.append(this.toString() + "\n");
		return build.toString();
	}
	
	public String toQDIMACS(QDIMACSBuilder build) {
		StringBuilder result = new StringBuilder();
		
		build.addVar(this.getName(), true);
		//build.incClause();
		
		if (!(left instanceof Atom || left instanceof Negation)) {
			result.append(left.toQDIMACS(build));
		}
		
		if (!(right instanceof Atom || right instanceof Negation)) {
			result.append(right.toQDIMACS(build));
		}

		// debug only
		//result.append(this.toString() +"\n");
		result.append(this.xorQDIMACS(build));
		//result.append("current number of clauses: " + build.getNbClause() + "\n");
		
		return result.toString();
	}
	
		
	/**
	 * C = A XOR B => (~A | ~B | ~C) & (A | B | ~C) & (A | ~B | C) & (~A | B | C)
	 * v <==> (~l xor r) expands to
	 * in our case C = v, A=~l, B=r
	 * v = ~l XOR r => (l | ~r | ~v) & (~l | r | ~v) & (~l | ~r | v) & (l | r | v)
	 * @param build
	 * @return
	 */
		public String xorQDIMACS(QDIMACSBuilder build) {
			StringBuilder result = new StringBuilder();
			Integer encodeV = build.getVarCode(this.getName());
			
			Integer encodeL = null;
			if(this.left instanceof Negation) {
				Negation neg = (Negation)left;
				encodeL = build.getVarCode(neg.getAtomName());
			} else {
				encodeL = build.getVarCode(this.left.getName());
			}
			
			Integer encodeR = null;
			if(this.right instanceof Negation) {
				Negation neg = (Negation)right;
				encodeR = build.getVarCode(neg.getAtomName());
			} else {
				encodeR = build.getVarCode(this.right.getName());
			}
				
			result.append(encodeL.toString() + " -" + encodeR.toString() + " -" + encodeV.toString() + " 0\n");
			build.incClause();
			result.append("-" + encodeL.toString() + " " + encodeR.toString() + " -" + encodeV.toString() + " 0\n");
			build.incClause();
			result.append("-" + encodeL.toString() + " -" + encodeR.toString() + " " + encodeV.toString() + " 0\n");
			build.incClause();
			result.append(encodeL.toString() + " " + encodeR.toString() + " " + encodeV.toString() + " 0\n");
			build.incClause();
			
			return result.toString();
		}
}
