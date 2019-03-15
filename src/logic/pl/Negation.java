package logic.pl;

import java.util.Set;

public class Negation extends Formula {
	private Atom atom;

	public Negation(String name, Atom atom) {
		this.name = name;
		this.atom = atom;
	}
	
	public Negation(Atom atom) {
		this("",atom);
	}

	public String toString() {
		return "-" + atom.getName();
	}
	
	public String getName() {
		return toString();
	}

	
	@Override
	public Set<Atom> getVariables() {
		return atom.getVariables();
	}

	@Override
	public String toQCir() {
		throw new UnsupportedOperationException("Cannot get a QCir for a Negation.");
	}
}
