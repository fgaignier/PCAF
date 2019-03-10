package model;

/*
 * generic Argument.
 * Only composed of a name
 */
public class Argument {

	protected String name;
	
	public Argument(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}
