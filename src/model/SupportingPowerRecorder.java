package model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SupportingPowerRecorder {
	protected int nb_extensions;
	protected Map<Argument, Integer> occurences;
	
	public SupportingPowerRecorder() {
		this.nb_extensions = 0;
		this.occurences = new HashMap<Argument, Integer>();
	}
	
	public void increaseNbExtensions() {
		this.nb_extensions ++;
	}
	
	/**
	 * returns the supporting power of a given argument 
	 * @param arg
	 * @return
	 */
	public double getSupportingPower(Argument arg) {
		double supporting_power=0;
		supporting_power = (double)occurences.get(arg).intValue();
		supporting_power = supporting_power/this.nb_extensions;
		return supporting_power;
	}

	/**
	 * increases the occurrence value of a given argument by 1
	 * @param arg
	 */
	public void increaseOccurence(Argument arg) {
		Integer current = this.occurences.get(arg);
		if(current == null) {
			current = new Integer(0);
		}
		Integer updated = new Integer(current.intValue()+1);
		this.occurences.put(arg, updated);
	}
	
	/**
	 * updates occurrences of all arguments (except control)
	 * accepted in the stable extension 
	 * @param cc : the stable extension
	 */
	public void updateOccurrences(StableExtension stable) {
		for(Argument arg : stable.getAccepted()) {
				increaseOccurence(arg);
		}
		this.increaseNbExtensions();
	}
	
	public void updateOccurencesList(Set<StableExtension> stable_list) {
		for(StableExtension stable : stable_list) {
			updateOccurrences(stable);
		}
	}

	public String toString() {
		StringBuffer result = new StringBuffer();
		for(Argument arg : occurences.keySet()) {
			result.append(arg.getName());
			result.append(" has supporting power: ");
			result.append(this.getSupportingPower(arg));
			result.append("\n");
		}
		
		return result.toString();
	}
}
