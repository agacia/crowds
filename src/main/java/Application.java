package main.java;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
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
	private static String separator = "	";
	private static String communityFileDataFormat = "step"+separator+"node id"+separator+"x"+separator+"y"+separator+"degree"+separator+"neighbors"+separator+"community id"+separator+"community score"+separator+"connected component id";
	private static String graphFileDataFormat = "step"+separator+"nodes"+separator+"edges"+separator+"average_degree"+separator+"degree_distribution"+separator+"diameter"+separator+"average_clustering_coefficient"+separator+"connected_components"+separator+"giant_component_size"+separator+"giant_component_id"+separator+"modularity"+separator+"communities"+separator+"giant_community_size"+separator+"giant_community_id"+separator+"avg_community_modularity"+separator+"iterations"+separator+"iterationModularities";
	
	private static String separator2 = ",";

	private static String dir = "";
	private static String outputDir = "/output/02/";
	private static String filePath = "/Users/agatagrzybek/Documents/divanet/fcd_0-05.dgs";

	private static String styleSheetUrl = "stylesheet.css";
	private static String communityOutputFile;
	private static String graphOutputFile;

	private static double deltaParameter = 0.05;
	private static int numberOfIterations = 10;
	private static int startStep = 0;
	private static int endStep = 10;
	
	private static String _prefix = "img";
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		dir = System.getProperty("user.dir");
        System.out.println("current dir = " + dir);
        
		System.out.println("args " + args.length + ": ");
		if (args.length > 1) {
			for (int i = 0; i < args.length; ++i) {
				String argName = args[i];
				String argValue = args[++i];
				System.out.println(argName + " " + argValue);
				if (argName.equals("--inputFile")) {
					filePath = argValue;
				}
				if (argName.equals("--outputDir")) {
					outputDir = argValue;
				}
				if (argName.equals("--delta")) {
					deltaParameter = Double.parseDouble(argValue);
				}
				if (argName.equals("--numberOfIterations")) {
					numberOfIterations = Integer.parseInt(argValue);
				}
				if (argName.equals("--startStep")) {
					startStep = Integer.parseInt(argValue);
				}
				if (argName.equals("--endStep")) {
					endStep = Integer.parseInt(argValue);
				}
			}
		}
		
		communityOutputFile = dir + outputDir + "communities.csv";
		graphOutputFile = dir + outputDir + "graph.txt";
		styleSheetUrl = "url('file:////"+dir+"/"+styleSheetUrl+"')";
		String img_prefix = dir+outputDir+_prefix;
		
		Application app = new Application();
		try {
			Graph graph = app.read(filePath, styleSheetUrl, img_prefix);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Finish");
	}
	

	public Graph read(String filePath, String styleSheetUrl, String prefix) throws IOException {
		FileSinkImages fsi = InitializeFSI(styleSheetUrl, prefix);
		HashMap<Marker, String> markers = InitializeMarkers();
		
		Graph graph = new DefaultGraph("fcd");
		
		System.out.println("Reading graph from " + filePath);
		FileSource fs = FileSourceFactory.sourceFor(filePath);

		System.out.println("Initializing graph...");
		// connected components
		ConnectedComponents cc = new ConnectedComponents(graph);
		cc.setCountAttribute(markers.get(Marker.MODULE));
//		cc.setCutAttribute("cut");
		cc.init(graph);		
		Modularity modularity = new Modularity(markers.get(Marker.MODULE));	
		modularity.init(graph);
		// communities 
		double m = 0.1; // node characteristic preference exponent
		double delta = deltaParameter; // Hop attenuation factor
		Leung communityDetection = new Leung(graph, markers.get(Marker.COMMUNITY), markers.get(Marker.WEIGHT), m, delta);
		communityDetection.staticMode();
		markers.put(Marker.COMMUNITY, communityDetection.getMarker());
		markers.put(Marker.COMMUNITY_SCORE, markers.get(Marker.COMMUNITY)+".score");
		Modularity modularityCom = new Modularity(markers.get(Marker.COMMUNITY));	
		modularityCom.init(graph);
		
		try {
			fs.addSink(graph);
		    fs.begin(filePath);
			graph.addSink(fsi);
			
			BufferedWriter outCommunity = new BufferedWriter(new FileWriter(communityOutputFile));
			outCommunity.write(communityFileDataFormat);
			outCommunity.newLine();
			BufferedWriter outGraph = new BufferedWriter(new FileWriter(graphOutputFile));
			outGraph.write(graphFileDataFormat);
			outGraph.newLine();
			HashMap<Object, Object> communities = new HashMap<Object, Object>();
			int maxComSize = 0;
			String maxComId = "";
			
			System.out.println("Running simulation for steps: from" + startStep + " to " + endStep);
			int step = 0;
		    while (fs.nextStep()) {
		    	if (step > endStep) {
		    		break;
		    	}
		    	if (step >= startStep) {
			    	System.out.println("\nstep " + step + " iterations: " + numberOfIterations);
		    		double[] stepsModularity = new double[numberOfIterations];
					communities = new HashMap<Object, Object>();

					double sumModularity = 0;
					// run iterations for community detection
					for (int i = 0; i < numberOfIterations; ++i) {
						computeAttributesForCommunityDetection(graph, markers.get(Marker.COMMUNITY));
						communityDetection.compute();
						double modularityIter = modularityCom.getMeasure();
						stepsModularity[i] = modularityIter;
						sumModularity += modularityIter;
						
						// write out nodes information at the last iteration
						if (i == (numberOfIterations - 1)) {
							for (Node node : graph.getNodeSet()) {
								WriteNode(outCommunity, separator, separator2, step, node, markers);
								Community community = (Community)node.getAttribute(markers.get(Marker.COMMUNITY));
								String communityId = community.getId();
								if (!communities.containsKey(communityId)) {
									communities.put(communityId, 1);
								}
								else {
									int comSize = (Integer)communities.get(communityId)+1;
									if (comSize > maxComSize) {
										maxComId = communityId;
										maxComSize = comSize;
									}
									communities.put(communityId,comSize);
								}
							}
						}
					}
					double avgModularity = sumModularity / numberOfIterations;
					sumModularity = 0;
					
				    WriteGraphTimestepStatistics(step, outGraph, graph, cc, modularity, avgModularity, stepsModularity, communities, maxComSize, maxComId, markers);
				}
				step++;
		    }

		    System.out.println("Read events: " + step);
			System.out.println("number of steps: " + graph.getStep());
			System.out.println("Number of nodes: " + graph.getNodeCount());
			System.out.println("Number of edges: " + graph.getEdgeCount());
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
	
	
	public Application() {
	}
	
	public FileSinkImages InitializeFSI(String styleSheetUrl, String prefix) throws IOException {
		OutputType type = OutputType.PNG;
		Resolution resolution = Resolutions.HD720;
		OutputPolicy outputPolicy = OutputPolicy.BY_STEP;
		FileSinkImages fsi = new FileSinkImages(prefix, type, resolution, outputPolicy );
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

	public void WriteGraphTimestepStatistics(int step, BufferedWriter out, Graph graph, 
			ConnectedComponents cc, Modularity modularity, double avgModularity, double[] stepsModularity, 
			HashMap<Object, Object> communities, int maxComSize, String maxComId,
			HashMap<Marker, String> markers) throws IOException {
		if (graph == null) {
			return;
		}
		//		"step"+separator+"nodes"+separator+"edges"+separator+"average_degree"+separator+"degree_distribution"+separator+"diameter"+separator+"average_clustering_coefficient"+separator+
    	out.write(step+separator+graph.getNodeCount()+separator+graph.getEdgeCount()+separator);
    	Toolkit toolkit = new Toolkit();
		out.write(toolkit.averageDegree(graph)+separator);
		int[] degreeDistribution = toolkit.degreeDistribution(graph);
		if (degreeDistribution != null) {
			for (int j = 0; j < degreeDistribution.length; ++j) {
				out.write(degreeDistribution[j]+separator2);
			}
			out.write(separator+toolkit.diameter(graph)+separator);
			out.write(toolkit.averageClusteringCoefficient(graph)+separator);
//			double[] clusteringCoefficients = toolkit.clusteringCoefficients(graph);
		}
//		"connected_components"+separator+"giant_component_size"+separator+"giant_component_size"+separator+"modularity"+
		if (cc != null) {
			out.write(cc.getConnectedComponentsCount()+separator);
			List<Node> giantComponent = cc.getGiantComponent();
			int giantComponentSize = giantComponent.size();
			out.write(giantComponentSize+separator);
			int ccId = 0;
			if (giantComponentSize > 0) {
				Node node = giantComponent.get(0);
				ccId = (Integer)node.getAttribute(markers.get(Marker.MODULE));
			}
			out.write(ccId+separator);
		}
		if (modularity != null) {
			out.write(modularity.getMeasure()+separator);
		}
//		"communities"+separator+"giant_community_size"+separator+"giant_community_id"+separator+
		out.write(communities.size()+separator);
		out.write(maxComSize+separator);
		out.write(maxComId+separator);
////		"communities"+separator+"giant_community_size"+separator+
//		ConnectedComponents com = new ConnectedComponents(graph);
//		com.setCountAttribute(markers.get(Marker.COMMUNITY));
////		com.setCutAttribute("cut");
//		com.init(graph);
//		out.write(com.getConnectedComponentsCount()+separator);
//		List<Node> giantComponent = com.getGiantComponent();
//		out.write(giantComponent.size()+separator);
		
//		"avg_community_modularity"+separator+"iterations"+separator+"iterationModularities";
		if (stepsModularity != null) {
			out.write(avgModularity+separator);
			out.write(stepsModularity.length+separator);
			for (int i = 0; i < stepsModularity.length; ++i) {
				out.write(stepsModularity[i]+separator2);
			}
		}
		out.newLine();
	}
	
	public void WriteNode(BufferedWriter out, String separator, String separatorEdges, int step, Node node, HashMap<Marker, String> markers) throws IOException {
		String nodeId = node.getId();
		Integer module = (Integer)node.getAttribute(markers.get(Marker.MODULE));
		Double x = (Double)node.getAttribute(markers.get(Marker.X));
		Double y = (Double)node.getAttribute(markers.get(Marker.Y));
		Community community = (Community)node.getAttribute(markers.get(Marker.COMMUNITY));
		
		Double communityScore = (Double)node.getAttribute(markers.get(Marker.COMMUNITY_SCORE));
		// step,nodeId,edgesCount,communityId,communityScore,moduleId
		int degree = node.getEdgeSet().size();
		out.write(step+separator+
				nodeId+separator+
				x+separator+
				y+separator+
				degree+separator);
		for (Edge edge : node.getEdgeSet()) {
			out.write(edge.getOpposite(node).getId()+separatorEdges);
		}
		out.write(
				separator+
				community.id()+separator+
				communityScore+separator+
				module);
		out.newLine();
		
//		if (degree > 0) {
//			if (community.id() == 7) {
//				node.setAttribute("ui.class", "community");
//			}
////			System.out.printf("node: %s, community: %s", node.getId(), community.id());
////			System.out.printf("node: %s, neighbors: %d, community: %s, community.score: %f, communityCount: %d, module: %d %n", nodeId, node.getEdgeSet().size(), community, communityScore, communityCounts.get(community.id()), module);
//		}
//		else {
//			node.setAttribute("ui.class", "");
//		}
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
//		if (edgeCount > 0)
//		System.out.println("max distance: " + maxDistance + " average distance: " + sumDistance / edgeCount);
	}
	
	public Double calculateDistance(Double nodeX, Double nodeY, Double neighborX, Double neighborY) {
		Double distance = Math.sqrt((nodeX-neighborX)*(nodeX-neighborX) + (nodeY-neighborY)*(nodeY-neighborY));
		return distance;
	}
	
	public void createMovie(String filePath, String styleSheetUrl, String prefix) {
		// FileSinkImages
		OutputType type = OutputType.PNG;
		Resolution resolution = Resolutions.HD720;
		OutputPolicy outputPolicy = OutputPolicy.BY_STEP;
		FileSinkImages fsi = new FileSinkImages(prefix, type, resolution, outputPolicy );
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
