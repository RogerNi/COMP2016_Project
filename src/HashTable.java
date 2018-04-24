import java.util.LinkedList;

public class HashTable {
	LinkedList<Graph.Node>[] table;
	Graph graph;

	public HashTable(Graph graph) {
		table = new LinkedList[7];
		this.graph = graph;
	}

	public void hash(LinkedList<Graph.Node> list) {
		int hashCode = graph.getVertexName(list).hashCode();
		hashCode %= table.length;
		int initHashCode = hashCode;
		int skip = 0;
		while (table[hashCode] != null) {
			skip++;
			hashCode += skip * skip;
			hashCode %= table.length;
			if (hashCode == initHashCode) {
				extend();
				hash(list);
				break;
			}
		}
		table[hashCode] = list;
	}

	public LinkedList<Graph.Node> get(String s) {
		int hashCode = s.hashCode() % table.length;
		int skip = 0;
		int initHashCode = hashCode;
		while (!graph.getVertexName(table[hashCode]).equals(s)) {
			skip++;
			hashCode += skip * skip;
			hashCode %= table.length;
			if (hashCode == initHashCode)
				return null;
		}
		return table[hashCode];
	}

	public boolean rmHash(String s) {
		int hashCode = s.hashCode() % table.length;
		int skip = 0;
		int initHashCode = hashCode;
		while (!graph.getVertexName(table[hashCode]).equals(s)) {
			skip++;
			hashCode += skip * skip;
			hashCode %= table.length;
			if (hashCode == initHashCode) {
				return false;
			}
		}
		table[hashCode] = null;
		return true;
	}

	private void extend() {
		LinkedList<Graph.Node>[] tempTable = table;
		table = new LinkedList[nextPrime(tempTable.length)];
		for (LinkedList list : tempTable) {
			hash(list);
		}
	}

	private int nextPrime(int thisPrime) {
		int out = 2 * thisPrime;
		while (true) {
			if (isPrime(out))
				return out;
			out++;
		}
	}

	private boolean isPrime(int thisNum) {
		for (int i = 2; i < Math.sqrt(thisNum); i++) {
			if (thisNum % i == 0)
				return false;
		}
		return true;
	}

}
