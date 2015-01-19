package test.java;

import static org.junit.Assert.*;

import org.graphstream.algorithm.community.MySandSharc;
import org.graphstream.algorithm.community.MySandSharc2;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.junit.Test;

public class SandSharcTest {

	@Test
	public void test() {
		MySandSharc2 sharc = new MySandSharc2();
		Graph graph = new SingleGraph("Tutorial 1");
		graph.addNode("A" );
		graph.addNode("B" );
		graph.addEdge("AB", "A", "B");
		Double nsim = sharc.neighborhood_similarity(graph.getNode("A"), graph.getNode("B"));
		System.out.println("AB nsim(AB) " + nsim);
//		assertEquals(1, nsim);
	}

	@Test
	public void test2() {
		MySandSharc2 sharc = new MySandSharc2();
		Graph graph = new SingleGraph("Tutorial 1");
		graph.addNode("A" );
		graph.addNode("B" );
		graph.addNode("C" );
		graph.addEdge("AB", "A", "B");
		graph.addEdge("BC", "B", "C");
		graph.addEdge("CA", "C", "A");
		Double nsim = sharc.neighborhood_similarity(graph.getNode("A"), graph.getNode("B"));
		System.out.println("ABC nsim(AB) " + nsim);
//		assertEquals(1, nsim);
	}
	
	@Test
	public void test3() {
		MySandSharc2 sharc = new MySandSharc2();
		Graph graph = new SingleGraph("Tutorial 1");
		graph.addNode("A" );
		graph.addNode("B" );
		graph.addNode("C" );
		graph.addNode("D" );
		graph.addEdge("AB", "A", "B");
		graph.addEdge("BC", "B", "C");
		graph.addEdge("BD", "B", "D");
		Double nsim = sharc.neighborhood_similarity(graph.getNode("A"), graph.getNode("B"));
		System.out.println("ABCD nsim(AB) " + nsim);
//		assertEquals(1, nsim);
	}
	
	@Test
	public void test4() {
		MySandSharc2 sharc = new MySandSharc2();
		Graph graph = new SingleGraph("Tutorial 1");
		graph.addNode("A" );
		graph.addNode("B" );
		graph.addNode("C" );
		graph.addNode("D" );
		graph.addNode("E" );
		graph.addEdge("AB", "A", "B");
		graph.addEdge("AE", "A", "E");
		graph.addEdge("BC", "B", "C");
		graph.addEdge("BD", "B", "D");
		Double nsim = sharc.neighborhood_similarity(graph.getNode("A"), graph.getNode("B"));
		System.out.println("ABCDE nsim(AB) " + nsim);
//		assertEquals(1, nsim);
	}	
	
	@Test
	public void test5() {
		MySandSharc2 sharc = new MySandSharc2();
		Graph graph = new SingleGraph("Tutorial 1");
		graph.addNode("A" );
		graph.addNode("B" );
		graph.addNode("C" );
		graph.addNode("D" );
		graph.addNode("E" );
		graph.addNode("F" );
		graph.addEdge("AB", "A", "B");
		graph.addEdge("AE", "A", "E");
		graph.addEdge("AF", "A", "F");
		graph.addEdge("BC", "B", "C");
		graph.addEdge("BD", "B", "D");
		Double nsim = sharc.neighborhood_similarity(graph.getNode("A"), graph.getNode("B"));
		System.out.println("ABCDEF nsim(AB) " + nsim);
//		assertEquals(1, nsim);
	}
}
