package generators;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import model.ControlAF;
import model.Argument;
import model.Attack;
import model.CArgument;
import model.CAttack;
import logic.pl.Atom;
import logic.pl.Conjunction;
import logic.pl.Disjunction;
import logic.pl.Equivalence;
import logic.pl.Formula;
import logic.pl.Implication;
import logic.pl.Negation;
import logic.qbf.Exists;
import logic.qbf.ForAll;
import logic.qbf.Free;
import logic.qbf.QBFFormula;
import logic.qbf.Quantifier;

/**
 * From a CAF returns the QBF formula attached 
 * once solved, this QBF formula gives the control configurations
 * @author Fabrice
 *
 */
public class StrongQBFEncoder extends ControllabilityEncoder {
	
	public StrongQBFEncoder(ControlAF instance) {
		this.instance = instance;
	}

	/**
	 * encodes according to the type:
	 *  ControllabilityEncoder.CREDULOUS
	 *  or
	 *  ControllabilityEncoder.SKEPTICAL
	 */
	@Override
	public QBFFormula encode(int type) {
		
		List<Quantifier> quantifiers = encodeQuantifiers();
		
		Formula semantics = encodeSemantics();
		
		Formula structure = encodeStructure();
		
		Conjunction phiStCr = new Conjunction("phiStCr");
		phiStCr.addSubformula(structure);
		phiStCr.addSubformula(semantics);
		phiStCr.addSubformula(encodeTarget());

		Formula targetThen = encodeTarget();
		Conjunction phiStIf = new Conjunction("phiStIf");
		phiStIf.addSubformula(structure);
		phiStIf.addSubformula(semantics);
		Implication phiStSk = new Implication("phiStSk", phiStIf, targetThen);
		
		// this is when undirected attacks are present
		// in case there are none we will have no use of excludedValues
		Formula excludedValues = encodeExcludedValues();
		Set<Atom> atoms = excludedValues.getVariables();
		
		Disjunction main = new Disjunction("main");
		if(type == ControllabilityEncoder.CREDULOUS) {
			main.addSubformula(phiStCr);
		}	else {
			main.addSubformula(phiStSk);
		}
	
		// if excludedValues is not empty, we add it
		// else it is not added
		if(atoms.size()>0) {
			main.addSubformula(excludedValues);
		}
		return new QBFFormula(quantifiers, main);
	}

	/**
	 * Encodes the target as the conjunction of atoms corresponding to the
	 * arguments.
	 * 
	 * @return
	 */
	private Formula encodeTarget() {
		Conjunction target = new Conjunction("target");
		for (Argument arg : instance.getTarget()) {
			target.addSubformula(new Atom("acc_" + arg.getName()));
		}
		return target;
	}

	/**
	 * Encodes the excluded value for the variables corresponding to undirected
	 * conflicts.
	 * 
	 * @return
	 */
	private Formula encodeExcludedValues() {
		Disjunction excluded = new Disjunction("excluded");
		for (Attack att : instance.getAttacksByType(CAttack.Type.UNDIRECTED)) {
			Argument from = att.getFrom();
			Argument to = att.getTo();
			Conjunction exclConj = new Conjunction("excl_" + from.getName() + "_" + to.getName());
			exclConj.addSubformula(new Negation(new Atom("att_" + from.getName() + "_" + to.getName())));
			exclConj.addSubformula(new Negation(new Atom("att_" + to.getName() + "_" + from.getName())));
			excluded.addSubformula(exclConj);
		}
		return excluded;
	}

	/**
	 * Encodes the free variables
	 */
	private Free encodeFreeVariables() {
		Free result = new Free();

		// fixed attacks
		for (Attack att : instance.getAttacksByType(CAttack.Type.CERTAIN)) {
			result.addVariable(new Atom("att_" + att.getFrom().getName() + "_" + att.getTo().getName()));
		}

		// control attacks
		for (Attack att : instance.getAttacksByType(CAttack.Type.CONTROL)) {
			result.addVariable(new Atom("att_" + att.getFrom().getName() + "_" + att.getTo().getName()));
		}

		// negation of absent attacks
		for (Attack att : instance.getNonAttacks()) {
			result.addVariable(new Atom("att_" + att.getFrom().getName() + "_" + att.getTo().getName()));
		}
		return result;
	}
	
	/**
	 * Encodes the prefix of the QBF formula.
	 * 
	 * @return
	 */
	private List<Quantifier> encodeQuantifiers() {
		List<Quantifier> res = new ArrayList<Quantifier>();

		// free variables must be declared in QCIR
		// free must the first quantifier (before forall and exists)
		Free freeConfig = this.encodeFreeVariables();
		res.add(freeConfig);
		
		Exists existsConfig = new Exists();
		// control arguments
		for (Argument arg : instance.getArgumentsByType(CArgument.Type.CONTROL)) {
			existsConfig.addVariable(new Atom("on_" + arg.getName()));
		}
		res.add(existsConfig);

		ForAll forall = new ForAll();
		// uncertain arguments
		for (Argument arg : instance.getArgumentsByType(CArgument.Type.UNCERTAIN)) {
			forall.addVariable(new Atom("on_" + arg.getName()));
		}
		// uncertain attacks
		for (Attack att : instance.getAttacksByType(CAttack.Type.UNCERTAIN)) {
			forall.addVariable(new Atom("att_" + att.getFrom().getName() + "_" + att.getTo().getName()));
		}
		// undirected attacks
		for (Attack att : instance.getAttacksByType(CAttack.Type.UNDIRECTED)) {
			forall.addVariable(new Atom("att_" + att.getFrom().getName() + "_" + att.getTo().getName()));
			forall.addVariable(new Atom("att_" + att.getTo().getName() + "_" + att.getFrom().getName()));
		}
		res.add(forall);

		Exists existsExtension = new Exists();
		// all arguments
		for (Argument arg : instance.getAllArguments()) {
			existsExtension.addVariable(new Atom("acc_" + arg.getName()));
		}
		res.add(existsExtension);

		return res;
	}

	/**
	 * Encodes the structure of the CAF.
	 * 
	 * @return
	 */
	private Formula encodeStructure() {
		Conjunction structure = new Conjunction("structure");

		// fixed attacks
		for (Attack att : instance.getAttacksByType(CAttack.Type.CERTAIN)) {
			structure.addSubformula(new Atom("att_" + att.getFrom().getName() + "_" + att.getTo().getName()));
		}

		// control attacks
		for (Attack att : instance.getAttacksByType(CAttack.Type.CONTROL)) {
			structure.addSubformula(new Atom("att_" + att.getFrom().getName() + "_" + att.getTo().getName()));
		}

		// undirected attacks
		for (Attack att : instance.getAttacksByType(CAttack.Type.UNDIRECTED)) {
			Disjunction disj = new Disjunction("disj_" + att.getFrom().getName() + "_" + att.getTo().getName());
			disj.addSubformula(new Atom("att_" + att.getFrom().getName() + "_" + att.getTo().getName()));
			disj.addSubformula(new Atom("att_" + att.getTo().getName() + "_" + att.getFrom().getName()));
			structure.addSubformula(disj);
		}
		// we say nothing about UNCERTAIN attacks
		
		// negation of absent attacks
		for (Attack att : instance.getNonAttacks()) {
			structure.addSubformula(new Negation(new Atom("att_" + att.getFrom().getName() + "_" + att.getTo().getName())));
		}

		return structure;
	}

	/**
	 * Encodes the relation between the semantics and the structure.
	 * 
	 * @return
	 */
	private Formula encodeSemantics() {
		Conjunction semantics = new Conjunction("semantics");

		Conjunction fixedArgs = new Conjunction("fixed");
		for (CArgument arg : instance.getArgumentsByType(CArgument.Type.FIXED)) {
			Conjunction conjImpl = new Conjunction("conjImpl_" + arg.getName());
			for (CArgument arg2 : instance.getAllArguments()) {
				// direction acc_y <=> (att_x_y => -acc_x)
				Implication impl = new Implication("impl_" + arg.getName() + "_" + arg2.getName(),
						new Atom("att_" + arg2.getName() + "_" + arg.getName()),
						new Negation(new Atom("acc_" + arg2.getName())));
				conjImpl.addSubformula(impl);
			}

			fixedArgs.addSubformula(
					new Equivalence("equiv_" + arg.getName(), new Atom("acc_" + arg.getName()), conjImpl));
		}
		semantics.addSubformula(fixedArgs);
		
		Conjunction controlArgs = new Conjunction("control");
		for (CArgument arg : instance.getArgumentsByType(CArgument.Type.CONTROL)) {
			Conjunction conjImpl = new Conjunction("conjImpl_" + arg.getName());
			for (CArgument arg2 : instance.getAllArguments()) {
				Implication impl = new Implication("impl_" + arg.getName() + "_" + arg2.getName(),
						new Atom("att_" + arg2.getName() + "_" + arg.getName()),
						new Negation(new Atom("acc_" + arg2.getName())));
				conjImpl.addSubformula(impl);
			}
			conjImpl.addSubformula(new Atom("on_" + arg.getName()));

			controlArgs.addSubformula(
					new Equivalence("equiv_" + arg.getName(), new Atom("acc_" + arg.getName()), conjImpl));
		}
		semantics.addSubformula(controlArgs);
		
		// fix me: if no uncertain arguments not add uncertainArgs to the formula
		Conjunction uncertainArgs = new Conjunction("uncertain");
		for (CArgument arg : instance.getArgumentsByType(CArgument.Type.UNCERTAIN)) {
			Conjunction conjImpl = new Conjunction("conjImpl_" + arg.getName());
			for (CArgument arg2 : instance.getAllArguments()) {
				Implication impl = new Implication("impl_" + arg.getName() + "_" + arg2.getName(),
						new Atom("att_" + arg2.getName() + "_" + arg.getName()),
						new Negation(new Atom("acc_" + arg2.getName())));
				conjImpl.addSubformula(impl);
			}
			conjImpl.addSubformula(new Atom("on_" + arg.getName()));

			uncertainArgs.addSubformula(
					new Equivalence("equiv_" + arg.getName(), new Atom("acc_" + arg.getName()), conjImpl));
		}
		semantics.addSubformula(uncertainArgs);

		return semantics;
	}

}
