package solvers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.IntVar;

import model.ArgumentFramework;
import model.CArgument;
import model.CAttack;
import model.PControlAF;

/**
 * Find completions over a given limit of probability
 * Find the most probable completion
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
		Map<String, IntVar> onArg = new HashMap<String, IntVar>();
		Map<String, IntVar> onAttack = new HashMap<String, IntVar>();
		
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
			IntVar on_arg = model.intVar("on-" + argName, new int[]{0,1});
			argVar.put(argName,  arg_var);
			onArg.put(argName, on_arg);
			
			//System.out.println("adding variable " + argName);
			//System.out.println("domains: " + this.getLnIntValue(proba) + "," + this.getLnIntValue(1-proba) + "," + 0);
		}
		// fixed arguments
		for(CArgument arg : fixedArgs) {
			String argName = arg.getName();
			IntVar arg_var = model.intVar(argName, 0);
			IntVar on_var = model.intVar("on-" + argName, new int[] {1});
			argVar.put(argName,  arg_var);
			onArg.put(argName, on_var);
			
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
			IntVar on_var = model.intVar("on-" + attName, new int[]{0,1,2});
			attackVar.put(attName,  att_var);
			onAttack.put(attName, on_var);
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
			IntVar on_var = model.intVar("on-" + attName, new int[]{0,1,2,3});
			attackVar.put(attName,  att_var);
			onAttack.put(attName, on_var);
			//System.out.println("adding variable " + attName);
			//System.out.println("domains: " + this.getLnIntValue(pd1) + "," + this.getLnIntValue(pd2) + "," + this.getLnIntValue(pd12) + "," + 0);
		}

		
		/*
		 * Constraints
		 */
		// uncertain arguments
		for(CArgument arg : uncertainArgs) {
			this.addArgumentConstraints(arg, model, argVar, onArg);
		}
		for(CAttack att : uncertainAtts) {
			this.addUAttackConstraints(att, model, argVar, onArg , attackVar, onAttack);
		}
		
		for(CAttack att : undirectedAtts) {
			this.addUDAttackConstraints(att, model, argVar, onArg , attackVar, onAttack);
	
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
			ArgumentFramework solution = this.buildAF(onArg, onAttack);
			result.add(solution);
		} 
		return result;
	}
	
	/**
	* add all the constraints for uncertain arguments
	 */
	private void addArgumentConstraints(CArgument arg, Model model, Map<String, IntVar> argVar, Map<String, IntVar> onArg) {
		IntVar onVar = onArg.get(arg.getName());
		IntVar valVar = argVar.get(arg.getName());
		int valarg1 = this.getLnIntValue(PCAF.getUargProba(arg));
		int valarg2 = this.getLnIntValue(1- PCAF.getUargProba(arg));
		Constraint c1 = model.arithm(onVar, "=", 0);
		Constraint c2 = model.arithm(onVar, "=", 1);
		Constraint c3 = model.arithm(valVar, "=", valarg1);
		Constraint c4 = model.arithm(valVar, "=", valarg2);
		model.or(c1,c3).post();
		model.or(c2,c4).post();
		
	}
	
	/**
	* add all the constraints for uncertain attacks
	 */
	private void addUAttackConstraints(CAttack att, Model model, Map<String, IntVar> argVar, Map<String, IntVar> onArg, Map<String, IntVar> attackVar, Map<String, IntVar> onAttack) {
		String attName = att.getFrom().getName() + "_" + att.getTo().getName();
		IntVar dij = attackVar.get(attName);
		IntVar onij = onAttack.get(attName);
		IntVar oni = onArg.get(att.getFrom().getName());
		IntVar onj = onArg.get(att.getTo().getName());
		int valij1 = this.getLnIntValue(PCAF.getUattProba(att));
		int valij2 = this.getLnIntValue(1- PCAF.getUattProba(att));
		
		Constraint c1 = model.arithm(onij, "!=", 0);
		Constraint c2 = model.arithm(oni, "=", 0);
		Constraint c3 = model.arithm(onj, "=", 0);
		model.or(c1,c2,c3).post();
		
		Constraint c4 = model.arithm(oni, "=", 1);
		Constraint c5 = model.arithm(onj, "=", 1);
		Constraint c6 = model.arithm(onij, "=", 0);
		Constraint bothpresent = model.and(c4,c5);
		model.or(bothpresent, c6).post();
		
		Constraint c7 = model.arithm(dij, "=", 0);
		Constraint c8 = model.arithm(dij, "!=", 0);
		model.or(c1,c7).post();
		model.or(c6,c8).post();

		Constraint c9 = model.arithm(dij, "=", valij1);
		Constraint c10 = model.arithm(dij, "=", valij2);
		Constraint c11 = model.arithm(onij, "!=", 1);
		Constraint c12 = model.arithm(onij, "!=", 2);

		Constraint c13 = model.and(bothpresent,c9);
		Constraint c14 = model.and(bothpresent,c10);

		model.or(c13, c11).post();
		model.or(c14, c12).post();
				
	}

	/**
	* add all the constraints for undirected attacks
	 */
	private void addUDAttackConstraints(CAttack att, Model model, Map<String, IntVar> argVar, Map<String, IntVar> onArg, Map<String, IntVar> attackVar, Map<String, IntVar> onAttack) {
		String attName = att.getFrom().getName() + "_" + att.getTo().getName();
		IntVar dij = attackVar.get(attName);
		IntVar onij = onAttack.get(attName);
		IntVar oni = onArg.get(att.getFrom().getName());
		IntVar onj = onArg.get(att.getTo().getName());
		int valij1 = this.getLnIntValue(PCAF.getUDAttFromToProba(att));
		int valij2 = this.getLnIntValue(PCAF.getUDAttToFromProba(att));
		int valij3 = this.getLnIntValue(1 - PCAF.getUDAttFromToProba(att) - PCAF.getUDAttToFromProba(att));
		
		Constraint c1 = model.arithm(onij, "!=", 0);
		Constraint c2 = model.arithm(oni, "=", 0);
		Constraint c3 = model.arithm(onj, "=", 0);
		model.or(c1,c2,c3).post();
		
		Constraint c4 = model.arithm(oni, "=", 1);
		Constraint c5 = model.arithm(onj, "=", 1);
		Constraint c6 = model.arithm(onij, "=", 0);
		Constraint bothpresent = model.and(c4,c5);
		model.or(bothpresent, c6).post();
		
		Constraint c7 = model.arithm(dij, "=", 0);
		Constraint c8 = model.arithm(dij, "!=", 0);
		
		model.or(c1,c7).post();
		model.or(c6,c8).post();
		
		Constraint c9 = model.arithm(dij, "=", valij1);
		Constraint c10 = model.arithm(dij, "=", valij2);
		Constraint c11 = model.arithm(dij, "=", valij3);
		Constraint c12 = model.arithm(onij, "!=", 1);
		Constraint c13 = model.arithm(onij, "!=", 2);
		Constraint c14 = model.arithm(onij, "!=", 3);
		
		Constraint c15 = model.and(bothpresent,c9);
		Constraint c16 = model.and(bothpresent,c10);
		Constraint c17 = model.and(bothpresent,c11);
		
		model.or(c12, c15).post();
		model.or(c13, c16).post();
		model.or(c14, c17).post();
				
	}

	/**
	 * build the corresponding AF
	 * @param onArg
	 * @param onAttack
	 * @return
	 */
	private ArgumentFramework buildAF(Map<String, IntVar> onArg, Map<String, IntVar> onAttack) {
		ArgumentFramework af = new ArgumentFramework();
		// build the structure (fixed part)
		//af.addAllCArguments(PCAF.getArgumentsByType(CArgument.Type.FIXED));
		
		// add all arguments with on=1
		for(String name : onArg.keySet()) {
			CArgument arg = PCAF.getArgumentByName(name);
			IntVar onVar = onArg.get(name);
			if(onVar.getValue() == 1) {
				af.addArgument(arg);
			} 
		}
		
		Set<CAttack> uncertainAtts = PCAF.getAttacksByType(CAttack.Type.UNCERTAIN);
		Set<CAttack> undirectedAtts = PCAF.getAttacksByType(CAttack.Type.UNDIRECTED);
		Set<CAttack> fixedAtts = PCAF.getAttacksByType(CAttack.Type.CERTAIN);
		
		for(CAttack att: fixedAtts) {
			// OK, here we need to check for the fixed attacks
			// since there is no value for those
			if(af.containsArgument(att.getFrom()) && af.containsArgument(att.getTo())) {
				af.addAttack(att);
			}
		}
		
		// no need to check the presence of the arguments
		// if the attack is on, we have the arguments on as well by construction of the constraints
		for(CAttack att: uncertainAtts) {
			String attName = att.getFrom().getName() + "_" + att.getTo().getName();
			IntVar var = onAttack.get(attName);
			if(var.getValue() == 1) {
				af.addAttack(att);
			} // else do nothing (cannot be present or not present)
		}

		// no need to check the presence of the arguments
		// if the attack is on, we have the arguments on as well by construction of the constraints
		for(CAttack att: undirectedAtts) {
			String attName = att.getFrom().getName() + "_" + att.getTo().getName();
			IntVar var = onAttack.get(attName);
			CAttack reverse = new CAttack(att.getTo(), att.getFrom(), CAttack.Type.UNDIRECTED);
			if(var.getValue() == 1) {
				af.addAttack(att);
			} else if(var.getValue() == 2) {
					af.addAttack(reverse);
			} else if(var.getValue() == 3) {
				af.addAttack(att);
				af.addAttack(reverse);
			} // else do nothing (cannot be present)
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
