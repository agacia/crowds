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
			args = new String[]{ "--inputFile",  "//Users/agata/shared/thesis_scenarios/highway_congestion/crowds/input/vanet.dgs", // "/Users/agata/workspace/Jean/Agata/vanet_probeData_v15-30+300_17032014.dgs", // "/Users/agata/Documents/PhD/sumo_scenarios/twolanes_long_120kmph_600/vanet.dgs",
				"--communityAlgorithmName", "SandSharc", // "SandSharc_ag", //"NewSawSharc_oryg", // "SandSharc_hybrid", // SandSharc_mobility SandSharc_link_duration   SandSharc_oryg StableCrowdz "MobileSandSharc_oryg", // "Crowdz", //"SandSharc_oryg",
				"--congestionAlgorithmName", "CongestionMeasure",
				"--goal", "communities",
				"--startStep", "0",
				"--endStep", "1000",
				"--outputDir", "/Users/agata/workspace/crowds/output/eclipse/",
				"--speedHistoryLength", "90",
				"--speedType", "timemean",
				"--numberOfIterations", "1"};
		}
		HashMap<String, String> programArgs = parseArgs(args);
		HashMap<MobileMarker, String> markers = initializeMarkers();
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
		if (communityAlgorithmName.equals("SandSharc_oryg") || 
				communityAlgorithmName.equals("SandSharc_mobility") || 
				communityAlgorithmName.equals("SandSharc_link_duration") ||
				communityAlgorithmName.equals("SandSharc_hybrid")) {
			communityAlgorithmName = "SandSharc_oryg";
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
		// Oryginal SandSharc + link_duration as stability measure
		if (algorithmName.equals("SandSharc_oryg")) {
			params.put("weightMarker", "");
		}
		// Oryginal SandSharc + mobility_metric as stability measure
		else if (algorithmName.equals("SandSharc_mobility")) {
			params.put("mobilitySimilarityMarker", markers.get(MobileMarker.MOBILITY_SIMILARITY));
			params.put("linkDurationMarker", markers.get(MobileMarker.LINK_DURATION));
			params.put("weightMarker", markers.get(MobileMarker.MOBILITY_SIMILARITY));
//			params.put("speedMarker", markers.get(MobileMarker.TIMEMEANSPEED));
//			params.put("angleMarker", markers.get(MobileMarker.ANGLE));
		}
		// Oryginal SandSharc + mobility_metric as stability measure
		else if (algorithmName.equals("SandSharc_link_duration")) {
			params.put("mobilitySimilarityMarker", markers.get(MobileMarker.MOBILITY_SIMILARITY));
			params.put("linkDurationMarker", markers.get(MobileMarker.LINK_DURATION));
			params.put("weightMarker", markers.get(MobileMarker.LINK_DURATION));
		}
		else if (algorithmName.equals("SandSharc") || algorithmName.equals("SandSharc_ag") || algorithmName.equals("SandSharc_hybrid") || algorithmName.equals("SandSharc_copy")) {
			params.put("mobilitySimilarityMarker", markers.get(MobileMarker.MOBILITY_SIMILARITY)); // used in CrowdsTest to print edges
			params.put("linkDurationMarker", markers.get(MobileMarker.LINK_DURATION)); // used in CrowdsTest to print edges
			params.put("weightMarker", "hybrid");
		}
		// Modified SandSharc + mobility_metric as stability measure
//		else if (algorithmName.equals("Crowdz")) {	
//			params.put("weightMarker", "mobility_sim");
//			params.put("speedMarker", markers.get(MobileMarker.TIMEMEANSPEED));
//			params.put("angleMarker", markers.get(MobileMarker.ANGLE));
//		}
//		else if (algorithmName.equals("StableCrowdz")) {	
//			params.put("weightMarker", markers.get(MobileMarker.LINK_DURATION));
//			params.put("mobilitySimilarityMarker", markers.get(MobileMarker.MOBILITY_SIMILARITY));
//		}
//		if (algorithmName.equals("Leung")) {
//			params.put("m", Double.parseDouble(programArgs.get("mParameter")));
//			params.put("delta", Double.parseDouble(programArgs.get("deltaParameter")));
//			params.put("weightMarker", markers.get(MobileMarker.WEIGHT));
//		}
//		else if (algorithmName.equals("MobileLeung")) {
//			params.put("weightMarker", markers.get(MobileMarker.WEIGHT)); 
//			params.put("mobilityMarkers", markers);
//			params.put("m", 0.1);
//			params.put("delta", 0.05);
//		}
		
//		else if (algorithmName.equals("MobileSandSharc") || algorithmName.equals("MobileSharc")) {
//			params.put("weightMarker", markers.get(MobileMarker.WEIGHT));
//			params.put("speedMarker", markers.get(MobileMarker.SPEED));
//			params.put("timeMeanSpeedMarker", markers.get(MobileMarker.TIMEMEANSPEED));
//			params.put("angleMarker", markers.get(MobileMarker.ANGLE));
//			params.put("congestionSpeedThreshold", Double.parseDouble(programArgs.get("congestionSpeedThreshold")));
//			params.put("speedType", programArgs.get("speedType"));
//		}
		else {
			params.put("weightMarker", "");
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
		markers.put(MobileMarker.SPEED, "vehicleSpeed");
		markers.put(MobileMarker.AVG_SPEED, "vehicleAvgSpeed");
		markers.put(MobileMarker.LANE, "vehicleLane");
		markers.put(MobileMarker.ANGLE, "vehicleAngle");
		markers.put(MobileMarker.SLOPE, "vehicleSlope");
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
