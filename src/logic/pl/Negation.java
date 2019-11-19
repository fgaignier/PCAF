package logic.pl;

import java.util.Set;

import generators.QDIMACSBuilder;

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

	public String getAtomName() {
		return atom.getName();
	}
	
	@Override
	public Set<Atom> getVariables() {
		return atom.getVariables();
	}

	@Override
	public String toQCir() {
		throw new UnsupportedOperationException("Cannot get a QCir for a Negation.");
	}
	
	public String toQDIMACS(QDIMACSBuilder build) {
		throw new UnsupportedOperationException("Cannot get a QDIMACS for a Negation.");
		//return "-" + build.getVarCode(this.getAtomName()).toString();
	}
}
