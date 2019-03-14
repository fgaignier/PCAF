package logic.pl;

import java.util.HashSet;
import java.util.Set;

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
}
