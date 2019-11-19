package test;

import java.util.ArrayList;
import java.util.List;

import generators.SATQDIMACSConverter;
import logic.pl.Atom;
import logic.pl.Implication;
import logic.pl.Negation;
import logic.pl.SatFormula;

public class ImplicationQDIMACSTest {

	public static void main(String[] args) {
		// test implication
		// all positive atoms
		SATQDIMACSConverter converter = null;
		SatFormula formula = null;
		List<String> variables = new ArrayList<String>();
		variables.add("a");
		variables.add("b");
		Implication imp1 = new Implication("main", new Atom("a"), new Atom("b"));
		formula = new SatFormula(variables, imp1);
		converter = new SATQDIMACSConverter(formula);
		System.out.println(imp1.toQCir());
		System.out.println(converter.toQDimacs());
		
		// with negative atoms
		Implication imp2 = new Implication("main", new Negation(new Atom("a")), new Atom("b"));
		formula = new SatFormula(variables, imp2);
		converter = new SATQDIMACSConverter(formula);
		System.out.println(imp2.toQCir());
		System.out.println(converter.toQDimacs());
		
		Implication imp3 = new Implication("main", new Atom("a"), new Negation(new Atom("b")));
		formula = new SatFormula(variables, imp3);
		converter = new SATQDIMACSConverter(formula);
		System.out.println(imp3.toQCir());
		System.out.println(converter.toQDimacs());
		
		Implication imp4 = new Implication("main", new Negation(new Atom("a")), new Negation(new Atom("b")));
		formula = new SatFormula(variables, imp4);
		converter = new SATQDIMACSConverter(formula);
		System.out.println(imp4.toQCir());
		System.out.println(converter.toQDimacs());

	}
}
