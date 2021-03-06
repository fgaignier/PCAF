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
	 * sets the target for the CAF
	 * replacing the previous target entirely
	 * @param target
	 */
	public void setTarget(Set<CArgument> target) {
		this.target = target;
	}
	
	/**
	 * returns the set of arguments to protect
	 * @return target
	 */
	public Set<CArgument> getTarget() {
		return target;
	}
	
	/**
	 * returns the size of the target
	 * @return target size
	 */
	public int getTargetSize() {
		return this.target.size();
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
	 * given an argument, returns the set of potential fixed arguments attacking this argument
	 * even for uncertain attacks
	 * @param a the argument considered
	 * @return a set of fixed arguments
	 */
	
	public Set<CArgument> getPotentialFixedAttackers(CArgument a) {
		Set<CArgument> result = new HashSet<CArgument>();
		Set<CArgument> fixed = this.getArgumentsByType(CArgument.Type.FIXED);
		for(CArgument other : fixed) {
			Attack att = new Attack(other, a);
			if(this.containsAttack(att)) {
				result.add(other);
			}
		}
		return result;
	}

	/**
	 * builds the AF from a root completion and a control entity cc:
	 *  adding all the control arguments in cc plus all the possible control attacks (the ones
	 * for which both arguments are present)
	 * @param cc
	 * @return
	 */
	public ArgumentFramework buildAF(ArgumentFramework completion, StableControlConfiguration cc) {
		//clone the completion
		ArgumentFramework result = completion.clone();
		// add the control arguments
		for(CArgument c : cc.getOnControl()) {
			result.addArgument(c);
		}
		// add all the possible control attacks
		// if one argument is missing, we ignore the attack
		Set<CAttack> control = this.getAttacksByType(CAttack.Type.CONTROL);
		for(CAttack att : control) {
			try {
				result.addAttack(att);
			} catch (UnknownArgumentError e) {
			//	System.out.println(e.getMessage());
			}
		}
		return result;
	}
	
	/**
	 * print some indications about the CAF:
	 * number of fixed arguments
	 * number of uncertain arguments
	 * number of control arguments
	 * number of fixed attacks
	 * number of uncertain attacks
	 * number of uncertain attacks
	 * number of undirected attacks
	 */
	public void summary() {
		StringBuffer result = new StringBuffer();
		// arguments
		int fixedArgs = this.getArgumentsByType(CArgument.Type.FIXED).size();
		int uncertainArgs = this.getArgumentsByType(CArgument.Type.UNCERTAIN).size();
		int controlArgs = this.getArgumentsByType(CArgument.Type.CONTROL).size();
		int totalArgs = fixedArgs + uncertainArgs + controlArgs;
		// attacks
		int fixedAtts = this.getAttacksByType(CAttack.Type.CERTAIN).size();
		int uncertainAtts = this.getAttacksByType(CAttack.Type.UNCERTAIN).size();
		int undirectedAtts = this.getAttacksByType(CAttack.Type.UNDIRECTED).size();
		int controlAtts = this.getAttacksByType(CAttack.Type.CONTROL).size();
		int totalAtts = fixedAtts + uncertainAtts + undirectedAtts + controlAtts;
		
		result.append("number of fixed arguments: " + fixedArgs);
		result.append(System.getProperty("line.separator"));
		result.append("number of uncertain arguments: " + uncertainArgs);
		result.append(System.getProperty("line.separator"));
		result.append("number of control arguments: " + controlArgs);
		result.append(System.getProperty("line.separator"));
		
		result.append("total number of arguments: " + totalArgs);
		result.append(System.getProperty("line.separator"));
		
		result.append("number of fixed attacks: " + fixedAtts);
		result.append(System.getProperty("line.separator"));
		result.append("number of uncertain attacks: " + uncertainAtts);
		result.append(System.getProperty("line.separator"));
		result.append("number of undirected attacks: " + undirectedAtts);
		result.append(System.getProperty("line.separator"));
		result.append("number of control attacks: " + controlAtts);
		result.append(System.getProperty("line.separator"));
		
		result.append("total number of attacks: " + totalAtts);
		result.append(System.getProperty("line.separator"));
		
		result.append("######################");
		result.append(System.getProperty("line.separator"));
		result.append("target size: " + this.getTargetSize());
		System.out.println(result.toString());
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
