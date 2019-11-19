package test;

import java.util.ArrayList;
import java.util.List;

import generators.SATQDIMACSConverter;
import logic.pl.Atom;
import logic.pl.Disjunction;
import logic.pl.Negation;
import logic.pl.SatFormula;

public class DisjunctionQDIMACSTest {

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
		Disjunction disj1 = new Disjunction("main");
		disj1.addSubformula(new Atom("a"));
		disj1.addSubformula(new Atom("b"));
		disj1.addSubformula(new Atom("c"));
		disj1.addSubformula(new Atom("d"));
		formula = new SatFormula(variables, disj1);
		converter = new SATQDIMACSConverter(formula);
		System.out.println(disj1.toQCir());
		System.out.println(converter.toQDimacs());
		
		// with negative atoms
		Disjunction disj2 = new Disjunction("main");
		disj2.addSubformula(new Atom("a"));
		disj2.addSubformula(new Negation(new Atom("b")));
		disj2.addSubformula(new Negation(new Atom("c")));
		disj2.addSubformula(new Atom("d"));
		formula = new SatFormula(variables, disj2);
		converter = new SATQDIMACSConverter(formula);
		System.out.println(disj2.toQCir());
		System.out.println(converter.toQDimacs());
	}
}
