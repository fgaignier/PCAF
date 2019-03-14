package logic.qbf;

import java.util.HashSet;
import java.util.Set;

import logic.Utils;
import logic.pl.Atom;

public class Exists extends Quantifier {
	public Exists() {
		variables = new HashSet<Atom>();
	}

	public Exists(Set<Atom> variables) {
		this.variables = variables;
	}
	
	public String toString() {
		return "exists(" + Utils.atomsToVarList(variables) + ")";
	}
}
