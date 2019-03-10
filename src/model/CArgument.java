package model;

/*
 * CArgument is a CAF Argument
 * As such it extends Argument
 * We add a type to model the king of argument
 * FIXED, CONTROL, UNCERTAIN
 */
public class CArgument extends Argument {
	
	protected Type type;
	
	public enum Type {
		FIXED,
		CONTROL,
		UNCERTAIN
	}
	 
	public CArgument(String name, Type type) {
		super(name);
		this.type = type;
	}

	public Type getType() {
		return type;
	}
}
