
public class Rule {
	public Token[] rule;
	public Rule(Token[] rule) {
		this.rule = rule;
	}
	
	public Rule() {
		this.rule = new Token[0];
	}
	
	public static Rule CreateEplsilonRule() {
		return new Rule(new Token[] {new Terminal(Terminal.TerminalType.EPSILON)});
	}
	
	public void AddItem(Token rule) {
		Token[] newRule = new Token[this.rule.length + 1];
		for (int i = 0; i < this.rule.length; i++) {
			newRule[i] = this.rule[i];
		}
		newRule[newRule.length-1] = rule;
		this.rule = newRule;
	}
	
//	public void ReplaceItem(Token replace, Token[] with) {
//		int i = 0;
//		boolean found = false;
//		while (i < rule.length) {
//			if (rule[i].name.equals(replace.name)) {
//				found = true;
//				break;
//			}
//			i++;
//		}
//		Token[] newRule = new Token[rule.length + with.length];
//		if (found) {
//			for (int j = 0; j < i; j++) {
//				newRule[j] = rule[j];
//			}
//			for (int j = 0; j < with.length; j++) {
//				newRule[j+i] = with[j];
//			}
//			for (int j = i+1; j < rule.length; j++) {
//				newRule[j+with.length] = rule[j];
//			}
//		}
//	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Token token : rule) {
			sb.append(token.toString());
			sb.append(" ");
		}
		return sb.toString();
	}
}
