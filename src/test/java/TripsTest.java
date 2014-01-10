package test.java;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;

import org.graphstream.algorithm.community.MobileMarker;

import main.java.Crowds;

public class TripsTest {
	
	private static String filePath = "/Users/agatagrzybek/Google Drive/PhD/workshop/sumo_scenarios/Luxembourg_6-8/fcd2vanet/graph_10s.dgs";
	private static String sep = "\t";
	private static String sep2 = ",";
	private static String communityFileDataFormat = "step"+sep+"node_id"+sep+"x"+sep+"y"+sep+"degree"+sep+"neighbors"+sep+"cc_id"+sep+"cc_size"+sep+"com_id"+sep+"com_score"+sep+"com_size";
	private static String graphFileDataFormat = "step"+sep+"nodes"+sep+"edges"+sep+"singletons"+sep+"connected"+sep+"avg_degree"+sep+"degree_distribution"+sep+"diameter"+sep+"avg_clustering_coefficient"+sep+"cc_count"+sep+"avg_cc_size"+sep+"max_cc_size"+sep+"max_cc_id"+sep+"cc_std_dev"+sep+"com_count"+sep+"avg_com_size"+sep+"max_com_size"+sep+"max_com_id"+sep+"min_com_size"+sep+"min_com_id"+sep+"std_com_dist"+sep+"size_distribution"+sep+"modularity"+sep+"com_modularity"+sep+"iterations"+sep+"iteration_modularities";
	private static String communityOutputFile;
	private static String graphOutputFile;
	private static String algorithmOutputFile;
	private static String dir = "";
	private static double mParameter = 0.1;
	private static double deltaParameter = 0.05;
	private static int numberOfIterations = 1;
	private static int startStep = 0;
	private static int endStep = 10;
	private static String styleSheetUrl = "css/stylesheet.css";
	private static String _prefix = "img";
	private static String img_prefix;
	private static String outputDir = "/Users/agatagrzybek/workspace/crowds/output/eclipse/";
//	private static String algorithm = "EpidemicCommunityAlgorithm";
//	private static String algorithm = "Leung";
	private static String algorithm = "MobileLeung";
	private static String[] mobilityMarkers = null;
	private static String mobilityMetric = "sDSD";
	private static String goal = "communities";
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
		
		HashMap<MobileMarker, String> markers = initializeMarkers();
		
		Crowds crowds = new Crowds();		
		crowds.set_goal(goal);
		crowds.readGraph(filePath);
		crowds.setRunningSteps(startStep, endStep);
		crowds.initializeConnectedComponents(markers.get(MobileMarker.MODULE));
		BufferedWriter outAlgorithm = null;
		
		Dictionary<String, Object> params = new Hashtable<String, Object>();
		if (algorithm.equals("Leung")) {
			params.put("m", mParameter);
			params.put("delta", deltaParameter);
			params.put("weightMarker", markers.get(MobileMarker.WEIGHT));
		}
		// MobileLeung
		if (algorithm.contains("MobileLeung")) {
			mobilityMetric = algorithm.toLowerCase().endsWith("sdsd") ? "sdsd" : "dsd";
			algorithm = "MobileLeung";
		}
		if (algorithm.equals("MobileLeung")) {
			
			try {
				outAlgorithm = new BufferedWriter(new FileWriter(algorithmOutputFile));
			} catch (IOException e) {
				e.printStackTrace();
			}
			params.put("weightMarker", markers.get(MobileMarker.WEIGHT)); 
			params.put("mobilityMarkers", markers);
			params.put("m", 0.1);
			params.put("delta", 0.05);
			params.put("mobilityMetric", mobilityMetric);
			params.put("outLog", outAlgorithm);
			
		}
		String[] printOutMarkers = { markers.get(MobileMarker.X), markers.get(MobileMarker.Y) };
		crowds.set_printOutMarkers(printOutMarkers);
		crowds.openOutputFiles(communityOutputFile, communityFileDataFormat, graphOutputFile, graphFileDataFormat, sep, sep2);
		crowds.detectCommunities(algorithm, markers.get(MobileMarker.COMMUNITY), params);
		if (algorithm.equals("MobileLeung")) {
			try {
				outAlgorithm.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static HashMap<MobileMarker, String> initializeMarkers() {
		HashMap<MobileMarker, String> markers = new HashMap<MobileMarker, String>();
		markers.put(MobileMarker.X, "x");
		markers.put(MobileMarker.Y, "y");
		markers.put(MobileMarker.WEIGHT, "weight");
		markers.put(MobileMarker.COMMUNITY, "community");
		markers.put(MobileMarker.COMMUNITY_SCORE, markers.get(MobileMarker.COMMUNITY)+".score");
		markers.put(MobileMarker.MODULE, "module");
		markers.put(MobileMarker.SPEED, "vehicleSpeed");
		markers.put(MobileMarker.LANE, "vehicleLane");
		markers.put(MobileMarker.ANGLE, "vehicleAngle");
		markers.put(MobileMarker.SLOPE, "vehicleSlope");
		markers.put(MobileMarker.POS, "vehiclePos");
		markers.put(MobileMarker.DISTANCE, "distance");
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
				if (argName.equals("--mobilityMetric")) {
					mobilityMetric = argValue.trim();
				}
				if (argName.equals("--goal")) {
					goal = argValue.trim();
				}
			}
		}
		communityOutputFile = outputDir + "communities.csv";
		graphOutputFile = outputDir + "graph.txt";
		algorithmOutputFile = outputDir + "algorithm.csv";
		styleSheetUrl = "url('file:////"+dir+"/"+styleSheetUrl+"')";
		img_prefix = dir+outputDir+_prefix;

		System.out.println("CROWDS STARTED --------------");
		System.out.println("Crowds parameters:");
		System.out.println("filePath\t" + filePath);
		System.out.println("dir\t" + dir);
		System.out.println("communityOutputFile\t" + communityOutputFile);
		System.out.println("graphOutputFile\t" + graphOutputFile);
		System.out.println("algorithmOutputFile\t" + algorithmOutputFile);
		System.out.println("goal\t" + goal);
		
	}
	
}
