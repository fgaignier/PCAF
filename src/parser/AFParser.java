package parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import model.Argument;
import model.Attack;
import model.ArgumentFramework;
import model.UnknownArgumentError;

public class AFParser {
	public static String ARG = "arg";
	public static String ATT = "att";
			
	private String filename;

	public AFParser(String string) {
		filename = string;
	}

	/*
	 * This method will load an Argument Framework from a file and return an instance of the AF
	 * The syntax of the file is described below.
	 * all ARGUMENTS must be defined first, else the attacks cannot be constructed
	 */
	public ArgumentFramework parse() {
		try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
			ArgumentFramework instance = new ArgumentFramework();
			String currentLine;
			Argument arg;
			Map<String, Argument> loadedArguments = new HashMap<String, Argument>();
			
			while ((currentLine = br.readLine()) != null) {
				if (currentLine.startsWith(AFParser.ARG)) {
					String tmp = currentLine.split("\\(")[1];
					String arg_name = tmp.split("\\)")[0];
					arg_name = arg_name.trim();
					arg = new Argument(arg_name);
					instance.addArgument(arg);
					loadedArguments.put(arg_name, arg);
				} else if (currentLine.startsWith(AFParser.ATT)) {
					String tmp = currentLine.split("\\(")[1];
					String tmp2 = tmp.split("\\)")[0];
					String[] tab = tmp2.split(",");
					Argument argFrom = loadedArguments.get(tab[0].trim());
					Argument argTo = loadedArguments.get(tab[1].trim());
					if(argFrom == null | argTo == null) {
						throw new UnknownArgumentError("cannot find argument from or to in this attack: (" + tab[0] + "," + tab[1] + ")");
					}
					instance.addAttack(new Attack(argFrom, argTo));
				}
			}

			return instance;
		} catch (IOException e) {
			e.printStackTrace();
		}

		throw new UnsupportedOperationException();
	}

}
