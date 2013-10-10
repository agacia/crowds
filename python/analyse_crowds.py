import os
import sys
from optparse import OptionParser
import fileinput
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from scipy import spatial
from numpy import * 
import numpy
from pylab import *
from matplotlib.mlab import csv2rec

parser = OptionParser("usage: %prog [options]")
parser.add_option('--inputFile', help=("Input file"), type="string", dest='inputFile')
parser.add_option('--outputDir', help=("Output dir name"), type="string", dest='outputDir')
parser.add_option('--type', help=("What to analyse"), type="string", dest='type')

parser.add_option('--groupIndex', help=("Column Index in tsv file of groupId"), type="int", dest='groupIndex')
parser.add_option('--groupType', help=("Group type"), type="string", dest='groupType')
(options, args) = parser.parse_args()

print "analyse {0}".format(options)

# check set options
if not options.inputFile:
	print "Usage: analyse --inputFile <FILE> --type <communities|vehicles|Size>" 
	print "Exiting..."
	exit()

# --------------------------------- VEHICLES

def processLineVehicles(line, args={}):	
	global step
	global globalStep
	global vehicles
	data = line.split(separator)
	now = int(data[0])
	# if now > 10:
	# 	return 
	# print line
	if (now != step):
		if step == -1:
			globalStep += now
		else:
			globalStep += 1
		step = now
					
	# add data
	vehicleId = str(data[1])	
	x = float(data[2])
	y = float(data[3])
	degree = float(data[4])
	neighbors = str(data[5]).split(separator2)
	communityId = -1
	connectedComponentId = -1
	if (degree > 0):
		connectedComponentId = int(data[6])
		connectedComponentSize = int(data[7])
		communityId = int(data[8])
		communityScore = float(data[9])
		communitySize = float(data[10])	
	
	# add vehicle to global vehicles
	if (vehicleId not in vehicles.keys()): 
		vehicles[vehicleId] = createVehicle(vehicleId, step, x, y, degree, connectedComponentId, communityId)
	else:
		addToVehicles(vehicles[vehicleId], step, x, y, degree, connectedComponentId, communityId)

	return 0

def addToVehicles(vehicle, step, x, y, degree, connectedComponentId, communityId):
	vehicle["pos"].append([x,y])
	vehicle["steps"].append(step)
	vehicle["degrees"].append(degree)
	vehicle["connectedComponents"].append(connectedComponentId)
	vehicle["communities"].append(communityId)
	vehicle["x"].append(x)
	vehicle["y"].append(y)

def createVehicle(id, step, x, y, degree, connectedComponentId, communityId):
	vehicle = {}
	vehicle["id"] = id
	vehicle["pos"] = [[x,y]]
	vehicle["steps"] = [step]
	vehicle["degrees"] = [degree]
	vehicle["connectedComponents"] = [connectedComponentId]
	vehicle["communities"] = [communityId]
	vehicle["x"] = [x]
	vehicle["y"] = [y]
	return vehicle

def summariseAnalysisVehicles():
	global outVehicles
	global outSum
	sumTravelSteps = 0
	sumConnectedSteps = 0
	sumCommunitySteps = 0
	movingVehicles = 0
	sumTripsInCommunity = 0
	sumTripsInConnected = 0
	sumChangesConnected = 0
	sumChangesCommunity = 0
	sumDegree = 0
	sumNumOfFisconnections = 0
	xmin = 1000000
	ymin = 1000000
	xmax = 0
	ymax = 0
	ydisc = []
	xpos  = []
	xinCommunity = []
	xcon = []
	xdisc = []
	ypos = []
	yinCommunity = []
	ycon = []
	xchanges = []
	ychanges = []
	sumStability = 0
	sumStabilityCom = 0
	outVehicles.write("vehicleId\tstart_step\tsteps_count\tdegree\tsteps_connected\tpercent_connected\tchanges_connected\tsteps_in_each_cc\tsteps_in_community\tpercent_in_community\tchanges_community\tsteps_in_each_community\tnumber_of_disconnection\ttrips_in_community\tstability\tstabilityCom\n")
	for vehicleId,vehicle in vehicles.iteritems():
		stepsCount = len(vehicle["steps"])
		if (stepsCount > 0):
			#initialise first step 
			movingVehicles += 1
			sumTravelSteps += stepsCount
			startStep = vehicle["steps"][0]
			lastCommunityId = vehicle["communities"][0]
			lastCCId = vehicle["connectedComponents"][0]
			changesCommunity = 0
			changesConnected = 0
			stepsConnected = 0
			stepsInCommunity = 0
			tripsInConnected = 0
			tripsInCommunity = 0
			numberofDisconnections = 0

			degrees = 0
			if lastCCId != -1:
				tripsInConnected += 1
				sumTripsInConnected += 1
			else:
				numberofDisconnections +=1
				sumNumOfFisconnections += 1
			if lastCommunityId != -1:
				tripsInCommunity += 1
				sumTripsInCommunity += 1
			# analyse next steps
			for i in range(0,stepsCount):
				step = vehicle["steps"][i]
				degrees += vehicle["degrees"][i]
				communityId = vehicle["communities"][i]
				x = vehicle["x"][i]
				y = vehicle["y"][i]
				xpos.append(x)
				ypos.append(y)
				if x < xmin:
					xmin = x
				if x > xmax:
					xmax = x
				if y < ymin:
					ymin = y
				if y > ymax:
					ymax = y
				if communityId != lastCommunityId:
					changesCommunity += 1
					sumChangesCommunity += 1
					xchanges.append(x)
					ychanges.append(y)
					if communityId != -1:
						tripsInCommunity += 1
						sumTripsInCommunity += 1
					lastCommunityId = communityId
				if communityId != -1:
					stepsInCommunity += 1
					sumCommunitySteps += 1	
					xinCommunity.append(x)
					yinCommunity.append(y)
				ccId = vehicle["connectedComponents"][i]
				if ccId != lastCCId:
					changesConnected += 1
					sumChangesConnected += 1
					lastCCId = ccId
					if ccId != -1:
						tripsInConnected += 1
						sumTripsInConnected += 1
					else:
						numberofDisconnections +=1
						sumNumOfFisconnections += 1
						xdisc.append(x)
						ydisc.append(y)
				if ccId != -1:
					stepsConnected += 1
					sumConnectedSteps += 1 	
					xcon.append(x)
					ycon.append(y)
			
			percentConnected = float(stepsConnected) / float(stepsCount)
			percentInCommunity = float(stepsInCommunity) / float(stepsCount)
			degree = float(degrees) / float(stepsCount)
			sumDegree += degree
			stepsInEachCC = 0
			if tripsInConnected > 0:
				stepsInEachCC = float(stepsConnected) / float(tripsInConnected)
			stepsInEachCommunity = 0
			if tripsInCommunity > 0:
				stepsInEachCommunity = float(stepsInCommunity) / float(tripsInCommunity)
			stability = float(stepsInEachCommunity) / float(stepsCount)
			
			sumStability += stability
			stabilityCom = 0
			if stepsInCommunity > 0:
				stabilityCom = float(stepsInEachCommunity) / float(stepsInCommunity)
			sumStabilityCom += stabilityCom
			# print "stepsCount {0}\t stepsInCommunity {1}\t tripsInCommunity {2}\t stepsInEachCommunity {3} \t stabilityCom {4} ".format( stepsCount, stepsInCommunity, tripsInCommunity, stepsInEachCommunity, stabilityCom)
			outVehicles.write("{0}\t{1}\t{2}\t{3}\t{4}\t{5}\t{6}\t{7}\t{8}\t{9}\t{10}\t{11}\t{12}\t{13}\t{14}\t{15}\n".format(
				vehicleId,startStep,stepsCount,degree,
				stepsConnected,percentConnected,changesConnected,stepsInEachCC,
				stepsInCommunity,percentInCommunity,changesCommunity,stepsInEachCommunity, numberofDisconnections,tripsInCommunity,
				stability, stabilityCom))
	
	# avegare statistics
	avgTraveledSteps = 0 # how many seconds a vehicle travels
	avgConnectedSteps = 0 # how many seconds a vehicle travels in a connected component
	avgCommunitySteps = 0 # how many seconds a vehicle travels in a community
	avgChangesConnected = 0 # how many times a vehicle changes connected component
	avgChangesCommunity = 0 # how many times a vehicle changes community
	avgStepsInEachCommunity = 0 # how many seconds a trip in community lasts
	avgStepsInEachCC = 0 # how many seconds a trip in connected components lasts
	avgNumberOfDisconnections = 0 # how many times a vehicle looses connection 
	avgDegree = 0
	avgsumStability = 0
	avgsumStabilityCom = 0
	if movingVehicles > 0:
		avgTraveledSteps = float(sumTravelSteps) / float(movingVehicles)
		avgConnectedSteps = float(sumConnectedSteps) / float(movingVehicles)
		avgCommunitySteps = float(sumCommunitySteps) / float(movingVehicles)
		avgChangesConnected = float(sumChangesConnected) / float(movingVehicles)
		avgChangesCommunity = float(sumChangesCommunity) / float(movingVehicles)
		avgStepsInEachCommunity = float(sumCommunitySteps) / float(sumTripsInCommunity)
		avgStepsInEachCC = float(sumConnectedSteps) / float(sumTripsInConnected)
		avgDegree = float(sumDegree) / float(movingVehicles)
		avgNumberOfDisconnections = float(sumNumOfFisconnections) / float(movingVehicles)
		avgStability2 = float(sumStability) / float(movingVehicles)
		avgStabilityCom2 = float(sumStabilityCom) / float(movingVehicles)
		
	avgPercentConnectedTime = 0
	avgPercentInCommunityTime = 0
	avgStability = 0 
	if sumTravelSteps > 0:
		avgPercentConnectedTime = float(sumConnectedSteps) / float(sumTravelSteps)
		avgPercentInCommunityTime = float(sumCommunitySteps) / float(sumTravelSteps)
		avgStability = float(avgStepsInEachCommunity) / float(sumTravelSteps)
		avgStabilityCom = float(avgStepsInEachCommunity) / float(sumCommunitySteps)
	outSum.write("vehicles:\t{0}, movingVehicles: {1}\n".format(len(vehicles), movingVehicles))
	outSum.write("avg degree:\t{0}\n".format(avgDegree))
	outSum.write("avg traveled steps:\t{0}\n".format(avgTraveledSteps))
	outSum.write("avg connected steps:\t{0}\n".format(avgConnectedSteps))
	outSum.write("avg community steps:\t{0}\n".format(avgCommunitySteps))
	outSum.write("avg percent time connected:\t{0}\n".format(avgPercentConnectedTime))
	outSum.write("avg percent time in community:\t{0}\n".format(avgPercentInCommunityTime))
	outSum.write("avg changes connected:\t{0}\n".format(avgChangesConnected))
	outSum.write("avg changes community:\t{0}\n".format(avgChangesCommunity))
	outSum.write("avg steps in each connected:\t{0}\n".format(avgStepsInEachCC))
	outSum.write("avg steps in each community:\t{0}\n".format(avgStepsInEachCommunity))
	outSum.write("avg number of disconnections:\t{0}\n".format(avgNumberOfDisconnections))
	outSum.write("avg Stability:\t{0}\n".format(avgStability))
	outSum.write("avg StabilityCom:\t{0}\n".format(avgStabilityCom))
	outSum.write("avg Stability:\t{0}\n".format(avgStability2))
	outSum.write("avg Stability:\t{0}\n".format(avgStabilityCom2))

	# the x distribution will be centered at -1, the y distro
	# at +1 with twice the width.
	xx = numpy.random.randn(3000)-1
	yy = numpy.random.randn(3000)*2+1
	# print xmin
	# print xmax
	# print ymin
	# print ymax
	
	numbins=100
	hist,xedges,yedges = np.histogram2d(xpos,ypos,bins=numbins,range=[[xmin,xmax],[ymin,ymax]])
	extent = [xedges[0], xedges[-1], yedges[0], yedges[-1] ]
	imshow(hist.T,extent=extent,interpolation='nearest',origin='lower')
	colorbar()
	# show()
	outputfile = os.path.join(options.outputDir, "map_density.png")
	plt.savefig(outputfile)

	hist,xedges,yedges = np.histogram2d(xdisc,ydisc,bins=numbins,range=[[xmin,xmax],[ymin,ymax]])
	extent = [xedges[0], xedges[-1], yedges[0], yedges[-1] ]
	plt.imshow(hist.T,extent=extent,interpolation='nearest',origin='lower')
	# colorbar()
	outputfile = os.path.join(options.outputDir, "map_disconnections.png")
	plt.savefig(outputfile)

	hist,xedges,yedges = np.histogram2d(xinCommunity,yinCommunity,bins=numbins,range=[[xmin,xmax],[ymin,ymax]])
	extent = [xedges[0], xedges[-1], yedges[0], yedges[-1] ]
	plt.imshow(hist.T,extent=extent,interpolation='nearest',origin='lower')
	# colorbar()
	outputfile = os.path.join(options.outputDir, "map_inCommunity.png")
	plt.savefig(outputfile)

	hist,xedges,yedges = np.histogram2d(xcon,ycon,bins=numbins,range=[[xmin,xmax],[ymin,ymax]])
	extent = [xedges[0], xedges[-1], yedges[0], yedges[-1] ]
	plt.imshow(hist.T,extent=extent,interpolation='nearest',origin='lower')
	# colorbar()
	outputfile = os.path.join(options.outputDir, "map_connected.png")
	plt.savefig(outputfile)

	hist,xedges,yedges = np.histogram2d(xchanges,ychanges,bins=numbins,range=[[xmin,xmax],[ymin,ymax]])
	extent = [xedges[0], xedges[-1], yedges[0], yedges[-1] ]
	plt.imshow(hist.T,extent=extent,interpolation='nearest',origin='lower')
	# colorbar()
	outputfile = os.path.join(options.outputDir, "map_changes.png")
	plt.savefig(outputfile)
	return 0

#----------------------------------

def processLineCommunities(line, args={}):	
	global step
	global globalStep
	global communities
	global vehiclesCount
	global currentVehiclesCount
	global outGroups

	data = line.split(separator)
	now = int(data[0])

	if (now != step):
		if step == -1:
			globalStep += now
		else:
			globalStep += 1
		step = now
		# print "globalstep {2}, step {1}, step vehicles: {4}, vehiclesCount: {3}, communities: {0}".format(len(communities), step-1, globalStep-1, vehiclesCount, currentVehiclesCount)
		currentVehiclesCount = 0

	# add data
	vehiclesCount += 1
	currentVehiclesCount += 1

	#step	node_id	x	y	degree	neighbors	cc_id	cc_size	com_id	com_score	com_size
	vehicleId = str(data[1])	
	x = float(data[2])
	y = float(data[3])
	degree = float(data[4])
	neighbors = str(data[5]).split(separator2)
	communityId = int(data[options.groupIndex])
	# add vehicle to a community
	communityScore = 1
	if options.groupType == "community":
		communityScore = float(data[7])
	if (communityId not in communities.keys()): 
		# print "step {2}, vehicle {0}, x,y: {3}{4}, degree: {5}, neighborsL {6}, community {1}, score: {7}".format(vehicleId, communityId, step,x,y,degree, neighbors, communityScore, len(data))
		communitySteps = {}
		communitySteps[step] = 1
		communities[communityId] = communitySteps
	elif step not in communities[communityId].keys():
			communities[communityId][step] = 1
	else:
		communities[communityId][step] += 1
	communityId = int(data[options.groupIndex])
	return 0

def summariseAnalysisCommunities(minCommunitySize, minConnectedSize):
	global outCommunities
	global outSum
	totalLifetime = 0
	totalSumSize = 0
	maxLifetime = 0
	maxLifetimeId = -1
	breakLength = 0
	nonZeroCommunities = 0
	maxSize = 0
	maxSizeId = -1
	maxSizeStep = -1

	outCommunities.write("id\tstart_step\tlifetime\tavg_size\tbreak_length\tsteps\tsizes\n")
	for id,communitySteps in communities.iteritems():
		lifetime = len(communitySteps)
		if lifetime > 0:
			totalLifetime += lifetime
			nonZeroCommunities += 1
			sizes = []
			steps = []
			sumSize = 0
			i = 0
			for step,vehiclesInStep in communitySteps.iteritems():
				steps.append(step)
				sizes.append(vehiclesInStep)
				# get max
				if vehiclesInStep > maxSize:
					maxSize = vehiclesInStep
					maxSizeId = id
					maxSizeStep = step
				# sum size
				sumSize += vehiclesInStep
				# chck if a break
				i += 1
				if len(steps) > 1:
					if step - steps[i-1] > 1:
						breakLength += 1		
			
			avgSize = 0
			avgSize = sumSize / lifetime
			totalSumSize += avgSize
			outCommunities.write("{0}\t{1}\t{2}\t{3}\t{4}\t{5}\t{6}\n".format(id, steps[0], lifetime, avgSize, breakLength, steps, sizes))

			if lifetime > maxLifetime:
				maxLifetime = lifetime
				maxLifetimeId = id
		
	# summarize 
	avgSize = 0
	avgLifetime = 0
	if nonZeroCommunities > 0:
		avgCommunitySize = float(totalSumSize) / float(nonZeroCommunities)
		avgLifetime = float(totalLifetime) / float(nonZeroCommunities)
	outSum.write( "group_count\tavg_size\tmax_size\tmax_size_id\tmax_size_step\tmax_size_lifetime\tavg_lifetime\tmax_lifetime\tmax_lifetime_id\n")
	outSum.write("{0}\t{1}\t{2}\t{3}\t{4}\t{4}\t{5}\t{6}\t{7}\t{8}\n".format(len(communities), avgCommunitySize, maxSize, maxSizeId, maxSizeStep, len(communities[maxSizeId]), avgLifetime, maxLifetime, maxLifetimeId))
	return 0

# def getFileNames(dirName, dirNames, filename):
# 	files = []
# 	for dirname in dirNames:
# 		dirPath = dirName + dirname
# 		files.append(os.path.join(dirPath, filename))
# 	return files

# def getCommunityFile(dirName):
# 	files = []
# 	for dirname, dirnames, filenames in os.walk(options.inputDir):
# 		for filename in filenames:
# 			if filename == options.inputFileName:
# 				files.append(os.path.join(dirname, filename))
# 	return files

# def findColors():
# 	text = '#A8BB19 '
# 	regex = '#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})'
# 	m = re.findall(regex, text)
# 	print m

# findColors()

#---------------------------------- SIZE

def processLineSize(line, args={}):	
	global step
	global globalStep
	global currentVehiclesCount
	global currentConnected
	global currentInCommunities
	global currentCommunities
	global currentCommunitiesIds
	global currentConnectedComponents
	global singletons
	global outSteps
	global maxDegree
	global sumDegree

	data = line.split(separator)
	now = float(data[0])

	if (now != step):
		if step == -1:
			globalStep += now
		else:
			globalStep += 1
		step = now
		if (currentVehiclesCount > 0.0):
			# summarise step	
			avgConnectedComponentSize = getGroupStats(currentConnectedComponents, "vehicles")
			avgCommunitySize = getGroupStats(currentCommunities, "vehicles")
			avgDegree = float(sumDegree) / float(currentVehiclesCount)
			outSteps.write("{0}\t{1}\t{2}\t{3}\t{4}\t{5}\t{6}\t{7}\t{8}\t{9}\t{10}\t{11}\t{12}\t{13}\t{14}\n".format(
				globalStep-1, currentVehiclesCount, singletons, currentConnected, currentInCommunities,
				len(currentConnectedComponents),
				avgConnectedComponentSize["maxSize"], avgConnectedComponentSize["avgSize"], avgConnectedComponentSize["maxSizeId"], 
				len(currentCommunities),
				avgCommunitySize["maxSize"], avgCommunitySize["avgSize"], avgCommunitySize["maxSizeId"],
				avgDegree, maxDegree))
		# reset currentStep
		currentVehiclesCount = 0
		currentConnected = 0
		currentInCommunities = 0
		singletons = 0
		maxDegree = 0
		sumDegree = 0
		currentCommunities = {}
		currentConnectedComponents = {}
		
	# add data
	vehicleId = str(data[1])	
	x = float(data[2])
	y = float(data[3])
	degree = float(data[4])
	neighbors = str(data[5]).split(separator2)
	currentVehiclesCount += 1
	sumDegree += degree
	if (degree > 0):
		if degree > maxDegree:
			maxDegree = degree
		# add vehicle to a community
		communityId = str(data[6])
		communityScore = float(data[7])
		currentInCommunities += 1
		# print "step {2}, vehicle {0}, x,y: {3}{4}, degree: {5}, neighborsL {6}, community {1}, score: {7}".format(vehicleId, communityId, step,x,y,degree, neighbors, communityScore, len(data))
		if (communityId not in currentCommunities.keys()):
			currentCommunities[communityId] = createGroup(communityId, x, y, vehicleId)
		else:
			addToGroup(currentCommunities[communityId], x, y, vehicleId)
		
		# add vehicle to a connected component
		connectedComponentId = int(data[8])
		currentConnected += 1
		if (connectedComponentId not in currentConnectedComponents.keys()): 	
			currentConnectedComponents[connectedComponentId] = createGroup(connectedComponentId, x, y, vehicleId)
		else:
			addToGroup(currentConnectedComponents[connectedComponentId], x, y, vehicleId)
	else:
		singletons += 1
	return 0

def getGroupStats(dict, itenKey):
	sumSize = 0
	maxSize = 0
	maxSizeId = 0
	avgSize = 0

	for key,value in dict.iteritems():
		size = 0
		if itenKey != None:
			size = len(value[itenKey])
		else:
			size = len(value)
		sumSize += size
		if size > maxSize:
			maxSize = size
			maxSizeId = key
	
	count = len(dict)
	if count > 0:
		avgSize = float(float(sumSize) / float(count))
	
	return {"avgSize":avgSize,"maxSize":maxSize,"maxSizeId":maxSizeId}

def addToGroup(group, x, y, vehicleId):
	group["vehicles"].append(vehicleId)
	group["points"].append([x,y])

def createGroup(id, x, y, vehicleId):
	group = {}
	group["id"] = id
	group["points"] = []
	group["vehicles"] = []
	group["points"].append([x,y])
	group["vehicles"].append(vehicleId)
	return group


#-----------------------------------------------------------

separator = '\t'
separator2 = ','
step = -1
globalStep = 0	
currentVehiclesCount = 0

if options.type == "vehicles":
	vehicles = {}
	outputFile = os.path.join(options.outputDir,  "analysis_vehicles.tsv")
	outSumFile = os.path.join(options.outputDir, "analysis_vehicles_sum.txt")
	outVehicles = open(outputFile, 'w')
	outSum = open(outSumFile, 'w')
	i = 0
	for line in fileinput.input(options.inputFile):
		if i > 0:
			processLineVehicles(line, args)
		i += 1
	summariseAnalysisVehicles()

if options.type == "communities":
	communities = {}
	vehiclesCount = 0
	outputFile = os.path.join(options.outputDir, "analysis_"+options.groupType+".tsv")
	outSumFile = os.path.join(options.outputDir, "analysis_"+options.groupType+"_sum.txt")
	outputGroupFile = os.path.join(options.outputDir, "analysis_"+options.groupType+"_groups.tsv")
	outGroups = open(outputGroupFile, 'w')
	outCommunities = open(outputFile, 'w')
	outSum = open(outSumFile, 'w')
	outGroups.write("id\tx\ty\tgroup\n")
	i = 0
	step = -1
	for line in fileinput.input(options.inputFile):
		if i > 0:
			processLineCommunities(line, args)
		i += 1
	minCommunitySize = 2
	minConnectedSize = 2
	summariseAnalysisCommunities(minCommunitySize,minConnectedSize)

if options.type == "size":
	outputFile = os.path.join(options.outputDir,  "analysis_size.tsv")
	outSteps = open(outputFile, 'w')
	currentInCommunities = 0
	currentConnected = 0
	sumDegree = 0
	currentCommunities = {}
	singletons = 0
	maxDegree = 0
	currentConnectedComponents = {}
	outSteps.write("step\tvehicles\tsingletons\tconnected\tin_communities\tconnected_components\tmax_connected_component_size\tavg_connected_component_size\tmax_connected_component_id\tcommunities\tmax_community_size\tavg_community_size\tmax_community_id\tavg_degree\tmax_degree\n")
	i = 0
	step = -1
	for line in fileinput.input(options.inputFile):
		if i > 0:
			processLineSize(line, args)
		i += 1

