package logic.pl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
}
