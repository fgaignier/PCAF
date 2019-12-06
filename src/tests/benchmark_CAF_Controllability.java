package tests;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Stream;

import generators.ControllabilityEncoder;
import model.ControlAF;
import model.StableControlConfiguration;
import parser.CAFParser;
import solvers.Monte_Carlo_CAF_CSP_Solver;

/**
 * must indicate the error level
 * must indicate CREDULOUS vs SKEPTICAL
 * @author Fabrice
 *
 */
public class benchmark_CAF_Controllability {
	public static String log_file = "controllability.txt";
	public static String stats_file = "controllability_stats.csv";
	public static String prefixCred = "CRE";
	public static String prefixSkep = "SKE";
	public static double error = 0.01;
	public static String csv_sep = ";";

	public static void claculate_cc(String path, int type) {
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
				//System.out.println(caf.toString());
				// here we just check controllability of CAF. Controlling power will therefore
				// be 1 (if success) or -2 (fail)
				Monte_Carlo_CAF_CSP_Solver solver = new Monte_Carlo_CAF_CSP_Solver(caf);
				
				Set<StableControlConfiguration> ccs = null;
				//STARTS THE TIMER JUST BEFORE CALCULATION
				timer.start();
				if(type == ControllabilityEncoder.CREDULOUS) {
					ccs = solver.getCredulousControlConfigurations(error);
				} else {
					ccs = solver.getSkepticalControlConfigurations(error);
				}
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
				
				if(ccs != null) {
					logControlConfigurations(ccs, log, stats_csv);
				} else {
					log.append("not controllable");
					log.append(System.getProperty("line.separator"));
				}
				log.append("####################");
				log.append(System.getProperty("line.separator"));
				
				stats_csv.append(System.getProperty("line.separator"));
				
			});
			if(type == ControllabilityEncoder.CREDULOUS) {
				util.Util.saveToFile(log.toString(), path + "\\result\\" + prefixCred + log_file);
				util.Util.saveToFile(stats_csv.toString(), path + "\\result\\" + prefixCred + stats_file);
			} else {
				util.Util.saveToFile(log.toString(), path + "\\result\\" + prefixSkep + log_file);
				util.Util.saveToFile(stats_csv.toString(), path + "\\result\\" + prefixSkep + stats_file);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void logControlConfigurations(Set<StableControlConfiguration> ccs, StringBuffer log, StringBuffer stats_csv) {
		int i=1;
		for(StableControlConfiguration cc : ccs) {
			log.append("######### mpce " + i + " ###########");
			log.append(System.getProperty("line.separator"));
			log.append(cc.toString());
			log.append(System.getProperty("line.separator"));
			
			stats_csv.append(cc.toString());
			stats_csv.append(csv_sep);
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
		if(args.length < 2) {
			System.out.println("parameters: directory type (0=CREDULOUS, 1=SKEPTICAL)");
			System.exit(1);
		}
		claculate_cc(args[0], Integer.parseInt(args[1]));
		
	}
}
