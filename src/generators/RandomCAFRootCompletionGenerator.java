package generators;

import java.util.Iterator;
import java.util.Set;

import model.ArgumentFramework;
import model.Attack;
import model.CArgument;
import model.CAttack;
import model.ControlAF;
import model.UnknownArgumentError;
import util.RandomGen;

/**
 * Returns root completions for a CAF (with no probability distribution)
 * Therefore the random part is purely random:
 * p=1/2 for uncertain elements
 * p=1/3 for undirected elements
 * @author Fabrice
 *
 */
public class RandomCAFRootCompletionGenerator {
	protected ControlAF CAF;

	public RandomCAFRootCompletionGenerator(ControlAF CAF) {
		this.CAF = CAF;
	}


	/**
	 * returns a root AF containing everything but:
	 * undirected attacks
	 * and control part (since it is a root AF)
	 * THIS IS NOT A VALID COMPLETION => PRIVATE USE ONLY
	 */
	private ArgumentFramework getSkeletonMaxRootCompletion() {
		ArgumentFramework result = new ArgumentFramework();

		// put all arguments (apart from control arguments)
		// Fixed Arguments
		Set<CArgument> fixedArgs = CAF.getArgumentsByType(CArgument.Type.FIXED);
		Iterator<CArgument> itf = fixedArgs.iterator();
		while(itf.hasNext()) {
			CArgument a = itf.next();
			result.addArgument(a);
		}
		// Uncertain Arguments
		Set<CArgument> uncertainArgs = CAF.getArgumentsByType(CArgument.Type.UNCERTAIN);
		Iterator<CArgument> itu = uncertainArgs.iterator();
		while(itu.hasNext()) {
			CArgument a = itu.next();
			result.addArgument(a);
		}

		// add attacks (fixed and uncertain)
		// since control argument can only be linked to control attacks
		// this will work (all arguments are there)
		// Fixed Attacks
		Set<CAttack> fixedAtts = CAF.getAttacksByType(CAttack.Type.CERTAIN);
		Iterator<CAttack> itaf = fixedAtts.iterator();
		while(itaf.hasNext()) {
			CAttack attf = itaf.next();
			//System.out.println("adding attack from " + attf.getFrom().getName() + " to " + attf.getTo().getName());
			result.addAttack(attf);
		}
		// Uncertain Attacks
		Set<CAttack> uncertainAtts = CAF.getAttacksByType(CAttack.Type.UNCERTAIN);
		Iterator<CAttack> itua = uncertainAtts.iterator();
		while(itua.hasNext()) {
			CAttack attu = itua.next();
			result.addAttack(attu);
		}

		// Here we do not add unknown direction attacks.
		// So we do not have a valid completion. Just a skeleton
		return result;
	}

	/**
	 * This method returns a specific completion of the CAF.
	 * THE UNIQUE MAXIMUM ROOT COMPLETION
	 * No arguments from the control part
	 * All FIXED and UNCERTAIN arguments
	 * All the FIXED and UNCERTAIN attacks
	 * both directions of undirected attacks
	 */
	public ArgumentFramework getMaxRootCompletion() {
		ArgumentFramework result = this.getSkeletonMaxRootCompletion();
		// unknown direction => here we set both sides
		Set<CAttack> undirAtts = CAF.getAttacksByType(CAttack.Type.UNDIRECTED);
		for(CAttack attud : undirAtts) {
			CArgument from = (CArgument)attud.getFrom();
			CArgument to = (CArgument)attud.getTo();
			result.addAttack(new Attack(from,to));
			result.addAttack(new Attack(to,from));
		}
		return result;
	}

	/**
	 * This method returns a specific completion of the CAF.
	 * A sub maximum root completion (for undirected attacks a specific direction is randomly chosen)
	 * No arguments from the control part
	 * All FIXED and UNCERTAIN arguments
	 * All the FIXED and UNCERTAIN attacks
	 * randomly assign a direction to UNDIRECTED attacks
	 */
	public ArgumentFramework getRandomMaxRootCompletion() {
		ArgumentFramework result = this.getSkeletonMaxRootCompletion();
		// add attacks: unknown direction => here we need to decide randomly the direction
		Set<CAttack> undirAtts = CAF.getAttacksByType(CAttack.Type.UNDIRECTED);
		Iterator<CAttack> itud = undirAtts.iterator();
		while(itud.hasNext()) {
			CAttack attud = itud.next();
			CArgument from = (CArgument)attud.getFrom();
			CArgument to = (CArgument)attud.getTo();
			if(RandomGen.randomBoolean() == true) {
				result.addAttack(new Attack(from,to));
			} else {
				result.addAttack(new Attack(to,from));
			}
		}
		return result;
	}


	/**
	 * This method returns a random completion of the CAF.
	 * No arguments from the control part
	 * FIXED and UNCERTAIN arguments are chosen randomly
	 * FIXED and UNCERTAIN attacks are chosen randomly (if arguments are present)
	 * randomly assignment of a direction (or both) for UNDIRECTED attacks (if arguments are present)
	 * This is a special case of a PCAF where probabilities are 1/2 for uncertain parts
	 * and 1/3 for undirected attacks
	 */
	public ArgumentFramework getRandomRootCompletion() {
		ArgumentFramework result = new ArgumentFramework();

		// put all Fixed arguments
		Set<CArgument> fixedArgs = CAF.getArgumentsByType(CArgument.Type.FIXED);
		for(CArgument arg : fixedArgs) {
			result.addArgument(arg);
		}
		// Chose randomly Uncertain Arguments
		Set<CArgument> uncertainArgs = CAF.getArgumentsByType(CArgument.Type.UNCERTAIN);
		for(CArgument arg : uncertainArgs) {
			if(RandomGen.randomBoolean() == true) {
				result.addArgument(arg);
			}
		}
		// add fixed attacks (for sure) if both arguments are in the AF
		Set<CAttack> fixedAtts = CAF.getAttacksByType(CAttack.Type.CERTAIN);
		for(CAttack att : fixedAtts) {
			try {
				result.addAttack(att);
			} catch(UnknownArgumentError uae) {
				// we do not care if one attack is not added
			}
		}
		// Uncertain Attacks (chosen randomly) if both arguments are in the AF
		Set<CAttack> uncertainAtts = CAF.getAttacksByType(CAttack.Type.UNCERTAIN);
		for(CAttack att : uncertainAtts) {
			if(RandomGen.randomBoolean() == true) {
				try {
					result.addAttack(att);
				} catch(UnknownArgumentError uae) {
					// we do not care if one attack is not added
				}
			}
		}

		// add attacks: unknown direction => here we need to decide randomly the direction
		// or both directions
		// of course if one argument is missing, the attack is not added
		Set<CAttack> undirected = this.CAF.getAttacksByType(CAttack.Type.UNDIRECTED);
		for(CAttack att : undirected) {
			// easier to check here
			if(!result.containsArgument(att.getFrom()) || !result.containsArgument(att.getTo())) {
				continue;
			}
			CAttack reverse = new CAttack(att.getTo(), att.getFrom(), CAttack.Type.UNDIRECTED);
			int random = RandomGen.getIndex(2);
			if(random == 0) {
				result.addAttack(att);
			} else if(random == 1) {
				result.addAttack(reverse);
			} else {
				result.addAttack(att);
				result.addAttack(reverse);
			}

		}
		return result;
	}

}
