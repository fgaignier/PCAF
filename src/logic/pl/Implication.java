package logic.pl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

	public List<List<Integer>> toQDIMACSList(QDIMACSBuilder build) {
		List<List<Integer>> result = new ArrayList<List<Integer>>();

		build.addVar(this.getName(), true);

		if (!(formulaIf instanceof Atom || formulaIf instanceof Negation)) {
			result.addAll(formulaIf.toQDIMACSList(build));
		}

		if (!(formulaThen instanceof Atom || formulaThen instanceof Negation)) {
			result.addAll(formulaThen.toQDIMACSList(build));
		}

		result.addAll(this.implQDIMACSList(build));
		return result;
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

		result.append(this.implQDIMACS(build));
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
	
	public List<List<Integer>> implQDIMACSList(QDIMACSBuilder build) {
		List<List<Integer>> result = new ArrayList<List<Integer>>();
		List<Integer> individual = null;
		//StringBuilder result = new StringBuilder();
		
		Integer encodeV = build.getVarCode(this.getName());
		Integer encodeC = null;
		int encodeCSign = 1;
		int encodeCNeg = 1;
		if(this.formulaIf instanceof Negation) {
			Negation neg = (Negation)formulaIf;
			encodeC = build.getVarCode(neg.getAtomName());
			encodeCSign = -1;
		} else {
			encodeC = build.getVarCode(this.formulaIf.getName());
			encodeCNeg = -1;
		}
		Integer encodeT = null;
		int encodeTSign = 1;
		int encodeTNeg = 1;
		if(this.formulaThen instanceof Negation) {
			Negation neg = (Negation)formulaThen;
			encodeT = build.getVarCode(neg.getAtomName());
			encodeTSign = -1;
		} else {
			encodeT = build.getVarCode(this.formulaThen.getName());
			encodeTNeg = -1;
		}

//		result.append(encodeCNeg + encodeC.toString() + encodeTSign + encodeT.toString() + " -" + encodeV.toString() + " 0\n");
		individual = new ArrayList<Integer>();
		individual.add(encodeCNeg*encodeC);
		individual.add(encodeTSign*encodeT);
		individual.add(-1*encodeV);
		result.add(individual);
		build.incClause();
		
//		result.append(encodeCSign + encodeC.toString() + " " + encodeV.toString() + " 0\n");
		individual = new ArrayList<Integer>();
		individual.add(encodeCSign*encodeC);
		individual.add(encodeV);
		result.add(individual);
		build.incClause();
		
//		result.append(encodeTNeg + encodeT.toString() + " " + encodeV.toString() +  " 0\n");
		individual = new ArrayList<Integer>();
		individual.add(encodeTNeg*encodeT);
		individual.add(encodeV);
		result.add(individual);
		build.incClause();

		return result;
	}
}
