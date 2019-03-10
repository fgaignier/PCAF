package model;

/*
 * generic Attack.
 * Only composed of two arguments:
 * From and To
 */
public class Attack {

	protected Argument from;
	protected Argument to;
	
	public Attack(Argument from, Argument to) {
		this.from = from;
		this.to= to;
	}
		
	public Argument getFrom() {
		return from;
	}
	
	public Argument getTo() {
		return to;
	}
	
	public String toString() {
		String result = new String();
		result = "(" + from.getName() + " , " + to.getName() + ")";
		return result;
	}
}
