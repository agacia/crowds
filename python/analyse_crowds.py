import os
import sys
import profile
from optparse import OptionParser
from crowdsAnalyser import CrowdsAnalyser

# Read options

parser = OptionParser("usage: %prog [options]")
parser.add_option('--inputFile', help=("Input file"), type="string", dest='inputFile')
parser.add_option('--outputFile', help=("Output file"), type="string", dest='outputFile')
parser.add_option('--inputDir', help=("input dir name"), type="string", dest='inputDir')
parser.add_option('--outputDir', help=("Output dir name"), type="string", dest='outputDir')
parser.add_option('--type', help=("What to analyse"), type="string", dest='type')
parser.add_option('--startStep', help=("Start step"), type="int", dest='startStep')
parser.add_option('--endStep', help=("Stop step"), type="int", dest='endStep')
parser.add_option('--groupIndex', help=("Column Index in tsv file of groupId"), type="int", dest='groupIndex')
parser.add_option('--groupType', help=("Group type"), type="string", dest='groupType')
(options, args) = parser.parse_args()

print "Running analyse_crowds {0}".format(options)

# check set options
# if not options.inputFile:
# 	print "Usage: analyse --inputFile <FILE> --type <communities|vehicles|Size>" 
# 	print "Exiting..."
# 	exit()

# -----------------------------------------------------------

def processLine(line, outFile, timeIndex, step):	
		# self.step	node_id	x	y	degree	neighbors	cc_id	cc_size	com_id	com_score	com_size
		# print line
		data = line.strip().split('\t')
		try: 
			time = float(data[timeIndex])
			if time % step == 0:
				outFile.write(line)
		except:
			# skip
			print "skip line ", line
		

def main():

	if options.type == "reduce":
		print ("read ", options.inputFile)
		print ("write ", options.outputFile)
		outFile = open(options.outputFile, 'w+')
		step = 10
		timeIndex = 0
		if "pandas" in options.inputFile:
			timeIndex = 1
		with open(options.inputFile, 'r') as f:
			header_line = next(f)
			outFile.write(header_line)
			for data_line in f:
				processLine(data_line, outFile, timeIndex, step)

	""" Analyses vehicles. Groups by vehicle and its community:
		"veh_com_pandas.tsv":
		com_size size - number of steps for given vehicle,community
		com_size mean - average size of the community for given vehicle,community
		"veh_com_changes_pandas.tsv":
		node_id	avg_steps_in_com steps avg_com_size num_changes
	"""
	if options.type == "vehicles":
		ca = CrowdsAnalyser()
		filepath = os.path.join(options.outputDir, "communities.csv")
		if not os.path.exists(filepath):
			filepath = os.path.join(options.outputDir, "crowds_communities.csv")
		ca.readFile(filepath)
		ca.analyseVehicles("node_id")
		ca.plot_vehicle_hist(options.outputDir)
		ca.writeVehiclesToFile(options.outputDir)

	if options.type == "modularity":
		ca = CrowdsAnalyser()
		filepath = os.path.join(options.outputDir, "graph.txt")
		if not os.path.exists(filepath):
			filepath = os.path.join(options.outputDir, "crowds_graph.csv")
		ca.readFile(filepath)
		ca.analyseModularity("step", "modularity", options.outputDir)
		ca.writeModularityToFile("modularity", options.outputDir)
		ca.analyseModularity("step", "weighted_com_modularity", options.outputDir)
		ca.writeModularityToFile("weighted_com_modularity", options.outputDir)


	if options.type == "plot_modularities":
		ca = CrowdsAnalyser()
		if options.outputDir:
			titlenames = [name for name in os.listdir(options.outputDir) if os.path.isdir(os.path.join(options.outputDir,name)) and "plots" not in name]
			ca.plot_modularities("weighted_com_modularity", "Weighted modularity", titlenames, options.outputDir, "crowds_graph.csv", options.startStep, options.endStep)
			# ca.plot_modularities("com_modularity", "Modularity", titlenames, options.outputDir, "crowds_graph.csv", options.startStep, options.endStep)
			ca.plot_modularities("avg_com_size", "Average community size", titlenames, options.outputDir, "crowds_graph.csv", options.startStep, options.endStep)
			ca.plot_modularities("com_count", "Number of communities", titlenames, options.outputDir, "crowds_graph.csv", options.startStep, options.endStep)
			ca.plot_modularities("max_com_size", "Maximum community size", titlenames, options.outputDir, "crowds_graph.csv", options.startStep, options.endStep)
		if options.inputFile:
			ca.plot_modularity("weighted_com_modularity", "weighted_com_modularity",  options.inputFile)
	
	if options.type == "stability":
		ca = CrowdsAnalyser()
		titlenames = [name for name in os.listdir(options.outputDir) if os.path.isdir(os.path.join(options.outputDir,name)) and "plots" not in name]
		ca.write_stability(options.outputDir, "crowds_communities.csv", titlenames, options.startStep, options.endStep)

	if options.type == "communities_life":
		ca = CrowdsAnalyser()
		titlenames = [name for name in os.listdir(options.outputDir) if os.path.isdir(os.path.join(options.outputDir,name)) and "plots" not in name]
		ca.analyse_communities_lifetime(options.outputDir , "com_id", titlenames, options.startStep, options.endStep)

	if options.type == "link_life":
		ca = CrowdsAnalyser()
		titlenames = [name for name in os.listdir(options.outputDir) if os.path.isdir(os.path.join(options.outputDir,name)) and "plots" not in name]
		ca.analyse_link_lifetime(options.outputDir , options.inputFile, options.startStep, options.endStep)


	if options.type == "graph":
		ca = CrowdsAnalyser()
		if options.inputFile:
			ca.analyse_graph(options.inputFile, options.outputDir)
		elif options.inputDir:
			ca.analyse_graphs(options.inputDir)

	# if options.type == "links":
	# 	ca = CrowdsAnalyser()
	# 	filepath = os.path.join(options.outputDir, "communities.csv")
	# 	if not os.path.exists(filepath):
	# 		filepath = os.path.join(options.outputDir, "crowds_communities.csv")
	# 	ca.readFile(filepath)
	# 	ca.analyseLinks("link_id",options.outputDir)

	if options.type == "communities_pandas":
		ca = CrowdsAnalyser()
		print "ca.readFile..."
		filepath = os.path.join(options.outputDir, "communities.csv")
		if not os.path.exists(filepath):
			filepath = os.path.join(options.outputDir, "crowds_communities.csv")
		ca.readFile(filepath)
		print "ca.analyseSteps()..."
		ca.analyseCommunities("step","com_id")
		ca.writeCommunitiesToFile(os.path.join(options.outputDir, "communities_pandas.csv"))

	# if options.type == "all":
	# 	ca = CrowdsAnalyser()
	# 	filepath = os.path.join(options.outputDir, "communities.csv")
	# 	if not os.path.exists(filepath):
	# 		filepath = os.path.join(options.outputDir, "crowds_communities.csv")
	# 	ca.readFile(filepath)
	# 	ca.analyseSteps()
	# 	ca.writeStepsToFile(os.path.join(options.outputDir, "communities_steps.tsv"))

	# if options.type == "compare_algorithms":
	# 	ca = CrowdsAnalyser()
	# 	algorithms=["timemean_last90_11_0", "timemean_last90_20_1"]
	# 	ca.compareAlgorithms(options.inputDir, algorithms, "comparison_steps.tsv")
	# 	ca.compareAlgorithmsNumberOfChanges(options.inputDir, algorithms, "comparison_changes.tsv")

	# if options.type == "compare_runs":
	# 	ca = CrowdsAnalyser()
	# 	# avg speed
	# 	ca.readFiles(options.inputDir, "communities_steps.tsv", "MobileSandSharc")
	# 	ca.writeStepsComparisonToFile(os.path.join(options.inputDir, "comparison_steps.tsv"))
	# 	ca.plotComparison(options.inputDir)
		
	# 	# number of changes
	# 	ca.readFiles(options.inputDir, "num_com_changes_pandas.tsv", "MobileSandSharc")
	# 	ca.writeComChangesComparisonToFile(options.inputDir)

	# if options.type == "communities":
	# 	outputFile = os.path.join(options.outputDir, "analysis_"+options.groupType+".tsv")
	# 	outSumFile = os.path.join(options.outputDir, "analysis_"+options.groupType+"_sum.txt")
	# 	outCommunities = open(outputFile, 'w')
	# 	outSum = open(outSumFile, 'w')
	# 	ca = CommunityAnalyser()
	# 	ca.readFile(options.inputFile)
	# 	ca.summariseAnalysis(outCommunities, outSum)
	

if __name__ == '__main__':
	main()
	# profile.run('main()')
 