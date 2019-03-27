package model;

import java.util.*;

import parser.CAFParser;

/**
 * This class is designed to model a CAF
 * It is not a classical Argument Framework.
 * We will store the arguments (according to their type)
 * We will store the attacks (according to their type)
 * We need as well a set of arguments to protect 
 */
public class ControlAF {

	// structures to store the arguments and
	// the attacks (classed by type)
	protected Map<CArgument.Type,Set<CArgument>> arguments;
	protected Map<CAttack.Type,Set<CAttack>> attacks;
	
	// structure to store the set of arguments to protect
	protected Set<CArgument> target;
	
	/**
	 * We initialize all data structures
	 */
	public ControlAF() {
		arguments = new HashMap<CArgument.Type,Set<CArgument>>();
		Set<CArgument> fixed = new HashSet<CArgument>();
		Set<CArgument> uncertain = new HashSet<CArgument>();
		Set<CArgument> control = new HashSet<CArgument>();
		
		arguments.put(CArgument.Type.FIXED, fixed);
		arguments.put(CArgument.Type.UNCERTAIN, uncertain);
		arguments.put(CArgument.Type.CONTROL, control);
		
		attacks = new HashMap<CAttack.Type,Set<CAttack>>();
		Set<CAttack> fixedA = new HashSet<CAttack>();
		Set<CAttack> uncertainA = new HashSet<CAttack>();
		Set<CAttack> undirectedA = new HashSet<CAttack>();
		Set<CAttack> controlA = new HashSet<CAttack>();
		
		attacks.put(CAttack.Type.CERTAIN, fixedA);
		attacks.put(CAttack.Type.UNCERTAIN, uncertainA);
		attacks.put(CAttack.Type.UNDIRECTED, undirectedA);
		attacks.put(CAttack.Type.CONTROL, controlA);
		
		target = new HashSet<CArgument>();
	}
	
	/**
	 * Add an argument
	 * @param arg
	 */
	public void addArgument(CArgument arg) {
		CArgument.Type t = arg.getType();
		Set<CArgument> args = arguments.get(t);
		args.add(arg);
	}
	
	/**
	 * Add an attack 
	 * @param att
	 */
	public void addAttack(CAttack att) {
		CAttack.Type t = att.getType();
		Set<CAttack> atts = attacks.get(t);
		atts.add(att);
	}
	
	/**
	 * add an argument to be protected
	 * here we first check that it really belong to the 
	 * fixed argument list
	 * @param arg
	 */
	public void addTarget(CArgument arg) {
		Set<CArgument> fixedA = arguments.get(CArgument.Type.FIXED);
		Iterator<CArgument> it = fixedA.iterator();
		boolean present = false;
		while(it.hasNext()) {
			CArgument current = it.next();
			if(current.getName().equals(arg.getName())) {
				target.add(current);
				present = true;
				break;
			}
		}
		if(!present) {
			throw new UnknownArgumentError("the argument " + arg.getName() + " is not present in the fixed part of the CAF");
		}
	}
	
	/**
	 * true if arg is a fixed argument
	 * @param arg
	 * @return
	 */
	public boolean isFixedArgument(Argument arg) {
		return this.getArgumentsByType(CArgument.Type.FIXED).contains(arg);
	}

	/**
	 * true if arg is an uncertain argument
	 * @param arg
	 * @return
	 */
	public boolean isUncertainArgument(Argument arg) {
		return this.getArgumentsByType(CArgument.Type.UNCERTAIN).contains(arg);
	}

	/**
	 * true if arg is a control argument
	 * @param arg
	 * @return
	 */
	public boolean isControlArgument(Argument arg) {
		return this.getArgumentsByType(CArgument.Type.CONTROL).contains(arg);
	}

	/**
	 * true if arg is an argument of the CAF
	 * regardless of its type
	 * @param arg
	 * @return
	 */
	public boolean containsArgument(Argument arg) {
		return isFixedArgument(arg) || isUncertainArgument(arg) || isControlArgument(arg);
	}
	
	/**
	 * returns the set of arguments by type
	 * @param type
	 * @return
	 */
	public Set<CArgument> getArgumentsByType(CArgument.Type type) {
		return arguments.get(type);
	}

	/**
	 * returns all arguments of the CAF (regardless of the type)
	 * CONTROL
	 * FIXED
	 * UNCERTAIN
	 * @return
	 */
	public Set<CArgument> getAllArguments() {
		Set<CArgument> result = new HashSet<CArgument>();
		result.addAll(this.getArgumentsByType(CArgument.Type.CONTROL));
		result.addAll(this.getArgumentsByType(CArgument.Type.FIXED));
		result.addAll(this.getArgumentsByType(CArgument.Type.UNCERTAIN));
		return result;
	}
	
	/**
	 * returns an argument of a given type by its name, null if it can not be found
	 * @param type
	 * @param name
	 * @return
	 */
	public CArgument getArgumentByNameAndType(CArgument.Type type, String name) { 
		Set<CArgument> args = this.getArgumentsByType(type);
		Iterator<CArgument> iter = args.iterator();
		while(iter.hasNext()) {
			CArgument current = iter.next();
			if(current.getName().equals(name)) {
				return current;
			}
		}
		return null;
	}
	
	/**
	 * returns an argument by its name (any kind of argument), null if it cannot be found
	 * @param name
	 * @return
	 */
	public CArgument getArgumentByName(String name) {
		CArgument result = null;
		// search in the fixed arguments
		result = this.getArgumentByNameAndType(CArgument.Type.FIXED, name);
		if(result != null) {
			return result;
		}
		// search in the uncertain arguments
		result = this.getArgumentByNameAndType(CArgument.Type.UNCERTAIN, name);
		if(result != null) {
			return result;
		}
		// search in the control arguments
		result = this.getArgumentByNameAndType(CArgument.Type.CONTROL, name);
		if(result != null) {
			return result;
		}
		return null;
	}
	
	/**
	 * returns the set of attacks by type
	 * @param type
	 * @return
	 */
	public Set<CAttack> getAttacksByType(CAttack.Type type) {
		return attacks.get(type);
	}
	
	/**
	 * returns true if the attack is present with the specified type
	 * @param att
	 * @param type
	 * @return
	 */
	public boolean isAttack(Attack att, CAttack.Type type) {
		Set<CAttack> attacks = this.getAttacksByType(type);
		Argument from_att = att.getFrom();
		Argument to_att = att.getTo();
		Argument from_other = null;
		Argument to_other = null;
		
		if(type != CAttack.Type.UNDIRECTED) {
			for(Attack other : attacks) {
				from_other = other.getFrom();
				to_other = other.getTo();
				if(from_att.getName().equals(from_other.getName()) && to_att.getName().equals(to_other.getName())) {
					return true;
				}
			}
		} else {
			for(Attack other : attacks) {
				from_other = other.getFrom();
				to_other = other.getTo();
				if(from_att.getName().equals(from_other.getName()) && to_att.getName().equals(to_other.getName())) {
					return true;
				}
				// for undirected attack can be saved in the reverse side. Still it is a present attack 
				if(from_att.getName().equals(to_other.getName()) && to_att.getName().equals(from_other.getName())) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * true if att is a fixed attack
	 * @param att
	 * @return
	 */
	public boolean isFixedAttack(Attack att) {
		return this.isAttack(att, CAttack.Type.CERTAIN);
	}

	/**
	 * true if att is an uncertain attack
	 * @param att
	 * @return
	 */
	public boolean isUncertainAttack(Attack att) {
		return this.isAttack(att, CAttack.Type.UNCERTAIN);
	}

	/**
	 * true if att is an undirected attack
	 * @param att
	 * @return
	 */
	public boolean isUndirectedAttack(Attack att) {
		return this.isAttack(att, CAttack.Type.UNDIRECTED);
	}

	/**
	 * true if att is a control attack
	 * @param att
	 * @return
	 */
	public boolean isControlAttack(Attack att) {
		return this.isAttack(att, CAttack.Type.CONTROL);
	}
	
	/**
	 * returns true if the attack att is contained in the CAF
	 * regardless of its type
	 * returns false else
	 * @param att
	 * @return
	 */
	public boolean containsAttack(Attack att) {
		return isFixedAttack(att) || isUncertainAttack(att) || isUndirectedAttack(att) || isControlAttack(att);
	}

	
	/**
	 * Builds the list of pairs of arguments that are NOT attacks in the CAF.
	 * 
	 * @return
	 */
	public List<Attack> getNonAttacks() {
		List<Attack> res = new ArrayList<Attack>();
		for (Argument from : this.getAllArguments()) {
			for (Argument to : this.getAllArguments()) {
				Attack att = new Attack(from, to);
				if (!containsAttack(att)) {
					res.add(att);
				} 
			}
		}
		return res;
	}
	
	/**
	 * returns the set of arguments to protect
	 * @return
	 */
	public Set<CArgument> getTarget() {
		return target;
	}
	
	/**
	 * Returns true if no arguments from AU U AF are attacking argumets from AC
	 */
	public boolean isHardestRootCompletionCompatible() {
		Set<CAttack> control = this.getAttacksByType(CAttack.Type.CONTROL);
		for(CAttack att : control) {
			CArgument from = this.getArgumentByName(att.getFrom().getName());
			if(from.getType() == CArgument.Type.FIXED || from.getType() == CArgument.Type.UNCERTAIN) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * According to a given completion
	 * for a given argument (any type), returns the set of arguments attacking this argument via a control attack
	 * if the argument is a control argument, the attackers can be in AC, AU, AF 
	 * but for AU, argument must belong to completion
	 * if the argument is not a control argument, the attackers can only be in AC
	 * @param a
	 * @return
	 */
	public Set<Argument> getControlAttackers(Argument a, ArgumentFramework completion) {
		Set<Argument> result = new HashSet<Argument>();
		Set<CAttack> attacks = this.getAttacksByType(CAttack.Type.CONTROL);
		
		for(CAttack current : attacks) {
			CArgument from = this.getArgumentByName(current.getFrom().getName());
			//CArgument to = this.getArgumentByName(current.getTo().getName());
			if(current.getTo().equals(a)) {
				if(completion.containsArgument(from)|| from.getType() == CArgument.Type.CONTROL) {
					result.add(current.getFrom());
				}
			}
		}
		return result;
	}
	
	/**
	 * For a given root completion of the CAF and an argument belonging to the completion
	 * (therefore not a control argument)
	 * returns all the attackers of that argument (including the control arguments)
	 * @param completion
	 * @param a
	 * @return
	 */
	public Set<Argument> getArgumentAttackers(ArgumentFramework completion, Argument a) {
		//check if a belongs to completion
		// returns UnknownArgumentException else
		if(!completion.containsArgument(a)) {
			throw new UnknownArgumentError("the argument " + a.getName() + " does not belong to the completion"); 
		}
		// attackers from the root completion
		Set<Argument> internal = completion.getAttackingArguments(a);
		// attackers from AC (since a does not belong to AC)
		Set<Argument> control = this.getControlAttackers(a, completion);
		
		internal.addAll(control);
		
		return internal;
	}
	
	/**
	 * Calculates free uncertain arguments for minimal move purposes as in Definition8 of the report
	 * iterate over uncertain attacks and check if it is present in the af.
	 * If yes, removing From and To from the list of free arguments
	 * If no, keeping in the list the list
	 *
	 * iterate over undirected attacks and check if both sides are present in the af.
	 * If yes, removing From and To from the list of free arguments
	 * If no, keeping in the list the list
	 *
	 */
	public Set<CArgument> getFreeUncertainArguments(ArgumentFramework af) {
		// add all uncertain arguments to result
		Set<CArgument> result = new HashSet<CArgument>(this.getArgumentsByType(CArgument.Type.UNCERTAIN)); 
		Set<CAttack> uncertain = this.getAttacksByType(CAttack.Type.UNCERTAIN);
		Set<CAttack> undirected = this.getAttacksByType(CAttack.Type.UNDIRECTED);
		
		for(CAttack catt : uncertain) {
			if(af.containsAttack(catt)) {
				result.remove(catt.getFrom());
				result.remove(catt.getTo());
			}
		}
			
		for(CAttack catt : undirected) {
			CAttack reverse = new CAttack(catt.getTo(), catt.getFrom(), CAttack.Type.UNDIRECTED);
			if(af.containsAttack(catt) && af.containsAttack(reverse)) {
				result.remove(catt.getFrom());
				result.remove(catt.getTo());
			}
		}
		return result;
	}
	
	/**
	 * String representation of CAF according to apx file format
	 */
	public String toString() {
		String result = new String();
		// first the arguments
		Set<CArgument> fixed = this.getArgumentsByType(CArgument.Type.FIXED);
		Set<CArgument> uncertain = this.getArgumentsByType(CArgument.Type.UNCERTAIN);
		Set<CArgument> control = this.getArgumentsByType(CArgument.Type.CONTROL);
		Iterator<CArgument> iterf = fixed.iterator();
		Iterator<CArgument> iteru = uncertain.iterator();
		Iterator<CArgument> iterc = control.iterator();
		CArgument current;
		// fixed arguments
		while(iterf.hasNext()) {
			current = iterf.next();
			result = result + CAFParser.FIXED_ARG + "(" + current.getName() + ").";
			result = String.format(result + "%n");
		}
		// uncertain arguments
		while(iteru.hasNext()) {
			current = iteru.next();
			result = result + CAFParser.UNCERTAIN_ARG + "(" + current.getName() + ").";
			result = String.format(result + "%n");
		}
		//control arguments
		while(iterc.hasNext()) {
			current = iterc.next();
			result = result + CAFParser.CONTROL_ARG + "(" + current.getName() + ").";
			result = String.format(result + "%n");
		}
		
		// Then the attacks
		Set<CAttack> fixedA = attacks.get(CAttack.Type.CERTAIN);
		Set<CAttack> uncertainA = attacks.get(CAttack.Type.UNCERTAIN);
		Set<CAttack> undirectedA = attacks.get(CAttack.Type.UNDIRECTED);
		Set<CAttack> controlA = attacks.get(CAttack.Type.CONTROL);
		Iterator<CAttack> iterFA = fixedA.iterator();
		Iterator<CAttack> iterUA = uncertainA.iterator();
		Iterator<CAttack> iterDA = undirectedA.iterator();
		Iterator<CAttack> iterCA = controlA.iterator();
		CAttack currentA;
		while(iterFA.hasNext()) {
			currentA = iterFA.next();
			result = result + CAFParser.FIXED_ATT + "(" + currentA.getFrom().getName() + "," + currentA.getTo().getName() + ").";
			result = String.format(result + "%n");
		}
		while(iterUA.hasNext()) {
			currentA = iterUA.next();
			result = result + CAFParser.UNCERTAIN_ATT + "(" + currentA.getFrom().getName() + "," + currentA.getTo().getName() + ").";
			result = String.format(result + "%n");
		}
		while(iterDA.hasNext()) {
			currentA = iterDA.next();
			result = result + CAFParser.UNDIRECTED_ATT + "(" + currentA.getFrom().getName() + "," + currentA.getTo().getName() + ").";
			result = String.format(result + "%n");
		}
		while(iterCA.hasNext()) {
			currentA = iterCA.next();
			result = result + CAFParser.CONTROL_ATT + "(" + currentA.getFrom().getName() + "," + currentA.getTo().getName() + ").";
			result = String.format(result + "%n");
		}
		
		Iterator<CArgument> iterP = target.iterator();
		while(iterP.hasNext()) {
			current = iterP.next();
			result = result + CAFParser.TARGET + "(" + current.getName() + ").";
			result = String.format(result + "%n");
		}
		return result;
	}
}
