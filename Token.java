
public abstract class Token {
	public String name;
	
	public String toString() {
		return name;
	}
	
	public abstract Token[] First();
	
	public abstract Token[] Follow();
	
	@Override
	public boolean equals(Object t) {
		if (!(t instanceof Token)) return false;
		return ((Token)t).name.equals(this.name);
	}
	
	@Override
	public int hashCode() {
		return this.name.hashCode();
	}
}
