package generators;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import logic.pl.*;
import model.Argument;
import model.ArgumentFramework;
import model.CArgument;
import model.ControlAF;

public class StrongSATEncoder {

	protected ArgumentFramework instance;
	protected ControlAF CAF;

	public StrongSATEncoder(ArgumentFramework instance, ControlAF CAF) {
		this.instance = instance;
		this.CAF = CAF;
	}

	public SatFormula encode(int type) {
		List<String> variables = getVariables();

		Conjunction formula = new Conjunction("main");
		Formula target = encodeTarget();
		Formula control = encodeControl();
		Formula conflictFree = encodeConflictFree();
		Formula controlConflictFree = encodeControlConflictFree();

		// target cannot be empty
		formula.addSubformula(target);
		
		// if no control argument, control is null
		if(control != null) {
			formula.addSubformula(control);
		}
		
		// cannot be null since target is not empty
		formula.addSubformula(conflictFree);
		
		// if no control argument, control is null
		if(controlConflictFree != null) {
			formula.addSubformula(controlConflictFree);
		}
		
		return new SatFormula(variables, formula);
	}

	public List<String> getVariables() {
		List<String> result = new ArrayList<String>();
		for (Argument a : instance.getAllArguments()) {
			result.add("acc_"+a.getName());
		}
		for (Argument a : CAF.getArgumentsByType(CArgument.Type.CONTROL)) {
			result.add("acc_" + a.getName());
			result.add("on_" + a.getName());
		}
		return result;
	}

	/**
	 * for each target element, it must be accepted
	 * @return
	 */
	public Formula encodeTarget() {
		Conjunction result = new Conjunction("target");
		Set<CArgument> T = CAF.getTarget();
		for(CArgument t : T) {
			result.addSubformula(new Atom("acc_" + t.getName()));
		}
		return result;
	}

	/**
	 * for each control argument on is equivalent to accepted
	 * returns null if no control arguments present
	 * @return
	 */
	public Formula encodeControl() {
		Conjunction result = new Conjunction("control");
		Set<CArgument> controlArgs = CAF.getArgumentsByType(CArgument.Type.CONTROL);
		if(controlArgs.isEmpty()) {
			return null;
		}
		for(CArgument a : controlArgs) {
			String name = "on_" + a.getName() + "_" + "acc_" + a.getName();
			Atom left = new Atom("on_" + a.getName());
			Atom right = new Atom("acc_" + a.getName());
			Equivalence equiv = new Equivalence(name, left, right);
			result.addSubformula(equiv);
		}
		return result;
	}

	/**
	 * encodes the conjunctions over all arguments of the instance
	 * for conflict freeness
	 * @return
	 */
	public Formula encodeConflictFree() {
		Conjunction result = new Conjunction("conflictfree");
		Set<Argument> args = this.instance.getAllArguments();
		for(Argument a : args) {
			result.addSubformula(this.clause(a));
		}
		return result;
	}

	/**
	 * the argument is accepted or it is not accepted (depending on its attackers)
	 * if there are no attackers it needs to be accepted
	 * so the not accepted part is null
	 * @param a
	 * @return
	 */
	protected Disjunction clause(Argument a) {
		Disjunction result = new Disjunction("clause_" + a.getName());
		Set<Argument> attackers = this.CAF.getArgumentAttackers(this.instance, a);
		Formula accepted = this.isAccepted(a, attackers);
		Formula notAccepted = this.isNotAccepted(a, attackers);
		result.addSubformula(accepted);
		if(notAccepted != null) {
			result.addSubformula(notAccepted);
		}
		return result;
	}

	/**
	 * all attackers are not accepted and the argument is accepted
	 * @param a
	 * @param attackers
	 * @return
	 */
	protected Conjunction isAccepted(Argument a, Set<Argument> attackers) {
		Conjunction result = new Conjunction("isAccepted_" + a.getName());
		Conjunction nonAttack = this.nonAttack(a, attackers);
		if(nonAttack != null) {
			result.addSubformula(nonAttack);
		}
		result.addSubformula(new Atom("acc_" + a.getName()));
		
		return result;
	}

	/**
	 * at least one attacker is accepted and the argument is not accepted
	 * if no attackers, returns null
	 * @param a
	 * @param attackers
	 * @return
	 */
	protected Conjunction isNotAccepted(Argument a, Set<Argument> attackers) {
		Conjunction result = new Conjunction("isNotAccepted_" + a.getName());
		Disjunction attack = this.attack(a, attackers);
		if(attack == null) {
			return null;
		}
		
		result.addSubformula(attack);
		result.addSubformula(new Negation(new Atom("acc_" + a.getName())));
		return result;
	}

	/**
	 * encodes the fact that no attacker of an argument is accepted
	 * and(not(accepted)) for all attackers of an argument
	 * if there are no attackers for a, returns null
	 * @param a
	 * @param attackers
	 * @return
	 */
	protected Conjunction nonAttack(Argument a, Set<Argument> attackers) {
		if(attackers.isEmpty()) {
			return null;
		}
		Conjunction nonAttack = new Conjunction("nonAttack_" + a.getName());
		for(Argument att : attackers) {
			nonAttack.addSubformula(new Negation(new Atom("acc_" + att.getName())));
		}
		return nonAttack;
	}

	/**
	 * encodes the fact that at least one attacker of an argument is accepted
	 * or(accepted) for all attackers of an argument
	 * if there are no attackers for a, returns null
	 * @param a
	 * @param attackers
	 * @return
	 */
	protected Disjunction attack(Argument a, Set<Argument> attackers) {
		if(attackers.isEmpty()) {
			return null;
		}
		Disjunction attack = new Disjunction("attack_" + a.getName());
		for(Argument att : attackers) {
			attack.addSubformula(new Atom("acc_" + att.getName()));
		}
		return attack;
	}

	/**
	 * checks for each control argument if it can be accepted
	 * returns null if no control arguments
	 * @return
	 */
	public Conjunction encodeControlConflictFree() {
		Conjunction result = new Conjunction("controlConflictfree");
		Set<CArgument> controlArgs = CAF.getArgumentsByType(CArgument.Type.CONTROL);
		if(controlArgs.isEmpty()) {
			return null;
		}
		int i = 0;
		for(CArgument a : controlArgs) {
			Set<Argument> attackers = this.CAF.getControlAttackers(a, this.instance);
			Formula controlNotAccepted = this.controlNotAccepted(a, attackers);
			if(controlNotAccepted != null) {
				result.addSubformula(controlNotAccepted);
				i++;
			}
		}
		// need to test if the conjunction is empty 
		// in case all control arguments are not attacked we set no constraint
		if(i == 0) { return null;}
		return result;	
	}

	/**
	 * for each control argument nonAttack or accepted
	 * @param c
	 * @param controlAttackers
	 * @return
	 */
	protected Disjunction controlNotAccepted(CArgument c, Set<Argument> controlAttackers) {
		Disjunction result = new Disjunction("controlNotAccepted_" + c.getName());
		Conjunction nonAttack = this.nonAttack(c, controlAttackers);
		if(nonAttack == null) {
			return null;
		}
		result.addSubformula(nonAttack);
		result.addSubformula(new Negation(new Atom("acc_" + c.getName())));
		return result;
	}

}

