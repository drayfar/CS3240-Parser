import java.util.HashMap;


public class ParsingTable {
	public HashMap<Nonterminal, HashMap<Terminal, Rule>> mnt;
	Terminal[] terminals;
	Nonterminal[] nonterminals;
	public ParsingTable(Terminal[] terminals, Nonterminal[] nonterminals) {
		this.terminals = terminals;
		this.nonterminals = nonterminals;
		mnt = new HashMap<Nonterminal, HashMap<Terminal, Rule>>();
		for (Nonterminal n : nonterminals) {
			for (Rule r : n.rules) {
				boolean eInFirst = false;
				for (Token a : r.rule[0].First()) {
					if (a.name.equals(Terminal.TerminalType.EPSILON.toString())) {
						eInFirst = true;
						continue;
					}
					if (mnt.containsKey(n)) mnt.get(n).put((Terminal) a, r);
					else {
						HashMap<Terminal, Rule> newthingy = new HashMap<Terminal, Rule>();
						newthingy.put((Terminal) a, r);
						mnt.put(n, newthingy);
					}
				}
				if (eInFirst) {
					for (Token a : n.Follow()) {
						if (mnt.containsKey(n)) mnt.get(n).put((Terminal) a, r);
						else {
							HashMap<Terminal, Rule> newthingy = new HashMap<Terminal, Rule>();
							newthingy.put((Terminal) a, r);
							mnt.put(n, newthingy);
						}
					}
				}
			}
		}
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(mnt + "\n");
		sb.append("M[N,T]\t\t|");
		for (Terminal t : terminals) {
			if (t.name.length() > 4) sb.append("  " + t.name + "\t\t\t\t|");
			else sb.append("  " + t.name + "\t\t\t\t\t|");
		}
		sb.append("\n");
		for (Nonterminal n : mnt.keySet()) {
			if (n.name.length() > 15) sb.append(n.name + "|");
			else if (n.name.length() > 9) sb.append(n.name + "\t|");
			else if (n.name.length() > 5) sb.append(n.name + "\t\t|");
			else sb.append(n.name + "\t\t\t|");
			for (Terminal t : terminals) {
				Rule r = mnt.get(n).get(t);
				if (r != null) {
					if (r.toString().length() > 29) sb.append("  " + r + " |");
					else if (r.toString().length() > 22) sb.append("  " + r + "\t|");
					else if (r.toString().length() > 19) sb.append("  " + r + "\t\t|");
					else if (r.toString().length() > 10) sb.append("  " + r + "\t\t\t|");
					else if (r.toString().length() > 5) sb.append("  " + r + "\t\t\t\t|");
					else sb.append("  " + r + "\t\t\t\t\t|");
				}
				else {
					sb.append("\t\t\t\t\t|");
				}
			}
			sb.append("\n");
		}
		return sb.toString();
	}
	
}
