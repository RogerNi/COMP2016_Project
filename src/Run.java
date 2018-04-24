import java.util.Scanner;

public class Run {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Graph graph = new Graph();
		System.out.println("Input commands to create a graph");
		Scanner in = new Scanner(System.in);
		String op = "";
		while (true) {
			System.out.print(">");
			op = in.nextLine();
			String[] ops = op.split(" ");
			try {
				switch (ops[0]) {
				case "vertex":
					if (!graph.addVertex(ops[1]))
						System.out.println("Vertex " + ops[1] + " already exists!");
					break;
				case "edge":
					int result = graph.addEdge(Integer.valueOf(ops[1]), ops[2], ops[3]);
					if (result == -1)
						System.out.println("Vertex " + ops[2] + " Not Exists!");
					if (result == -2)
						System.out.println("Edge already exists!");
					if (result == -3)
						System.out.println("Vertex " + ops[3] + " Not Exists!");
					break;
				case "weight":
					if (!graph.modifyEdge(Integer.valueOf(ops[1]), ops[2], ops[3])) {
						System.out.println("Edge Not Found!");
					}
					break;
				case "-vertex":
					if (!graph.delVertex(ops[1]))
						System.out.println("Vertex Not Found!");
					break;
				case "-edge":
					if (!graph.delEdge(ops[1], ops[2])) {
						System.out.println("Edge Not Found!");
					}
					break;
				case "contents":
					System.out.println(graph.showContents());
					break;
				case "topo":
					String[] out = graph.topo();
					if (out == null)
						System.out.println("Graph is not a DAG!");
					else {
						for (int i = 0; i < out.length; i++) {
							System.out.print(out[i] + " ");
						}
						System.out.println();
					}
					break;
				case "path":
					System.out.print(graph.dijkstra(ops[1]));
					break;
				default:
					System.out.println("Input Invalid!");
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Input Invalid!");
			}
		}
	}
}
