package test.java;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import main.java.Crowds;

public class CrowdsTest {
	
	public enum Marker {
	    X, Y, WEIGHT, MODULE, COMMUNITY, COMMUNITY_SCORE, COMMUNITY2, COMMUNITY_SCORE2, SPEED, LANE, ANGLE, SLOPE, POS
	}
	
	private static String filePath = "/Users/agatagrzybek/Google Drive/PhD/workshop/sumo_scenarios/Luxembourg_6-8/fcd2vanet/graph_10s.dgs";
	private static String sep = "\t";
	private static String sep2 = ",";
	private static String communityFileDataFormat = "step"+sep+"node_id"+sep+"x"+sep+"y"+sep+"degree"+sep+"neighbors"+sep+"cc_id"+sep+"cc_size"+sep+"com_id"+sep+"com_score"+sep+"com_size";
	private static String graphFileDataFormat = "step"+sep+"nodes"+sep+"edges"+sep+"singletons"+sep+"connected"+sep+"avg_degree"+sep+"degree_distribution"+sep+"diameter"+sep+"avg_clustering_coefficient"+sep+"cc_count"+sep+"avg_cc_size"+sep+"max_cc_size"+sep+"max_cc_id"+sep+"cc_std_dev"+sep+"com_count"+sep+"avg_com_size"+sep+"max_com_size"+sep+"max_com_id"+sep+"min_com_size"+sep+"min_com_id"+sep+"std_com_dist"+sep+"modularity"+sep+"com_modularity"+sep+"iterations"+sep+"iteration_modularities";
	private static String communityOutputFile;
	private static String graphOutputFile;
	private static String dir = "";
	private static double mParameter = 0.1;
	private static double deltaParameter = 0.05;
	private static int numberOfIterations = 1;
	private static int startStep = 0;
	private static int endStep = 10;
	private static String styleSheetUrl = "css/stylesheet.css";
	private static String _prefix = "img";
	private static String img_prefix;
	private static String outputDir = "/Users/agatagrzybek/GraphStreamWorkspace/crowds/output/eclipse/";
//	private static String algorithm = "EpidemicCommunityAlgorithm";
//	private static String algorithm = "Leung";
	private static String algorithm = "MobileLeung";
//	private static String algorithm = "Sharc";
//	private static String algorithm = "SawSharc";
//	private static String algorithm = "DynSharc";
//	private static String algorithm = "NewSawSharc";
//	private static String algorithm = "SandSharc";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		readArgs(args);
		HashMap<Marker, String> markers = initializeMarkers();
		
		Crowds crowds = new Crowds();		
		crowds.readGraph(filePath);
		crowds.setRunningSteps(startStep, endStep);
		crowds.initializeConnectedComponents(markers.get(Marker.MODULE));
		
		Dictionary<String, Object> params = new Hashtable<String, Object>();
		if (algorithm.equals("Leung")) {
			params.put("m", mParameter);
			params.put("delta", deltaParameter);
			params.put("weightMarker", markers.get(Marker.WEIGHT));
		}
		// MobileLeung
		if (algorithm.equals("MobileLeung")) {
			params.put("weightMarker", markers.get(Marker.WEIGHT)); 
			String[] mobilityMarkers = { markers.get(Marker.SPEED), markers.get(Marker.LANE), markers.get(Marker.ANGLE) };
			params.put("mobilityMarkers", mobilityMarkers);
			params.put("m", 0.1);
			params.put("delta", 0.05);
		}
		String[] printOutMarkers = { markers.get(Marker.X), markers.get(Marker.Y) };
		crowds.set_printOutMarkers(printOutMarkers);
		crowds.openOutputFiles(communityOutputFile, communityFileDataFormat, graphOutputFile, graphFileDataFormat, sep, sep2);
		crowds.detectCommunities(algorithm, markers.get(Marker.COMMUNITY), params);
		
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
		return markers;
	}

	public static void readArgs(String[] args) {
		dir = System.getProperty("user.dir");
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
				if (argName.equals("--algorithm")) {
					algorithm = argValue.trim();
				}
			}
		}
		communityOutputFile = outputDir + "communities.csv";
		graphOutputFile = outputDir + "graph.txt";
		styleSheetUrl = "url('file:////"+dir+"/"+styleSheetUrl+"')";
		img_prefix = dir+outputDir+_prefix;
		
		System.out.println("Crowds parameters:");
		System.out.println("filePath:\t" + filePath);
		System.out.println("algorithm:\t" + algorithm);
		System.out.println("dir:\t" + dir);
		System.out.println("communityOutputFile:\t" + communityOutputFile);
		System.out.println("graphOutputFile:\t" + graphOutputFile);
		
		
	}
	
}
