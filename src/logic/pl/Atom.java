package logic.pl;

import java.util.HashSet;
import java.util.Set;

import generators.QDIMACSBuilder;

public class Atom extends Formula {

	public Atom(String name) {
		this.name = name;
	}

	public String toString() {
		return name;
	}

	@Override
	public Set<Atom> getVariables() {
		Set<Atom> res = new HashSet<Atom>();
		res.add(this);
		// TODO Auto-generated method stub
		return res;
	}

	@Override
	public String toQCir() {
		throw new UnsupportedOperationException("Cannot get QCir of an Atom.");
	}

	public String toQDIMACS(QDIMACSBuilder build) {
		return build.getVarCode(this.getName()).toString();
	}
	
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}

		if (!(other instanceof Atom)) {
			return false;
		}

		Atom o = (Atom) other;
		return this.name.equals(o.getName());
	}
	
	public int hashCode() {
		return name.hashCode();
	}
}
