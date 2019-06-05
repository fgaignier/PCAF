package generators;

import java.util.Set;

import model.ArgumentFramework;
import model.CArgument;
import model.CAttack;
import model.PControlAF;
import util.RandomGen;

/**
 * Returns random root completions for PCAF
 * Therefore it computes the random completions according to the distribution of the completions.
 * @author Fabrice
 *
 */
public class RandomPCAFRootCompletionGenerator {

	private PControlAF PCAF;
	
	public RandomPCAFRootCompletionGenerator(PControlAF PCAF) {
		this.PCAF = PCAF;
	}
	
	/**
	 * for uncertain arguments and uncertain attacks
	 * returns a value between 0 and 1
	 * @return
	 */
	public double getUncertain() {
		return RandomGen.randomDouble(0.0, 1.0);
	}
	
	/**
	 * for undirected attacks. Returns 2 values between 0 and 1 (t3 and t4)
	 * but t3+t4 < 1
	 * Therefore we choose randomly before the first value to be drawn: t3 or t4
	 * For more information, refer to the report
	 * @return
	 */
	public double[] getUndirected() {
		double t3 = 0;
		double t4 = 0;
		boolean first = RandomGen.randomBoolean();
		if(first) {
			t3 = RandomGen.randomDouble(0.0, 1.0);
			t4 = RandomGen.randomDouble(0.0, 1-t3);
		} else {
			t4 = RandomGen.randomDouble(0.0, 1.0);
			t3 = RandomGen.randomDouble(0.0, 1-t4);
		}
		double[] result = new double[2];
		result[0] = t3;
		result[1] = t4;
		return result;
	}
	
	
	/**
	 * returns a random root completion
	 * according to the PCAF root completions probability distribution
	 * @return
	 */
	public ArgumentFramework getRandomRootCompletion() {
		ArgumentFramework af = new ArgumentFramework();
		this.addFixedArguments(af);
		this.addUncertainArguments(af);
		this.addFixedAttacks(af);
		this.addUncertainAttacks(af);
		this.addUndirectedAttacks(af);
		return af;
	}
	
	private void addFixedArguments(ArgumentFramework af) {
		Set<CArgument> fixed = this.PCAF.getArgumentsByType(CArgument.Type.FIXED);
		for(CArgument arg : fixed) {
			af.addArgument(arg);
		}
	}
	
	private void addUncertainArguments(ArgumentFramework af) {
		Set<CArgument> uncertain = this.PCAF.getArgumentsByType(CArgument.Type.UNCERTAIN);
		for(CArgument arg : uncertain) {
			double t1 = this.getUncertain();
			double proba = PCAF.getUargProba(arg);
			if(proba >= t1) {
				af.addArgument(arg);
			}
		}
	}
	
	private void addFixedAttacks(ArgumentFramework af) {
		Set<CAttack> fixed = this.PCAF.getAttacksByType(CAttack.Type.CERTAIN);
		for(CAttack att : fixed) {
			try {
				af.addAttack(att);
			} catch (Error e) {
				// we do not care if one attack is not added
			}
		}
	}

	private void addUncertainAttacks(ArgumentFramework af) {
		Set<CAttack> uncertain = this.PCAF.getAttacksByType(CAttack.Type.UNCERTAIN);
		for(CAttack att : uncertain) {
			double t2 = this.getUncertain();
			double proba = this.PCAF.getUattProba(att);
			if(proba >= t2) {
				try {
					af.addAttack(att);
				} catch (Error e) {
					// we do not care if one attack is not added
				}
			}
		}
	}
	
	private void addUndirectedAttacks(ArgumentFramework af) {
		Set<CAttack> undirected = this.PCAF.getAttacksByType(CAttack.Type.UNDIRECTED);
		for(CAttack att : undirected) {
			double[] t = this.getUndirected();
			double t3 = t[0];
			double t4 = t[1];
			double p3 = this.PCAF.getUDAttFromToProba(att);
			double p4 = this.PCAF.getUDAttToFromProba(att);
			int o1 = 0;
			int o2 = 0;
			int o3 = 0;
			// easier to check here
			if(!af.containsArgument(att.getFrom()) || !af.containsArgument(att.getTo())) {
				continue;
			}
			CAttack reverse = new CAttack(att.getTo(), att.getFrom(), CAttack.Type.UNDIRECTED);
			if(p3 >= t3) {
				o1 = 1;
			}
			if(p4 >= t4) {
				o2 = 1;
			}
			if(p3+p4 <= t3 +t4) {
				o3 = 1;
			}
			int option = chooseOption(o1,o2,o3);
			if(option == 1) {
				af.addAttack(att);
			} else if(option == 2) {
				af.addAttack(reverse);
			} else {
				af.addAttack(att);
				af.addAttack(reverse);
			}
		}
	}

	private int chooseOption(int o1, int o2, int o3) {
		if(o1 == 0) {
			if(o2 == 0 && o3 == 1) {
				return 3;
			} else if (o2 == 1 && o3 == 0) {
				return 2;
			} else {
				return RandomGen.randInt(2, 3);
			}
		} else {
			if(o2 == 0 && o3 == 0) {
				return 1;
			} else if (o2 == 0 && o3 == 1) {
				if(RandomGen.randomBoolean()) {
					return 1;
				} else {
					return 3;
				}
			} else if (o2 == 1 && o3 == 0) {
				return RandomGen.randInt(1, 2);
			}
			else {
				return RandomGen.randInt(1, 3);
			}
		}
	}
}
