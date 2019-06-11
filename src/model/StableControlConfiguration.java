package model;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * data structure to store a control configuration
 * consists of a set of control arguments
 */
public class StableControlConfiguration {

	protected SortedSet<CArgument> onControl;
	
	public StableControlConfiguration() {
		this.onControl = new TreeSet<CArgument>();
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
	
	public boolean contains(Argument c) {
		if(onControl.contains(c)) {
			return true;
		}
		return false;
	}
	
	/**
	 * tells if the control configuration is empty
	 * no control arguments
	 * @return
	 */
	public boolean isEmpty() {
		return this.onControl.isEmpty();
	}
	
	/**
	 * returns true if both contain the same control elements
	 * false else
	 * no matter what the accepted elements are
	 * @param other
	 * @return
	 */
	public boolean equals(StableControlConfiguration other) {
		if(other.isEmpty() && this.isEmpty()) {
			return true;
		}
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
		//result.append("(");
		if(this.isEmpty()) {
			result.append("empty cc,");
		}
		for(CArgument arg : this.onControl) {
				result.append(arg.getName());
				result.append(",");
		}  
		result.deleteCharAt(result.length() -1);
		//result.append(")");
		return result.toString();
	}
	
	
}
