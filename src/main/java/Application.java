import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.graphstream.algorithm.Algorithm;
import org.graphstream.algorithm.ConnectedComponents;
import org.graphstream.algorithm.Toolkit;
import org.graphstream.algorithm.community.Community;
import org.graphstream.algorithm.community.Leung;
import org.graphstream.algorithm.measure.Modularity;
import org.graphstream.graph.Edge;
import org.graphstream.graph.ElementNotFoundException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.*;
import org.graphstream.stream.file.FileSinkImages;
import org.graphstream.stream.file.FileSinkImages.LayoutPolicy;
import org.graphstream.stream.file.FileSinkImages.OutputPolicy;
import org.graphstream.stream.file.FileSinkImages.OutputType;
import org.graphstream.stream.file.FileSinkImages.Resolution;
import org.graphstream.stream.file.FileSinkImages.Resolutions;
import org.graphstream.stream.file.FileSource;
import org.graphstream.stream.file.FileSourceDGS;
import org.graphstream.stream.file.FileSourceFactory;
import org.graphstream.ui.graphicGraph.stylesheet.StyleConstants.Units;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;
import org.graphstream.ui.swingViewer.View;
import org.graphstream.ui.swingViewer.Viewer;
import org.graphstream.ui.layout.Layout;
import org.graphstream.ui.layout.LayoutRunner;
import org.graphstream.ui.layout.springbox.implementations.LinLog;
import static org.graphstream.algorithm.Toolkit.*;

public class Application {

	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String filePath = "/Users/agatagrzybek/PhD/workshop/sumo_scenarios/Luxembourg_6-8/fcd2vanet/vanet_0-05min.dgs";
		String styleSheetUrl = "url('file:////Users/agatagrzybek/GraphStreamWorkspace/graph-stream-project/stylesheet.css')";
		String outputFileCommunities = "/Users/agatagrzybek/GraphStreamWorkspace/graph-stream-project/communities.csv";
		String outputFileGraph = "/Users/agatagrzybek/GraphStreamWorkspace/graph-stream-project/graph.txt";
		Application app = new Application();
		System.out.println("Reading graph " + filePath);
		try {
			Graph graph = app.read(filePath, styleSheetUrl, outputFileCommunities, outputFileGraph);
			System.out.println("number of steps: " + graph.getStep());
			System.out.println("Number of nodes: " + graph.getNodeCount());
			System.out.println("Number of edges: " + graph.getEdgeCount());
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Finish");
	}
	
	public FileSinkImages InitializeFSI(String styleSheetUrl) throws IOException {
		String prefix = "prefix_";
		OutputType type = OutputType.PNG;
		Resolution resolution = Resolutions.HD720;
		OutputPolicy outputPolicy = OutputPolicy.BY_STEP;
		FileSinkImages fsi = new FileSinkImages( prefix, type, resolution, outputPolicy );
		String styleSheet = styleSheetUrl;
		fsi.setStyleSheet(styleSheet);
		fsi.setLayoutPolicy( LayoutPolicy.NO_LAYOUT);
//		fsi.addLogo( "path/to/logo", x, y );
		fsi.begin(prefix);
		return fsi;
	}
	
	public HashMap<Marker, String> InitializeMarkers() {
		HashMap<Marker, String> markers = new HashMap<Marker, String>();
		markers.put(Marker.X, "x");
		markers.put(Marker.Y, "y");
		markers.put(Marker.WEIGHT, "weight");
		markers.put(Marker.COMMUNITY, "community");
		markers.put(Marker.COMMUNITY_SCORE, markers.get(Marker.COMMUNITY)+".score");
		markers.put(Marker.MODULE, "module");
		return markers;
	}
	
	public enum Marker {
	    X, Y, WEIGHT, MODULE, COMMUNITY, COMMUNITY_SCORE
	}

	public void WriteGraphTimestepStatistics(BufferedWriter out, Graph graph, ConnectedComponents cc, Modularity modularity) throws IOException {
    	out.write("Step:\t" + graph.getStep());
    	out.newLine();
    	out.write("Number of nodes:\t" + graph.getNodeCount());
    	out.newLine();
    	out.write("Number of edges:\t" + graph.getEdgeCount());
    	out.newLine();
    	Toolkit toolkit = new Toolkit();
		Double avgDegree = toolkit.averageDegree(graph);
		out.write("Average degree:\t" + avgDegree);
		out.newLine();
		int[] degreeDistribution = toolkit.degreeDistribution(graph);
		if (degreeDistribution != null) {
			out.write("Degree distribution: ");
			for (int j = 0; j < degreeDistribution.length; ++j) {
				out.write(degreeDistribution[j]+"\t");
			}
			out.newLine();
			out.write("Diameter: " + toolkit.diameter(graph));
			out.newLine();
			out.write("Average clustering coefficient: " + toolkit.averageClusteringCoefficient(graph));
			out.newLine();
//			double[] clusteringCoefficients = toolkit.clusteringCoefficients(graph);
		}
		if (cc != null) {
			out.write("Connected component(s):\t" + cc.getConnectedComponentsCount());
			out.newLine();
			List<Node> giantComponent = cc.getGiantComponent();
			out.write("Giant component size:\t" + giantComponent.size());
			out.newLine();
		}
		if (modularity != null) {
			out.write("Modularity: " + modularity.getMeasure());
			out.newLine();
		}
	}
	
	public void WriteNode(BufferedWriter out, String separator, String separatorEdges, int step, Node node, HashMap<Marker, String> markers) throws IOException {
		String nodeId = node.getId();
		Integer module = (Integer)node.getAttribute(markers.get(Marker.MODULE));
		Double x = (Double)node.getAttribute(markers.get(Marker.X));
		Double y = (Double)node.getAttribute(markers.get(Marker.Y));
		Community community = (Community)node.getAttribute(markers.get(Marker.COMMUNITY));
		Double communityScore = (Double)node.getAttribute(markers.get(Marker.COMMUNITY_SCORE));
		// step,nodeId,edgesCount,communityId,communityScore,moduleId
		out.write(step+separator+
				nodeId+separator+
				node.getEdgeSet().size()+separator);
		for (Edge edge : node.getEdgeSet()) {
			out.write(edge.getOpposite(node).getId()+separatorEdges);
		}
		if (node.getEdgeSet().size() > 0) {
			out.write(separator);
		}
		out.write(
				community+separator+
				communityScore+separator+
				module);
		out.newLine();
	}
	
	public Graph read(String filePath, String styleSheetUrl, String communityOutputFile, String graphOutputFile) throws IOException {
		FileSinkImages fsi = InitializeFSI(styleSheetUrl);
		HashMap<Marker, String> markers = InitializeMarkers();
		
		Graph graph = new DefaultGraph("fcd");
		FileSource fs = FileSourceFactory.sourceFor(filePath);
		
		ConnectedComponents cc = new ConnectedComponents(graph);
		cc.setCountAttribute(markers.get(Marker.MODULE));
//		cc.setCutAttribute("cut");
		cc.init(graph);
		
		Modularity modularity = new Modularity(markers.get(Marker.MODULE));	
		modularity.init(graph);

		Leung communityDetection = new Leung(graph, markers.get(Marker.COMMUNITY), markers.get(Marker.WEIGHT));
		communityDetection.staticMode();
		markers.put(Marker.COMMUNITY, communityDetection.getMarker());
		markers.put(Marker.COMMUNITY_SCORE, markers.get(Marker.COMMUNITY)+".score");
		
		try {
			fs.addSink(graph);
		    fs.begin(filePath);
			graph.addSink(fsi);

			BufferedWriter outCommunity = new BufferedWriter(new FileWriter(communityOutputFile));
			BufferedWriter outGraph = new BufferedWriter(new FileWriter(graphOutputFile));
			
			Integer communityOfNode = 0;
			String separator = ",";
			
			int step = 0;
		    while (fs.nextStep()) {
		    	step++;
				WriteGraphTimestepStatistics(outGraph, graph, cc, modularity);
				
//				HashMap<Integer, Integer> communityCounts = new HashMap<Integer, Integer>();
				computeAttributesForCommunityDetection(graph, markers.get(Marker.COMMUNITY));
				communityDetection.compute();
				for (Node node : graph.getNodeSet()) {
					WriteNode(outCommunity, ",", ":", step, node, markers);
//					if (communityCounts.get(community.id()) == null) {
//						communityCounts.put(community.id(), 1);
//					}
//					else {
//						communityCounts.put(community.id(), communityCounts.get(community.id())+1);
//					}
					
//					if (community.id() == communityOfNode) {
//						node.setAttribute("ui.class", "community");
//						System.out.printf("node: %s, neighbors: %d, community: %s, community.score: %f, communityCount: %d, module: %d %n", nodeId, node.getEdgeSet().size(), community, communityScore, communityCounts.get(community.id()), module);
//					}
//					else {
//						node.setAttribute("ui.class", "");
//					}
				}
		    }
		    System.out.println("Read events: " + step);
		    fs.end();
			fsi.end();
			outGraph.close();
			outCommunity.close();
	    } catch( IOException e) {
	    	e.printStackTrace();
	    } catch (ElementNotFoundException e) {
			e.printStackTrace();
	    }
		finally {
			fs.removeSink(graph);
		}
		return graph;
	}
	
	public void computeAttributesForCommunityDetection(Graph graph, String marker) {
		String xMarker = "x";
		String yMarker = "y";
		Double maxDistance = 0.0;
		Double sumDistance = 0.0;
		int edgeCount = 0;
		Collection<Node> neighbors = graph.getNodeSet();
		for (Node node : neighbors) {
			String nodeId = node.getId();
			Double nodeX = (Double)node.getAttribute(xMarker);
			Double nodeY = (Double)node.getAttribute(yMarker);
			for (Edge edge : node.getEdgeSet()) {
				Node neighbor = edge.getOpposite(node);
				Double neighborX = (Double)neighbor.getAttribute(xMarker);
				Double neighborY = (Double)neighbor.getAttribute(yMarker);
				Double distance = calculateDistance(nodeX, nodeY, neighborX, neighborY);
				// calculate distance
				edge.setAttribute(marker, distance);
				sumDistance += distance;
				edgeCount ++;
				if (distance > maxDistance) {
					maxDistance = distance;
				}
			}
		}
		if (edgeCount > 0)
		System.out.println("max distance: " + maxDistance + " average distance: " + sumDistance / edgeCount);
	}
	
	public Double calculateDistance(Double nodeX, Double nodeY, Double neighborX, Double neighborY) {
		Double distance = Math.sqrt((nodeX-neighborX)*(nodeX-neighborX) + (nodeY-neighborY)*(nodeY-neighborY));
		return distance;
	}
	
	public void createMovie(String filePath, String styleSheetUrl) {
		// FileSinkImages
		String prefix = "prefix_";
		OutputType type = OutputType.PNG;
		Resolution resolution = Resolutions.HD720;
		OutputPolicy outputPolicy = OutputPolicy.BY_STEP;
		FileSinkImages fsi = new FileSinkImages( prefix, type, resolution, outputPolicy );
		String styleSheet = styleSheetUrl;
		fsi.setStyleSheet(styleSheet);
		fsi.setLayoutPolicy( LayoutPolicy.NO_LAYOUT);
//		fsi.addLogo( "path/to/logo", x, y );
		
		try {
			FileSourceDGS dgs = new FileSourceDGS();
			dgs.begin(filePath);
			dgs.addSink(fsi);
			fsi.begin(prefix);
			while ( dgs.nextStep() ) {
				System.out.println("next step..");
			}
			dgs.end();
			fsi.end();
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void display(Graph graph, String styleSheetUrl) {
		graph.addAttribute("ui.stylehseet", styleSheetUrl);
		graph.addAttribute("ui.quality");
		graph.addAttribute("ui.antialias");
		
		SpriteManager spriteManager = new SpriteManager(graph);
		Sprite spriteTime = spriteManager.addSprite("SpriteTime");
		spriteTime.setPosition(Units.PX, 12, 180, 0);
		
		Viewer viewer = graph.display();
		View view = viewer.getDefaultView();
		view.resizeFrame(800, 600);
		
		int i = 0;
		while(!graph.hasAttribute("ui.viewClosed")) {
			spriteTime.setAttribute("ui.label", String.format("Time %d", i++));
			try {
				viewer.wait(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}
