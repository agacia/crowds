package test.java;

import java.io.IOException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;

import org.graphstream.algorithm.community.MobileMarker;

import main.java.Crowds;

public class CrowdsTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		System.out.println(args.length);
		if (args.length < 2) {
			args = new String[]{ "--inputFile", 
//					"/Users/agata/Documents/PhD/sumo_scenarios/Kirchberg/kirchberg_300.dgs",
					// "/Users/agata/Documents/PhD/sumo_scenarios/Manhattan/final_10-20.dgs", 
					// "/Users/agata/Documents/PhD/sumo_scenarios/Luxembourg_6-8/sumoOutput/vanet.dgs",
					 "/Users/agata/shared/thesis_scenarios/highway_congestion/crowds/input/vanet.dgs",
//					"/Users/agata/shared/thesis_scenarios/higway_nocongestion/network/input/vanet.dgs",
//					 "/Users/agata/Documents/PhD/sumo_scenarios/twolanes_long_120kmph_600/vanet.dgs",
				"--communityAlgorithmName", "SandSharc_copy_linkduration" , // "MySandSharc4_hybrid",  // "SandSharc_copy_hybrid", 
				"--congestionAlgorithmName", "CongestionMeasure",
				"--goal", "communities",
				"--startStep", "0",
				"--endStep", "600",
				"--outputDir", "/Users/agata/workspace/crowds/output/eclipse/",
				"--speedHistoryLength", "90",
				"--speedType", "timemean",
				"--numberOfIterations", "1"};
		}
		HashMap<String, String> programArgs = parseArgs(args);
		HashMap<MobileMarker, String> markers = initializeMarkers();
		if (programArgs.get("filePath").indexOf("Luxembourg") != -1 || programArgs.get("filePath").indexOf("twolanes") != -1
				|| programArgs.get("filePath").indexOf("highway") != -1) {
			markers = initializeMarkersLuxembourgOrHighway();
		}
		Dictionary<String, Object> communityAlgorithmParams = getAlgorithmParams(programArgs, markers);
		Dictionary<String, Object> congestionAlgorithmParams = getCongestionParams(programArgs, markers);
		
		Crowds crowds = new Crowds();	
//		try {
//			crowds.setImagesGeneration(true);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		crowds.setSettings(Integer.parseInt(programArgs.get("startStep")), 
				Integer.parseInt(programArgs.get("endStep")),
				Integer.parseInt(programArgs.get("numberOfIterations")), 
				programArgs.get("goal"));
		crowds.openOutputFiles(programArgs.get("outputDir"));
		crowds.readGraph(programArgs.get("filePath"));
		
		crowds.initializeConnectedComponents(markers.get(MobileMarker.MODULE));
		String communityAlgorithmName = programArgs.get("communityAlgorithmName");
		if (communityAlgorithmName.equals("SandSharc_nostab") || 
				communityAlgorithmName.equals("SandSharc_mobility") || 
				communityAlgorithmName.equals("SandSharc_linkduration") ||
				communityAlgorithmName.equals("SandSharc_hybrid")) {
			communityAlgorithmName = "SandSharc";
		}
		if (communityAlgorithmName.equals("SandSharc_copy_nostab") || 
				communityAlgorithmName.equals("SandSharc_copy_mobility") || 
				communityAlgorithmName.equals("SandSharc_copy_linkduration") ||
				communityAlgorithmName.equals("SandSharc_copy_hybrid")) {
			communityAlgorithmName = "SandSharc_copy";
		}
		if (communityAlgorithmName.equals("MySandSharc_nostab") || 
				communityAlgorithmName.equals("MySandSharc_mobility") || 
				communityAlgorithmName.equals("MySandSharc_linkduration") ||
				communityAlgorithmName.equals("MySandSharc_hybrid")) {
			communityAlgorithmName = "MySandSharc";
		}
		if (communityAlgorithmName.equals("MySandSharc2_nostab") || 
				communityAlgorithmName.equals("MySandSharc2_mobility") || 
				communityAlgorithmName.equals("MySandSharc2_linkduration") ||
				communityAlgorithmName.equals("MySandSharc2_hybrid")) {
			communityAlgorithmName = "MySandSharc2";
		}
		if (communityAlgorithmName.equals("MySandSharc3_nostab") || 
				communityAlgorithmName.equals("MySandSharc3_mobility") || 
				communityAlgorithmName.equals("MySandSharc3_linkduration") ||
				communityAlgorithmName.equals("MySandSharc3_hybrid")) {
			communityAlgorithmName = "MySandSharc3";
			System.out.println("communityAlgorithmName " + communityAlgorithmName);
		}
		if (communityAlgorithmName.equals("MySandSharc4_nostab") || 
				communityAlgorithmName.equals("MySandSharc4_mobility") || 
				communityAlgorithmName.equals("MySandSharc4_linkduration") ||
				communityAlgorithmName.equals("MySandSharc4_hybrid")) {
			communityAlgorithmName = "MySandSharc4";
			System.out.println("communityAlgorithmName " + communityAlgorithmName);
		}
		if (communityAlgorithmName.equals("MySandSharc5_nostab") || 
				communityAlgorithmName.equals("MySandSharc5_mobility") || 
				communityAlgorithmName.equals("MySandSharc5_linkduration") ||
				communityAlgorithmName.equals("MySandSharc5_hybrid")) {
			communityAlgorithmName = "MySandSharc5";
			System.out.println("communityAlgorithmName " + communityAlgorithmName);
		}
		if (communityAlgorithmName.equals("MySandSharc6_nostab") || 
				communityAlgorithmName.equals("MySandSharc6_mobility") || 
				communityAlgorithmName.equals("MySandSharc6_linkduration") ||
				communityAlgorithmName.equals("MySandSharc6_hybrid")) {
			communityAlgorithmName = "MySandSharc6";
			System.out.println("communityAlgorithmName " + communityAlgorithmName);
		}
		crowds.initializeCommunityDetectionAlgorithm(communityAlgorithmName, markers.get(MobileMarker.COMMUNITY), communityAlgorithmParams);
		crowds.initializeCongestionDetectionAlgorithm(programArgs.get("congestionAlgorithmName"), markers.get(MobileMarker.CONGESTION), congestionAlgorithmParams);
		
		crowds.detectCommunities();
	}
	
	public static Hashtable<String, Object> getCongestionParams(HashMap<String, String> programArgs, HashMap<MobileMarker, String> markers) {
		Hashtable<String, Object> params = new Hashtable<String, Object>();
		String algorithmName = programArgs.get("congestionAlgorithmName");
		if (algorithmName.equals("CongestionCrowdz") || algorithmName.equals("CongestionMeasure")) {	
			params.put("weightMarker", "weight");
			params.put("speedMarker", markers.get(MobileMarker.SPEED));
			params.put("angleMarker", markers.get(MobileMarker.ANGLE));
			params.put("mobilitySimilarityMarker", markers.get(MobileMarker.MOBILITY_SIMILARITY));
			params.put("laneMarker", markers.get(MobileMarker.LANE));
			params.put("dynamismMarker", markers.get(MobileMarker.DYNAMISM));
			params.put("linkDurationMarker", markers.get(MobileMarker.LINK_DURATION));
			params.put("hybridMarker", "hybrid");
			params.put("speedHistoryLength", Integer.parseInt(programArgs.get("speedHistoryLength")));
			params.put("speedType", programArgs.get("speedType"));
		}
		return params;
	}
	
	public static Dictionary<String, Object> getAlgorithmParams(HashMap<String, String> programArgs, HashMap<MobileMarker, String> markers) {
		Dictionary<String, Object> params = new Hashtable<String, Object>();
		String algorithmName = programArgs.get("communityAlgorithmName");
		params.put("mobilitySimilarityMarker", markers.get(MobileMarker.MOBILITY_SIMILARITY)); // used in CrowdsTest to print edges
		params.put("linkDurationMarker", markers.get(MobileMarker.LINK_DURATION)); // used in CrowdsTest to print edges
		
		// Oryginal SandSharc 
		if (algorithmName.indexOf("nostab") != -1) {
			params.put("weightMarker", "");
		}
		// Oryginal SandSharc + mobility_metric as stability measure
		else if (algorithmName.indexOf("mobility") != -1 ) {
			params.put("weightMarker", markers.get(MobileMarker.MOBILITY_SIMILARITY));
		}
		// Oryginal SandSharc + linkduration as stability measure
		else if (algorithmName.indexOf("linkduration") != -1) {
			params.put("weightMarker", markers.get(MobileMarker.LINK_DURATION));
		}
		else if (algorithmName.indexOf("hybrid") != -1) {
			params.put("weightMarker", "hybrid");
		}
		
//		if (algorithmName.equals("Leung")) {
//			params.put("m", Double.parseDouble(programArgs.get("mParameter")));
//			params.put("delta", Double.parseDouble(programArgs.get("deltaParameter")));
//			params.put("weightMarker", markers.get(MobileMarker.WEIGHT));
//		}
//		
		else {
			System.err.println("Community detection algorithm not known " + algorithmName + " " + params);
		}
		return params;
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
		markers.put(MobileMarker.SPEED, "speed");
		markers.put(MobileMarker.LANE, "link_id");
		markers.put(MobileMarker.ANGLE, "angle");
		markers.put(MobileMarker.POS, "offset");
		markers.put(MobileMarker.DISTANCE, "distance");
		markers.put(MobileMarker.DYNAMISM, "dynamism");
		markers.put(MobileMarker.TIMEMEANSPEED, "timeMeanSpeed");
		markers.put(MobileMarker.LINK_DURATION, "linkDuration");
		markers.put(MobileMarker.MOBILITY_SIMILARITY, "mob_sim");
		return markers;
	}
	
	public static HashMap<MobileMarker, String> initializeMarkersLuxembourgOrHighway() {
		HashMap<MobileMarker, String> markers = new HashMap<MobileMarker, String>();
		markers.put(MobileMarker.X, "x");
		markers.put(MobileMarker.Y, "y");
		markers.put(MobileMarker.WEIGHT, "weight");
		markers.put(MobileMarker.COMMUNITY, "community");
		markers.put(MobileMarker.COMMUNITY_SCORE, markers.get(MobileMarker.COMMUNITY)+".score");
		markers.put(MobileMarker.CONGESTION, "congestion");
		markers.put(MobileMarker.MODULE, "module");
		markers.put(MobileMarker.SPEED, "vehicleSpeed");
		markers.put(MobileMarker.LANE, "vehicleLane");
		markers.put(MobileMarker.ANGLE, "vehicleAngle");
		markers.put(MobileMarker.POS, "vehiclePos");
		markers.put(MobileMarker.DISTANCE, "distance");
		markers.put(MobileMarker.DYNAMISM, "dynamism");
		markers.put(MobileMarker.TIMEMEANSPEED, "timeMeanSpeed");
		markers.put(MobileMarker.LINK_DURATION, "linkDuration");
		markers.put(MobileMarker.MOBILITY_SIMILARITY, "mob_sim");
		return markers;
	}

	public static HashMap<String, String> parseArgs(String[] args) {
		HashMap<String, String> programArgs = new HashMap<String, String>();

		if (args.length > 1) {
			for (int i = 0; i < args.length; ++i) {
				String argName = args[i];
				String argValue = args[++i];				
				if (argName.equals("--communityAlgorithmName")) {
					programArgs.put("communityAlgorithmName", argValue.trim());
				}
				if (argName.equals("--congestionAlgorithmName")) {
					programArgs.put("congestionAlgorithmName", argValue.trim());
				}
				if (argName.equals("--goal")) {
					programArgs.put("goal", argValue.trim());
				}
				if (argName.equals("--inputFile")) {
					programArgs.put("filePath", argValue.trim());
				}
				if (argName.equals("--startStep")) {
					programArgs.put("startStep", argValue.trim());
				}
				if (argName.equals("--endStep")) {
					programArgs.put("endStep", argValue.trim());
				}
				if (argName.equals("--outputDir")) {
					programArgs.put("outputDir", argValue.trim());
				}
				if (argName.equals("--numberOfIterations")) {
					programArgs.put("numberOfIterations", argValue.trim());
				}
				if (argName.equals("--speedHistoryLength")) {
					programArgs.put("speedHistoryLength", argValue.trim());
				}
				if (argName.equals("--speedType")) {
					programArgs.put("speedType", argValue.trim());;
				}
			}
		}
		return programArgs;
	}
	
}
