import matplotlib
import numpy as np
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from matplotlib.mlab import csv2rec
from scipy import spatial
from pylab import *


class VehicleAnalyser:

	def __init__(self):
		self.step = -1
		self.currentvehiclesCount = 0
		self.args = {"separator":'\t', "separator2":','}
		self.vehicles = {}
	
	def readFile(self, filename):
		self.filename = filename	
		with open(filename, 'r') as f:
			header_line = next(f)
			for data_line in f:
				self.processLine(data_line)
	

	def processLine(self, line):	
		# self.step	node_id	x	y	degree	neighbors	cc_id	cc_size	com_id	com_score	com_size
		data = line.strip().split(self.args['separator'])
		if len(data) != 12:
			print "Warning! Skipping wrong line (len(data)={1}): {0}".format(line, len(data))
			return

		now = int(data[0])
		if (now != self.step):
			self.step = now
			print "self.step {0}: {1}".format(self.step, line)
						
		# add data
		vehicleId = str(data[1])	
		x = float(data[2])
		y = float(data[3])
		degree = float(data[4])
		neighbors = str(data[5]).split(self.args['separator2'])
		connectedComponentId = int(data[6])
		connectedComponentSize = int(data[7])
		communityId = int(data[8])
		communityScore = float(data[9])
		communitySize = float(data[10])	
		
		# add vehicle to all self.vehicles
		if (vehicleId not in self.vehicles.keys()): 
			self.vehicles[vehicleId] = self.createVehicle(vehicleId, x, y, degree, connectedComponentId, communityId)
		else:
			self.addToVehicles(vehicleId, x, y, degree, connectedComponentId, communityId)
		return self.step

	def addToVehicles(self, id, x, y, degree, connectedComponentId, communityId):
		vehicle = self.vehicles[id]
		vehicle["pos"].append([x,y])
		vehicle["steps"].append(self.step)
		vehicle["degrees"].append(degree)
		vehicle["connectedComponents"].append(connectedComponentId)
		vehicle["communities"].append(communityId)
		vehicle["x"].append(x)
		vehicle["y"].append(y)

	def createVehicle(self, id,  x, y, degree, connectedComponentId, communityId):
		vehicle = {}
		vehicle["id"] = id
		vehicle["pos"] = [[x,y]]
		vehicle["steps"] = [self.step]
		vehicle["degrees"] = [degree]
		vehicle["connectedComponents"] = [connectedComponentId]
		vehicle["communities"] = [communityId]
		vehicle["x"] = [x]
		vehicle["y"] = [y]
		return vehicle

	def summariseAnalysis(self, outSum, outVehicles):
		sumTravelSteps = 0
		sumConnectedSteps = 0
		sumPercentConnected = 0
		sumAvgDegree = 0
		sumAvgDegreeWhenChanged = 0
		sumJoins = 0
		sumDisconnections = 0
		sumChanges = 0
		sumavgStepsInEachCommunity = 0
		sumNumberOfUniqueCommunities = 0
		sumStability = 0

		xmin = 1000000
		ymin = 1000000
		xmax = 0
		ymax = 0
		xpos  = []
		ypos = []
		xconnected  = []
		yconnected = []
		xjoins = []
		yjoins = []
		xdisconnections = []
		ydisconnections = []
		xchanges = []
		ychanges = []
		
		outVehicles.write("vehicleId\tstart_step\tsteps_count\tavg_degree\tavg_degree_when_changed\tsteps_connected\tpercent_connected\tjoins\tchanges_community\tdisconnections\tnumber_of_unique_communities\tsteps_in_each_community\tstd_dev_step\tmax_steps\tstability\n")
		for vehicleId,vehicle in self.vehicles.iteritems():
			stepsCount = len(vehicle["steps"])
			if (stepsCount > 0):
				#initialise first self.step 
				sumTravelSteps += stepsCount
				startStep = vehicle["steps"][0]
				lastCommunityId = vehicle["communities"][0]
				lastDegree = vehicle["degrees"][0]
				connected = 0
				disconnections = 0
				changes = 0
				joins = 0
				degrees = 0
				degreeWhenChange = 0
				stepsInEachCommunity = {}

				for i in range(0,stepsCount):
					self.step = vehicle["steps"][i]
					degree = vehicle["degrees"][i]
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
					degrees += degree
					if (degree > 0):
						connected += 1
						xconnected.append(x)
						yconnected.append(y)

					# second self.step and more
					if i > 0:
						if lastDegree == 0 and degree > 0:
							joins +=1
							xjoins.append(x)
							yjoins.append(y)
						elif lastDegree > 0 and degree == 0:
							disconnections += 1
							xdisconnections.append(x)
							ydisconnections.append(y)
							# comKey = "d{0}".format(disconnections)
						if lastCommunityId != communityId and (lastDegree != 0 or degree != 0):
							changes += 1
							xchanges.append(x)
							ychanges.append(y)
							degreeWhenChange += degree
							# comKey = communityId
					if (degree > 0):
						comKey = communityId
					else:
						comKey = "d{0}".format(disconnections)

					if not comKey in stepsInEachCommunity.keys():
						stepsInEachCommunity[comKey] = 0
					stepsInEachCommunity[comKey] += 1
					#print "self.step {0}\tveh {1}\tdegree {2}\tcommunityId {3}\tcomKey {4}\n".format(self.step, vehicleId, degree, communityId, comKey)
					lastDegree = degree
					lastCommunityId = communityId

				sumConnectedSteps += connected
				vehiclePercentConnected = float(connected) / float(stepsCount)
				sumPercentConnected += vehiclePercentConnected
				vehicleAvgDegree = float(degrees) / float(stepsCount)
				sumAvgDegree += vehicleAvgDegree
				sumJoins += joins
				sumDisconnections += disconnections
				sumChanges += changes
				avgDegreeWhenChanged = 0
				if changes > 0:
					avgDegreeWhenChanged = float(degreeWhenChange) / float(changes)
				sumAvgDegreeWhenChanged += avgDegreeWhenChanged
				numberOfUniqueCommunities = len(stepsInEachCommunity)
				sumNumberOfUniqueCommunities += numberOfUniqueCommunities
				avgStepsInEachCommunity = 0
				stdDevSteps = 0
				maxSteps = 0
				if numberOfUniqueCommunities > 0:
					for steps in stepsInEachCommunity.values():
						avgStepsInEachCommunity += steps
					avgStepsInEachCommunity /= float(numberOfUniqueCommunities)
					arr = np.array(stepsInEachCommunity.values())
					stdDevSteps = np.std(arr)
					maxSteps = np.max(arr)
				sumNumberOfUniqueCommunities += numberOfUniqueCommunities
				sumavgStepsInEachCommunity += avgStepsInEachCommunity
				stability = avgStepsInEachCommunity / float(stepsCount)
				sumStability += stability
				
				outVehicles.write("{0}\t{1}\t{2}\t{3}\t{4}\t{5}\t{6}\t{7}\t{8}\t{9}\t{10}\t{11}\t{12}\t{13}\t{14}\n".format(
					vehicleId,startStep,stepsCount,vehicleAvgDegree, avgDegreeWhenChanged,
					connected,vehiclePercentConnected,joins, changes, disconnections, numberOfUniqueCommunities, 
					avgStepsInEachCommunity, stdDevSteps, maxSteps, stability))
		
		# avegare statistics
		avgTraveledSteps = 0 # how many seconds a vehicle travels
		avgConnectedSteps = 0
		avgPercentConnected = 0
		avgDegree = 0
		avgJoins = 0
		avgDisconnections = 0
		avgChangesCommunity = 0 # how many times a vehicle changes community
		avgStepsInEachCommunity = 0 # how many seconds a trip in community lasts
		avgStability = 0
		avgNumberOfUniquenetssCommunities = 0
		avgDegreeWhenChanged = 0

		vehiclesCount = len(self.vehicles)
		if vehiclesCount > 0:
			avgTraveledSteps = float(sumTravelSteps) / float(vehiclesCount)
			avgConnectedSteps = float(sumConnectedSteps) / float(vehiclesCount)
			avgPercentConnected = float(sumPercentConnected) / float(vehiclesCount)
			avgDegree = float(sumAvgDegree) / float(vehiclesCount)
			avgDegreeWhenChanged = float(sumAvgDegreeWhenChanged) / float(vehiclesCount)
			avgJoins = float(sumJoins) / float(vehiclesCount)
			avgDisconnections = float(sumDisconnections) / float(vehiclesCount)
			avgChangesCommunity = float(sumChanges) / float(vehiclesCount)
			avgStepsInEachCommunity = float(sumavgStepsInEachCommunity) / float(vehiclesCount)
			avgStability = float(sumStability) / float(vehiclesCount)
			avgNumberOfUniquenetssCommunities = float(sumNumberOfUniqueCommunities) / float(vehiclesCount)

		outSum.write("vehicles\tavg_traveled_steps\tavg_connected_steps\tavg_percent_connected\tavg_degree\tavg_degree_when_changed\tavg_joins\tavg_disconnections\tavg_number_unique_com\tavg_changes_community\tavg_steps_in_each_community\tavg_stability\n")
		outSum.write("{0}\t{1}\t{2}\t{3}\t{4}\t{5}\t{6}\t{7}\t{8}\t{9}\t{10}\t{11}\n".format(vehiclesCount,avgTraveledSteps,avgConnectedSteps,avgPercentConnected,avgDegree,avgDegreeWhenChanged,avgJoins,avgDisconnections,avgNumberOfUniquenetssCommunities,avgChangesCommunity,avgStepsInEachCommunity,avgStability))

		# the x distribution will be centered at -1, the y distro
		# at +1 with twice the width.
		xx = np.random.randn(3000)-1
		yy = np.random.randn(3000)*2+1
		numbins=100
		hist,xedges,yedges = np.histogram2d(xpos,ypos,bins=numbins,range=[[xmin,xmax],[ymin,ymax]])
		extent = [xedges[0], xedges[-1], yedges[0], yedges[-1] ]
		imshow(hist.T,extent=extent,interpolation='nearest',origin='lower')
		colorbar()
		# show()
		outputfile = os.path.join(options.outputDir, "map_density.png")
		plt.savefig(outputfile)

		hist,xedges,yedges = np.histogram2d(xconnected,yconnected,bins=numbins,range=[[xmin,xmax],[ymin,ymax]])
		extent = [xedges[0], xedges[-1], yedges[0], yedges[-1] ]
		plt.imshow(hist.T,extent=extent,interpolation='nearest',origin='lower')
		# colorbar()
		outputfile = os.path.join(options.outputDir, "map_connected.png")
		plt.savefig(outputfile)

		hist,xedges,yedges = np.histogram2d(xjoins,yjoins,bins=numbins,range=[[xmin,xmax],[ymin,ymax]])
		extent = [xedges[0], xedges[-1], yedges[0], yedges[-1] ]
		plt.imshow(hist.T,extent=extent,interpolation='nearest',origin='lower')
		# colorbar()
		outputfile = os.path.join(options.outputDir, "map_joins.png")
		plt.savefig(outputfile)

		hist,xedges,yedges = np.histogram2d(xdisconnections,ydisconnections,bins=numbins,range=[[xmin,xmax],[ymin,ymax]])
		extent = [xedges[0], xedges[-1], yedges[0], yedges[-1] ]
		plt.imshow(hist.T,extent=extent,interpolation='nearest',origin='lower')
		# colorbar()
		outputfile = os.path.join(options.outputDir, "map_disconnections.png")
		plt.savefig(outputfile)

		hist,xedges,yedges = np.histogram2d(xchanges,ychanges,bins=numbins,range=[[xmin,xmax],[ymin,ymax]])
		extent = [xedges[0], xedges[-1], yedges[0], yedges[-1] ]
		plt.imshow(hist.T,extent=extent,interpolation='nearest',origin='lower')
		# colorbar()
		outputfile = os.path.join(options.outputDir, "map_changes.png")
		plt.savefig(outputfile)
		return 0


	# read line by line
	def processLine(self, line):	
		data = line.strip().split(self.args['separator'])
		now = int(data[0])
		if (now != self.step):
			self.step = now
			print "self.step {0}: {1}".format(self.step, line)

		# add data
		# vehicleId = str(data[1])	
		# x = float(data[2])
		# y = float(data[3])
		# degree = float(data[4])
		# neighbors = str(data[5]).split(self.args['separator2'])
		# connectedComponentId = int(data[6])
		# connectedComponentSize = int(data[7])
		# communityId = int(data[8])
		# communityScore = float(data[9])
		# communitySize = float(data[10])	
		
		# # add vehicle to global self.vehicles
		# if (vehicleId not in self.vehicles.keys()): 
		# 	self.vehicles[vehicleId] = createVehicle(vehicleId, self.step, x, y, degree, connectedComponentId, communityId)
		# else:
		# 	self.addToVehicles(self.vehicles[vehicleId], self.step, x, y, degree, connectedComponentId, communityId)
