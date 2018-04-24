import java.util.LinkedList;

public class Graph {
	class Node {
		String name;
		int weight;

		Node(String name, int weight) {
			this.name = name;
			this.weight = weight;
		}

		Node(String name) {
			this.name = name;
			weight = 0;
		}
	}

	public String getVertexName(LinkedList<Node> list) {
		try {
			return list.get(0).name;
		} catch (Exception e) {
			return "";
		}
	}

	LinkedList<LinkedList<Node>> adjacencyList;
	HashTable hashTable;

	public Graph() {
		this.adjacencyList = new LinkedList<LinkedList<Node>>();
		this.hashTable = new HashTable(this);
	}

	private int locateVertex(String name) {
		for (int i = 0; i < this.adjacencyList.size(); i++) {
			if (this.adjacencyList.get(i).get(0).name.equals(name)) {
				return i;
			}
		}
		return -1;
	}

	private LinkedList<Node> locateVertexWithHash(String name) {
		return hashTable.get(name);
	}

	private Node locateEdge(String A, String B) {
		LinkedList<Node> subList = this.locateVertexWithHash(A);
		for (Node node : subList) {
			if (node.name.equals(B)) {
				return node;
			}
		}
		return null;
	}

	public boolean addVertex(String name) {
		if (this.locateVertexWithHash(name) != null)
			return false;
		this.adjacencyList.add(new LinkedList<Node>());
		this.adjacencyList.getLast().add(new Node(name));
		hashTable.hash(this.adjacencyList.getLast());
		return true;
	}

	public boolean delVertex(String name) {
		LinkedList<Node> list = this.locateVertexWithHash(name);
		if (list == null)
			return false;
		else {
			hashTable.rmHash(name);
			this.adjacencyList.remove(list);
			delAll(name);
			return true;
		}
	}

	private void delAll(String name) {
		for (int i = 0; i < this.adjacencyList.size(); i++) {
			LinkedList<Node> list = this.adjacencyList.get(i);
			int size = list.size();
			for (int j = 0; j < size; j++) {
				if (list.get(j).name.equals(name))
					list.remove(j);
			}
		}
	}

	public int addEdge(int weight, String A, String B) {
		LinkedList<Node> list = this.locateVertexWithHash(A);
		if (list == null)
			return -3;
		// Vertex B Not Exists!
		if (this.locateVertexWithHash(B) == null)
			return -1;
		// Vertex A Not Exists!
		Node node = this.locateEdge(A, B);
		if (node != null)
			return -2;
		// Edge A to B Exists!

		list.add(new Node(B, weight));
		return 1;
		// Successful
	}

	public boolean modifyEdge(int weight, String A, String B) {
		Node node = this.locateEdge(A, B);
		if (node == null)
			return false;
		node.weight = weight;
		return true;
	}

	public boolean delEdge(String A, String B) {
		Node node = this.locateEdge(A, B);
		if (node == null)
			return false;
		this.locateVertexWithHash(A).remove(node);
		return true;
	}

	public String showContents() {
		String out = "";
		out += "vertex";
		for (LinkedList<Node> list : this.adjacencyList) {
			out += " ";
			out += list.get(0).name;
		}
		for (int i = 0; i < this.adjacencyList.size(); i++) {
			for (int j = 0; j < this.adjacencyList.get(i).size(); j++) {
				if (j != 0) {
					Node node = this.adjacencyList.get(i).get(j);
					out += "\nedge " + node.weight + " " + this.adjacencyList.get(i).get(0).name + " " + node.name;
				}
			}
		}
		return out;
	}

	private int[] indegreeTable() {
		int[] table = new int[this.adjacencyList.size()];
		for (int i = 0; i < this.adjacencyList.size(); i++) {
			LinkedList<Node> list = this.adjacencyList.get(i);
			for (int j = 0; j < list.size(); j++) {
				if (j != 0) {
					table[this.locateVertex(list.get(j).name)]++;
				}
			}
		}
		return table;
	}

	public String[] topo() {
		String[] out = new String[this.adjacencyList.size()];
		int outWritePosition = 0;
		int[] table = this.indegreeTable();
		int currLocation = 0;
		while (true) {
			currLocation = locateAndMinusZero(table);
			if (currLocation < 0)
				break;
			out[outWritePosition] = this.adjacencyList.get(currLocation).get(0).name;
			outWritePosition++;
		}
		if (outWritePosition < out.length - 1)
			return null;
		return out;
	}

	private int locateAndMinusZero(int[] table) {
		for (int i = 0; i < table.length; i++) {
			if (table[i] == 0) {
				LinkedList<Node> list = this.adjacencyList.get(i);
				for (int j = 0; j < list.size(); j++) {
					if (j != 0) {
						table[this.locateVertex(list.get(j).name)]--;
					}
				}
				table[i] = -1;
				return i;
			}
		}
		return -1;
	}

	public String dijkstra(String vertex) {
		String out = "";
		int from = this.locateVertex(vertex);
		if (from < 0)
			return "Vertex not found!\n";
		int[][] table = this.dijkstraTable(from);

		for (int i = 0; i < this.adjacencyList.size(); i++) {
			out += this.findPathAndMin(table, from, i) + "\n";
		}
		return out;
	}

	private String findPathAndMin(int[][] table, int from, int to) {
		String out;
		if (table[1][to] < 0)
			out = " (unreachable)";
		else
			out = " (" + table[1][to] + ")";
		while (true) {
			if (table[2][to] < 0) {
				out = this.adjacencyList.get(to).get(0).name + out;
				break;
			}
			if (to == from) {
				out = this.adjacencyList.get(to).get(0).name + out;
				break;
			}
			out = " > " + this.adjacencyList.get(to).get(0).name + out;
			to = table[2][to];
		}
		return out;
	}

	private int[][] dijkstraTable(int vertexNum) {
		int[][] table = new int[3][this.adjacencyList.size()];
		int known = 0;
		for (int i = 0; i < table[0].length; i++) {
			table[0][i] = 0;
			table[1][i] = -1;
			table[2][i] = -1;
		}
		table[1][vertexNum] = 0;
		table[2][vertexNum] = -2;
		while (known < table[0].length) {
			int minLocation = this.findMinDistUnkonwn(table);
			table[0][minLocation] = 1;

			int currDist = table[1][minLocation];
			if (currDist < 0) {
				known++;
				continue;
			}
			LinkedList<Node> list = this.adjacencyList.get(minLocation);
			for (Node n : list) {
				if (n.weight != 0) {
					int currLocation = this.locateVertex(n.name);
					int dist = currDist + n.weight;
					if (this.compareWithMinusOneAsBiggest(dist, table[1][currLocation]) < 0) {
						table[1][currLocation] = dist;
						table[2][currLocation] = minLocation;
					}
				}
			}
			known++;
		}
		return table;
	}

	private int findMinDistUnkonwn(int[][] table) {
		int min = -1;
		int minLocation = -1;
		for (int i = 0; i < table[0].length; i++) {
			if (table[0][i] == 0) {
				if (this.compareWithMinusOneAsBiggest(min, table[1][i]) >= 0) {
					min = table[1][i];
					minLocation = i;
				}
			}
		}
		return minLocation;
	}

	private int compareWithMinusOneAsBiggest(int a, int b) {
		if (a == -1)
			if (b == -1)
				return 0;
			else
				return 1;
		else if (b == -1)
			return -1;
		else
			return a - b;
	}

}
