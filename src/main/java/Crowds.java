package main.java;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.graphstream.algorithm.APSP;
import org.graphstream.algorithm.APSP.APSPInfo;
import org.graphstream.algorithm.APSP.TargetPath;
import org.graphstream.algorithm.Algorithm;
import org.graphstream.algorithm.ConnectedComponents;
import org.graphstream.algorithm.Toolkit;
import org.graphstream.algorithm.community.Community;
import org.graphstream.algorithm.community.DecentralizedCommunityAlgorithm;
import org.graphstream.algorithm.community.CongestionMeasure;
import org.graphstream.algorithm.measure.CommunityDistribution;
import org.graphstream.algorithm.community.EdgeMeasure;
import org.graphstream.algorithm.measure.MobileCommunityMeasure;
import org.graphstream.algorithm.measure.Modularity;
import org.graphstream.graph.Edge;
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
import org.graphstream.stream.file.FileSourceFactory;

public class Crowds {
	private static String sep = "\t";
	private static String sep2 = ",";
	private static String communityFileDataFormat = "step"+sep+"node_id"+sep+"x"+sep+"y"+sep+"degree"+sep+"com_id"+sep+"com_score"+sep+"com_size"+sep+"link_id"+sep+"speed"+sep+"avg_speed"+sep+"num_stops"+sep+"is_originator"+sep+"dynamism"+sep+"timeMeanSpeed"+sep+"maxHistoryRecords"+sep+"timeMeanSpeed.count";
	private static String graphFileDataFormat = "step"+sep+"nodes"+sep+"edges"+sep+"singletons"+sep+"connected"+sep+"avg_degree"+sep+"degree_distribution"+sep+"diameter"+sep+"avg_clustering_coefficient"+sep+"cc_count"+sep+"avg_cc_size"+sep+"max_cc_size"+sep+"max_cc_id"+sep+"cc_std_dev"+sep+"com_count"+sep+"avg_com_size"+sep+"max_com_size"+sep+"max_com_id"+sep+"min_com_size"+sep+"min_com_id"+sep+"std_com_dist"+sep+"size_distribution"+sep+"modularity"+sep+"com_modularity"+sep+"iterations"+sep+"iteration_modularities"+sep+"weighted_com_modularity"+sep+"speed_avg"+sep+"speed_std"+sep+"speed_avg_std"+sep+"avgSpeed_avg"+sep+"avgSpeed_std"+sep+"avgSpeed_avg_std"+sep+"avg_edge_weight";

	private FileSource _filesource = null;
	private Graph _graph = null;
	private DecentralizedCommunityAlgorithm _communityAlgorithm = null;
	private CongestionMeasure _congestionAlgorithm = null;
	
	// measures
	ConnectedComponents _cc = null;
	int _singletons = 0;
	int _connected = 0;
	Modularity _modularity = null;
	Modularity _weightedModularity= null;
	CommunityDistribution _ccDistribution = null;
	CommunityDistribution _communityDistribution = null;
	MobileCommunityMeasure _mobileCommunityMeasure = null;
	EdgeMeasure _edgeMeasure = null;

	String _ccMarker = null;
	String _communityMarker = null;
	String _communityScoreMarker = null;
	String _weightMarker = null;
	String _linkDurationMarker = null;
	String _mobilitySimilarityMarker = null;
	
	String _speedMarker = null;
	
	// settings
	int _numberOfIterations = 0;
	private int _startStep = 0;
	int _stopStep = 0;
	String _goal = null;
	
	// images
	Boolean _createImages = false;
	FileSinkImages _fsi = null;
	private String _styleSheetUrl = null;
	private String _imgPrefix = null;
	
	// writing out
	BufferedWriter _outCommunity;
	String[] _outCommunityHeaders;
	BufferedWriter _outGraph;
	BufferedWriter _outEdges;
	String[] _outGraphHeaders;
	BufferedWriter _outAlgorithm;
	
	public Crowds() {
		// set default values different than null and 0, false
		_numberOfIterations = 1;
		_goal = "communities";
		_styleSheetUrl = "css/stylesheet.css";
		_imgPrefix = "img";
	}
	
	public void setSettings(int startStep, int stopStep) {
		this._startStep = startStep;
		this._stopStep = stopStep;
	}
	
	public void setSettings(int startStep, int stopStep, int numberOfIterations, String goal) {
		this._startStep = startStep;
		this._stopStep = stopStep;
		this._numberOfIterations = numberOfIterations;
		this._goal = goal;
	}
	
	/**
	 * Opens DGS file and initialize graph
	 */
	public void readGraph(String filePath) {
		System.out.println("Reading graph from\t" + filePath);
		try {
			_filesource = FileSourceFactory.sourceFor(filePath);
			_graph = new DefaultGraph("fcd");
			_filesource.addSink(_graph);
		    _filesource.begin(filePath);
			if (_createImages) {
				if (_styleSheetUrl == null || _styleSheetUrl.isEmpty()) {
					System.err.println("In order to create images, please specify a correct styleSheetUrl. styleSheetUrl: "+ _styleSheetUrl); 
					_createImages = false;
				}
				initializeFsi();
				_graph.addSink(_fsi);
			}
		}
		catch (Exception e) {
			_graph = null;
			_filesource = null;
			System.err.println("Error " + e.getMessage());
			e.printStackTrace();
			return;
		}
	}
	
	public void initializeConnectedComponents(String marker) {
		if (_graph == null) {
			System.err.println("Please read graph first.");
			return;
		}
		_cc = new ConnectedComponents(_graph);
		_cc.setCountAttribute(marker);
		_ccMarker = marker;
		_cc.init(_graph);	
		_ccDistribution = new CommunityDistribution(marker);
		_ccDistribution.init(_graph);
	}
			
	public void initializeCommunityDetectionAlgorithm(String algorithmName, String marker, Dictionary<String, Object> params) {
		if (_graph == null) {
			System.err.println("Please read graph first");
			return;
		}
		if (algorithmName == null || algorithmName.isEmpty() || marker == null || marker.isEmpty()) {
			System.err.println("Algorithm and marker cannot be empty.");
			return;
		}
		_communityAlgorithm = null;
		try {
			Class algorithmClass = Class.forName("org.graphstream.algorithm.community." + algorithmName);
			_communityAlgorithm = (DecentralizedCommunityAlgorithm) algorithmClass.newInstance();
		} catch (Exception e) {
			System.err.println("Error! " + "Algorithm org.graphstream.algorithm.community." + algorithmName + " cannot be created.");
			e.printStackTrace();
		} 
		if (_communityAlgorithm == null) {
			System.err.println("Error! Algorithm org.graphstream.algorithm.community." + algorithmName + " not found.");
			return;
		}
		_communityAlgorithm.setParameters(params);
//		_communityAlgorithm.staticMode();
		_communityAlgorithm.setMarker(marker);
		_communityAlgorithm.init(_graph);
		_communityMarker = _communityAlgorithm.getMarker();
		_communityScoreMarker = _communityMarker + ".score";
		_weightMarker = (String)_communityAlgorithm.getParameter("weightMarker");
		_linkDurationMarker = (String)_communityAlgorithm.getParameter("linkDurationMarker");
		_mobilitySimilarityMarker = (String)_communityAlgorithm.getParameter("mobilitySimilarityMarker");
		System.out.println("Crowds _weightMarker " + _weightMarker);
		_speedMarker = (String)_communityAlgorithm.getParameter("speedMarker");
		_modularity = new Modularity(_communityMarker);
		_modularity.init(_graph);
		_weightedModularity = new Modularity(_communityMarker, _weightMarker);
		_weightedModularity.init(_graph);
		_communityDistribution = new CommunityDistribution(_communityMarker);
		_communityDistribution.init(_graph);
		_mobileCommunityMeasure = new MobileCommunityMeasure(_communityMarker, _speedMarker);
		_mobileCommunityMeasure.init(_graph);
		System.out.println("_weightMarker " + _weightMarker);
		_edgeMeasure = new EdgeMeasure(_weightMarker);
		_edgeMeasure.init(_graph);
		writeOutAlgorithmInfo(_communityAlgorithm, params, _communityMarker);
	}
	
	public void initializeCongestionDetectionAlgorithm(String algorithmName, String marker, Dictionary<String, Object> params) {
		if (_graph == null) {
			System.err.println("Please read graph first");
			return;
		}
		if (algorithmName == null || algorithmName.isEmpty() || marker == null || marker.isEmpty()) {
			System.err.println("Algorithm and marker cannot be empty.");
			return;
		}
		_congestionAlgorithm = null;
		try {
			Class congestionClass = Class.forName("org.graphstream.algorithm.community." + algorithmName);
			_congestionAlgorithm = (CongestionMeasure) congestionClass.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		if (_congestionAlgorithm == null) {
			System.err.println("Error! Algorithm org.graphstream.algorithm.community." + algorithmName + " not found.");
			 return;
		}
		_congestionAlgorithm.setParameters(params);
		_congestionAlgorithm.init(_graph);

		writeOutAlgorithmInfo(_congestionAlgorithm, params, marker);
	}
	
	private Boolean checkPreliminaries() {
		if (_graph == null) {
			System.err.println("_filesource null. Please call readGraph first.");
			return false;
		}
		if (_startStep == 0 && _stopStep == 0) {
			System.err.println("Please set settings first.");
			return false;
		}
		if (_communityAlgorithm == null) {
			System.err.println("Please initialize community detection algorithm first.");
			return false;
		}
		return true;
	}
	
	private void computeGraphStep(int step) {
		_cc.compute();
		_ccDistribution.compute();
		double sumModularity = 0;
		double[] stepsModularity = new double[_numberOfIterations];
		for (int i = 0; i < _numberOfIterations; ++i) {
			_singletons = 0;
			_connected = 0;
			if (_congestionAlgorithm != null) {
				_congestionAlgorithm.compute();
			}
			_communityAlgorithm.compute();
			_modularity.compute();
			_weightedModularity.compute();
			_communityDistribution.compute();
			_mobileCommunityMeasure.compute();
			_edgeMeasure.compute();
			double modularityIter = _modularity.getMeasure();
			stepsModularity[i] = modularityIter;
			sumModularity += modularityIter;
			if (i == (_numberOfIterations - 1)) { // write out nodes information during the the last iteration
				writeNodes(step);
			}
		}
		double avgModularity = sumModularity / _numberOfIterations;
		writeGraphTimestepStatistics(step, avgModularity, stepsModularity, _weightedModularity.getMeasure());
	}
	
	public void detectCommunities() {
		if (!checkPreliminaries()) {
			return;
		}						
		int step = 0;
		long start_time = System.currentTimeMillis();
		writeOut("Starting simulation for steps:\t" + _startStep + " - " + _stopStep + " at " + start_time);
		try {
			while (_filesource.nextStep()) {
				if (step > _stopStep) {
					break;
				}
				if (step >= _startStep) {
					long currentTime = System.currentTimeMillis();
//			    	System.out.println("step:\t" + step + "\tstopStep\t"+_stopStep + "\tnodes\t"+_graph.getNodeCount() + "\tedgess\t"+_graph.getEdgeCount() + "\ttime\t" + currentTime + "\tduration\t" + (currentTime - start_time));
				}					
				if (_goal.equals("communities")) { computeGraphStep(step); }					
				++step;
			}
			if (_goal.equals("ASPL")) { computeASPL(); }
			writeOut("Finished at step " + step + ", graph.getStep: " +  _graph.getStep() + ", _fileSource.nextStep: " + _filesource.nextStep());
			System.out.println("Finished at step " + step + ", graph.getStep: " +  _graph.getStep() + ", _fileSource.nextStep: " + _filesource.nextStep());
			writeOutSummary(System.currentTimeMillis()-start_time);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			cleanup();
		}
	    return;
	}
	
	private void cleanup() {
		try {
			_filesource.removeSink(_graph);
			_filesource.end();
			if (_createImages) { _fsi.end(); }
			_outGraph.close();
			_outCommunity.close();
			_outAlgorithm.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void detectCommunitiesStreaming() {
		if (!checkPreliminaries()) {
			return;
		}						
		int step = 0;
		long start_time = System.currentTimeMillis();
		System.out.println("Starting simulation for steps:\t" + _startStep + " - " + _stopStep + " at " + start_time);
		try {
			while (step < _stopStep) {
				if (!_filesource.nextStep()) {
					Thread.sleep(250);
				}
				else {
					if (step >= _startStep) {
						long currentTime = System.currentTimeMillis();
//				    	System.out.println("step:\t" + step + "\tstopStep\t"+_stopStep + "\tnodes\t"+_graph.getNodeCount() + "\tedgess\t"+_graph.getEdgeCount() + "\ttime\t" + currentTime + "\tduration\t" + (currentTime - start_time));
					}					
					if (_goal.equals("communities")) { computeGraphStep(step); }
					++step;
				}
			}
			if (_goal.equals("ASPL")) { computeASPL(); }
			writeOut("Finished at step " + step + ", graph.getStep: " +  _graph.getStep() + ", _fileSource.nextStep: " + _filesource.nextStep());
			writeOutSummary(System.currentTimeMillis()-start_time);
		} catch (IOException e) {
			e.printStackTrace();
		}
		catch (InterruptedException e) {
			System.out.println("InterruptedException " + e);
			e.printStackTrace();
		}
		finally {
			cleanup();
		}
	    return;
	}
	
	private void computeASPL() {
		System.out.println("Computing aspl for step:\t");
		APSP apsp = new APSP();
        apsp.init(_graph); // registering apsp as a sink for the graph
        apsp.setDirected(false); // undirected graph
        apsp.setWeightAttributeName("weight"); // ensure that the attribute name used is "weight"
        apsp.compute(); // the method that actually computes shortest paths
        double allSum = 0;
        for (Node node1 : _graph.getNodeSet()) {
			String id1 = node1.getId(); 
        	APSPInfo info = _graph.getNode(id1).getAttribute(APSPInfo.ATTRIBUTE_NAME);
			double sumShortest = 0;	
			HashMap<String, TargetPath>	targets = info.targets;
			if (targets.size() > 0) {
				for (TargetPath path : targets.values()) {
					sumShortest += path.distance;
				}
				double avgShortest = sumShortest / targets.size();
				allSum += avgShortest;
			}
        }
        double allAvgShortest = allSum / (double)_graph.getNodeCount();
        System.out.println(" avg shortest path to all targets " + allAvgShortest);
	}
	
	private void writeOut(String str) {
		if (_outAlgorithm == null) {
			return;
		}
		try {
			_outAlgorithm.write(str + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void writeOutAlgorithmInfo(Algorithm algorithm, Dictionary<String,Object> algorithmParameters, String marker) {
		if (_outAlgorithm == null) {
			System.err.println("aaa " + _outAlgorithm);
			return;
		}
		try {
			_outAlgorithm.write("Running community detection ... " + "\n");
			_outAlgorithm.write("Algorithm\t" + algorithm + "\n");
			_outAlgorithm.write("Parameters\t" + algorithmParameters + "\n");
			_outAlgorithm.write("Marker\t" + marker + "\n");
			_outAlgorithm.write("_weightMarker " + _weightMarker);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void writeOutSummary(long duration) {
		if (_outAlgorithm == null) {
			return;
		}
		try {
			_outAlgorithm.write("Finish simulation at " + System.currentTimeMillis() + ", simulation time: " + duration + "\n");
			_outAlgorithm.write("number of steps\t" + _graph.getStep() + "\n");
			_outAlgorithm.write("Number of nodes\t" + _graph.getNodeCount() + "\n");
			_outAlgorithm.write("Number of edges\t" + _graph.getEdgeCount() + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void openOutputFiles(String outputDir) {
		String dir = System.getProperty("user.dir");
		_styleSheetUrl = "url('file:////"+dir+"/"+ _styleSheetUrl +"')";
		_imgPrefix = outputDir + _imgPrefix;
		
		String communityOutputFile = outputDir + "crowds_communities.csv";
		String graphOutputFile = outputDir + "crowds_graph.csv";
		String edgesOutputFile = outputDir + "crowds_edges.csv";
		String algorithmOutputFile = outputDir + "crowds_algorithm.csv";
		try {
			_outCommunity = new BufferedWriter(new FileWriter(communityOutputFile));
			_outCommunityHeaders = communityFileDataFormat.split(sep);
			_outCommunity.write(communityFileDataFormat);
			_outCommunity.newLine();
			_outGraph = new BufferedWriter(new FileWriter(graphOutputFile));
			_outGraphHeaders = graphFileDataFormat.split(sep);
			_outGraph.write(graphFileDataFormat);
			_outGraph.newLine();
			_outEdges = new BufferedWriter(new FileWriter(edgesOutputFile));
			_outEdges.write("step\tedge_id\tdegree_a\tdegree_b\tsim_n\tlink_duration\tmobility_similarity\tweight");
			_outEdges.newLine();
			_outAlgorithm = new BufferedWriter(new FileWriter(algorithmOutputFile));
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * "step"
	 * "node_id" "x" "y"
	 * "degree" "neighbors"
	 * "cc_id" "cc_size" 
	 * "com_id" "com_score" "com_size";
	 * @param step
	 */
    private void writeNodes(int step) {
		for (Node node : _graph.getNodeSet()) {
			Community community = (Community)node.getAttribute(_communityMarker);
			Object linkId = "";
			String linkidMarker = "link_id";
			if (node.hasAttribute("vehicleLane")) {
				linkidMarker = "vehicleLane";
			}
			linkId = (Object)node.getAttribute(linkidMarker);
			Object speed = 0;
			String speedMarker = "speed";
			if (node.hasAttribute("vehicleSpeed")) {
				speedMarker = "vehicleSpeed";
			}
			speed = (Object)node.getAttribute(speedMarker);
			Object avgSpeed = 0;
			if (node.hasAttribute("speed")) {
				avgSpeed = (Object)node.getAttribute("speed");
				if (avgSpeed.equals(0.0) && !speed.equals(0)) {
					avgSpeed = speed;
				}
			}
			Integer numberOfStopsOnLane = 0;
			String numberOfStopsMarker = linkidMarker +".stops";
			if (node.hasAttribute(numberOfStopsMarker)) {
				numberOfStopsOnLane = node.getAttribute(numberOfStopsMarker);
			}
			String communityId = community.getId();
			Double score = 0.0;
			if (_communityScoreMarker != null && !_communityScoreMarker.isEmpty()) { score = node.getAttribute(_communityScoreMarker); }		
			int degree = node.getEdgeSet().size();
			String isOriginator = "0";
			if (node.hasAttribute(_communityMarker + ".originator")) {
				Boolean originator = (Boolean)node.getAttribute(_communityMarker + ".originator");
				isOriginator = originator ? "1" : "0";
			}
			Double dynamism = 0.0;
			if (node.hasAttribute("dynamism")) {
				dynamism = (Double)node.getAttribute("dynamism");
			}
			Double timeMeanSpeed = 0.0;
			if (node.hasAttribute("timeMeanSpeed")) {
				timeMeanSpeed = (Double)node.getAttribute("timeMeanSpeed");
			}
			Integer maxHistoryRecords = 0;
			if (node.hasAttribute("maxHistoryRecords")) {
				maxHistoryRecords = (Integer)node.getAttribute("maxHistoryRecords");
			}
			Integer timeMeanSpeedCount = 0;
			if (node.hasAttribute("timeMeanSpeed.count")) {
				timeMeanSpeedCount = (Integer)node.getAttribute("timeMeanSpeed.count");
			}
			
			DecimalFormat df = new DecimalFormat("#.##");
			try {
				if (node.hasAttribute("x")) {
				int comSize = _communityDistribution.communitySize(community);
					_outCommunity.write(step+"\t");
					_outCommunity.write(node.getId()+"\t");
					_outCommunity.write(df.format(node.getAttribute("x"))+"\t");
					_outCommunity.write(df.format(node.getAttribute("y"))+"\t");
					_outCommunity.write(degree+"\t");
					_outCommunity.write(communityId+"\t"); // comId
					_outCommunity.write(df.format(score)+"\t");
					_outCommunity.write(comSize+"\t"); // comSize
					_outCommunity.write(linkId.toString()+"\t"); // linkId
					_outCommunity.write(df.format(speed)+"\t"); // speed
					_outCommunity.write(df.format(avgSpeed)+"\t"); // avgspeed
					_outCommunity.write(numberOfStopsOnLane.toString()+"\t"); // number of stops on lane
					_outCommunity.write(isOriginator+"\t"); // number of stops on lane
					_outCommunity.write(df.format(dynamism) +"\t"); // dynamic property (>0 accelarated , < 0 deccelarated)
					_outCommunity.write(df.format(timeMeanSpeed)+"\t"); // timeMeanSpeed
					_outCommunity.write(df.format(maxHistoryRecords)+"\t"); // timeMeanSpeed
					_outCommunity.write(df.format(timeMeanSpeedCount)); // timeMeanSpeed
					_outCommunity.write("\n"); 
					_outCommunity.flush();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if (degree > 0) {
				++_connected;
			}
			else {
				++_singletons;
			}
		}
	}
	
    public Dictionary<String, Object> getMeasures(int step, double avgModularity, double[] stepsModularity, double weightedModularity) {
    	Dictionary<String, Object> values = new Hashtable<String, Object>();
		values.put("step", step);
		values.put("nodes", _graph.getNodeCount());
		values.put("edges", _graph.getEdgeCount());		
		values.put("singletons", _singletons);
		values.put("connected", _connected);
		values.put("avg_degree", Toolkit.averageDegree(_graph));
		int[] degreeDistribution = Toolkit.degreeDistribution(_graph);
		if (degreeDistribution != null) {
			String degreeDistributionStr = "";
			for (int j = 0; j < degreeDistribution.length; ++j) {
				degreeDistributionStr += degreeDistribution[j] + sep2;
			}
			values.put("degree_distribution",degreeDistributionStr);
		}
		values.put("diameter", Toolkit.diameter(_graph));
		values.put("avg_clustering_coefficient", Toolkit.averageClusteringCoefficient(_graph));
		values.put("cc_count",_cc.getConnectedComponentsCount());
		values.put("avg_cc_size", _ccDistribution.average());
		List<Node> giantComponent = _cc.getGiantComponent();
		int ccId = -1;
		if (giantComponent == null) {
			values.put("max_cc_size",-1);
			values.put("max_cc_id", -1);
			values.put("cc_std_dev", -1);
		}
		else {
			if (giantComponent.size() > 0) {
				Node node = giantComponent.get(0);
				ccId = (Integer)node.getAttribute(_ccMarker);
			}
			values.put("max_cc_size",giantComponent.size());
			values.put("max_cc_id", ccId);
			values.put("cc_std_dev", _ccDistribution.average());
		}
		values.put("com_count", _communityDistribution.number());
		values.put("avg_com_size", _communityDistribution.average());
		values.put("max_com_size", _communityDistribution.maxCommunitySize());
		values.put("max_com_id",(_communityDistribution.biggestCommunity()==null ? -1: (Community)_communityDistribution.biggestCommunity()));
		values.put("min_com_size", _communityDistribution.minCommunitySize());
		values.put("min_com_id",(_communityDistribution.smallestCommunity()==null ? -1: (Community)_communityDistribution.smallestCommunity()));
		values.put("std_com_dist",_ccDistribution.stdev());
		int[] sizeDistribution = _ccDistribution.sizeDistribution();
		if (sizeDistribution != null) {
			String distributionStr = "";
			for (int j = 0; j < sizeDistribution.length; ++j) {
				distributionStr += sizeDistribution[j] + sep2;
			}
			values.put("size_distribution",distributionStr);
		}
		// modularity
		values.put("modularity",_modularity.getMeasure());
		values.put("com_modularity", avgModularity);
		values.put("iterations",_numberOfIterations);
		if (stepsModularity != null) {
			String modularities = "";
			for (int i = 0; i < stepsModularity.length; ++i) {
				modularities += stepsModularity[i]+ sep2;
			}
			values.put("iteration_modularities", modularities);
		}
		values.put("weighted_com_modularity", weightedModularity);
		if (_mobileCommunityMeasure != null) {
			values.put("speed_avg", _mobileCommunityMeasure.averageValue());
			values.put("speed_std", _mobileCommunityMeasure.stdev());
			values.put("speed_avg_std",_mobileCommunityMeasure.averageStddev());
		}
		if (_edgeMeasure != null) {
			values.put("avg_edge_weight", _edgeMeasure.getMean());
		}
		return values;
    }
    
    /** Writes cvs file with following columns:
     * @param step Current step
     * @param avgModularity average modularity of detected community structure
     * @param stepsModularity modularity for each iteration step
     * @param _comDist community statistics 
     * @throws IOException
     */
	public void writeGraphTimestepStatistics(int step, double avgModularity, double[] stepsModularity, double weightedModularity) {
		Dictionary<String, Object> values = getMeasures(step, avgModularity, stepsModularity, weightedModularity);
		int headersCount = _outGraphHeaders.length;
		int i = 0;
		try {
			for (String column : _outGraphHeaders) {
				if (++i != headersCount) {
					_outGraph.write(values.get(column) + sep);
				}
				else {
					Object val = values.get(column);
					if (val != null) {
						_outGraph.write(val.toString());
					}
				}
			}
			_outGraph.newLine();
			_outGraph.flush();
			
//			write graph edges
			Iterator<Edge> it = _graph.getEdgeIterator();
			while (it.hasNext()) {
				Edge edge = it.next();
				// step\tedge_id\tdegree_a\t\degree_b\tsim_n\tlink_duration\tmobility_similarity\tweight
				int degree_a = edge.getNode0().getDegree();
				int degree_b = edge.getNode1().getDegree();
				Double linkDuration = edge.hasAttribute(this._linkDurationMarker) ? (Double)edge.getAttribute(this._linkDurationMarker) : 0.0;
				Double mobilitySimilarity = edge.hasAttribute(this._mobilitySimilarityMarker) ? (Double)edge.getAttribute(this._mobilitySimilarityMarker) : 0.0;
				Double weight = edge.hasAttribute(this._weightMarker) ? (Double)edge.getAttribute(this._weightMarker) : 0.0;
				Double n_sim = edge.hasAttribute("n_sim") ? (Double)edge.getAttribute("n_sim") : 0.0;
				if (edge.getId().equals("60550-63244")) {
					System.err.println("step\t" + step + "\t" + edge.getId() + "\t" +  degree_a +"\t" +  degree_b +"\t" +  n_sim + "\t" + linkDuration + "\t" + mobilitySimilarity + "\t" + weight + "\t" );
				
				}
				_outEdges.write(step + "\t" + edge.getId() + "\t" +  degree_a +"\t" +  degree_b +"\t" +  n_sim + "\t" + linkDuration + "\t" + mobilitySimilarity + "\t" + weight + '\n');
			}
			_outEdges.flush();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	public void setImagesGeneration(boolean generateImages) throws IOException {
		_createImages = generateImages;
		
	}
	
	private void initializeFsi() throws IOException {
		OutputType type = OutputType.PNG;
		Resolution resolution = Resolutions.HD720;
		OutputPolicy outputPolicy = OutputPolicy.BY_STEP;
		_fsi = new FileSinkImages(_imgPrefix, type, resolution, outputPolicy );
		_fsi.setStyleSheet(_styleSheetUrl);
		_fsi.setLayoutPolicy( LayoutPolicy.NO_LAYOUT);
		_fsi.begin(_imgPrefix);
	}

}
