import matplotlib
import numpy as np
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from matplotlib.mlab import csv2rec
from scipy import spatial
from pylab import *


class CommunityAnalyser:

	def __init__(self):
		self.step = -1
		self.currentself.vehiclesCount = 0
		self.args = {"separator":'\t', "separator2":','}
		self.communities = {}
	
	def readFile(self, filename):
		self.filename = filename	
		with open(filename, 'r') as f:
			header_line = next(f)
			for data_line in f:
				self.processLine(data_line)
	
	def processLine(line):	
		data = line.split(self.args['separator'])
		now = int(data[0])

		if (now != self.step):
			self.step = now
		
		#self.step	node_id	x	y	degree	neighbors	cc_id	cc_size	com_id	com_score	com_size
		vehicleId = str(data[1])	
		x = float(data[2])
		y = float(data[3])
		degree = float(data[4])
		neighbors = str(data[5]).split(self.args['separator2'])
		communityId = int(data[options.groupIndex])
		# add vehicle to a community
		communityScore = 1
		if options.groupType == "community":
			communityScore = float(data[9])
		if (communityId not in self.communities.keys()): 
			# print "self.step {2}, vehicle {0}, x,y: {3}{4}, degree: {5}, neighborsL {6}, community {1}, score: {7}".format(vehicleId, communityId, self.step,x,y,degree, neighbors, communityScore, len(data))
			communitysteps = {}
			communitysteps[self.step] = 1
			communities[communityId] = communitysteps
		elif self.step not in communities[communityId].keys():
				communities[communityId][self.step] = 1
		else:
			communities[communityId][self.step] += 1
		communityId = int(data[options.groupIndex])
		return self.step

	def summariseAnalysis(self, outCommunities, outSum):
		totalLifetime = 0
		totalSumSize = 0
		maxLifetime = 0
		maxLifetimeId = -1
		breakLength = 0
		nonZeroCommunities = 0
		maxSize = 0
		maxSizeId = -1
		maxSizeself.step = -1

		outCommunities.write("id\tstart_self.step\tlifetime\tavg_size\tbreak_length\tsteps\tsizes\n")
		for id,communitysteps in self.communities.iteritems():
			lifetime = len(communitysteps)
			if lifetime > 0:
				totalLifetime += lifetime
				nonZeroCommunities += 1
				sizes = []
				steps = []
				sumSize = 0
				i = 0
				for step,vehiclesInself.step in communitysteps.iteritems():
					steps.append(self.step)
					sizes.append(vehiclesInself.step)
					# get max
					if vehiclesInself.step > maxSize:
						maxSize = vehiclesInself.step
						maxSizeId = id
						maxSizeself.step = self.step
					# sum size
					sumSize += vehiclesInself.step
					# chck if a break
					i += 1
					if len(steps) > 1:
						if self.step - steps[i-1] > 1:
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
		outSum.write( "group_count\tavg_size\tmax_size\tmax_size_id\tmax_size_self.step\tmax_size_lifetime\tavg_lifetime\tmax_lifetime\tmax_lifetime_id\n")
		outSum.write("{0}\t{1}\t{2}\t{3}\t{4}\t{4}\t{5}\t{6}\t{7}\t{8}\n".format(len(communities), avgCommunitySize, maxSize, maxSizeId, maxSizeself.step, len(communities[maxSizeId]), avgLifetime, maxLifetime, maxLifetimeId))
		return 0