package test.java;

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
			args = new String[]{ "--inputFile", "/Users/agata/workspace/scenarios/Kirchberg/sumoOutput-accident/400-400-200/Kirchberg-400-400-200.dgs_link_duration.dgs",
				"--communityAlgorithmName", "NewSawSharc_oryg",
				"--congestionAlgorithmName", "CongestionMeasure",
				"--goal", "communities",
				"--startStep", "0",
				"--endStep", "100",
				"--outputDir", "/Users/agata/workspace/crowds/output/eclipse/",
				"--speedHistoryLength", "10",
				"--speedType", "instant",
				"--numberOfIterations", "1"};
		}
		HashMap<String, String> programArgs = parseArgs(args);
		HashMap<MobileMarker, String> markers = initializeMarkers();
		Dictionary<String, Object> communityAlgorithmParams = getAlgorithmParams(programArgs, markers);
		Dictionary<String, Object> congestionAlgorithmParams = getCongestionParams(programArgs, markers);
		
		Crowds crowds = new Crowds();	
		crowds.setSettings(Integer.parseInt(programArgs.get("startStep")), 
				Integer.parseInt(programArgs.get("endStep")),
				Integer.parseInt(programArgs.get("numberOfIterations")), 
				programArgs.get("goal"));
		crowds.openOutputFiles(programArgs.get("outputDir"));
		crowds.readGraph(programArgs.get("filePath"));
		
		crowds.initializeConnectedComponents(markers.get(MobileMarker.MODULE));
		crowds.initializeCommunityDetectionAlgorithm(programArgs.get("communityAlgorithmName"), markers.get(MobileMarker.COMMUNITY), communityAlgorithmParams);
		crowds.initializeCongestionDetectionAlgorithm(programArgs.get("congestionAlgorithmName"), markers.get(MobileMarker.CONGESTION), congestionAlgorithmParams);
		
		crowds.detectCommunities();
	}
	
	public static Hashtable<String, Object> getCongestionParams(HashMap<String, String> programArgs, HashMap<MobileMarker, String> markers) {
		Hashtable<String, Object> params = new Hashtable<String, Object>();
		String algorithmName = programArgs.get("congestionAlgorithmName");
		if (algorithmName.equals("CongestionCrowdz") || algorithmName.equals("CongestionMeasure")) {	
			params.put("weightMarker", "");
			params.put("speedMarker", markers.get(MobileMarker.SPEED));
			params.put("laneMarker", markers.get(MobileMarker.LANE));
			params.put("dynamismMarker", markers.get(MobileMarker.DYNAMISM));
			params.put("linkDurationMarker", markers.get(MobileMarker.LINK_DURATION));
			params.put("speedHistoryLength", Integer.parseInt(programArgs.get("speedHistoryLength")));
			params.put("speedType", programArgs.get("speedType"));
		}
		return params;
	}
	
	public static Dictionary<String, Object> getAlgorithmParams(HashMap<String, String> programArgs, HashMap<MobileMarker, String> markers) {
		Dictionary<String, Object> params = new Hashtable<String, Object>();
		String algorithmName = programArgs.get("communityAlgorithmName");
		if (algorithmName.equals("NewSawSharc_oryg")) {
			params.put("weightMarker", markers.get(MobileMarker.LINK_DURATION));
		}
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
//		else if (algorithmName.equals("Crowdz")) {	
//			params.put("weightMarker", "");
//			params.put("speedMarker", markers.get(MobileMarker.SPEED));
//			params.put("avgSpeedMarker", markers.get(MobileMarker.AVG_SPEED));
//			params.put("angleMarker", markers.get(MobileMarker.ANGLE));
//			params.put("dynamismMarker", markers.get(MobileMarker.DYNAMISM));
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
//				//  mobile congestion detection
//				if (argName.equals("--congestionSpeedThreshold")) {
//					programArgs.put("congestionSpeedThreshold", argValue.trim());
//				}
				if (argName.equals("--speedHistoryLength")) {
					programArgs.put("speedHistoryLength", argValue.trim());
				}
				if (argName.equals("--speedType")) {
					programArgs.put("speedType", argValue.trim());;
				}
//				// Leung
//				if (argName.equals("--delta")) {
//					programArgs.put("deltaParameter", argValue);
//				}
//				if (argName.equals("--mParameter")) {
//					programArgs.put("mParameter", argValue);
//				}
			}
		}
		return programArgs;
	}
	
}
