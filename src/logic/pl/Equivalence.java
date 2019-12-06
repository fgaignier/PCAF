package logic.pl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
		String leftName = null;
		if(left instanceof Negation) {
			leftName = ((Negation)left).getAtomName();
		} else {
			leftName = "-" + left.getName();
		}
		return name + " = xor(" + leftName + ", " + right.getName() + ")";
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
	
	public List<List<Integer>> toQDIMACSList(QDIMACSBuilder build) {
		List<List<Integer>> result = new ArrayList<List<Integer>>();
		build.addVar(this.getName(), true);
	
		if (!(left instanceof Atom || left instanceof Negation)) {
			result.addAll(left.toQDIMACSList(build));
		}
		
		if (!(right instanceof Atom || right instanceof Negation)) {
			result.addAll(right.toQDIMACSList(build));
		}
		result.addAll(this.xorQDIMACSList(build));
		return result;
	}
	
	public String toQDIMACS(QDIMACSBuilder build) {
		StringBuilder result = new StringBuilder();
		
		build.addVar(this.getName(), true);
		
		if (!(left instanceof Atom || left instanceof Negation)) {
			result.append(left.toQDIMACS(build));
		}
		
		if (!(right instanceof Atom || right instanceof Negation)) {
			result.append(right.toQDIMACS(build));
		}

		result.append(this.xorQDIMACS(build));
		
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
			String encodeLSign = " ";
			String encodeLNeg = " ";
			if(this.left instanceof Negation) {
				Negation neg = (Negation)left;
				encodeL = build.getVarCode(neg.getAtomName());
				encodeLSign = " -";
			} else {
				encodeL = build.getVarCode(this.left.getName());
				encodeLNeg = " -";
			}
			
			Integer encodeR = null;
			String encodeRSign = " ";
			String encodeRNeg = " ";
			if(this.right instanceof Negation) {
				Negation neg = (Negation)right;
				encodeR = build.getVarCode(neg.getAtomName());
				encodeRSign = " -";
			} else {
				encodeR = build.getVarCode(this.right.getName());
				encodeRNeg = " -";
			}
				
			result.append(encodeLSign + encodeL.toString() + encodeRNeg + encodeR.toString() + " -" + encodeV.toString() + " 0\n");
			build.incClause();
			result.append(encodeLNeg + encodeL.toString() + encodeRSign + encodeR.toString() + " -" + encodeV.toString() + " 0\n");
			build.incClause();
			result.append(encodeLNeg + encodeL.toString() + encodeRNeg + encodeR.toString() + " " + encodeV.toString() + " 0\n");
			build.incClause();
			result.append(encodeLSign + encodeL.toString() + encodeRSign + encodeR.toString() + " " + encodeV.toString() + " 0\n");
			build.incClause();
			
			return result.toString();
		}
		
		public List<List<Integer>> xorQDIMACSList(QDIMACSBuilder build) {
			List<List<Integer>> result = new ArrayList<List<Integer>>();
			List<Integer> individual = null;
//			StringBuilder result = new StringBuilder();
			Integer encodeV = build.getVarCode(this.getName());
			
			Integer encodeL = null;
			int encodeLSign = 1;
			int encodeLNeg = 1;
			if(this.left instanceof Negation) {
				Negation neg = (Negation)left;
				encodeL = build.getVarCode(neg.getAtomName());
				encodeLSign = -1;
			} else {
				encodeL = build.getVarCode(this.left.getName());
				encodeLNeg = -1;
			}
			
			Integer encodeR = null;
			int encodeRSign = 1;
			int encodeRNeg = 1;
			if(this.right instanceof Negation) {
				Negation neg = (Negation)right;
				encodeR = build.getVarCode(neg.getAtomName());
				encodeRSign = -1;
			} else {
				encodeR = build.getVarCode(this.right.getName());
				encodeRNeg = -1;
			}

			
//			result.append(encodeLSign + encodeL.toString() + encodeRNeg + encodeR.toString() + " -" + encodeV.toString() + " 0\n");
			individual = new ArrayList<Integer>();
			individual.add(encodeLSign*encodeL);
			individual.add(encodeRNeg*encodeR);
			individual.add(-1*encodeV);
			result.add(individual);
			build.incClause();
			
//			result.append(encodeLNeg + encodeL.toString() + encodeRSign + encodeR.toString() + " -" + encodeV.toString() + " 0\n");
			individual = new ArrayList<Integer>();
			individual.add(encodeLNeg*encodeL);
			individual.add(encodeRSign*encodeR);
			individual.add(-1*encodeV);
			result.add(individual);
			build.incClause();

//			result.append(encodeLNeg + encodeL.toString() + encodeRNeg + encodeR.toString() + " " + encodeV.toString() + " 0\n");
			individual = new ArrayList<Integer>();
			individual.add(encodeLNeg*encodeL);
			individual.add(encodeRNeg*encodeR);
			individual.add(encodeV);
			result.add(individual);
			build.incClause();

//			result.append(encodeLSign + encodeL.toString() + encodeRSign + encodeR.toString() + " " + encodeV.toString() + " 0\n");
			individual = new ArrayList<Integer>();
			individual.add(encodeLSign*encodeL);
			individual.add(encodeRSign*encodeR);
			individual.add(encodeV);
			result.add(individual);
			build.incClause();

			
			return result;
		}
}
