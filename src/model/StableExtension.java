package model;

import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * data structure to store a stable extension
 * consists of a set of accepted arguments
 */
public class StableExtension {
	protected SortedSet<Argument> accepted;
	
	public StableExtension() {
		this.accepted = new TreeSet<Argument>();
	}
	
	/**
	 * add an accepted argument
	 */
	public void addAccepted(Argument a) {
		accepted.add(a);
	}
		
	/**
	 * returns the arguments accepted
	 */
	public Set<Argument> getAccepted() {
		return accepted;
	}
	
	/**
	 * return true if both are equal
	 * it is enough to check that the accepted sets are the same
	 */
	public boolean equals(StableExtension other) {
		Set<Argument> otherAccepted = other.getAccepted();
		if(!otherAccepted.containsAll(this.accepted)) {
			return false;
		}
		if(!this.accepted.containsAll(otherAccepted)) {
			return false;
		}
		return true;
	}
	
	/**
	 * Override toString
	 * returns a string representation of stable extension
	 */
	public String toString() {
		String result = new String();
		Iterator<Argument> iter = accepted.iterator();
		while(iter.hasNext()) {
			Argument current = iter.next();
			result = result + "accepted argument " + current.getName();
			result = String.format(result + "%n");
		}		
		return result;
	}
	
	public boolean contains(Set<CArgument> target) {
		for(Argument arg : target) {
			if(!this.accepted.contains(arg)) {
				return false;
			}
		}
		return true;
	}
}
