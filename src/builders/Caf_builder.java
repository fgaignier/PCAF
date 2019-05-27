package builders;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Stream;

import generators.CAFGenerator;
import model.ArgumentFramework;
import model.ControlAF;
import model.StableExtension;
import parser.AFParser;
import solvers.CSP_AF_Solver;

public class Caf_builder { 

	public static String ext = "apx";
	public static String subfolder = "caf";
	
	/**
	 * from the AF filename will create the corresponding filename for CAF
	 * in the caf subdirectory
	 * @param filename
	 * @return
	 */
	public static String convertToApx(String filename) {
		StringBuffer result = new StringBuffer();
		String[] path = filename.split("\\\\");
		String name = path[path.length-1];
		String[] no_ext = name.split("\\.");
		String root = filename.replaceAll(name, "");
		result.append(root);
		result.append(subfolder);
		result.append("\\");
		result.append(no_ext[0]);
		result.append(".");
		result.append(ext);
		System.out.println(result.toString());
		return result.toString();
	}
	
	public static void build_CAF_from_AF(String path, int pArgF, int pArgU, int pAttF, int pAttU) {
		StringBuffer log = new StringBuffer();
		try {
			Stream<Path> names = Files.list(Paths.get(path))
					.filter(Files::isRegularFile);
			names.forEach(n-> {
				ArgumentFramework af = AFParser.parse(n.toString());
				CAFGenerator generator = new CAFGenerator(af, pArgF, pArgU, pAttF, pAttU);
				ControlAF caf = generator.generate();
				log.append(caf.toString());
				try {
					util.Util.saveToFile(log.toString(), convertToApx(n.toString()));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
							});
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

	public static void main(String[] args) {
		build_CAF_from_AF("C:\\Users\\Fabrice\\eclipse-workspace\\PCAF\\tests\\barabasi\\20000", 50, 30, 40, 30);
		
	}
}


