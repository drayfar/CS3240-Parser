import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GrammarParser {
	private Terminal[] terminals;
	private Nonterminal[] nonterminals;
	private Nonterminal startToken;
	//THIS IS THE PARSING TABLE (implemented as a 2D hashmap)
	public ParsingTable parsingTable;
	
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
		removeLeftRecursion();
		leftFactor();
		createFirstSets();
		createFollowSets();
		parsingTable = new ParsingTable(removeEpsilon(terminals), nonterminals);
		System.out.println(this);
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
			Nonterminal nonterminal = nonterminals[i];
			for (int j = 0; j < i; j++) {
				ArrayList<Token[]> with = new ArrayList<Token[]>();
				for (Rule rule : nonterminals[j].rules) {
					with.add(rule.rule);
				}
				Token[][] withArray = with.toArray(new Token[0][0]);
				nonterminal.replaceRule(nonterminals[j], with.toArray(new Token[0][0]));
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
		boolean somethingChanged;
		do {
			somethingChanged = false;
			for (Nonterminal A : nonterminals) {
				if (A.rules.size() < 2) continue;
				ArrayList<ArrayList<Token>> prefixes = new ArrayList<ArrayList<Token>>(A.rules.size());
				//Populate the ArrayLists
				for (Rule rule : A.rules) {
					ArrayList<Token> r = new ArrayList<Token>(rule.rule.length);
					for (Token t : rule.rule) {
						r.add(t);
					}
					prefixes.add(r);
				}
				//Find the longest prefix that matches 2 or more
				int maxMatch = 0;
				List<Token> maxPrefix = null;
				for (ArrayList<Token> prefix : prefixes) {
					for (int i = prefix.size(); i > 0; i--) {
						boolean match = false;
						for (ArrayList<Token> other : prefixes) {
							if (prefix == other || i > other.size()) continue;
							if (prefix.subList(0, i).equals(other.subList(0, i))) {
								match = true;
								break;
							}
						}
						if (match && i > 0 && i > maxMatch) { 
							maxMatch = i;
							maxPrefix = prefix.subList(0, i);
							break;
						}
					}
					if (maxPrefix != null) break;
				}
				if (maxPrefix != null && maxPrefix.size() > 0 && !(maxPrefix.size() == 1 && maxPrefix.get(0).name.equals(Terminal.TerminalType.EPSILON.toString()))) {
					somethingChanged = true;
					ArrayList<Integer> matchingRules = new ArrayList<Integer>();
					for (int i = 0; i < A.rules.size(); i++) {
						Rule r = A.rules.get(i);
						boolean match = true;
						for (int i1 = 0; i1 < maxPrefix.size(); i1++) {
							if (r.rule.length <= i1 || !r.rule[i1].equals(maxPrefix.get(i1))) match = false;
						}
						if (match) {
							matchingRules.add(i);
						}
					}
					boolean newNonterminalIsNew = true;
					Nonterminal newNonterminal = null;
					for (Nonterminal n : nonterminals) {
						if (n.name.equals(A.name + "'")) {
							newNonterminal = n;
							newNonterminalIsNew = false;
						}
					}
					if (newNonterminalIsNew) newNonterminal = new Nonterminal(A.name + "'");
					for (Integer i : matchingRules) {
						Rule r = A.rules.get(i);
						Rule newRule = new Rule();
						newRule.rule = new Token[r.rule.length - maxPrefix.size()];
						for (int i1 = maxPrefix.size(); i1 < r.rule.length; i1++) {
							newRule.rule[i1-maxPrefix.size()] = r.rule[i1];
						}
						if (newRule.rule.length == 0) {
							Terminal epsilon = null;
							for (Terminal t : terminals) {
								if (t.name.equals(Terminal.TerminalType.EPSILON.toString())) epsilon = t;
							}
							if (epsilon == null) {
								epsilon = new Terminal(Terminal.TerminalType.EPSILON);
								Terminal[] newTerminals = new Terminal[terminals.length + 1];
								for (int j = 0; j < terminals.length; j++) {
									newTerminals[j] = terminals[j];
								}
								newTerminals[terminals.length] = epsilon;
								terminals = newTerminals;
							}
							newRule.rule = new Token[]{epsilon};
						}
						newNonterminal.AddRule(newRule);
					}
					Rule newRule = new Rule();
					newRule.rule = new Token[maxPrefix.size() + 1];
					for (int i = 0; i < maxPrefix.size(); i++) {
						newRule.rule[i] = maxPrefix.get(i);
					}
					newRule.rule[newRule.rule.length - 1] = newNonterminal;
					ArrayList<Rule> rulesToRemove = new ArrayList<Rule>();
					for (int i : matchingRules) {
						rulesToRemove.add(A.rules.get(i));
					}
					for (Rule r : rulesToRemove) A.rules.remove(r);
					A.rules.add(newRule);
					if (newNonterminalIsNew) {
						Nonterminal[] newNonterminals = new Nonterminal[nonterminals.length + 1];
						for (int i = 0; i < nonterminals.length; i++) {
							newNonterminals[i] = nonterminals[i];
						}
						newNonterminals[nonterminals.length] = newNonterminal;
						nonterminals = newNonterminals;
					}
				}
			}
		} while (somethingChanged);
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
		Terminal end = null;
		for (Terminal t : terminals) {
			if (t.name.equals(Terminal.TerminalType.END.toString())) end = t;
		}
		startToken.EnsureFollowContains(new Token[] {end}); //END
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
			if (i < ret.length) {
				ret[i] = t;
			}
			i++;
		}
		return ret;
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
