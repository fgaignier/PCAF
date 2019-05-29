package builders;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import generators.CAFGenerator;
import model.ArgumentFramework;
import model.ControlAF;
import parser.AFParser;

/**
 * mass production of CAF from regular Argument Framework database
 * @author Fabrice
 *
 */
public class Caf_builder { 

	public static String ext = "apx";
	public static String subfolder = "caf";
	
	/**
	 * from the AF filename will create the corresponding filename for CAF
	 * in the caf subdirectory
	 * @param filename
	 * @return
	 */
	public static String convertToApx(String filename, int target_size) {
		StringBuffer result = new StringBuffer();
		String[] path = filename.split("\\\\");
		String name = path[path.length-1];
		String[] no_ext = name.split("\\.");
		String root = filename.replaceAll(name, "");
		result.append(root);
		result.append(subfolder);
		result.append("\\");
		result.append(no_ext[0]);
		result.append("_T");
		result.append(target_size);
		result.append(".");
		result.append(ext);
		//System.out.println(result.toString());
		return result.toString();
	}
	
	public static void build_CAF_from_AF(String path, int pArgF, int pArgU, int pAttF, int pAttU, int target_size) {
		try {
			Stream<Path> names = Files.list(Paths.get(path))
					.filter(Files::isRegularFile);
			names.forEach(n-> {

				StringBuffer log = new StringBuffer();
				ArgumentFramework af = AFParser.parse(n.toString());

				CAFGenerator generator = new CAFGenerator(af, pArgF, pArgU, pAttF, pAttU);
				ControlAF caf = generator.generate(target_size);
				if(caf == null) {
					System.out.println("could not build CAF from file:");
					System.out.println(n.toString());
				} else {
					log.append(caf.toString());
					try {
						util.Util.saveToFile(log.toString(), convertToApx(n.toString(), target_size));
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
				}

			});
		} catch (IOException e) {
			e.printStackTrace();
		} 

	}

	/**
	 * need the location directory of AF files as parameter
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length <2) {
			System.out.println("need to give the directory where AF files are located and the target size");
			System.exit(1);
		}
		String directory = args[0];
		int target_size = Integer.parseInt(args[1]);
		build_CAF_from_AF(directory, 50, 30, 40, 30, target_size);
		
	}
}


