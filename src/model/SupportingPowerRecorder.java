package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SupportingPowerRecorder {
	protected int nb_extensions;
	protected Map<Argument, Integer> occurences;
	//protected List<StableExtension> extensions;
	protected List<ArgumentFramework> completions;

	public SupportingPowerRecorder() {
		this.nb_extensions = 0;
		this.occurences = new HashMap<Argument, Integer>();
		//this.extensions = new ArrayList<StableExtension>();
		this.completions = new ArrayList<ArgumentFramework>();
	}

	public int getOccurences(Argument arg) {
		return occurences.get(arg).intValue();
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

	/*
	public boolean present(StableExtension se) {
		for(StableExtension other: this.extensions) {
			if(se.equals(other)) {
				return true;
			}
		}
		return false;
	} */

	public boolean present(ArgumentFramework af) {
		for(ArgumentFramework other: this.completions) {
			if(af.equals(other)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * updates occurrences of all arguments (except control)
	 * accepted in the stable extension 
	 * this is for skeptical acceptance only
	 * @param cc : the stable extension
	 */
	/*
	public void updateOccurrencesSke(StableExtension stable) {
		if(!present(stable)) {
			for(Argument arg : stable.getAccepted()) {
					increaseOccurence(arg);
			}
			this.increaseNbExtensions();
			this.extensions.add(stable);
		} 
	} */

	public void updateOccurrencesSke(StableExtension stable) {
		//System.out.println(stable.toString());
		for(Argument arg : stable.getAccepted()) {
			increaseOccurence(arg);
		}
		this.increaseNbExtensions();
	}
	
	/**
	 * given a list of stable extensions for a given AFr will update the occurence list
	 * for skeptical acceptance only. Therefore scope all extensions 
	 * and update the occurrence list for each extension
	 * @param stable_list
	 */
	public void updateOccurencesList(Set<StableExtension> stable_list, ArgumentFramework af) {
		if(!present(af)) {
			for(StableExtension stable : stable_list) {
				updateOccurrencesSke(stable);
			}
			this.completions.add(af);
		} 
	}

	/**
	 * given a list of stable extensions for a given AFr will update the occurence list
	 * for skeptical acceptance only. Therefore scope all extensions 
	 * and update the occurrence list for each extension
	 * @param stable_list
	 */
	public void updateOccurencesListSke(Set<StableExtension> stable_list, ArgumentFramework af) {
		if(!present(af)) {
			System.out.println("af NOT present");
			for(StableExtension stable : stable_list) {
				updateOccurrencesSke(stable);
			}
			this.completions.add(af);
		} else {
			System.out.println("af present");
		}
	}

	/**
	 * updates occurrences of all arguments (except control)
	 * accepted in the stable extension 
	 * this is for credulous acceptance only
	 * therefore once an argument has been found, we must remember it 
	 * and not treat it again (if it is found several times, its values only increases by one anyway)
	 * @param cc : the stable extension
	 */
	public void updateOccurrencesCred(StableExtension stable, Set<Argument> present) {
		for(Argument arg : stable.getAccepted()) {
			// if the argument has been dealt with already, nothing to be done
			if(!present.contains(arg)) {
				increaseOccurence(arg);
				present.add(arg);
			}
		}
		// here we do not count the exact number of extensions
		// there can be at most one increaseOccurence by set of stable extension for one AFr
		//		this.increaseNbExtensions();
	}

	/**
	 * given a list of stable extensions for a given AFr will update the occurrence list
	 * for skeptical acceptance only. Therefore scope all extensions 
	 * and update the occurence list for each extension
	 * @param stable_list
	 */
	public void updateOccurencesListCred(Set<StableExtension> stable_list) {
		Set<Argument> present = new HashSet<Argument>();
		for(StableExtension stable : stable_list) {
			updateOccurrencesCred(stable, present);
		}
		// we increase only by the number of root AF treated and not the total number
		// of extensions scoped
		this.increaseNbExtensions();
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
