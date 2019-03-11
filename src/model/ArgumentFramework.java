package model;

import java.util.*;

/*
 * Argument Framework Dung style.
 * Simply a graph represented as a HashMap(Argument, attacked)
 * And another graph represented as a HashMap(Argument, attackers)
 * In order to retrieve in time O(1) attacking arguments and attacked arguments for a given argument
 * First all arguments must be added to the AF
 * Then attacks can be added
 */
public class ArgumentFramework {

	public static int BY_RECEIVED_ATT = 0;
	public static int BY_EMITTING_ATT = 1;
	
	// neighbors are arguments attacked
	protected Map<Argument, Set<Argument>> graph_attacked;
	// neighbors are arguments attacking
	protected Map<Argument, Set<Argument>> graph_attacking;
	
	public ArgumentFramework() {
		 graph_attacked = new HashMap<Argument, Set<Argument>>();
		 graph_attacking = new HashMap<Argument, Set<Argument>>();
	}
	
	/*
	 * clone an AF (never duplicates the arguments)
	 */
	public ArgumentFramework clone() {
		ArgumentFramework result = new ArgumentFramework();
		result.addAllArguments(this.getAllArguments());
		result.addAllAttacks(this.getAllAttacks());
		return result;
	}
	
	/*
	 * add a new argument
	 * attacks must be added later
	 * @param the new argument
	 */
	public void addArgument(Argument a) {
		HashSet<Argument> attacked = new HashSet<Argument>();
		HashSet<Argument> attacking = new HashSet<Argument>();
		
		graph_attacked.put(a, attacked);
		graph_attacking.put(a, attacking);
	}
	
	/*
	 * remove an existing argument and all the linked attacks
	 *  returns UnknownArgumentError if argument not present 
	 */
	public void removeArgument(Argument a) {
		if(!this.containsArgument(a)) {
			throw new UnknownArgumentError("could not find argument " + a.getName() + " in af.removeArgument");
		}
		// removes the argument from the list
		this.graph_attacked.remove(a);
		this.graph_attacking.remove(a);
		// checks if other arguments attack this argument
		removeAttacksFrom(a);
		// checks if other arguments are attacked by this argument
		removeAttacksTo(a);
	}
	
	private void removeAttacksFrom(Argument a) {
		Set<Argument> arguments = this.getAllArguments();
		Iterator<Argument> iter = arguments.iterator();
		while(iter.hasNext()) {
			Argument arg = iter.next();
			Set<Argument> attacked = this.getAttackedArguments(arg);
			attacked.remove(a);
		}
	}
	
	private void removeAttacksTo(Argument a) {
		Set<Argument> arguments = this.getAllArguments();
		Iterator<Argument> iter = arguments.iterator();
		while(iter.hasNext()) {
			Argument arg = iter.next();
			Set<Argument> attacking = this.getAttackingArguments(arg);
			attacking.remove(a);
		}
	}
	
	/*
	 * add all arguments from a Set
	 * attacks must be added later
	 * @param set of arguments to be added
	 */
	public void addAllArguments(Set<Argument> args) {
		Iterator<Argument> it = args.iterator();
		while(it.hasNext()) {
			Argument a = it.next();
			this.addArgument(a);
		}
	}
	
	/*
	 * add an attack
	 * If the origin or destination argument of the attack is not yet in the graph
	 * a UnknownArgumentError will be thrown
	 * @param the attack 
	 */
	public void addAttack(Attack att) {
		Argument from = att.getFrom();
		Argument to = att.getTo();
		Set<Argument> attackedFrom = graph_attacked.get(from);
		Set<Argument> attackingTo = graph_attacking.get(to);
		if(attackedFrom == null || attackingTo == null ) {
			throw new UnknownArgumentError("one end of the attack is unknown from the AF");
		}
		attackedFrom.add(to);
		attackingTo.add(from);
	}
	
	/*
	 * attack (From, To) 
	 * must remove To in one list and From in the other
	 */
	public void removeAttack(Attack att) {
		this.getAttackedArguments(att.getFrom()).remove(att.getTo());
		this.getAttackingArguments(att.getTo()).remove(att.getFrom());
	}
	
	/*
	 * add all attacks to the AF
	 */
	public void addAllAttacks(Set<Attack> attacks) {
		Iterator<Attack> iter = attacks.iterator();
		while(iter.hasNext()) {
			this.addAttack(iter.next());
		}
	}
	
	/*
	 * returns all the arguments from the Argument Framework
	 *
	 */
	public Set<Argument> getAllArguments() {
		return graph_attacked.keySet();
	}
	
	public int getArgumentSize() {
		return this.getAllArguments().size();
	}
	
	/*
	 * Sorting the arguments according to the number of attackers or arguments attacked
	 * In order to deal with arguments with the same value, adding a reminder
	 */
	public Collection<Argument> getAllArgumentsSorted(int sort) {
		Map<Double, Argument> temp = new TreeMap<Double, Argument>();
		double max_arg = this.getArgumentSize();
		double current_arg = 0;
		Iterator<Argument> iter = this.getAllArguments().iterator();
		while(iter.hasNext()) {
			Argument arg = iter.next();
			double nb = 0;
			if(sort == BY_RECEIVED_ATT) {
				nb = this.getNumberOfAttackingArgs(arg) + current_arg/max_arg;
			} else if(sort == BY_EMITTING_ATT) {
				nb = this.getNumberOfAttackedArgs(arg) + current_arg/max_arg;
			}
	//		System.out.println("adding argument " + arg.getName() + " with value " + nb);
			temp.put(new Double(nb), arg);
			current_arg ++;
		}
		return temp.values();
	}
	
	/*
	 * returns all the attacks from the Argument Framework
	 */
	public Set<Attack> getAllAttacks() {
		Set<Attack> attacks = new HashSet<Attack>();
		Set<Argument> arguments = this.getAllArguments();
		Iterator<Argument> iter = arguments.iterator();
		while(iter.hasNext()) {
			Argument current = iter.next();
			Set<Argument> neighbours = this.getAttackedArguments(current);
			Iterator<Argument> neighboursIter = neighbours.iterator();
			while(neighboursIter.hasNext()) {
				Argument other = neighboursIter.next();
				Attack att = new Attack(current, other);
				attacks.add(att);
			}
		}
		return attacks;
	}
	
	/*
	 * returns the number of arguments attacking one argument
	 */
	public int getNumberOfAttackingArgs(Argument a) {
		return this.getAttackingArguments(a).size();
	}
	
	/*
	 * returns the number of arguments attacked by one argument
	 */
	public int getNumberOfAttackedArgs(Argument a) {
		return this.getAttackedArguments(a).size();
	}
	
	/*
	 * get all the arguments attacked by an argument a
	 */
	public Set<Argument> getAttackedArguments(Argument a) {
		return graph_attacked.get(a);
	}
	
	/*
	 * get the Set of attacking arguments for a given argument
	 */
	public Set<Argument> getAttackingArguments(Argument a) {
		return graph_attacking.get(a);
	}
	
	/*
	 * returns true if the AF contains the argument a
	 */
	public boolean containsArgument(Argument a) {
		Set<Argument> all = this.getAllArguments();
		if(all.contains(a)) {
			return true;
		} else {
			return false;
		}
	}
	
	/*
	 * returns true if the AF contains the attack att
	 */
	public boolean containsAttack(Attack att) {
		Set<Argument> attacked = this.getAttackedArguments(att.getFrom());
		if(attacked.contains(att.getTo())) {
			return true;
		}
		return false;
	}
	
	/*
	 * changes the direction of an attack
	 * @param att
	 * will modify the AF in changing the direction of att
	 */
	public void reverseAttack(Attack att) {
		if(!this.containsAttack(att)) {
			throw new UnknownAttackError("the attack " + att.toString() + " does not belong to the AF");
		}
		this.removeAttack(att);
		this.addAttack(new Attack(att.getTo(), att.getFrom()));
	}
	
	/*
	 * returns true if this AF is included in other
	 * linear time algorithm (in number of arguments + attacks)
	 * @param other
	 */
	public boolean isIncludedIn(ArgumentFramework other) {
		Set<Argument> arguments = this.getAllArguments();
		Iterator<Argument> iter = arguments.iterator();
		// iteration through the arguments
		while(iter.hasNext()) {
			Argument current = iter.next();
			// to be included in other, the argument must be in other
			if(!other.containsArgument(current)) {
				return false;
			}
			// for a given argument a in this, all attacks from a must be in other 
			Set<Argument> neighbours = this.getAttackedArguments(current);
			Set<Argument> otherNeighbours = other.getAttackedArguments(current);
			if(!otherNeighbours.containsAll(neighbours)) {
				return false;
			}
		}
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 * toString to get a representation of the AF
	 * for debug purposes only
	 */
	public String toString() {
		String result = new String();
		String arguments = new String();
		String attacks = new String();
		Set<Argument> args = this.getAllArguments();
		Iterator<Argument> iter = args.iterator();
		while(iter.hasNext()) {
			Argument arg = iter.next();
			arguments = arguments + "arg(" + arg.getName() + ").";
			arguments = String.format(arguments + "%n");
			Set<Argument> atts = this.getAttackedArguments(arg);
			Iterator<Argument> iterA = atts.iterator();
			while(iterA.hasNext()) {
				Argument to = iterA.next();
				attacks = attacks + "att(" + arg.getName() + "," + to.getName() + ").";
				attacks = String.format(attacks + "%n");
			}
		}
		result = arguments + attacks;
		return result;
	}
}
