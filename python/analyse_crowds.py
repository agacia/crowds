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
		print ("read ", options.outputFile)
		outFile = open(options.outputFile, 'w')
		step = 10
		timeIndex = 0
		if "pandas" in options.inputFile:
			timeIndex = 1
		with open(options.inputFile, 'r') as f:
			header_line = next(f)
			outFile.write(header_line)
			for data_line in f:
				processLine(data_line, outFile, timeIndex, step)

	if options.type == "vehicles":
		ca = CrowdsAnalyser()
		ca.readFile(os.path.join(options.outputDir, "communities.csv"))
		ca.analyseVehicles("node_id",options.outputDir)
		ca.writeVehiclesToFile(options.outputDir)

	if options.type == "links":
		ca = CrowdsAnalyser()
		ca.readFile(os.path.join(options.outputDir, "communities.csv"))
		ca.analyseLinks("link_id",options.outputDir)
		# ca.writeLinksToFile(options.outputDir)

	if options.type == "communities_pandas":
		ca = CrowdsAnalyser()
		print "ca.readFile..."
		ca.readFile(os.path.join(options.outputDir, "communities.csv"))
		print "ca.analyseSteps()..."
		ca.analyseCommunities("step","com_id")
		ca.writeCommunitiesToFile(os.path.join(options.outputDir, "communities_pandas.tsv"))
		# ca.plotCommunitySize(options.outputDir)

	if options.type == "all":
		ca = CrowdsAnalyser()
		ca.readFile(os.path.join(options.outputDir, "communities.csv"))
		# ca.analyseCommunities("step","com_id")
		# ca.writeCommunitiesToFile(os.path.join(options.outputDir, "communities_pandas.tsv"))
		# ca.plotCommunitySize(options.outputDir)
		ca.analyseSteps()
		ca.writeStepsToFile(os.path.join(options.outputDir, "communities_steps.tsv"))
		# ca.analyseVehicles("node_id")
		# ca.writeVehiclesToFile(os.path.join(options.outputDir, "vehicles_pandas.tsv"))
		# ca.analyseNumberOfCommunityChanges("node_id")
		# ca.writeCommunityChangesToFile(os.path.join(options.outputDir, "community_changes.tsv"))

	if options.type == "compare_algorithms":
		ca = CrowdsAnalyser()
		# ca.compareAlgorithms(options.inputDir, 
		# 	["mobWeighted_0.0","mobWeighted_0.1","mobWeighted_0.2","mobWeighted_0.3",
		# 	"mobWeighted_0.4","mobWeighted_0.5","mobWeighted_0.6","mobWeighted_0.7",
		# 	"mobWeighted_0.8","mobWeighted_0.9","mobWeighted_1.0"], 
		# 	"comparison_steps.tsv")
		#["nsim_0", "nsim_sr_1", "nsim_dsd_2", "nsim-epidemic_3", "nsim_sr-epidemic_4", "nsim_dsd-epidemic_5"],
			# ["nsim_100_0", "nsim_200_1","nsim_300_2",
			# "nsim_sr_100_3", "nsim_sr_200_4","nsim_sr_300_5",
			# "nsim_dsd_100_6", "nsim_dsd_200_7","nsim_dsd_300_8"],
		# algorithms=["nsim_0", "dsd_1", "dsd_only_2", "mobile_sharc_dsd_only_3"]
		# algorithms=["nsim_0", "nsim_dsd_1", "only_dsd_2"]
		# algorithms=["congsim_20_0", "congsim_avgspeed_20_1", "congsim_nsim_20_2" ,"congsim_nsim_avgspeed_20_3"]
		# algorithms=["newsawsharc_weighted_0", "sawsharc_weighted_1"]
		# algorithms=["newsawsharc_20_0", "newsawsharc_11_1"]
		# algorithms=["cong_thres_11_0", "cong_thres_20_1"]
		# algorithms=["cong_thres_20_200_0", "cong_thres_20_300_1", "cong_thres_11_200_2", "cong_thres_11_300_3"]
		algorithms=["timemean_last90_11_0", "timemean_last90_20_1"]
		ca.compareAlgorithms(options.inputDir, algorithms, "comparison_steps.tsv")
		ca.compareAlgorithmsNumberOfChanges(options.inputDir, algorithms, "comparison_changes.tsv")

	if options.type == "compare_runs":
		ca = CrowdsAnalyser()
		# avg speed
		ca.readFiles(options.inputDir, "communities_steps.tsv", "MobileSandSharc")
		ca.writeStepsComparisonToFile(os.path.join(options.inputDir, "comparison_steps.tsv"))
		ca.plotComparison(options.inputDir)
		
		# number of changes
		ca.readFiles(options.inputDir, "num_com_changes_pandas.tsv", "MobileSandSharc")
		ca.writeComChangesComparisonToFile(options.inputDir)

	if options.type == "communities":
		outputFile = os.path.join(options.outputDir, "analysis_"+options.groupType+".tsv")
		outSumFile = os.path.join(options.outputDir, "analysis_"+options.groupType+"_sum.txt")
		outCommunities = open(outputFile, 'w')
		outSum = open(outSumFile, 'w')
		ca = CommunityAnalyser()
		ca.readFile(options.inputFile)
		ca.summariseAnalysis(outCommunities, outSum)
	

if __name__ == '__main__':
	main()
	# profile.run('main()')
 