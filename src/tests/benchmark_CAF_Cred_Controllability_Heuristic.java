package tests;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Stream;

import model.ControlAF;
import model.StableControlConfiguration;
import parser.CAFParser;
import solvers.Monte_Carlo_CAF_Solver_Heuristic;

/**
 * must indicate the error level
 * must indicate CREDULOUS vs SKEPTICAL
 * @author Fabrice
 *
 */
public class benchmark_CAF_Cred_Controllability_Heuristic {
	public static String log_file = "controllabilityH.txt";
	public static String stats_file = "controllabilityH_stats.csv";
	public static double error = 0.01;
	public static String csv_sep = ";";

	public static void claculate_cc(String path) {
		StringBuffer log = new StringBuffer();
		StringBuffer stats_csv = new StringBuffer();
		stats_csv.append(getHeader());
		util.Timer timer = new util.Timer();
		try {
			Stream<Path> names = Files.list(Paths.get(path))
					.filter(Files::isRegularFile);
			names.forEach(n-> {
				System.out.println(n.toString());
				log.append(n.toString());
				log.append(System.getProperty("line.separator"));
				stats_csv.append(n.toString());
				stats_csv.append(csv_sep);
				ControlAF caf = CAFParser.parse(n.toString());
				// here we just check controllability of CAF. Controlling power will therefore
				// be 1 (if success) or -2 (fail)
				Monte_Carlo_CAF_Solver_Heuristic solver = new Monte_Carlo_CAF_Solver_Heuristic(caf);
				//STARTS THE TIMER JUST BEFORE CALCULATION
				timer.start();
				Set<StableControlConfiguration> ccs = solver.getCredulousControlConfigurations(error);
				// STOPS THE TIMER AND GET CALCULATION TIME
				long time = timer.stop();
				
				// log
				log.append("total time: " + time + " miliseconds");
				log.append(System.getProperty("line.separator"));
				log.append("controlling power = " + solver.getControllingPower());
				log.append(System.getProperty("line.separator"));
				log.append("total number of simulations = " + solver.getNumberSimu());
				System.out.println("controlling power = " + solver.getControllingPower());
				System.out.println("total number of simulations = " + solver.getNumberSimu());
				log.append(System.getProperty("line.separator"));

				// stats
				stats_csv.append(caf.getTargetSize());
				stats_csv.append(csv_sep);
				stats_csv.append(solver.getControllingPower());
				stats_csv.append(csv_sep);
				stats_csv.append(solver.getNumberSimu());
				stats_csv.append(csv_sep);
				stats_csv.append(time);
				stats_csv.append(csv_sep);
				stats_csv.append(System.getProperty("line.separator"));
				
				if(ccs != null) {
					logControlConfigurations(ccs, log);
				} else {
					log.append("not controllable");
				}
				log.append("####################");
				log.append(System.getProperty("line.separator"));
			});
			util.Util.saveToFile(log.toString(), path + "\\" + log_file);
			util.Util.saveToFile(stats_csv.toString(), path + "\\" + stats_file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void logControlConfigurations(Set<StableControlConfiguration> ccs, StringBuffer log) {
		int i=1;
		for(StableControlConfiguration cc : ccs) {
			log.append("######### mpce " + i + " ###########");
			log.append(System.getProperty("line.separator"));
			log.append(cc.toString());
			log.append(System.getProperty("line.separator"));
			i++;
		}
	}
	
	public static String getHeader() {
		StringBuffer result = new StringBuffer();
		result.append("file");
		result.append(csv_sep);
		result.append("target size");
		result.append(csv_sep);
		result.append("power");
		result.append(csv_sep);
		result.append("simu");
		result.append(csv_sep);
		result.append("time");
		result.append(System.getProperty("line.separator"));
		return result.toString();
	}
	public static void main(String[] args) {
		if(args.length < 1) {
			System.out.println("must give the target directory as parameter");
			System.exit(1);
		}
		claculate_cc(args[0]);
		
	}
}
