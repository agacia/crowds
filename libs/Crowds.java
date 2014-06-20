package main.java;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.graphstream.algorithm.APSP;
import org.graphstream.algorithm.APSP.APSPInfo;
import org.graphstream.algorithm.APSP.TargetPath;
import org.graphstream.algorithm.Algorithm;
import org.graphstream.algorithm.ConnectedComponents;
import org.graphstream.algorithm.Toolkit;
import org.graphstream.algorithm.community.Community;
import org.graphstream.algorithm.community.DecentralizedCommunityAlgorithm;
import org.graphstream.algorithm.community.CongestionMeasure;
import org.graphstream.algorithm.community.EpidemicCommunityAlgorithm;
import org.graphstream.algorithm.community.Leung;
import org.graphstream.algorithm.community.MobileLeung;
import org.graphstream.algorithm.measure.CommunityDistribution;
import org.graphstream.algorithm.measure.MobileCommunityMeasure;
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
import org.graphstream.stream.file.FileSourceFactory;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;
import org.graphstream.ui.swingViewer.View;
import org.graphstream.ui.swingViewer.Viewer;
import org.graphstream.ui.layout.Layout;
import org.graphstream.ui.layout.LayoutRunner;
import org.graphstream.ui.layout.springbox.implementations.LinLog;
import com.sun.org.apache.xpath.internal.operations.Bool;

import static org.graphstream.algorithm.Toolkit.*;

public class Crowds {
	
	private FileSource _filesource;
	private Graph _graph;
	private String _ccMarker;
	private String _communityMarker;
	private String _scoreMarker;
	private String[] _printOutMarkers;
	private String[] _mobilityMarkers;
	private Dictionary<String, Object> _algorithmParameters;
	private ConnectedComponents _cc;
	private CommunityDistribution _ccDist;
	private Modularity _modularity;
	private DecentralizedCommunityAlgorithm _algorithm;
	private CongestionMeasure _congestionAlgorithm;
	private int _startStep;
	int _stopStep;
	int _numberOfIterations;
	FileSinkImages _fsi;
	Boolean _createImages;
	String _imgPrefix;
	String _styleSheetUrl;
	int _singletons;
	int _connected;
	BufferedWriter _outCommunity;
	String[] _outCommunityHeaders;
	BufferedWriter _outGraph;
	String[] _outGraphHeaders;
	String _sep;
	String _sep2;
	CommunityDistribution _comDist;
	MobileCommunityMeasure _mobCom;
	String _goal = "communities";
	String numberOfStopsMarker = "vehicleLane.stops";
	
	public Crowds() {
		_numberOfIterations = 1;
		_startStep = 0;
		_stopStep = 0;
		_createImages = false;
		_singletons = 0;
		_connected = 0;
	}
	
	public int get_numberOfIterations() {
		return _numberOfIterations;
	}

	public void set_numberOfIterations(int _numberOfIterations) {
		this._numberOfIterations = _numberOfIterations;
	}
	
	public void set_printOutMarkers(String[] _printOutMarkers) {
		this._printOutMarkers = _printOutMarkers;
	}
	
	public void set_goal(String goal) {
		this._goal = goal;
	}

	/**
	 * Opens DGS file and initialize graph
	 */
	public void readGraph(String filePath) {

		System.out.println("Reading graph from\t" + filePath);
		System.out.println("Goal\t" + _goal);
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
			System.err.println("Error " + e.getMessage());
			e.printStackTrace();
			return;
		}
	}
	
	public void initializeConnectedComponents(String marker) {
		initializeConnectedComponents(marker, null);
	}
	
	public void initializeConnectedComponents(String marker, String cutMarker) {
		_ccMarker = marker;
		System.out.println("Marker for connected components and modularity\t\"" + _ccMarker + "\"");
		_cc = new ConnectedComponents(_graph);
		_cc.setCountAttribute(_ccMarker);
		if (cutMarker != null && !cutMarker.isEmpty()) {
			_cc.setCutAttribute("cut");
		}
		_ccDist = new CommunityDistribution(_ccMarker);
		_ccDist.init(_graph);
		_cc.init(_graph);		
		_modularity = new Modularity(_ccMarker);	
		_modularity.init(_graph);
	}
	
	public void setRunningSteps(int startStep, int stopStep) {
		this._startStep = startStep;
		this._stopStep = stopStep;
	}
	
	private Boolean checkPreliminaries() {
		if (_filesource == null) {
			System.err.println("_filesource null. Please call readGraph first.");
			return false;
		}
		if (_startStep == 0 && _stopStep == 0) {
			System.err.println("Please setRunningSteps first.");
			return false;
		}
		return true;
	}
	
	public void detectCommunities(String algorithm, String marker, Dictionary<String, Object> params, String congestion, String congestionMarker, Dictionary<String, Object> congestionParams) {
		if (!checkPreliminaries()) {
			return;
		}
		if (algorithm == null || algorithm.isEmpty() || marker == null || marker.isEmpty()) {
			System.err.println("Algorithm and marker cannot be empty.");
			return;
		}
		// Get algorithm for community detection
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
		// Get congestion
		Class congestionClass;
		if (congestion != null && !congestion.isEmpty()) {
			try {
				congestionClass = Class.forName("org.graphstream.algorithm.community." + congestion);
				_congestionAlgorithm = (CongestionMeasure) congestionClass.newInstance();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			_congestionAlgorithm.setParameters(congestionParams);
			_congestionAlgorithm.init(_graph);
			printAlgorithmInfo(_congestionAlgorithm, congestionParams, numberOfStopsMarker);
		}
		_algorithmParameters = params;
		_algorithm.init(_graph);
		_algorithm.staticMode();
		_algorithm.setMarker(marker);
		_algorithm.setParameters(_algorithmParameters);
		_communityMarker = _algorithm.getMarker();
		_scoreMarker = _communityMarker + ".score";
		printAlgorithmInfo(_algorithm, _algorithmParameters, _communityMarker);
		
		Modularity modularityCom = new Modularity(_communityMarker);
		Modularity weightedModularityCom = new Modularity(_communityMarker, (String)params.get("weightMarker"));
		if (_goal.equals("communities")) {
			modularityCom.init(_graph);
			weightedModularityCom.init(_graph);
			_comDist = new CommunityDistribution(_communityMarker);
			_comDist.init(_graph);
			String mobileMarker = (String)_algorithmParameters.get("speedMarker");
			_mobCom = mobileMarker == null ? null : new MobileCommunityMeasure(_communityMarker, mobileMarker);
			if (_mobCom != null) {
				_mobCom.init(_graph);
			}
		}	
		int step = 0;
		long start_time = System.currentTimeMillis();
		System.out.println("Starting simulation for steps:\t" + _startStep + " - " + _stopStep + " at " + start_time);
		

		try {
			// streaming
			while (step < _stopStep) {
				if (!_filesource.nextStep()) {
					Thread.sleep(250);
				}
				else {
			// end streaming
					// not streaming
//				while (_filesource.nextStep()) {
//						if (step > _stopStep) {
//							break;
//						}
					// end not streaming
					if (step >= _startStep) {
						long currentTime = System.currentTimeMillis();
				    	System.out.println("step:\t" + step + "\tstopStep\t"+_stopStep + "\tnodes\t"+_graph.getNodeCount() + "\tedgess\t"+_graph.getEdgeCount() + "\ttime\t" + currentTime + "\tduration\t" + (currentTime - start_time));
					}
					if (_goal.equals("communities"))	{
					_cc.compute();
					_ccDist.compute();
						double sumModularity = 0;
						double[] stepsModularity = new double[_numberOfIterations];
						for (int i = 0; i < _numberOfIterations; ++i) {
							_singletons = 0;
							_connected = 0;
							if (_congestionAlgorithm != null) {
								_congestionAlgorithm.compute();
							}
							_algorithm.compute();
							modularityCom.compute();
							weightedModularityCom.compute();
							_comDist.compute();
							if (_mobCom != null) { _mobCom.compute(); }
							double modularityIter = modularityCom.getMeasure();
							stepsModularity[i] = modularityIter;
							sumModularity += modularityIter;
							if (i == (_numberOfIterations - 1)) { // write out nodes information during the the last iteration
								writeNodes(step);
							}
						}
						double avgModularity = sumModularity / _numberOfIterations;
						WriteGraphTimestepStatistics(step, avgModularity, stepsModularity, weightedModularityCom.getMeasure());
					}
					++step;
				}
			// streaming
			}
			// end streaming
			System.out.println("Finished at step " + step + ", graph.getStep: " +  _graph.getStep() + ", _fileSource.nextStep: " + _filesource.nextStep());
			printSummary(System.currentTimeMillis()-start_time);
			if (_goal.equals("ASPL")) {
				System.out.println("Computing aspl for step:\t" + step);
				computeASPL();
			}
			_filesource.end();
			if (_createImages) {
				_fsi.end();
			}
			_outGraph.close();
			_outCommunity.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// streaming
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			System.out.println("InterruptedException " + e);
			e.printStackTrace();
		}
		// end streaming
		finally {
			_filesource.removeSink(_graph);
		}
	    return;
	}
	
	private void computeASPL() {
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
	
	private void printAlgorithmInfo(Algorithm algorithm, Dictionary<String,Object> algorithmParameters, String marker) {
		System.out.println("Running community detection ... ");
		System.out.println("Algorithm\t" + algorithm);
		System.out.println("Parameters\t" + algorithmParameters);
		System.out.println("Marker\t" + marker);
	}
	
	private void printSummary(long duration) {
		System.out.println("Finish simulation at " + System.currentTimeMillis() + ", simulation time: " + duration);
		System.out.println("number of steps\t" + _graph.getStep());
		System.out.println("Number of nodes\t" + _graph.getNodeCount());
		System.out.println("Number of edges\t" + _graph.getEdgeCount());
	}
	
	public void openOutputFiles(String communityOutputFile, String communityFileDataFormat, String graphOutputFile, String graphFileDataFormat, String sep, String sep2) {
		System.out.println("Open output files\t" + communityOutputFile + ", " + graphOutputFile);
		_sep = sep;
		_sep2 = sep2;
		try {
			_outCommunity = new BufferedWriter(new FileWriter(communityOutputFile));
			_outCommunityHeaders = communityFileDataFormat.split(_sep);
			_outCommunity.write(communityFileDataFormat);
			_outCommunity.newLine();
			_outGraph = new BufferedWriter(new FileWriter(graphOutputFile));
			_outGraphHeaders = graphFileDataFormat.split(_sep);
			_outGraph.write(graphFileDataFormat);
			_outGraph.newLine();
			
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
			Object cc = node.getAttribute(_ccMarker);
			Community community = (Community)node.getAttribute(_communityMarker);
			Object linkId = "";
			if (node.hasAttribute("vehicleLane")) {
				linkId = (Object)node.getAttribute("vehicleLane");
			}
			Object speed = 0;
			if (node.hasAttribute("vehicleSpeed")) {
				speed = (Object)node.getAttribute("vehicleSpeed");
			}
			Object avgSpeed = 0;
			if (node.hasAttribute("vehicleAvgSpeed")) {
				avgSpeed = (Object)node.getAttribute("vehicleAvgSpeed");
				if (avgSpeed.equals(0.0) && !speed.equals(0)) {
					avgSpeed = speed;
				}
			}
			Integer numberOfStopsOnLane = 0;
			if (node.hasAttribute(numberOfStopsMarker)) {
				numberOfStopsOnLane = node.getAttribute(numberOfStopsMarker);
			}
			String communityId = community.getId();
			Double score = 0.0;
			if (_scoreMarker != null && !_scoreMarker.isEmpty()) {
				score = node.getAttribute(_scoreMarker);
			}		
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
				int comSize = _comDist.communitySize(community);
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
			} catch (IOException e) {
				// TODO Auto-generated catch block
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
	
    /** Writes cvs file with following columns:
     * "step" "nodes" "edges" "singletons" "connected"
     * "avg_degree" "degree_distribution" "diameter" "avg_clustering_coefficient"
     * "cc_count" "avg_cc_size" "max_cc_size" "max_cc_id" "cc_std_dev" 
     * "com_count" "avg_com_size" "max_com_size" "max_com_id" "min_com_size" "min_com_id" "std_com_dist"
     * "modularity" "com_modularity" "iterations" "iteration_modularities";
     * @param step Current step
     * @param avgModularity average modularity of detected community structure
     * @param stepsModularity modularity for each iteration step
     * @param _comDist community statistics 
     * @throws IOException
     */
	public void WriteGraphTimestepStatistics(int step, double avgModularity, double[] stepsModularity, double weightedModularity) throws IOException {
		Dictionary<String, Object> values = new Hashtable<String, Object>();
		values.put("step", step);
		values.put("nodes", _graph.getNodeCount());
		values.put("edges", _graph.getEdgeCount());		
		values.put("singletons", _singletons);
		values.put("connected", _connected);
		
		// stats
		Toolkit toolkit = new Toolkit();
		values.put("avg_degree", toolkit.averageDegree(_graph));
		int[] degreeDistribution = toolkit.degreeDistribution(_graph);
		if (degreeDistribution != null) {
			String degreeDistributionStr = "";
			for (int j = 0; j < degreeDistribution.length; ++j) {
				degreeDistributionStr += degreeDistribution[j] + _sep2;
			}
			values.put("degree_distribution",degreeDistributionStr);
		}
		values.put("diameter", toolkit.diameter(_graph));
		Double coef = toolkit.averageClusteringCoefficient(_graph);
		values.put("avg_clustering_coefficient", toolkit.averageClusteringCoefficient(_graph));

		// cc "cc_count" "avg_cc_size" "max_cc_size" "max_cc_id" "cc_std_dev"
		values.put("cc_count",_cc.getConnectedComponentsCount());
		values.put("avg_cc_size", _ccDist.average());
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
			values.put("cc_std_dev", _ccDist.average());
		}
		values.put("com_count",_comDist.number());
		values.put("avg_com_size", _comDist.average());
		values.put("max_com_size", _comDist.maxCommunitySize());
		values.put("max_com_id",(_comDist.biggestCommunity()==null ? -1: (Community)_comDist.biggestCommunity()));
		values.put("min_com_size", _comDist.minCommunitySize());
		values.put("min_com_id",(_comDist.smallestCommunity()==null ? -1: (Community)_comDist.smallestCommunity()));
		values.put("std_com_dist",_comDist.stdev());
		int[] sizeDistribution = _comDist.sizeDistribution();
		if (sizeDistribution != null) {
			String distributionStr = "";
			for (int j = 0; j < sizeDistribution.length; ++j) {
				distributionStr += sizeDistribution[j] + _sep2;
			}
			values.put("size_distribution",distributionStr);
		}
		
		// modularity
		values.put("modularity",_modularity.getMeasure());
		values.put("com_modularity",avgModularity);
		values.put("iterations",_numberOfIterations);
		if (stepsModularity != null) {
			String modularities = "";
			for (int i = 0; i < stepsModularity.length; ++i) {
				modularities += stepsModularity[i]+ _sep2;
			}
			values.put("iteration_modularities", modularities);
		}

		values.put("weighted_com_modularity", weightedModularity);
		
		if (_mobCom != null) {
			values.put("speed_avg", _mobCom.averageValue());
			values.put("speed_std", _mobCom.stdev());
			values.put("speed_avg_std",_mobCom.averageStddev());
		}
	
		int headersCount = _outGraphHeaders.length;
		int i = 0;
		for (String column : _outGraphHeaders) {
			if (++i != headersCount) {
				_outGraph.write(values.get(column) + _sep);
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
	}

	
	public void setImagesGeneration(String styleSheetUrl, String prefix) throws IOException {
		_createImages = true;
		_styleSheetUrl = styleSheetUrl;
		_imgPrefix = prefix;
		
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
