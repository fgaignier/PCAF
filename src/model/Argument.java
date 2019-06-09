package model;

/*
 * generic Argument.
 * Only composed of a name
 */
public class Argument implements Comparable<Argument> {

	protected String name;
	
	public Argument(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	@Override
	public int compareTo(Argument other) {
		return this.name.compareTo(other.getName());
	}
}
