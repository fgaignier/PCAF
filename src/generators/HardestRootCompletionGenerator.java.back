package generators;

import model.*;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;

/**
 * False. Should be removed
 * @author Fabrice
 *
 */
public class HardestRootCompletionGenerator {
	
	protected ControlAF CAF;
	protected RandomCAFRootCompletionGenerator gen;
	
	public HardestRootCompletionGenerator(ControlAF CAF) {
		this.CAF = CAF;
		this.gen = new RandomCAFRootCompletionGenerator(CAF);
	}
	
	/**
	 * evaluate the new strength of an argument target in a new Weighted Argument Framework
	 * compared to its previous strength
	 * negative value if the strength decreases
	 * positive if it increases
	 */
	private double evaluateMinimalChangeImpactWRT(Argument target, Map<Argument, Double> strength, WeightedArgumentFramework waf) {
		Map<Argument, Double> updated = waf.h_categorizer(strength);
		Double old = strength.get(target);
		Double update = updated.get(target);
		return update.doubleValue() - old.doubleValue();
	}
	
	/**
	 * returns the argument framework that is the hardest to control with regard to target
	 * the goal is to find the completion giving the minimum value of strength for target
	 * we start with a maximum root completion (randomly chosen)
	 * 1) we decide on the direction of undirected attacks first (scanning all max root completions and keeping the best one)
	 * 2) we decide on the presence of uncertain attacks
	 * 3) we decide on the presence of uncertain arguments (if not linked to uncertain attacks). Removing such argument 
	 * will therefore test the completions where undirected attacks and/or fixed attacks are removed because linked to an 
	 * uncertain argument
	 */
	public WeightedArgumentFramework getHardestRootCompletionWRT(Argument target) {
		if(! this.CAF.isHardestRootCompletionCompatible()) {
			throw new UnsupportedOperationException("Calculation cannot be made. There are attacks from AU or AF to AC");
		}
		//WeightedArgumentFramework waf = new WeightedArgumentFramework(gen.getRandomMaxRootCompletion());
		WeightedArgumentFramework waf = new WeightedArgumentFramework(gen.getMaxRootCompletion());
		// test the change of direction of undirected attacks
		waf = this.setUndirectedAttacks(waf, target);
		// test the removal of uncertain attacks
		waf = this.setUncertainAttacks(waf, target);
		// test the removal of uncertain arguments (if not linked to uncertain attacks)
		//Set<CArgument> freeU = CAF.getFreeUncertainArguments(waf);
		waf = this.setUncertainArguments(waf, target);
		return waf;
	}
	
	private WeightedArgumentFramework setUndirectedAttacks(WeightedArgumentFramework waf, Argument target) {
		Set<CAttack> undirected = CAF.getAttacksByType(CAttack.Type.UNDIRECTED);
		Iterator<CAttack> iter = undirected.iterator();
		while(iter.hasNext()) {
			CAttack att = iter.next();
			CAttack reverseAtt = new CAttack(att.getTo(), att.getFrom(), CAttack.Type.UNDIRECTED);
			Map<Argument, Double> strength = waf.h_categorizer();
			double impact = 0;
			waf.removeAttack(att);
			impact = this.evaluateMinimalChangeImpactWRT(target, strength, waf);
			// if impact not strictly negativ, leave the attack
			if(impact >=0) {
				waf.addAttack(att);
			}
			waf.removeAttack(reverseAtt);
			impact = this.evaluateMinimalChangeImpactWRT(target, strength, waf);
			// if impact not strictly negativ, leave the attack
			if(impact >=0) {
				waf.addAttack(reverseAtt);
			}

			/*
			if(waf.containsAttack(att)) {
				waf.reverseAttack(att);
				impact = this.evaluateMinimalChangeImpactWRT(target, strength, waf);
				// impact change only if it strictly decreases the Deg(t), therefore reinstate the attack 
				// if it has a positive or null impact
				if(impact >=0) {
					waf.reverseAttack(reverseAtt);
				}
			} else if(waf.containsAttack(reverseAtt)) {
				waf.reverseAttack(reverseAtt);
				impact = this.evaluateMinimalChangeImpactWRT(target, strength, waf);
				// impact change only if it strictly decreases the Deg(t), therefore reinstate the attack 
				// if it has a positive or null impact
				if(impact >=0) {
					waf.reverseAttack(att);
				}
			} else {
				System.out.println("WARNING: undirected attack not present for hardest completion calculation");
			}
			*/
		}
		return waf;
	}
	
	private WeightedArgumentFramework setUncertainAttacks(WeightedArgumentFramework waf, Argument target) {
		Set<CAttack> uncertain = CAF.getAttacksByType(CAttack.Type.UNCERTAIN);
		Iterator<CAttack> iter = uncertain.iterator();
		while(iter.hasNext()) {
			CAttack att = iter.next();
			Map<Argument, Double> strength = waf.h_categorizer();
			double impact = 0;
			if(waf.containsAttack(att)) {
				waf.removeAttack(att);
				impact = this.evaluateMinimalChangeImpactWRT(target, strength, waf);
				// impact change only if it strictly decreases the Deg(t), therefore reinstate the attack 
				// if it has a positive or null impact
				if(impact >=0) {
					waf.addAttack(att);
				}
			} else {
				System.out.println("WARNING: uncertain attack not present for hardest completion calculation");
			}
		}
		return waf;
	}
	
	private WeightedArgumentFramework setUncertainArguments(WeightedArgumentFramework waf, Argument target) {
		WeightedArgumentFramework clone = null;
		Set<CArgument> uncertain = CAF.getArgumentsByType(CArgument.Type.UNCERTAIN);
		for(CArgument current : uncertain) {
			Map<Argument, Double> strength = waf.h_categorizer();
			double impact = 0;
			clone = waf.clone();
			clone.removeArgument(current);
			impact = this.evaluateMinimalChangeImpactWRT(target, strength, clone);
			// impact change only if it strictly decreases the Deg(t)
			if(impact <0) {
				waf.removeArgument(current);
			}
			
		}
		return waf;
	}

}
