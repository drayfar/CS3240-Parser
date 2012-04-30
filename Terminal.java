
public class Terminal extends Token {
	public static enum TerminalType {BEGIN, END, PRINT, LEFTPAR, RIGHTPAR, SEMICOLON, ID, ASSIGN, READ, COMMA, INTNUM, PLUS, MINUS, MULTIPLY, MODULO, EPSILON};
	private TerminalType type;
	
	public Terminal(TerminalType type) {
		this.type = type;
		this.name = type.toString();
	}
	
	public Token[] First() {
		return new Token[] {this};
	}
	
	public Token[] Follow() {
		return null;
	}
}
