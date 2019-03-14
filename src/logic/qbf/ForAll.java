package logic.qbf;

import java.util.HashSet;
import java.util.Set;

import logic.Utils;
import logic.pl.Atom;

public class ForAll extends Quantifier {
	public ForAll(Set<Atom> variables) {
		this.variables = variables;
	}
	
	public ForAll() {
		this.variables = new HashSet<Atom>();
	}
	
	public String toString() {
		return "forall(" + Utils.atomsToVarList(variables) + ")";
	}
}
