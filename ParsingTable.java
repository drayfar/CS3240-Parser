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
					HashMap<Terminal, Rule> newthingy = new HashMap<Terminal, Rule>();
					newthingy.put((Terminal) a, r);
					mnt.put(n, newthingy);
				}
				if (eInFirst) {
					for (Token a : n.Follow()) {
						HashMap<Terminal, Rule> newthingy = new HashMap<Terminal, Rule>();
						newthingy.put((Terminal) a, r);
						mnt.put(n, newthingy);
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
			if (t.name.length() > 5) sb.append("  " + t.name + "\t\t\t|");
			else sb.append("  " + t.name + "\t\t\t\t|");
		}
		sb.append("\n");
		for (Nonterminal n : nonterminals) {
			if (n.name.length() > 9) sb.append(n.name + "\t|");
			else if (n.name.length() > 5) sb.append(n.name + "\t\t|");
			else sb.append(n.name + "\t\t\t|");
			String item = "";
			for (Terminal t : terminals) {
				Rule r = mnt.get(n).get(t);
				if (r != null) {
					if (r.toString().length() > 10) item += "  " + r + "\t\t|";
					else if (r.toString().length() > 5) item += "  " + r + "\t\t\t|";
					else item += "  " + r + "\t\t\t\t|";
				}
				else item += "\t\t\t\t|";
			}
			sb.append(item);
			sb.append("\n");
		}
		return sb.toString();
	}
	
}
