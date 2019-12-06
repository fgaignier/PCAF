package test;

import java.util.ArrayList;
import java.util.List;

import generators.SATQDIMACSConverter;
import logic.pl.Atom;
import logic.pl.Conjunction;
import logic.pl.Negation;
import logic.pl.SatFormula;

public class ConjunctionQDIMACSTest {

	public static void main(String[] args) {
		// test conjunction
		// all positive atoms
		SATQDIMACSConverter converter = null;
		SatFormula formula = null;
		List<String> variables = new ArrayList<String>();
		variables.add("a");
		variables.add("b");
		variables.add("c");
		variables.add("d");
		Conjunction conj1 = new Conjunction("main");
		conj1.addSubformula(new Atom("a"));
		conj1.addSubformula(new Atom("b"));
		conj1.addSubformula(new Atom("c"));
		conj1.addSubformula(new Atom("d"));
		formula = new SatFormula(variables, conj1);
		converter = new SATQDIMACSConverter(formula);
		System.out.println("QCIR representation");
		System.out.println(conj1.toQCir());
		System.out.println("QDIMACS original representation");
		System.out.println(converter.toQDimacs());
		System.out.println("QDIMACS new representation");
		System.out.println(converter.toQDimacsList());
		
		// with negative atoms
		Conjunction conj2 = new Conjunction("main");
		conj2.addSubformula(new Atom("a"));
		conj2.addSubformula(new Negation(new Atom("b")));
		conj2.addSubformula(new Negation(new Atom("c")));
		conj2.addSubformula(new Atom("d"));
		formula = new SatFormula(variables, conj2);
		converter = new SATQDIMACSConverter(formula);
		System.out.println("QCIR representation");
		System.out.println(conj2.toQCir());
		System.out.println("QDIMACS original representation");
		System.out.println(converter.toQDimacs());
		System.out.println("QDIMACS new representation");
		System.out.println(converter.toQDimacsList());
	}
}
