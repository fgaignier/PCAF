package logic.qbf;

import java.util.Set;

import logic.pl.Atom;

public abstract class Quantifier {
	protected Set<Atom> variables ;
	
	public void addVariable(Atom at) {
		variables.add(at);
	}
}
