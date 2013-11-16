package test.java;

import java.io.IOException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;

import org.graphstream.algorithm.community.DecentralizedCommunityAlgorithm;
import org.graphstream.algorithm.community.MobileLeung;
import org.graphstream.algorithm.measure.CommunityDistribution;
import org.graphstream.algorithm.measure.Modularity;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.stream.file.FileSource;
import org.graphstream.stream.file.FileSourceFactory;

import main.java.Crowds;

public class MobileLeungTest {
	
//	private static String algorithm = "Sharc";
//	private static String algorithm = "SawSharc";
//	private static String algorithm = "DynSharc";
//	private static String algorithm = "NewSawSharc";
//	private static String algorithm = "SandSharc";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		HashMap<MobileMarker, String> markers = initializeMarkers();
		Dictionary<String, Object> params = new Hashtable<String, Object>();
		
		String algorithm = "MobileLeung";
		params.put("weightMarker", markers.get(MobileMarker.WEIGHT)); 
		String[] mobilityMetrics = {"DSD", "sDSD"};
		String[] mobilityMarkers = {markers.get(MobileMarker.SPEED };
		int i = 0;
		for (String metric : mobilityMetrics) {
			for (MobileMarker marker : MobileMarker.values()) {
				if (marker.toString().toLowerCase().equals(metric.toLowerCase())) {
					mobilityMarkers[i++] = markers.get(marker);
				}
			}
		}
		params.put("mobilityMarkers", mobilityMarkers);
		params.put("m", 0.1);
		params.put("delta", 0.05);
		
		String filePath = "/Users/agatagrzybek/Google Drive/PhD/workshop/sumo_scenarios/Luxembourg_6-8/fcd2vanet/graph_10s.dgs";
		FileSource filesource = null;
		DecentralizedCommunityAlgorithm _algorithm = null;
		Graph graph = new DefaultGraph("fcd");
		try {
			filesource = FileSourceFactory.sourceFor(filePath);	
			filesource.addSink(graph);
		    filesource.begin(filePath);
		    Class algorithmClass;
			try {
				algorithmClass = Class.forName("org.graphstream.algorithm.community." + algorithm);
				_algorithm = (DecentralizedCommunityAlgorithm) algorithmClass.newInstance();
			} catch (Exception e) {
				System.err.println("Error! " + "Algorithm org.graphstream.algorithm.community." + algorithm + " cannot be created.");
				e.printStackTrace();
			} 
			if (_algorithm == null) {
				System.err.println("Error! Algorithm org.graphstream.algorithm.community." + algorithm + " not found.");
				return;
			}
			String communityMarker = markers.get(Marker.COMMUNITY);
			String scoreMarker = communityMarker + ".score";
			
			_algorithm.init(graph);
			_algorithm.staticMode();
			_algorithm.setMarker(communityMarker);
			_algorithm.setParameters(params);
			
			Modularity modularityCom = new Modularity(communityMarker);	
			modularityCom.init(graph);
			
			CommunityDistribution comDist = new CommunityDistribution(communityMarker);
			comDist.init(graph);
			
			int step = 0;
			long startTime = System.currentTimeMillis();
			
			while (filesource.nextStep()) {
				long currentTime = System.currentTimeMillis();
			    System.out.println("step:\t" + step
			    			+ "\tnodes\t"+graph.getNodeCount() + "\tedgess\t"+graph.getEdgeCount()
			    			+ "\ttime\t" + currentTime + "\tduration\t" + (currentTime - startTime));
				
				double sumModularity = 0;
				_algorithm.compute();
				modularityCom.compute();
				comDist.compute();
				double modularityIter = modularityCom.getMeasure();
				++step;
			}
			System.out.println("End\t" + (System.currentTimeMillis()-startTime));
			filesource.end();
//			_outGraph.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			filesource.removeSink(graph);
		}
		
	}
	
	public static HashMap<Marker, String> initializeMarkers() {
		HashMap<Marker, String> markers = new HashMap<Marker, String>();
		markers.put(Marker.X, "x");
		markers.put(Marker.Y, "y");
		markers.put(Marker.WEIGHT, "weight");
		markers.put(Marker.COMMUNITY, "community");
		markers.put(Marker.COMMUNITY_SCORE, markers.get(Marker.COMMUNITY)+".score");
		markers.put(Marker.COMMUNITY2, "community2");
		markers.put(Marker.COMMUNITY_SCORE2, markers.get(Marker.COMMUNITY2)+".score");
		markers.put(Marker.MODULE, "module");
		markers.put(Marker.SPEED, "vehicleSpeed");
		markers.put(Marker.LANE, "vehicleLane");
		markers.put(Marker.ANGLE, "vehicleAngle");
		markers.put(Marker.SLOPE, "vehicleSlope");
		markers.put(Marker.POS, "vehiclePos");
		markers.put(Marker.DISTANCE, "distance");
		markers.put(Marker.DSD, "dsd");
		markers.put(Marker.IDSD, "idsd");
		return markers;
	}
	
}
