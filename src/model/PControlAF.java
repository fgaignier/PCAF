package model;

import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.Iterator;

import javafx.util.Pair;
import parser.CAFParser;

/**
 * Extends the class Control AF
 * Adding the probability distributions p1, p2, p3
 * for uncertain arguments, uncertain attacks and undirected attacks
 * p1 and p2 return a double
 * p3 returns a pair of doubles
 * @author Fabrice
 *
 */
public class PControlAF extends ControlAF {
	// uncertain arguments
	protected Map <CArgument, Double> uargProba;
	// uncertain attacks
	protected Map <CAttack, Double> uattProba;
	// undirected attacks
	protected Map <CAttack, Pair<Double, Double>> udattProba;
	
	/**
	 * constructs an empty P Control AF
	 */
	public PControlAF() {
		super();
		uargProba = new HashMap<CArgument, Double>();
		uattProba = new HashMap<CAttack, Double>();
		udattProba = new HashMap<CAttack, Pair<Double, Double>>();
	}
	
	/**
	 * Constructs a PControlAF from a CAF
	 * It will use the according probability distribution (as stated in the paper)
	 * @param CAF
	 */
	public PControlAF(ControlAF CAF) {
		super();
		uargProba = new HashMap<CArgument, Double>();
		uattProba = new HashMap<CAttack, Double>();
		udattProba = new HashMap<CAttack, Pair<Double, Double>>();
		// add all arguments
		Set<CArgument> args = CAF.getAllArguments();
		for(CArgument arg : args) {
			if(arg.getType() == CArgument.Type.UNCERTAIN) {
				this.addArgument(arg, 0.5);
			} else {
				this.addArgument(arg);
			}
		}
		// add all attacks (depending on the type
		Set<CAttack> fixed = CAF.getAttacksByType(CAttack.Type.CERTAIN);
		Set<CAttack> control = CAF.getAttacksByType(CAttack.Type.CONTROL);
		Set<CAttack> uncertain = CAF.getAttacksByType(CAttack.Type.UNCERTAIN);
		Set<CAttack> undirected = CAF.getAttacksByType(CAttack.Type.UNDIRECTED);
		//fixed (proba = 1)
		for(CAttack att : fixed) {
			this.addAttack(att);
		}
		// control (proba = 1)
		for(CAttack att : control) {
			this.addAttack(att);
		}
		//uncertain (proba = 1/2)
		for(CAttack att : uncertain) {
			this.addAttack(att, 0.5);
		}
		//undirected (proba = [1/3, 1/3])
		for(CAttack att : undirected) {
			this.addAttack(att, 1/3, 1/3);
		}
		// sets the target
		super.setTarget(CAF.getTarget());
	}
	
	public Map<CArgument, Double> getUargProbas() {
		return this.uargProba;
	}
	
	public Map<CAttack, Double> getUattProbas() {
		return this.uattProba;
	}
	
	public Map<CAttack, Pair<Double, Double>> getUDattProbas() {
		return this.udattProba;
	}
	
	public double getUargProba(CArgument a) {
		Double proba = this.getUargProbas().get(a);
		return proba.doubleValue();
	}

	public double getUattProba(CAttack att) {
		Double proba = this.getUattProbas().get(att);
		return proba.doubleValue();
	}
	
	public Pair<Double, Double> getUDattProba(CAttack att) {
		return this.getUDattProbas().get(att);
	}
	
	public double getUDAttFromToProba(CAttack att) {
		return this.getUDattProba(att).getKey().doubleValue();
	}
	
	public double getUDAttToFromProba(CAttack att) {
		return this.getUDattProba(att).getValue().doubleValue();
	}
	
	/**
	 * only for uncertain arguments. Else an error is thrown
	 * the argument is added to the CAF with its probability 
	 */
	public void addArgument(CArgument arg, double proba) {
		if(arg.getType() != CArgument.Type.UNCERTAIN) {
			throw new UnsupportedOperationException("only uncertain arguments can be added with probability");
		}
		super.addArgument(arg);
		this.uargProba.put(arg, new Double(proba));
	}
	
	/**
	 * not for uncertain arguments. Else an error is thrown
	 * the argument is added to the CAF  
	 */

	public void addArgument(CArgument arg) {
		if(arg.getType() == CArgument.Type.UNCERTAIN) {
			throw new UnsupportedOperationException("need a probability for adding uncertain arguments");
		}
		super.addArgument(arg);
	}
	
	/**
	 * only for CERTAIN and CONTROL attacks
	 * Other types of attacks will require a proba (or a pair of probas) 
	 */
	public void addAttack(CAttack att) {
		if(att.getType() != CAttack.Type.CERTAIN && att.getType() != CAttack.Type.CONTROL) {
			throw new UnsupportedOperationException("need a probability for adding this kind of attack");
		}
		super.addAttack(att);
	}
	
	public void addAttack(CAttack att, double proba) {
		if(att.getType() != CAttack.Type.UNCERTAIN) {
			throw new UnsupportedOperationException("only for UNCERTAIN attacks");
		}
		super.addAttack(att);
		this.uattProba.put(att, new Double(proba));
	}
	
	public void addAttack(CAttack att, double dirFromTo, double dirToFrom) {
		if(att.getType() != CAttack.Type.UNDIRECTED) {
			throw new UnsupportedOperationException("need two probability for adding UNDIRECTED attacks");
		}
		super.addAttack(att);
		Pair<Double, Double> pair = new Pair<Double, Double>(new Double(dirFromTo), new Double(dirToFrom));
		this.udattProba.put(att, pair);
	}
	
	
	/**
	 * String representation of CAF according to apx file format
	 */
	public String toString() {
		String result = new String();
		// first the arguments
		Set<CArgument> fixed = this.getArgumentsByType(CArgument.Type.FIXED);
		Set<CArgument> uncertain = this.getArgumentsByType(CArgument.Type.UNCERTAIN);
		Set<CArgument> control = this.getArgumentsByType(CArgument.Type.CONTROL);
		Iterator<CArgument> iterf = fixed.iterator();
		Iterator<CArgument> iteru = uncertain.iterator();
		Iterator<CArgument> iterc = control.iterator();
		CArgument current;
		// fixed arguments
		while(iterf.hasNext()) {
			current = iterf.next();
			result = result + CAFParser.FIXED_ARG + "(" + current.getName() + ").";
			result = String.format(result + "%n");
		}
		// uncertain arguments
		while(iteru.hasNext()) {
			current = iteru.next();
			result = result + CAFParser.UNCERTAIN_ARG + "(" + current.getName() + ") : ";
			result = result + this.getUargProba(current);
			result = String.format(result + "%n");
		}
		//control arguments
		while(iterc.hasNext()) {
			current = iterc.next();
			result = result + CAFParser.CONTROL_ARG + "(" + current.getName() + ").";
			result = String.format(result + "%n");
		}
		
		// Then the attacks
		Set<CAttack> fixedA = attacks.get(CAttack.Type.CERTAIN);
		Set<CAttack> uncertainA = attacks.get(CAttack.Type.UNCERTAIN);
		Set<CAttack> undirectedA = attacks.get(CAttack.Type.UNDIRECTED);
		Set<CAttack> controlA = attacks.get(CAttack.Type.CONTROL);
		Iterator<CAttack> iterFA = fixedA.iterator();
		Iterator<CAttack> iterUA = uncertainA.iterator();
		Iterator<CAttack> iterDA = undirectedA.iterator();
		Iterator<CAttack> iterCA = controlA.iterator();
		CAttack currentA;
		while(iterFA.hasNext()) {
			currentA = iterFA.next();
			result = result + CAFParser.FIXED_ATT + "(" + currentA.getFrom().getName() + "," + currentA.getTo().getName() + ").";
			result = String.format(result + "%n");
		}
		while(iterUA.hasNext()) {
			currentA = iterUA.next();
			result = result + CAFParser.UNCERTAIN_ATT + "(" + currentA.getFrom().getName() + "," + currentA.getTo().getName() + ") : ";
			result = result + this.getUattProba(currentA);
			result = String.format(result + "%n");
		}
		while(iterDA.hasNext()) {
			currentA = iterDA.next();
			result = result + CAFParser.UNDIRECTED_ATT + "(" + currentA.getFrom().getName() + "," + currentA.getTo().getName() + ") : ";
			result = result + this.getUDAttFromToProba(currentA) + "/" + this.getUDAttToFromProba(currentA);
			result = String.format(result + "%n");
		}
		while(iterCA.hasNext()) {
			currentA = iterCA.next();
			result = result + CAFParser.CONTROL_ATT + "(" + currentA.getFrom().getName() + "," + currentA.getTo().getName() + ").";
			result = String.format(result + "%n");
		}
		
		Iterator<CArgument> iterP = target.iterator();
		while(iterP.hasNext()) {
			current = iterP.next();
			result = result + CAFParser.TARGET + "(" + current.getName() + ").";
			result = String.format(result + "%n");
		}
		return result;
	}
}
