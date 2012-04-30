import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GrammarParser {
	private Terminal[] terminals;
	private Nonterminal[] nonterminals;
	private Nonterminal startToken;
	private ParsingTable parsingTable;
	
	public GrammarParser(String filename) throws IOException, FileNotFoundException {
		File inputFile = new File(filename);
		FileReader inputReader;
		inputReader = new FileReader(inputFile);
		char[] cbuf = new char[(int) inputFile.length()];
		int charCount = -1;
		charCount = inputReader.read(cbuf);
		String data = "";
		if (charCount > 0) data = new String(cbuf);
		String[] split = data.split("\n");
		terminals = generateTerminals(split);
		nonterminals = generateNonterminals(split);
		startToken = generateStartToken(split);
		addRules(split);
		System.out.println(this);
		removeLeftRecursion();
		System.out.println(this);
		createFirstSets();
		System.out.println(this);
		createFollowSets();
		System.out.println(this);
		parsingTable = new ParsingTable(removeEpsilon(terminals), nonterminals);
		System.out.println(parsingTable);
	}
	
	private Terminal[] generateTerminals(String[] data){
		String terminalsLine = findLineWith("%Tokens", data);
		if (terminalsLine == null) return null;
		String[] terminalNames = terminalsLine.split(" ");
		Terminal[] terminals = new Terminal[terminalNames.length + 1];
		for (int i = 0; i < terminals.length-1; i++) {
			terminals[i] = new Terminal(Terminal.TerminalType.valueOf(terminalNames[i]));
		}
		terminals[terminals.length - 1] = new Terminal(Terminal.TerminalType.END);
		return terminals;
	}
	
	private Nonterminal[] generateNonterminals(String[] data) {
		String nonterminalsLine = findLineWith("%Non-terminals", data);
		if (nonterminalsLine == null) return null;
		String[] nonterminalNames = nonterminalsLine.split(" ");
		Nonterminal[] nonterminals = new Nonterminal[nonterminalNames.length];
		for (int i = 0; i < nonterminalNames.length; i++) {
			nonterminals[i] = new Nonterminal(nonterminalNames[i]);
		}
		return nonterminals;
	}
	
	private Nonterminal generateStartToken(String[] data) {
		String startTokenName = findLineWith("%Start", data);
		for (Nonterminal nonterminal : nonterminals) {
			if (nonterminal.name.equals(startTokenName.trim()))	{
				startToken = nonterminal;
				break;
			}
		}
		return startToken;
	}
	
	private String findLineWith(String target, String[] data) {
		String line = null;
		for (String s : data) {
			s = s.trim();
			if (s.contains(target)) {
				line = s.replace(target, "").trim();
			}
		}
		return line;
	}
	
	private Terminal findMatchingTerminal(String name) {
		for (Terminal t : terminals) {
			if (t.name.equals(name)) return t;
		}
		return null;
	}
	
	private Nonterminal findMatchingNonterminal(String name) {
		for (Nonterminal t : nonterminals) {
			if (t.name.equals(name)) return t;
		}
		return null;
	}
	
	private void addRules(String[] data) {
		int start = -1;
		for (int i = 0; i < data.length; i++) {
			String s = data[i].trim();
			if (s.contains("%Rules")) {
				start = i+1;
			}
		}
		for (int i = start; i < data.length; i++) {
			String[] split = data[i].split(":");
			String nonterminalName = split[0].trim();
			String[] splitIntoRules = split[1].split("\\|");
			ArrayList<Rule> rules = new ArrayList<Rule>();
			for (String s : splitIntoRules) {
				String[] splitRuleString = s.split(" ");
				Token[] rule = new Token[splitRuleString.length];
				for (int j = 0; j < rule.length; j++) {
					if (splitRuleString[j].trim().length() < 1) continue;
					if (splitRuleString[j].contains("<")) {
						rule[j] = findMatchingNonterminal(splitRuleString[j].trim());
					}
					else {
						rule[j] = findMatchingTerminal(splitRuleString[j].trim());
					}
				}
				rules.add(new Rule(rule));
			}
			for (Nonterminal n : nonterminals) {
				if (n.name.equals(nonterminalName)) {
					for (Rule rule : rules) {
						n.AddRule(rule);
					}
				}
			}
 		}
	}
	
	private void removeLeftRecursion() {
		int iterations = nonterminals.length; //so we don't iterate too many times when adding to nonterminals
		for (int i = 0; i < iterations; i++) {
			System.out.println("Before iteration " + i);
			System.out.println(this);
			Nonterminal nonterminal = nonterminals[i];
			for (int j = 0; j < i; j++) {
				ArrayList<Token[]> with = new ArrayList<Token[]>();
				for (Rule rule : nonterminals[j].rules) {
					with.add(rule.rule);
				}
				Token[][] withArray = with.toArray(new Token[0][0]);
				nonterminal.replaceRule(nonterminals[j], with.toArray(new Token[0][0]));
				System.out.println("After i=" + i + " j=" + j);
				System.out.println(this);
			}
			if (nonterminal.isLeftRecursive()) {
				//Remove immediate left recursion
				Nonterminal newNonterminal = new Nonterminal(nonterminal.name+"'");
				newNonterminal.AddRule(Rule.CreateEplsilonRule());
				ArrayList<Token[]> rulesWithoutLeftRecursion = new ArrayList<Token[]>(); 
				ArrayList<Rule> rulesToDelete = new ArrayList<Rule>();
				ArrayList<Rule> rulesToAdd =  new ArrayList<Rule>();
				for (Rule rule : nonterminal.rules) {
					if (rule.rule[0].name.equals(nonterminal.name)) {
						Token[] newRuleArray = new Token[rule.rule.length];
						for (int k = 1; k < rule.rule.length; k++) {
							newRuleArray[k-1] = rule.rule[k];
						}
						newRuleArray[newRuleArray.length-1] = newNonterminal;
						newNonterminal.AddRule(new Rule(newRuleArray));
					}
					else {
						Rule newRule = new Rule(rule.rule);
						newRule.AddItem(newNonterminal);
						rulesToAdd.add(newRule);
					}
					rulesToDelete.add(rule);
				}
				for (Rule rule : rulesToDelete) {
					nonterminal.DeleteRule(rule);
				}
				for (Rule rule : rulesToAdd) {
					nonterminal.AddRule(rule);
				}
				Nonterminal[] newNonterminals = new Nonterminal[nonterminals.length + 1];
				for (int i1 = 0; i1 < nonterminals.length; i1++) {
					newNonterminals[i1] = nonterminals[i1];
				}
				newNonterminals[newNonterminals.length-1] = newNonterminal;
				nonterminals = newNonterminals;
			}
		}
	}
	
	private void leftFactor() {
		
	}
	
	private void createFirstSets() {
		boolean somethingChanged;
		do {
			somethingChanged = false;
			for (Nonterminal nonterminal : nonterminals) {
				for (Rule rule : nonterminal.rules) {
					Token[] f = null;
					int i = 0;
					do {
						f = rule.rule[i].First();
						somethingChanged = somethingChanged || nonterminal.EnsureFirstContains(f);
						i++;
					} while (f != null && containsEpsilon(f) && i < rule.rule.length);
					if (i == rule.rule.length && containsEpsilon(f)) somethingChanged = somethingChanged || nonterminal.EnsureFirstContains(new Token[]{new Terminal(Terminal.TerminalType.EPSILON)});
				}
	 		}
		} while (somethingChanged);
	}
	
	private void createFollowSets() {
		startToken.EnsureFollowContains(new Terminal[] {new Terminal(Terminal.TerminalType.END)});
		boolean somethingChanged;
		do {
			somethingChanged = false;
			for (Nonterminal nonterminal : nonterminals) {
				for (Rule rule : nonterminal.rules) {
					for (int i = 0; i < rule.rule.length; i++) {
						Token t = rule.rule[i];
						if (t instanceof Terminal) continue;
						Nonterminal n = (Nonterminal) t;
						Token n1;
						if (i == rule.rule.length - 1) {
							n1 = new Terminal(Terminal.TerminalType.EPSILON);
						}
						else {
							n1 = rule.rule[i+1];
						}
						Token[] f = n1.First();
						boolean containsE = false;
						if (containsEpsilon(f)) {
							containsE = true;
							f = removeEpsilon(f);
						}
						somethingChanged |= n.EnsureFollowContains(f);
						if (containsE) somethingChanged |= n.EnsureFollowContains(nonterminal.Follow());
					}
				}
			}
		} while (somethingChanged);
	}
	
	private boolean containsEpsilon(Token[] tokens) {
		for (Token t : tokens) {
			if (t.name.equals(Terminal.TerminalType.EPSILON.toString())) return true;
		}
		return false;
	}
	
	private Token[] removeEpsilon(Token[] tokens) {
		Token[] ret = new Token[tokens.length - 1];
		int i = 0;
		for (Token t : tokens) {
			if (t.name.equals(Terminal.TerminalType.EPSILON.toString())) continue;
			ret[i] = t;
			i++;
		}
		return ret;
	}
	
	private Terminal[] removeEpsilon(Terminal[] tokens) {
		Terminal[] ret = new Terminal[tokens.length - 1];
		int i = 0;
		for (Terminal t : tokens) {
			if (t.name.equals(Terminal.TerminalType.EPSILON.toString())) continue;
			ret[i] = t;
			i++;
		}
		return ret;
	}
	
	private void createTable() {
		
		
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("%Tokens ");
		for (Terminal terminal : terminals) {
			sb.append(terminal);
			sb.append(" ");
		}
		sb.append("\n%Non-terminals ");
		for (Nonterminal nonterminal : nonterminals) {
			sb.append(nonterminal);
			sb.append(" ");
		}
		sb.append("\n%Start ");
		sb.append(startToken);
		sb.append("\n%Rules\n");
		for (Nonterminal nonterminal : nonterminals) {
			sb.append(nonterminal);
			sb.append(" : ");
			for (int i = 0; i < nonterminal.rules.size(); i++) {
				sb.append(nonterminal.rules.get(i));
				if (i < nonterminal.rules.size()-1) sb.append("| ");
			}
			sb.append("\n");
		}
		for (Nonterminal nonterminal : nonterminals) {
			if (nonterminal.First() != null) {
				sb.append("First(" + nonterminal.name + ") = { ");
				for (Token f : nonterminal.First()) {
					sb.append(f + " ");
				}
				sb.append("}\n");
			}
		}
		for (Nonterminal nonterminal : nonterminals) {
			if (nonterminal.Follow() != null) {
				sb.append("Follow(" + nonterminal.name + ") = { ");
				for (Token f : nonterminal.Follow()) {
					sb.append(f + " ");
				}
				sb.append("}\n");
			}
		}
		return sb.toString();
	}
	
}
