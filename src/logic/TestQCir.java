package logic;

import java.util.ArrayList;
import java.util.List;

import logic.pl.Atom;
import logic.pl.Conjunction;
import logic.pl.Disjunction;
import logic.pl.Negation;
import logic.qbf.Exists;
import logic.qbf.ForAll;
import logic.qbf.QBFFormula;
import logic.qbf.Quantifier;

public class TestQCir {
	public static void main(String[] args) {
		Conjunction g1 = new Conjunction("g1");
		g1.addSubformula(new Atom("v1"));
		g1.addSubformula(new Atom("v2"));
		
		Conjunction g2 = new Conjunction("g2");
		g2.addSubformula(new Negation("",new Atom("v1")));
		g2.addSubformula(new Negation("",new Atom("v2")));
		g2.addSubformula(new Atom("v3"));
		
		Disjunction g3 = new Disjunction("g3");
		g3.addSubformula(g1);
		g3.addSubformula(g2);
		
		ForAll forall = new ForAll();
		forall.addVariable(new Atom("v1"));
		Exists exists = new Exists();
		exists.addVariable(new Atom("v2"));
		exists.addVariable(new Atom("v3"));
		
		List<Quantifier> quantifiers = new ArrayList<Quantifier>();
		quantifiers.add(forall);
		quantifiers.add(exists);
		QBFFormula formula = new QBFFormula(quantifiers, g3);
		System.out.println(formula);
	}
}
