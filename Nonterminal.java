import java.util.ArrayList;


public class Nonterminal extends Token {
	public ArrayList<Rule> rules;
	private ArrayList<Token> first;
	private ArrayList<Token> follow;
	
	public Nonterminal(String name) {
		this.name = name;
		rules = new ArrayList<Rule>();
		first = new ArrayList<Token>();
		follow = new ArrayList<Token>();
	}
	
	public Token[] First() {
		return first.toArray(new Token[]{});
	}
	
	public Token[] Follow() {
		return follow.toArray(new Token[]{});
	}
	
	public boolean EnsureFirstContains(Token[] set) {
		boolean somethingChanged = false;
		for (Token t : set) {
			if (t.equals(this) || firstContains(t)) continue;
			first.add(t);
			somethingChanged = true;
		}
		return somethingChanged;
	}
	
	private boolean firstContains(Token t) {
		for (Token f : first) {
			if (f.equals(t)) return true;
		}
		return false;
	}
	
	public boolean EnsureFollowContains(Token[] set) {
		boolean somethingChanged = false;
		for (Token t : set) {
			if (t.equals(this) || followContains(t)) continue;
			follow.add(t);
			somethingChanged = true;
		}
		return somethingChanged;
	}
	
	private boolean followContains(Token t) {
		for (Token f : follow) {
			if (f.equals(t)) return true;
		}
		return false;
	}
	
	public void AddRule(Rule rule) {
		Rule cleanRule = cleanRule(rule);
		rules.add(cleanRule);
	}
	
	public void DeleteRule(Rule rule) {
		rules.remove(rule);
	}
	
	private Rule cleanRule(Rule rule) {
		ArrayList<Token> cleanRule = new ArrayList<Token>(rule.rule.length);
		for (Token token : rule.rule) {
			if (token != null) {
				cleanRule.add(token);
			}
		}
		return new Rule(cleanRule.toArray(new Token[cleanRule.size()]));
	}
	
	public boolean isLeftRecursive() {
		for (Rule rule : rules){
			if (rule.rule[0].name.equals(name)) {
				return true;
			}
		}
		return false;
	}
	
	public void replaceRule(Token replace, Token[][] with) {
		ArrayList<Rule> rulesToDelete = new ArrayList<Rule>();
		ArrayList<Rule> rulesToAdd = new ArrayList<Rule>();
		for (Rule rule : rules) {
			if (rule.rule[0].name.equals(replace.name)) {
				for (Token[] w : with) {
					Token[] newRuleArray = new Token[rule.rule.length + w.length - 1];
					for (int i = 0; i < w.length; i++) {
						newRuleArray[i] = w[i];
					}
					for (int i = w.length; i < newRuleArray.length; i++) {
						newRuleArray[i] = rule.rule[i - w.length + 1];
					}
					rulesToAdd.add(new Rule(newRuleArray));
				}
				rulesToDelete.add(rule);
			}		
		}
		for (Rule rule : rulesToDelete) {
			this.DeleteRule(rule);
		}
		for (Rule rule : rulesToAdd) {
			this.AddRule(rule);
		}
	}
}
