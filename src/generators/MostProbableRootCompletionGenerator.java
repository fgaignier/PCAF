package generators;

import model.*;
import solvers.Completion_Proba_Calculator;

import java.util.Set;
import javafx.util.Pair;

public class MostProbableRootCompletionGenerator {
	
	protected PControlAF PCAF;
	protected RandomRootCompletionGenerator gen;
	protected double maxproba;
	protected Completion_Proba_Calculator cpc;
	
	public MostProbableRootCompletionGenerator(PControlAF PCAF) {
		this.PCAF = PCAF;
		this.gen = new RandomRootCompletionGenerator(PCAF);
		this.maxproba = -1;
		this.cpc = new Completion_Proba_Calculator(this.PCAF);
	}
	
	public double getProbability() {
		return this.maxproba;
	}
	
	public ArgumentFramework getMostProbableRootCompletion() {
		ArgumentFramework af = gen.getMaxRootCompletion();
		// calculate the probability of max root completion
		maxproba = cpc.getProbability(af);
		// test the change of direction of undirected attacks
		af = this.setUndirectedAttacks(af);
		// test the removal of uncertain attacks
		af = this.setUncertainAttacks(af);
		// test the removal of uncertain arguments (if not linked to uncertain attacks)
		//Set<CArgument> freeU = PCAF.getFreeUncertainArguments(af);
		af = this.setUncertainArguments(af);
		return af;
	}
	
	private ArgumentFramework setUndirectedAttacks(ArgumentFramework af) {
		Set<CAttack> undirected = PCAF.getAttacksByType(CAttack.Type.UNDIRECTED);
		for(CAttack att : undirected) {
			Pair<Double, Double> probas = PCAF.getUDattProba(att);
			CAttack reverseAtt = new CAttack(att.getTo(), att.getFrom(), CAttack.Type.UNDIRECTED);
			//chose the biggest
			double p0 = probas.getKey().doubleValue();
			double p1 = probas.getKey().doubleValue();
			double p2 = 1 - p0 -p1;
			if(p0 >= p1 && p0 >= p2) {
				//System.out.println("removing undirected attack (" + reverseAtt.getFrom().getName() + "," +  reverseAtt.getTo().getName() + ")");
				af.removeAttack(reverseAtt);
				//update maxproba 
				maxproba = (maxproba/p2)*p0;
			} else if(p1 >= p0 && p1 >=p2) {
				//System.out.println("removing undirected attack (" + att.getFrom().getName() + "," +  att.getTo().getName() + ")");
				af.removeAttack(att);
				//update maxproba 
				maxproba = (maxproba/p2)*p1;
			}
			// else do nothing, we keep both sides
		}
		return af;
	}
	
	private ArgumentFramework setUncertainAttacks(ArgumentFramework af) {
		Set<CAttack> uncertain = PCAF.getAttacksByType(CAttack.Type.UNCERTAIN);
		for(CAttack att : uncertain) {
			double proba = PCAF.getUattProba(att);
			if(proba <0.5) {
				//System.out.println("removing uncertain attack (" + att.getFrom().getName() + "," +  att.getTo().getName() + ")");
				af.removeAttack(att);
				//update maxproba 
				maxproba = (maxproba/proba)*(1- proba);
			}
		}
		return af;
	}
	
	/**
	 * deprecated. Need to review the theory, but minimal moves do not always provide a good result
	 * @param af
	 * @param freeU
	 * @return
	 */
	/*
	private ArgumentFramework setUncertainArguments(ArgumentFramework af, Set<CArgument> freeU) {
		// it is much harder to mesure the full impact of such removal
		// so we test it on a clone and if it does increase to probability we really remove it
		// this is all linear calculations anyway
		ArgumentFramework clone = null;
		for(CArgument current : freeU) {
			clone = af.clone();
			clone.removeArgument(current);
			double updatedProba = cpc.getProbability(clone);
			if(updatedProba > this.maxproba) {
				af.removeArgument(current);
				this.maxproba = updatedProba;
			}
		}
		return af;
	} */
	
	/**
	 * iterate through the uncertain arguments and checks if the removal has
	 * a positive impact. If yes, the removal is performed
	 * THIS IS NOT A MINIMAL MOVE !!!!!
	 * @param af
	 * @return
	 */
	private ArgumentFramework setUncertainArguments(ArgumentFramework af) {
		// it is much harder to mesure the full impact of such removal
		// so we test it on a clone and if it does increase to probability we really remove it
		// this is all linear calculations anyway
		ArgumentFramework clone = null;
		Set<CArgument> uncertain = PCAF.getArgumentsByType(CArgument.Type.UNCERTAIN);
		for(CArgument current : uncertain) {
			clone = af.clone();
			clone.removeArgument(current);
			double updatedProba = cpc.getProbability(clone);
			if(updatedProba > this.maxproba) {
				af.removeArgument(current);
				this.maxproba = updatedProba;
			}
		}
		return af;
	}

}
