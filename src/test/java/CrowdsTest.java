package test.java;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;

import org.graphstream.algorithm.community.CongestionMeasure;
import org.graphstream.algorithm.community.MobileMarker;

import main.java.Crowds;

public class CrowdsTest {

//	private static String filePath = "/Users/agatagrzybek/Google Drive/PhD/workshop/sumo_scenarios/Luxembourg_6-8/fcd2vanet/graph_10s.dgs";
//	private static String filePath = "/Users/agatagrzybek/Google Drive/PhD/workshop/sumo_scenarios/manhattan/p_05/vanet_200.dgs";
//	private static String filePath = "/Users/agatagrzybek/Google Drive/PhD/workshop/sumo_scenarios/cross3ltl/vanet.dgs";
//	private static String filePath = "/Users/agatagrzybek/Google Drive/PhD/workshop/sumo_scenarios/twolanes_long/vanet.dgs";
//	private static String filePath = "/Users/agatagrzybek/workspace/Jean/Agata/dgs_probeData_v8-20_avg.dgs"; 
//	private static String filePath = "/Users/agatagrzybek/workspace/Jean/Agata/probeData_v15-30_avg_direction_clean.dgs"; 
//	private static String filePath = "/Users/agatagrzybek/workspace/Jean/Agata/probeData_v15-30_avg_test.dgs"; 
//	private static String filePath = "/Users/agatagrzybek/Google Drive/PhD/workshop/sumo_scenarios/twolanes_long/vanet.dgs";
//	private static String filePath = "/Users/agatagrzybek/workspace/Jean/Agata/probeData_v15-30_avg_direction_clean_200.dgs";
//	private static String filePath = "/Users/agatagrzybek/Google Drive/PhD/workshop/sumo_scenarios/twolanes_long_120kmph_600/vanet.dgs";
//	private static String filePath = "/Users/agatagrzybek/Google Drive/PhD/workshop/sumo_scenarios/twolanes_long_120kmph_600_speed/vanet.dgs";
//	private static String filePath = "/Users/agatagrzybek/Google Drive/PhD/workshop/sumo_scenarios/twolanes_long_120kmph_600_speed/vanet_avgspeed_direction.dgs";
//	private static String filePath = "/Users/agatagrzybek/workspace/Jean/Agata/vanet_probeData_v15-30+200_17032014.dgs";
//	private static String filePath = "/Users/agatagrzybek/workspace/ovnis/scenarios/Kirchberg/sumoOutput-accident/400-400-200/Kirchberg-400-400-200.dgs";
//	private static String filePath = "/Users/agatagrzybek/workspace/ovnis/scenarios/Kirchberg/sumoOutput-accident/900-50-50/Kirchberg-flow-05.dgs";
//	private static String filePath = "/Users/agatagrzybek/workspace/gs-algo/src-test/org/graphstream/algorithm/measure/test/data/TestCongestionMeasure_long.dgs";
	private static String filePath = "/Users/agatagrzybek/workspace/ovnis/scenarios/Kirchberg/sumoOutput-accident/900-50-50/Kirchberg-accident-900-50-50-100PR.dgs";
			
	private static String sep = "\t";
	private static String sep2 = ",";
	private static String communityFileDataFormat = "step"+sep+"node_id"+sep+"x"+sep+"y"+sep+"degree"+sep+"com_id"+sep+"com_score"+sep+"com_size"+sep+"link_id"+sep+"speed"+sep+"avg_speed"+sep+"num_stops"+sep+"is_originator"+sep+"dynamism"+sep+"timeMeanSpeed"+sep+"maxHistoryRecords"+sep+"timeMeanSpeed.count";
	private static String graphFileDataFormat = "step"+sep+"nodes"+sep+"edges"+sep+"singletons"+sep+"connected"+sep+"avg_degree"+sep+"degree_distribution"+sep+"diameter"+sep+"avg_clustering_coefficient"+sep+"cc_count"+sep+"avg_cc_size"+sep+"max_cc_size"+sep+"max_cc_id"+sep+"cc_std_dev"+sep+"com_count"+sep+"avg_com_size"+sep+"max_com_size"+sep+"max_com_id"+sep+"min_com_size"+sep+"min_com_id"+sep+"std_com_dist"+sep+"size_distribution"+sep+"modularity"+sep+"com_modularity"+sep+"iterations"+sep+"iteration_modularities"+sep+"weighted_com_modularity"+sep+"speed_avg"+sep+"speed_std"+sep+"speed_avg_std"+sep+"avgSpeed_avg"+sep+"avgSpeed_std"+sep+"avgSpeed_avg_std";
	private static String communityOutputFile;
	private static String graphOutputFile;
	private static String algorithmOutputFile;
	private static String dir = "";
	private static double mParameter = 0.1;
	private static double deltaParameter = 0.05;
	private static int numberOfIterations = 1;
	private static int startStep = 0;
	private static int endStep = 1000;
	private static String styleSheetUrl = "css/stylesheet.css";
	private static String _prefix = "img";
	private static String img_prefix;
	private static String outputDir = "/Users/agatagrzybek/workspace/crowds/output/eclipse/";
//	private static String algorithm = "EpidemicCommunityAlgorithm";
//	private static String algorithm = "Leung";
//	private static String algorithm = "MobileLeung";
	private static String[] mobilityMarkers = null;
	private static String mobilityMetric = "sDSD";
	private static String goal = "communities";
//	private static String algorithm = "Sharc";
//	private static String algorithm = "SawSharc";
//	private static String algorithm = "DynSharc";
//	private static String algorithm = "NewSawSharc";
//	private static String algorithm = "SandSharc";
	private static String algorithm = "MobileSandSharc";
//	private static String algorithm = "Crowdz";
	private static String congestion = "CongestionMeasure";
	private static Double mobilityWeight = 0.5;
	private static Double mobilitySimilarityThreshold = 0.5; // 0 means no threshold
	private static Double maxSpeed = 50.0; // 0 means no threshold
//	private static Double congestionSpeedThreshold = 25.0; // freeways
	private static Double congestionSpeedThreshold = 0.0; // urban
	private static Integer speedHistoryLength = 10;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		readArgs(args);
		
		HashMap<MobileMarker, String> markers = initializeMarkers();

		Crowds crowds = new Crowds();	
//		try {
//			crowds.setImagesGeneration(styleSheetUrl, img_prefix);
//		}
//		catch (IOException e) {
//			
//		}
		crowds.set_goal(goal);
		crowds.readGraph(filePath);
		crowds.setRunningSteps(startStep, endStep);
		crowds.initializeConnectedComponents(markers.get(MobileMarker.MODULE));
		BufferedWriter outAlgorithm = null;
		// MobileLeung
		if (algorithm.contains("MobileLeung")) {
			mobilityMetric = algorithm.toLowerCase().endsWith("sdsd") ? "sdsd" : "dsd";
			algorithm = "MobileLeung";
		}
				
		Dictionary<String, Object> params = new Hashtable<String, Object>();
		if (algorithm.equals("Leung")) {
			params.put("m", mParameter);
			params.put("delta", deltaParameter);
			params.put("weightMarker", markers.get(MobileMarker.WEIGHT));
		}
		else if (algorithm.equals("MobileLeung")) {	
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
		else if (algorithm.equals("Crowdz") || algorithm.equals("MobileSandSharc") || algorithm.equals("MobileSharc")) {	
			params.put("weightMarker", "");
			params.put("speedMarker", markers.get(MobileMarker.SPEED));
			params.put("avgSpeedMarker", markers.get(MobileMarker.AVG_SPEED));
			params.put("angleMarker", markers.get(MobileMarker.ANGLE));
			params.put("dynamismMarker", markers.get(MobileMarker.DYNAMISM));
			params.put("timeMeanSpeedMarker", markers.get(MobileMarker.TIMEMEANSPEED));
			
			if (algorithm.equals("MobileSandSharc") || algorithm.equals("MobileSharc")) {
				params.put("weightMarker", markers.get(MobileMarker.WEIGHT));
				params.put("mobilityWeight", mobilityWeight);
				params.put("mobilitySimilarityThreshold", mobilitySimilarityThreshold);
				params.put("maxSpeed", maxSpeed);
				params.put("congestionSpeedThreshold", congestionSpeedThreshold);
			}
		}
		else {
			params.put("weightMarker", "");
		}
		
		Dictionary<String, Object> congestionParams = new Hashtable<String, Object>();
		if (congestion.equals("CongestionCrowdz") || congestion.equals("CongestionMeasure")) {	
			congestionParams.put("weightMarker", "");
			congestionParams.put("speedMarker", markers.get(MobileMarker.SPEED));
			congestionParams.put("avgSpeedMarker", markers.get(MobileMarker.AVG_SPEED));
			congestionParams.put("laneMarker", markers.get(MobileMarker.LANE));
			congestionParams.put("posMarker", markers.get(MobileMarker.POS));
			congestionParams.put("dynamismMarker", markers.get(MobileMarker.DYNAMISM));
			congestionParams.put("speedHistoryLength", speedHistoryLength);
		}
		
		String[] printOutMarkers = { markers.get(MobileMarker.X), markers.get(MobileMarker.Y) };
		crowds.set_printOutMarkers(printOutMarkers);
		crowds.openOutputFiles(communityOutputFile, communityFileDataFormat, graphOutputFile, graphFileDataFormat, sep, sep2);
		crowds.detectCommunities(algorithm, markers.get(MobileMarker.COMMUNITY), params, congestion, markers.get(MobileMarker.CONGESTION), congestionParams);
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
		markers.put(MobileMarker.CONGESTION, "congestion");
		markers.put(MobileMarker.MODULE, "module");
		markers.put(MobileMarker.SPEED, "vehicleSpeed");
		markers.put(MobileMarker.AVG_SPEED, "vehicleAvgSpeed");
		markers.put(MobileMarker.LANE, "vehicleLane");
		markers.put(MobileMarker.ANGLE, "vehicleAngle");
		markers.put(MobileMarker.SLOPE, "vehicleSlope");
		markers.put(MobileMarker.POS, "vehiclePos");
		markers.put(MobileMarker.DISTANCE, "distance");
		markers.put(MobileMarker.DYNAMISM, "dynamism");
		markers.put(MobileMarker.TIMEMEANSPEED, "timeMeanSpeed");
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
				if (argName.equals("--congestion")) {
					congestion = argValue.trim();
				}
				if (argName.equals("--mobilityMetric")) {
					mobilityMetric = argValue.trim();
				}
				if (argName.equals("--goal")) {
					goal = argValue.trim();
				}
				if (argName.equals("--mobilityWeight")) {
//					mobilityWeight = Double.parseDouble(argValue);
//					mobilitySimilarityThreshold = Double.parseDouble(argValue);
//					maxSpeed = Double.parseDouble(argValue);
					congestionSpeedThreshold = Double.parseDouble(argValue);
				}
				if (argName.equals("--param")) {
					speedHistoryLength = Integer.parseInt(argValue);
				}
				if (argName.equals("--mobilitySimilarityThreshold")) {
					mobilitySimilarityThreshold = Double.parseDouble(argValue);
				}
			}
		}
		communityOutputFile = outputDir + "communities.csv";
		graphOutputFile = outputDir + "graph.txt";
		algorithmOutputFile = outputDir + "algorithm.csv";
		styleSheetUrl = "url('file:////"+dir+"/"+styleSheetUrl+"')";
		img_prefix = outputDir+_prefix;
		BufferedWriter _outSettings;
		try {
			_outSettings = new BufferedWriter(new FileWriter(outputDir + "crowdz_settings.csv"));
			_outSettings.write("CROWDZ" + "\n");
			_outSettings.write("congestionSpeedThreshold " + congestionSpeedThreshold + "\n");
			_outSettings.write("speedHistoryLength " + speedHistoryLength + "\n");
			_outSettings.write("filePath\t" + filePath + "\n");
			_outSettings.write("dir\t" + dir + "\n");
			_outSettings.write("communityOutputFile\t" + communityOutputFile + "\n");
			_outSettings.write("graphOutputFile\t" + graphOutputFile + "\n");
			_outSettings.write("algorithmOutputFile\t" + algorithmOutputFile + "\n");
			_outSettings.write("algorithm\t" + algorithm + "\n");
			_outSettings.write("congestion\t" + congestion + "\n");
			_outSettings.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		
		
		
	}
	
}
