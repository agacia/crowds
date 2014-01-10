import os
import sys
import profile
from optparse import OptionParser
from crowdsAnalyser import CrowdsAnalyser

# Read options

parser = OptionParser("usage: %prog [options]")
parser.add_option('--inputFile', help=("Input file"), type="string", dest='inputFile')
parser.add_option('--outputDir', help=("Output dir name"), type="string", dest='outputDir')
parser.add_option('--type', help=("What to analyse"), type="string", dest='type')
parser.add_option('--groupIndex', help=("Column Index in tsv file of groupId"), type="int", dest='groupIndex')
parser.add_option('--groupType', help=("Group type"), type="string", dest='groupType')
(options, args) = parser.parse_args()

print "Running analyse_crowds {0}".format(options)

# check set options
if not options.inputFile:
	print "Usage: analyse --inputFile <FILE> --type <communities|vehicles|Size>" 
	print "Exiting..."
	exit()

# -----------------------------------------------------------

def main():

	if options.type == "all":
		ca = CrowdsAnalyser()
		ca.readFile(options.inputFile)
		# ca.analyseCommunities("step","com_id")
		# ca.writeCommunitiesToFile(os.path.join(options.outputDir, "communities_pandas.tsv"))
		# ca.plotCommunitySize(options.outputDir)
		ca.analyseVehicles("step","node_id")
		ca.writeVehiclesToFile(os.path.join(options.outputDir, "vehicles_pandas.tsv"))
		ca.analyseNumberOfCommunityChanges("node_id")
		ca.writeCommunityChangesToFile(os.path.join(options.outputDir, "community_changes.tsv"))

	if options.type == "vehicles":
		outputFile = os.path.join(options.outputDir,  "analysis_vehicles.tsv")
		outSumFile = os.path.join(options.outputDir, "analysis_vehicles_sum.txt")
		outVehicles = open(outputFile, 'w')
		outSum = open(outSumFile, 'w')
		va = VehicleAnalyser()
		va.readFile(options.inputFile)
		va.summariseAnalysis(outSum, outVehicles)

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
 