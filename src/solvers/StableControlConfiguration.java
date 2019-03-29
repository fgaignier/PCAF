package solvers;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

import model.CArgument;

/*
 * data structure to store a stable control configuration
 * consists of a set of accepted arguments (from AF and AU)
 * and a set of accepted control arguments
 */
public class StableControlConfiguration {

	protected Set<CArgument> accepted;
	protected Set<CArgument> onControl;
	
	public StableControlConfiguration() {
		this.accepted = new HashSet<CArgument>();
		this.onControl = new HashSet<CArgument>();
	}
	
	/*
	 * add an accepted argument
	 */
	public void addAccepted(CArgument a) {
		accepted.add(a);
	}
	
	/*
	 * add a control argument on
	 */
	public void addOnControl(CArgument c) {
		onControl.add(c);
	}
	
	/*
	 * returns the arguments accepted
	 */
	public Set<CArgument> getAccepted() {
		return accepted;
	}
	
	/*
	 * returns the control arguments that are "on"
	 */
	public Set<CArgument> getOnControl() {
		return onControl;
	}
	
	/**
	 * return true if both are equal
	 * it is enough to check that the accepted sets are the same
	 */
	/*
	public boolean equals(StableControlConfiguration other) {
		Set<CArgument> otherAccepted = other.getAccepted();
		if(!otherAccepted.containsAll(this.accepted)) {
			return false;
		}
		if(!this.accepted.containsAll(otherAccepted)) {
			return false;
		}
		return true;
	} */
	
	/**
	 * returns true if both contain the same control elements
	 * false else
	 * no matter what the accepted elements are
	 * @param other
	 * @return
	 */

	public boolean equals(StableControlConfiguration other) {
		Set<CArgument> otherControl = other.getOnControl();
		if(!otherControl.containsAll(this.getOnControl())) {
			return false;
		}
		if(!this.getOnControl().containsAll(otherControl)) {
			return false;
		}
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String result = new String();
		Iterator<CArgument> iter = accepted.iterator();
		while(iter.hasNext()) {
			CArgument current = iter.next();
			if(current.getType() == CArgument.Type.FIXED || current.getType() == CArgument.Type.CONTROL) {
				result = result + "accepted argument " + current.getName();
			}
			if(current.getType() == CArgument.Type.CONTROL) {
				result = result + " control argument on";
			}
			result = String.format(result + "%n");
		}
		
		return result;
	}
}
