package model;

import java.util.Set;
import java.util.HashSet;

/**
 * data structure to store a control configuration
 * consists of a set of control arguments
 */
public class StableControlConfiguration {

	protected Set<CArgument> onControl;
	
	public StableControlConfiguration() {
		this.onControl = new HashSet<CArgument>();
	}
	
	/**
	 * add a control argument on
	 */
	public void addOnControl(CArgument c) {
		onControl.add(c);
	}
		
	/**
	 * returns the control arguments that are "on"
	 */
	public Set<CArgument> getOnControl() {
		return onControl;
	}
	
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
	
	/**
	 * represents only the control part
	 */
	public String toString() {
		StringBuffer result = new StringBuffer();
		for(CArgument arg : this.onControl) {
				result.append("control argument on: ");
				result.append(arg.getName());
				result.append("\n");
		}  
		return result.toString();
	}
	
	
}
