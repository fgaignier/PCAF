package model;

import java.util.*;

/*
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
	protected Set<CArgument> toProtect;
	
	/*
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
		
		toProtect = new HashSet<CArgument>();
	}
	
	/*
	 * Add an argument 
	 */
	public void addArgument(CArgument a) {
		CArgument.Type t = a.getType();
		Set<CArgument> args = arguments.get(t);
		args.add(a);
	}
	
	/*
	 * Add an attack 
	 */
	public void addAttack(CAttack att) {
		CAttack.Type t = att.getType();
		Set<CAttack> atts = attacks.get(t);
		atts.add(att);
	}
	
	/*
	 * add an argument to be protected
	 * here we first check that it really belong to the 
	 * fixed argument list
	 */
	public void addProtectedArgument(CArgument arg) {
		Set<CArgument> fixedA = arguments.get(CArgument.Type.FIXED);
		Iterator<CArgument> it = fixedA.iterator();
		boolean present = false;
		while(it.hasNext()) {
			CArgument current = it.next();
			if(current.getName().equals(arg.getName())) {
				toProtect.add(current);
				present = true;
				break;
			}
		}
		if(!present) {
			throw new UnknownArgumentError("the argument " + arg.getName() + " is not present in the fixed part of the CAF");
		}
	}
	
	/*
	 * returns the set of arguments by type
	 */
	public Set<CArgument> getArgumentsByType(CArgument.Type type) {
		return arguments.get(type);
	}

	/*
	 * returns an argument of a given type by its name, null if it can not be found
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
	
	/*
	 * returns an argument by its name (any kind of argument), null if it cannot be found
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
	
	/*
	 * returns the set of attacks by type
	 */
	public Set<CAttack> getAttacksByType(CAttack.Type type) {
		return attacks.get(type);
	}
	
	/*
	 * returns the set of arguments to protect
	 */
	public Set<CArgument> getArgumentsToProtect() {
		return toProtect;
	}
	
	/*
	 * for a given argument (any type), returns the set of arguments attacking this argument via a control attack
	 * if the argument is a control argument, the attackers can be in AC, AU, AF
	 * if the argument is not a control argument, the attackers can only be in AC
	 */
	public Set<Argument> getControlAttackers(Argument a) {
		Set<Argument> result = new HashSet<Argument>();
		Set<CAttack> attacks = this.getAttacksByType(CAttack.Type.CONTROL);
		Iterator<CAttack> iter = attacks.iterator();
		while(iter.hasNext()) {
			CAttack current = iter.next();
			if(current.getTo().equals(a)) {
				result.add(current.getFrom());
			}
		}
		return result;
	}
	
	/*
	 * For a given root completion of the CAF and an argument belonging to the completion
	 * (therefore not a control argument)
	 * returns all the attackers of that argument (including the control arguments)
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
		Set<Argument> control = this.getControlAttackers(a);
		
		internal.addAll(control);
		
		return internal;
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 * String representation of CAF according to import file format
	 */
	public String toString() {
		String result = new String();
		// first the arguments
		Set<CArgument> fixed = arguments.get(CArgument.Type.FIXED);
		Set<CArgument> uncertain = arguments.get(CArgument.Type.UNCERTAIN);
		Set<CArgument> control = arguments.get(CArgument.Type.CONTROL);
		Iterator<CArgument> iterf = fixed.iterator();
		Iterator<CArgument> iteru = uncertain.iterator();
		Iterator<CArgument> iterc = control.iterator();
		CArgument current;
		// fixed arguments
		while(iterf.hasNext()) {
			current = iterf.next();
			result = result + "f_arg(" + current.getName() + ").";
			result = String.format(result + "%n");
		}
		// uncertain arguments
		while(iteru.hasNext()) {
			current = iteru.next();
			result = result + "u_arg(" + current.getName() + ").";
			result = String.format(result + "%n");
		}
		//control arguments
		while(iterc.hasNext()) {
			current = iterc.next();
			result = result + "c_arg(" + current.getName() + ").";
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
			result = result + "att(" + currentA.getFrom().getName() + "," + currentA.getTo().getName() + ").";
			result = String.format(result + "%n");
		}
		while(iterUA.hasNext()) {
			currentA = iterUA.next();
			result = result + "u_att(" + currentA.getFrom().getName() + "," + currentA.getTo().getName() + ").";
			result = String.format(result + "%n");
		}
		while(iterDA.hasNext()) {
			currentA = iterDA.next();
			result = result + "ud_att(" + currentA.getFrom().getName() + "," + currentA.getTo().getName() + ").";
			result = String.format(result + "%n");
		}
		while(iterCA.hasNext()) {
			currentA = iterCA.next();
			result = result + "c_att(" + currentA.getFrom().getName() + "," + currentA.getTo().getName() + ").";
			result = String.format(result + "%n");
		}
		
		Iterator<CArgument> iterP = toProtect.iterator();
		while(iterP.hasNext()) {
			current = iterP.next();
			result = result + "target(" + current.getName() + ").";
			result = String.format(result + "%n");
		}
		return result;
	}
}
