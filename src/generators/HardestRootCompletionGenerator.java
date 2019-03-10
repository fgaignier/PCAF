package generators;

import model.*;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

public class HardestRootCompletionGenerator {
	
	protected ControlAF CAF;
	protected RandomRootCompletionGenerator gen;
	
	public HardestRootCompletionGenerator(ControlAF CAF) {
		this.CAF = CAF;
		this.gen = new RandomRootCompletionGenerator(CAF);
	}
	
	/*
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
	
	/*
	 * returns the argument framework that is the hardest to control with regard to target
	 * the goal is to find the completion giving the minimum value of strength for target
	 * we start with a maximum root completion (randomly chosen)
	 * 1) we decide on the direction of undirected attacks first (scanning all max root completions and keeping the best one)
	 * 2) we decide on the presence of uncertain attacks
	 * 3) we decide on the presence of uncertain arguments (if not linked to uncertain attacks). Removing such argument 
	 * will therefore test the completions where undirected attacks and/or fixed atacks are removed because linke to an 
	 * uncertain argument
	 */
	public WeightedArgumentFramework getHardestRootCompletionWRT(Argument target) {
		WeightedArgumentFramework waf = new WeightedArgumentFramework(gen.getRandomMaxRootCompletion());
		// test the change of direction of undirected attacks
		waf = this.setUndirectedAttacks(waf, target);
		// test the removal of uncertain attacks
		waf = this.setUncertainAttacks(waf, target);
		// test the removal of uncertain arguments (if not linked to uncertain attacks)
		Set<Argument> freeU = getFreeUncertainArguments(waf);
		waf = this.setUncertainArguments(waf, target, freeU);
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
			if(waf.containsAttack(att)) {
				waf.reverseAttack(att);
				impact = this.evaluateMinimalChangeImpactWRT(target, strength, waf);
				if(impact >=0) {
					waf.reverseAttack(reverseAtt);
				}
			} else if(waf.containsAttack(reverseAtt)) {
				waf.reverseAttack(reverseAtt);
				impact = this.evaluateMinimalChangeImpactWRT(target, strength, waf);
				if(impact >=0) {
					waf.reverseAttack(att);
				}
			} else {
				System.out.println("WARNING: undirected attack not present for hardest completion calculation");
			}
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
				if(impact >=0) {
					waf.addAttack(att);
				}
			} else {
				System.out.println("WARNING: uncertain attack not present for hardest completion calculation");
			}
		}
		return waf;
	}
	
	/*
	 * iterate over uncertain attacks and check if From or To are part of waf and are uncertain arguments.
	 * If yes, removing from the list of free arguments
	 * If no, keeping in the list the list
	 */
	private Set<Argument> getFreeUncertainArguments(WeightedArgumentFramework waf) {
		// add all uncertain arguments to result
		Set<Argument> result = new HashSet<Argument>(CAF.getArgumentsByType(CArgument.Type.UNCERTAIN)); 
		Set<CAttack> uncertain = CAF.getAttacksByType(CAttack.Type.UNCERTAIN);
		Iterator<CAttack> iter = uncertain.iterator();
		// iterate over uncertain attacks
		while(iter.hasNext()) {
			CAttack catt = iter.next();
			if(waf.containsAttack(catt)) {
				result.remove(catt.getFrom());
				result.remove(catt.getTo());
			}
		}
		return result;
	}
	
	private WeightedArgumentFramework setUncertainArguments(WeightedArgumentFramework waf, Argument target, Set<Argument> freeU) {
		Iterator<Argument> iter = freeU.iterator();
		WeightedArgumentFramework clone = null;
		while(iter.hasNext()) {
			Argument current = iter.next();
			Map<Argument, Double> strength = waf.h_categorizer();
			double impact = 0;
			clone = waf.clone();
			clone.removeArgument(current);
			impact = this.evaluateMinimalChangeImpactWRT(target, strength, clone);
			if(impact <0) {
				waf = clone;
			}
			
		}
		return waf;
	}
}
