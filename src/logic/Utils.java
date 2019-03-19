package logic;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import logic.pl.Atom;
import logic.pl.Formula;
import logic.pl.Negation;

public class Utils {

	public static String toVarList(List<Formula> formulas) {
		if (formulas.isEmpty())
			throw new UnsupportedOperationException("Cannot transform an empty list.");
			//return new String("");
		
		StringBuilder build = new StringBuilder();
		int i = 0;
		for (; i < formulas.size() - 1; i++) {
			if ((formulas.get(i) instanceof Atom || formulas.get(i) instanceof Negation))
				build.append(formulas.get(i).toString() + ", ");
			else
				build.append(formulas.get(i).getName() + ", ");
		}
		if ((formulas.get(i) instanceof Atom || formulas.get(i) instanceof Negation))
			build.append(formulas.get(i).toString());
		else
			build.append(formulas.get(i).getName());
		return build.toString();
	}

	public static String toVarList(Set<Formula> formulas) {
		if (formulas.isEmpty())
			throw new UnsupportedOperationException("Cannot transform an empty list.");
		StringBuilder build = new StringBuilder();
		Iterator<Formula> it = formulas.iterator();
		while (it.hasNext()) {
			build.append(it.next().getName());
			if (it.hasNext())
				build.append(", ");
		}
		return build.toString();
	}

	public static String atomsToVarList(Set<Atom> variables) {
		if (variables.isEmpty())
			throw new UnsupportedOperationException("Cannot transform an empty list.");
		StringBuilder build = new StringBuilder();
		Iterator<Atom> it = variables.iterator();
		while (it.hasNext()) {
			build.append(it.next().getName());
			if (it.hasNext())
				build.append(", ");
		}
		return build.toString();
	}
}
