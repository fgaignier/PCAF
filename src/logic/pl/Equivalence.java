package logic.pl;

import java.util.HashSet;
import java.util.Set;

public class Equivalence extends Formula {
	private Formula left;
	private Formula right;

	public Equivalence(String name, Formula left, Formula right) {
		this.name = name;
		this.left = left;
		this.right = right;
	}

	public String toString() {
		return name + " = xor(-" + left.getName() + ", " + right.getName() + ")";
	}

	@Override
	public Set<Atom> getVariables() {
		Set<Atom> res = new HashSet<Atom>();
		res.addAll(right.getVariables());
		res.addAll(left.getVariables());
		return res;
	}

	@Override
	public String toQCir() {
		StringBuilder build = new StringBuilder();

		if (!(left instanceof Atom || left instanceof Negation)) {
			build.append(left.toQCir());
		}
		
		if (!(right instanceof Atom || right instanceof Negation)) {
			build.append(right.toQCir());
		}

		build.append(this.toString() + "\n");
		return build.toString();
	}
}
