package solvers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;

import model.ArgumentFramework;
import model.CArgument;
import model.CAttack;
import model.PControlAF;

/**
 * Find completions over a given limit of probability
 * Find the most probable completion
 * for this we have a much better solution implemented in MostProbableRootCompletionGenerator
 * This is just for comparison purposes
 * @author Fabrice
 *
 */
public class CSP_PCAF_Proba_Solver {
	
	protected PControlAF PCAF;
	// need to convert the value to integer
	public static double precision = 100;
	
	// minimum proba to be considered
	public static double minimum_proba = 0.0001;
	
	public CSP_PCAF_Proba_Solver(PControlAF PCAF) {
		this.PCAF = PCAF;
	}
	
	/**
	 * need to convert double logarithms to int values (for variables domains)
	 * @param d
	 * @return
	 */
	public int getLnIntValue(double d) {
		double temp = Math.log(d)*precision;
		return (int) temp;
	}
	
	public Set<ArgumentFramework> getMostProbableRootCompletion() {
		double best = minimum_proba;
		double current = this.getCurrentBest(best);
		while(current  > best) {
			best = current;
			current = this.getCurrentBest(best);
		}
		
		return this.getCompletionsOverLimit(best);
	}
	
	private double getCurrentBest(double best) {
		Completion_Proba_Calculator cpc = new Completion_Proba_Calculator(this.PCAF);
		Set<ArgumentFramework> currentBest = getCompletionsOverLimit(best);
		double maxProba = best;
		for(ArgumentFramework af : currentBest) {
			double current = cpc.getProbability(af);
			if(current > maxProba) {
				maxProba = current;
			}
		}
		return maxProba;
	}
	
	/**
	 * returns all completions over a given probability (limit) to occur
	 * @param limit
	 * @return
	 */
	public Set<ArgumentFramework> getCompletionsOverLimit(double limit) {
		
		if(limit == 0) {
			throw new UnsupportedOperationException("cannot return all completions. Please choose a limit > 0");
		}
		// variables must be stored in a structure for later access
		// variables for arguments (uncertain and fixed) and attacks (uncertain and undirected)
		Map<String, IntVar> argVar = new HashMap<String, IntVar>();
		Map<String, IntVar> attackVar = new HashMap<String, IntVar>();
		
		// 1. Create the CSP Model
		Model model = new Model("Over Limit Completion Solver");
		
        // 2. Create variables
		Set<CArgument> uncertainArgs = PCAF.getArgumentsByType(CArgument.Type.UNCERTAIN);
		Set<CArgument> fixedArgs = PCAF.getArgumentsByType(CArgument.Type.FIXED);
		// uncertain arguments
		for(CArgument arg : uncertainArgs) {
			String argName = arg.getName();
			double proba = PCAF.getUargProba(arg);
			IntVar arg_var = model.intVar(argName, new int[]{this.getLnIntValue(proba),this.getLnIntValue(1-proba)});
			argVar.put(argName,  arg_var);
			//System.out.println("adding variable " + argName);
			//System.out.println("domains: " + this.getLnIntValue(proba) + "," + this.getLnIntValue(1-proba) + "," + 0);
		}
		// fixed arguments
		for(CArgument arg : fixedArgs) {
			String argName = arg.getName();
			IntVar arg_var = model.intVar(argName, 0);
			argVar.put(argName,  arg_var);
			//System.out.println("adding variable " + argName);
			//System.out.println("domains: " + 0);
		}

		Set<CAttack> uncertainAtts = PCAF.getAttacksByType(CAttack.Type.UNCERTAIN);
		Set<CAttack> undirectedAtts = PCAF.getAttacksByType(CAttack.Type.UNDIRECTED);
		//uncertain attacks
		for(CAttack att : uncertainAtts) {
			double proba = PCAF.getUattProba(att);
			String attName = att.getFrom().getName() + "_" + att.getTo().getName();
			IntVar att_var = model.intVar(attName, new int[]{this.getLnIntValue(proba),this.getLnIntValue(1-proba), 0});
			attackVar.put(attName,  att_var);
			//System.out.println("adding variable " + attName);
			//System.out.println("domains: " + this.getLnIntValue(proba) + "," + this.getLnIntValue(1-proba) + "," + 0);
		}

		//undirected attacks
		for(CAttack att : undirectedAtts) {
			//System.out.println("undirected attack " + att.toString());
			double pd1 = PCAF.getUDAttFromToProba(att);
			double pd2 = PCAF.getUDAttToFromProba(att);
			double pd12 = 1 - pd1 -pd2;
			String attName = att.getFrom().getName() + "_" + att.getTo().getName();
			IntVar att_var = model.intVar(attName, new int[]{this.getLnIntValue(pd1),this.getLnIntValue(pd2), this.getLnIntValue(pd12), 0});
			attackVar.put(attName,  att_var);
			//System.out.println("adding variable " + attName);
			//System.out.println("domains: " + this.getLnIntValue(pd1) + "," + this.getLnIntValue(pd2) + "," + this.getLnIntValue(pd12) + "," + 0);
		}

		
		/*
		 * Constraints
		 */
		/*
		 * val(dij) < 0 iff val(ai) = ln(p1(ai)) and val(aj)=ln(p1(aj))
		 * if ai fixed ln(p1(ai)) = 0
		 * if aj fixed ln(p1(aj)) = 0
		 */
		for(CAttack att : uncertainAtts) {
			//System.out.println("undirected attack " + att.toString());
			String attName = att.getFrom().getName() + "_" + att.getTo().getName();
			CArgument argFrom = PCAF.getArgumentByName(att.getFrom().getName());
			CArgument argTo = PCAF.getArgumentByName(att.getTo().getName());
			IntVar dij = attackVar.get(attName);
			IntVar ai = argVar.get(att.getFrom().getName());
			IntVar aj = argVar.get(att.getTo().getName());
			int valai = 0;
			if(argFrom.getType() == CArgument.Type.UNCERTAIN) {
				valai = this.getLnIntValue(PCAF.getUargProba(argFrom));
			}
			int valaj = 0;
			if(argTo.getType() == CArgument.Type.UNCERTAIN) {
				valaj = this.getLnIntValue(PCAF.getUargProba(argTo));
			}
			int total = valai + valaj;
			
			IntVar[] sum = new IntVar[3];
			sum[0] = dij;
			sum[1] = ai;
			sum[2] = aj;
			model.sum(sum, "!=", total).post();
			//System.out.println("adding constraint sum(" + fromTabToString(sum) + "!=" + total);
		}
		
		/*
		 * val(dij) < 0 iff val(ai) = ln(p1(ai)) and val(aj)=ln(p1(aj))
		 * if ai fixed ln(p1(ai)) = 0
		 * if aj fixed ln(p1(aj)) = 0
		 */
		for(CAttack att : undirectedAtts) {
			String attName = att.getFrom().getName() + "_" + att.getTo().getName();
			CArgument argFrom = PCAF.getArgumentByName(att.getFrom().getName());
			CArgument argTo = PCAF.getArgumentByName(att.getTo().getName());
			IntVar dij = attackVar.get(attName);
			IntVar ai = argVar.get(att.getFrom().getName());
			IntVar aj = argVar.get(att.getTo().getName());
			int valai = 0;
			if(argFrom.getType() == CArgument.Type.UNCERTAIN) {
				valai = this.getLnIntValue(PCAF.getUargProba(argFrom));
			}
			int valaj = 0;
			if(argTo.getType() == CArgument.Type.UNCERTAIN) {
				valaj = this.getLnIntValue(PCAF.getUargProba(argTo));
			}
			int total = valai + valaj;
			
			IntVar[] sum = new IntVar[3];
			sum[0] = dij;
			sum[1] = ai;
			sum[2] = aj;
			// add the constraint to the model
			model.sum(sum, "!=", total).post();
			//System.out.println("adding constraint sum(" + fromTabToString(sum) + "!=" + total);
		}
	
		// global constraint
		// sum(val(ai)) + sum(val(dij)) >= ln(k) 
		// argument part
		IntVar[] sumaidij = new IntVar[uncertainArgs.size() + uncertainAtts.size() + undirectedAtts.size()];
		int i =0;
		for(CArgument arg : uncertainArgs) {
			IntVar ai = argVar.get(arg.getName());
			sumaidij[i] = ai;
			i++;
		}

		// uncertain attack part
		for(CAttack att : uncertainAtts) {
			String attName = att.getFrom().getName() + "_" + att.getTo().getName();
			IntVar dij = attackVar.get(attName);
			sumaidij[i] = dij;
			i++;
		}

		// undirected attack part
		for(CAttack att : undirectedAtts) {
			String attName = att.getFrom().getName() + "_" + att.getTo().getName();
			IntVar dij = attackVar.get(attName);
			sumaidij[i] = dij;
			i++;
		}

		model.sum(sumaidij, ">=", getLnIntValue(limit)).post();
		
		//System.out.println("adding constraint sum(" + fromTabToString(sumaidij) + ">=" + getLnIntValue(limit));
		
		// 4. Solve the problem and return the set of solutions
		Set<ArgumentFramework> result = new HashSet<ArgumentFramework>();
		while(model.getSolver().solve()) {
			ArgumentFramework solution = this.buildAF(argVar, attackVar);
			result.add(solution);
		} 
		return result;
	}
	
	private ArgumentFramework buildAF(Map<String, IntVar> argVar, Map<String, IntVar> attackVar) {
		ArgumentFramework af = new ArgumentFramework();
		// build the structure (fixed part)
		af.addAllCArguments(PCAF.getArgumentsByType(CArgument.Type.FIXED));
		af.addAllCAttacks(PCAF.getAttacksByType(CAttack.Type.CERTAIN));
		
		// add uncertain arguments if it is on
		for(String name : argVar.keySet()) {
			//System.out.println("evaluating addition of " + name);
			CArgument arg = PCAF.getArgumentByName(name);
			IntVar var = argVar.get(name);
			if(arg.getType() == CArgument.Type.UNCERTAIN) {
				int onValue = this.getLnIntValue(PCAF.getUargProba(arg));
				//System.out.println("on value= " + onValue);
				//System.out.println("var value= " + var.getValue());
				if(var.getValue() == onValue) {
					//System.out.println("argument added");
					af.addArgument(arg);
				} 
			}
		}
		
		Set<CAttack> uncertainAtts = PCAF.getAttacksByType(CAttack.Type.UNCERTAIN);
		Set<CAttack> undirectedAtts = PCAF.getAttacksByType(CAttack.Type.UNDIRECTED);
		
		for(CAttack att: uncertainAtts) {
			String attName = att.getFrom().getName() + "_" + att.getTo().getName();
			IntVar var = attackVar.get(attName);
			int onValue = this.getLnIntValue(PCAF.getUattProba(att));
			if(var.getValue() == onValue) {
				af.addAttack(att);
			}
		}

		for(CAttack att: undirectedAtts) {
			//System.out.println("adding attack " + att.toString());
			String attName = att.getFrom().getName() + "_" + att.getTo().getName();
			IntVar var = attackVar.get(attName);
			int dir1Value = this.getLnIntValue(PCAF.getUDattProba(att).getKey().doubleValue());
			int dir2Value = this.getLnIntValue(PCAF.getUDattProba(att).getValue().doubleValue());
			CAttack reverse = new CAttack(att.getTo(), att.getFrom(), CAttack.Type.UNDIRECTED);
			if(af.containsArgument(att.getFrom()) && af.containsArgument(att.getTo())) {
				if(var.getValue() == dir1Value) {
					af.addAttack(att);
				} else if(var.getValue() == dir2Value) {
					af.addAttack(reverse);
				} else {
					af.addAttack(att);
					af.addAttack(reverse);
				}
			}
		}

		return af;
	}
	
	/**
	 * protected internal use only
	 * toString method for an array of IntVar
	 */
	protected String fromTabToString(IntVar[] tab) {
		String result = new String();
		for(int i = 0; i<tab.length; i++) {
			result = result + tab[i].getName() + " , ";
		}
		
		return result;
	}

	
}
