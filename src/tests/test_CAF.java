package tests;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import generators.ControllabilityEncoder;
import generators.QBFQDIMACSConverter;
import generators.SATQDIMACSConverter;
import generators.StrongQBFEncoder;
import generators.StrongSATEncoder;
import logic.pl.SatFormula;
import logic.qbf.QBFFormula;
import model.ArgumentFramework;
import model.ControlAF;
import model.StableControlConfiguration;
import model.SupportingPowerRecorder;
import parser.CAFParser;
import solvers.Monte_Carlo_CAF_Solver;
import solvers.Monte_Carlo_CAF_Solver_Heuristic;
import util.Util;

public class test_CAF {

	private ControlAF CAF;

	public test_CAF(ControlAF CAF) {
		this.CAF = CAF;
	}

	public test_CAF() {
		this.CAF = null;
	}

	public ControlAF getCAF() {
		return this.CAF;
	}

	public void load_CAF_from_file(String file) {
		this.CAF = CAFParser.parse(file);
	}

	public void saveQDIMACSToFile(String file, int type) {
		StrongQBFEncoder encoder = new StrongQBFEncoder(this.CAF);

		QBFFormula qbf = encoder.encode(type);
		QBFQDIMACSConverter converter = new QBFQDIMACSConverter(qbf);

		try {
			Util.saveToFile(converter.toQDimacs(), file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void saveSATQDIMACSToFile(String file, ArgumentFramework instance) {
		StrongSATEncoder encoder = new StrongSATEncoder(instance, this.CAF);
		System.out.println("******** INSTANCE :");
		System.out.println(instance.toString());
		System.out.println("******** END INSTANCE :");
		SatFormula formula = encoder.encode(0);
		System.out.println("******** START QCIR ENCODING :");
		System.out.println(formula.getMatrix().toQCir());
		System.out.println("******** END QCIR ENCODING :");
		SATQDIMACSConverter converter = new SATQDIMACSConverter(formula);
		try {
			Util.saveToFile(converter.toQDimacs(), file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void saveQCIRToFile(String file, int type) {
		StrongQBFEncoder encoder = new StrongQBFEncoder(this.CAF);

		QBFFormula qbf = encoder.encode(type);

		try {
			Util.saveToFile(qbf.toQCIR(), file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public void solve_with_monte_carlo(int N, int type) {
		Monte_Carlo_CAF_Solver solver = new Monte_Carlo_CAF_Solver(this.CAF);

		Set<StableControlConfiguration> result = null;

		if(type == ControllabilityEncoder.CREDULOUS) {
			result = solver.getCredulousControlConfigurations(N);

			System.out.println("---------------------- CREDULOUS SOLUTIONS----------------");
			System.out.println("controlling power = " + solver.getControllingPower());
			printSolutions(result);
			System.out.println("---------------------- SUPPORTING POWER----------------");
			printSupportingPower(solver.getSupportingPowerRecorders());
		} else {
			result = solver.getSkepticalControlConfigurations(N);
			System.out.println("---------------------- SKEPTICAL SOLUTIONS----------------");
			System.out.println("controlling power = " + solver.getControllingPower());
			printSolutions(result);
			System.out.println("---------------------- SUPPORTING POWER----------------");
			printSupportingPower(solver.getSupportingPowerRecorders());
		}
	}

	public void solve_with_monte_carlo(double error, int type) {
		Monte_Carlo_CAF_Solver solver = new Monte_Carlo_CAF_Solver(this.CAF);

		Set<StableControlConfiguration> result = null;

		if(type == ControllabilityEncoder.CREDULOUS) {
			result = solver.getCredulousControlConfigurations(error);

			System.out.println("---------------------- CREDULOUS SOLUTIONS----------------");
			System.out.println("controlling power = " + solver.getControllingPower());
			System.out.println("number simulations = " + solver.getNumberSimu());
			printSolutions(result);
			System.out.println("---------------------- SUPPORTING POWER----------------");
			printSupportingPower(solver.getSupportingPowerRecorders());
		} else {
			result = solver.getSkepticalControlConfigurations(error);
			System.out.println("---------------------- SKEPTICAL SOLUTIONS----------------");
			System.out.println("controlling power = " + solver.getControllingPower());
			System.out.println("number simulations = " + solver.getNumberSimu());
			printSolutions(result);
			System.out.println("---------------------- SUPPORTING POWER----------------");
			printSupportingPower(solver.getSupportingPowerRecorders());
		}
	}

	public void solve_with_heuristic(double error, int type) {
		Monte_Carlo_CAF_Solver_Heuristic solver = new Monte_Carlo_CAF_Solver_Heuristic(this.CAF);

		Set<StableControlConfiguration> result = null;

		if(type == ControllabilityEncoder.CREDULOUS) {
			result = solver.getCredulousControlConfigurations(error);

			System.out.println("---------------------- CREDULOUS SOLUTIONS----------------");
			System.out.println("controlling power = " + solver.getControllingPower());
			System.out.println("number simulations = " + solver.getNumberSimu());
			printSolutions(result);
		} else {
			result = solver.getSkepticalControlConfigurations(error);
			System.out.println("---------------------- SKEPTICAL SOLUTIONS----------------");
			System.out.println("controlling power = " + solver.getControllingPower());
			System.out.println("number simulations = " + solver.getNumberSimu());
			printSolutions(result);
		}
	}

	private static void printSolutions(Set<StableControlConfiguration> solutions) {
		int i = 1;
		if(solutions == null) {
			return;
		}
		Iterator<StableControlConfiguration> iter = solutions.iterator();
		while(iter.hasNext()) {
			System.out.println("--------- printing solution " + i + "-----------");
			System.out.println(iter.next().toString());
			i++;
		}
	}

	private static void printSupportingPower(Map<StableControlConfiguration, SupportingPowerRecorder> recorders) {
		if(recorders == null) {
			return;
		}
		for(StableControlConfiguration scc : recorders.keySet()) {
			System.out.println("for control configuration: ");
			System.out.println(scc.toString());
			System.out.println("supporting power: ");
			System.out.println(recorders.get(scc).toString());
		}
	}
}
