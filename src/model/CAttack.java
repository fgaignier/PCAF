package model;

/*
 * CAttack is a CAF attack. 
 * As such it extends Attack.
 * We add a type to model the king of the attack
 * CERTAIN, UNCERTAIN or UNDIRECTED
 */
public class CAttack extends Attack {
	 
	protected Type type;

	public enum Type {
		CERTAIN,
	    UNCERTAIN,
	    UNDIRECTED,
	    CONTROL
	}

	public CAttack(Argument from, Argument to, Type type) {
		super(from, to);
		this.type = type;
	}
	
	public Type getType() {
		return type;
	}
	
	public String toString() {
		return "(" + this.getFrom().getName() + "," + this.getTo().getName() + ")";
	}
	
}
