package solvers;

import java.util.Set;

import model.ArgumentFramework;
import model.CArgument;
import model.CAttack;
import model.PControlAF;

/**
 * Given a PCAF and a completion
 * Will calculate the probability of this completion to occur
 * Exactly as described in the report
 * @author Fabrice
 *
 */
public class Completion_Proba_Calculator {
	
	protected PControlAF PCAF;
	
	public Completion_Proba_Calculator(PControlAF PCAF) {
		this.PCAF = PCAF;
	}
	
	public double getProbability(ArgumentFramework completion) {
		double p = 1;
		Set<CArgument> uargs = PCAF.getArgumentsByType(CArgument.Type.UNCERTAIN);
		Set<CAttack> uatts = PCAF.getAttacksByType(CAttack.Type.UNCERTAIN);
		Set<CAttack> udatts = PCAF.getAttacksByType(CAttack.Type.UNDIRECTED);
		
		for(CArgument uarg : uargs) {
			if(completion.containsArgument(uarg)) {
				p = p*PCAF.getUargProba(uarg);
			} else {
				p = p*(1 - PCAF.getUargProba(uarg));
			}
		}
		for(CAttack uatt : uatts) {
			if(PCAF.containsArgument(uatt.getFrom()) && PCAF.containsArgument(uatt.getTo())) {
				if(completion.containsAttack(uatt)) {
					p = p*PCAF.getUattProba(uatt);
				} else {
					p = p*(1-PCAF.getUattProba(uatt));
				}
			}
		}
		for(CAttack udatt : udatts) {
			if(PCAF.containsArgument(udatt.getFrom()) && PCAF.containsArgument(udatt.getTo())) {
				CAttack reverse = new CAttack(udatt.getTo(), udatt.getFrom(), CAttack.Type.UNDIRECTED); 
				if(completion.containsAttack(udatt) && completion.containsAttack(reverse)) {
					p = p*(1- PCAF.getUDAttFromToProba(udatt) - PCAF.getUDAttToFromProba(udatt));
				} else {
					if(!completion.containsAttack(reverse)) {
						p = p*PCAF.getUDAttFromToProba(udatt);
					} else {
						p = p*PCAF.getUDAttToFromProba(udatt);
					}
				}
			}
		}
		return p;
	}
}
