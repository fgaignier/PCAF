package generators;

import model.ArgumentFramework;
import model.ControlAF;
import model.Argument;
import model.CArgument;
import model.Attack;
import model.CAttack;
import util.RandomGen;

public class CAFGenerator {
	private ArgumentFramework af;
	/**
	 * Probability that an argument from af is fixed
	 */
	private int pArgF;
	/**
	 * Probability that an argument from af is uncertain
	 */
	private int pArgU;
	/**
	 * Probability that an attack from af is fixed
	 */
	private int pAttF ;
	/**
	 * Probability that an attack from af is uncertain
	 */
	private int pAttU ;

	/**
	 * imput argument framework, probability of fixed argument, probability of uncertain argument
	 * probability of fixed attack and uncertain attack
	 * ALL IN % INTEGER VALUES
	 * @param af
	 * @param pArgF
	 * @param pArgU
	 * @param pAttF
	 * @param pAttU
	 */
	public CAFGenerator(ArgumentFramework af, int pArgF, int pArgU, int pAttF, int pAttU) {
		this.af = af;
		this.pArgF = pArgF;
		this.pArgU = pArgU;
		if (pArgF < 0) {
			throw new IllegalArgumentException("The probability " + pArgF + " is negative.");
		}
		if (pArgU < 0) {
			throw new IllegalArgumentException("The probability " + pArgU + " is negative.");
		}
		if (pArgF + pArgU > 100) {
			throw new IllegalArgumentException("The sum of " + pArgF + " and " + pArgU + " is greater than 100.");
		}
		this.pAttF = pAttF ;
		this.pAttU = pAttU ;
		if (pAttF < 0) {
			throw new IllegalArgumentException("The probability " + pAttF + " is negative.");
		}
		if (pAttU < 0) {
			throw new IllegalArgumentException("The probability " + pAttU + " is negative.");
		}
		if (pAttF + pAttU > 100) {
			throw new IllegalArgumentException("The sum of " + pAttF + " and " + pAttU + " is greater than 100.");
		}
	}

	public ControlAF generate() {
		ControlAF caf = new ControlAF();
		
		for(Argument arg : af.getAllArguments()) {
			int val = RandomGen.getProba();
			CArgument carg = null;
			if(val <= pArgF) {
				carg = new CArgument(arg.getName(),CArgument.Type.FIXED);
			}else if(val <= pArgF + pArgU) {
				carg = new CArgument(arg.getName(),CArgument.Type.UNCERTAIN);
			}else {
				carg = new CArgument(arg.getName(),CArgument.Type.CONTROL);
			}
			caf.addArgument(carg);
		}
		for(Attack att : af.getAllAttacks()) {
			CArgument from = caf.getArgumentByName(att.getFrom().getName());
			CArgument to = caf.getArgumentByName(att.getTo().getName());
			CAttack  catt = null;
			if(from.getType() == CArgument.Type.CONTROL|| from.getType() == CArgument.Type.CONTROL) {
				catt = new CAttack(from, to, CAttack.Type.CONTROL);
			}else {
				int val = RandomGen.getProba();
				if(val <= pAttF) {
					catt = new CAttack(from, to, CAttack.Type.CERTAIN);
				}else if(val <= pAttF + pAttU) {
					catt =new CAttack(from, to, CAttack.Type.UNCERTAIN);
				}else {
					catt = new CAttack(from, to, CAttack.Type.UNDIRECTED);
				}
			}
			caf.addAttack(catt);
		}
		return caf;
	}
}