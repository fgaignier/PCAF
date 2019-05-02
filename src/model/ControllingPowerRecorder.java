package model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ControllingPowerRecorder {
	protected int nb_extensions;
	protected Map<CArgument, Integer> occurences;
	
	public ControllingPowerRecorder() {
		this.nb_extensions = 0;
		this.occurences = new HashMap<CArgument, Integer>();
	}
	
	public void addArguments(Set<CArgument> args) {
		for(CArgument arg : args) {
			addArgument(arg);
		}
	}
	
	public void increaseNbExtensions() {
		this.nb_extensions ++;
	}
	
	public void addArgument(CArgument arg) {
		if(arg.getType() == CArgument.Type.CONTROL) {
			throw new UnsupportedOperationException("no supporting power for control arguments: " + arg.toString());
		}
		Integer present = occurences.get(arg);
		if(present != null) {
			throw new UnsupportedOperationException("argument already present: " + arg.toString());
		}
		occurences.put(arg, new Integer(0));
	}
	
	/**
	 * returns the supporting power of a given argument 
	 * @param arg
	 * @return
	 */
	public double getSupportingPower(CArgument arg) {
		double supporting_power=0;
		supporting_power = (double)occurences.get(arg).intValue();
		supporting_power = supporting_power/this.nb_extensions;
		return supporting_power;
	}

	/**
	 * increases the occurrence value of a given argument by 1
	 * @param arg
	 */
	public void increaseOccurence(CArgument arg) {
		Integer current = this.occurences.get(arg);
		if(current == null) {
			throw new UnsupportedOperationException("argument is missing: " + arg.toString());
		}
		Integer updated = new Integer(current.intValue()+1);
		this.occurences.put(arg, updated);
	}
	
	/**
	 * updates occurrences of all arguments (except control)
	 * accepted in the stable extension 
	 * @param cc : the stable extension
	 */
	public void updateOccurrences(StableControlConfiguration cc) {
		for(CArgument arg : cc.getAccepted()) {
			if(arg.getType() != CArgument.Type.CONTROL) {
				increaseOccurence(arg);
			}
		}
	}
	
	public void updateOccurencesList(Set<StableControlConfiguration> cc_list) {
		for(StableControlConfiguration cc : cc_list) {
			updateOccurrences(cc);
		}
	}

	public String toString() {
		StringBuffer result = new StringBuffer();
		for(CArgument arg : occurences.keySet()) {
			result.append(arg.toString());
			result.append(" has supporting power: ");
			result.append(this.getSupportingPower(arg));
		}
		
		return result.toString();
	}
}
