package logic.qbf;

import java.util.HashSet;
import java.util.Set;

import logic.Utils;
import logic.pl.Atom;

public class Free extends Quantifier {
	public Free(Set<Atom> variables) {
		this.variables = variables;
	}
	
	public Free() {
		this.variables = new HashSet<Atom>();
	}
	
	public String toString() {
		return "free(" + Utils.atomsToVarList(variables) + ")";
	}
}
