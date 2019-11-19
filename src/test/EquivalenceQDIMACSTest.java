package test;

import java.util.ArrayList;
import java.util.List;

import generators.SATQDIMACSConverter;
import logic.pl.Atom;
import logic.pl.Equivalence;
import logic.pl.Negation;
import logic.pl.SatFormula;

public class EquivalenceQDIMACSTest {

	public static void main(String[] args) {
		// test conjunction
		// all positive atoms
		SATQDIMACSConverter converter = null;
		SatFormula formula = null;
		List<String> variables = new ArrayList<String>();
		variables.add("a");
		variables.add("b");
		Equivalence equiv1 = new Equivalence("main", new Atom("a"), new Atom("b"));
		formula = new SatFormula(variables, equiv1);
		converter = new SATQDIMACSConverter(formula);
		System.out.println(equiv1.toQCir());
		System.out.println(converter.toQDimacs());
		
		// with negative atoms
		Equivalence equiv2 = new Equivalence("main", new Negation(new Atom("a")), new Atom("b"));
		formula = new SatFormula(variables, equiv2);
		converter = new SATQDIMACSConverter(formula);
		System.out.println(equiv2.toQCir());
		System.out.println(converter.toQDimacs());
		
		Equivalence equiv3 = new Equivalence("main", new Atom("a"), new Negation(new Atom("b")));
		formula = new SatFormula(variables, equiv3);
		converter = new SATQDIMACSConverter(formula);
		System.out.println(equiv3.toQCir());
		System.out.println(converter.toQDimacs());
		
		Equivalence equiv4 = new Equivalence("main", new Negation(new Atom("a")), new Negation(new Atom("b")));
		formula = new SatFormula(variables, equiv4);
		converter = new SATQDIMACSConverter(formula);
		System.out.println(equiv4.toQCir());
		System.out.println(converter.toQDimacs());

	}
}
