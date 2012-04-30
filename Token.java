
public abstract class Token {
	public String name;
	
	public String toString() {
		return name;
	}
	
	public abstract Token[] First();
	
	public abstract Token[] Follow();
	
	public boolean equals(Token t) {
		return t.name.equals(this.name);
	}
}
