import os
import math
import random
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt

# from pandas.tools.plotting import bootstrap_plot

class CrowdsAnalyser:
	"store file metadata"
	def __init__(self):
		self.filename = None
		self.communities = None

	def readFile(self, filename):
		self.filename = filename  
		self.df = pd.read_csv(filename, sep="\t")
		print("Reading file", filename)
		return
	
	def rand_data(self, integ):
		'''
		Function that generates 'integ' random values between [0.,1.)
		'''
		rand_dat = [random.random() for _ in range(integ)]
		return rand_dat

	def analyseCommunities(self, stepColumn, communityColumn):
		# group by step and com_id to get statistics for each community at every step
		gb = self.df.groupby([stepColumn, communityColumn])
		self.communities = gb.aggregate({
			"degree":[np.size, np.average, np.std, np.min, np.max],
			"speed":[np.size, np.average, np.std, np.min, np.max], 
			"avg_speed":[np.average, np.std, np.min, np.max], 
			"num_stops":[np.average, np.std, np.min, np.max],
			"x":[np.min, np.max], "y":[np.min, np.max],
			"com_size":[np.min, np.max, np.average, np.std]}).reset_index()
		# add column with community range calculated as distance between min position of a vehicle and maximum position of vehicle 
		self.communities["range"] = self.calculateDistance(self.communities.x.amin,self.communities.y.amin,self.communities.x.amax,self.communities.y.amax)

	def analyseVehicles(self, stepColumn, vehicleColumn):
		# group by step and com_id to get statistics for each community at every step
		gb = self.df.groupby([vehicleColumn, stepColumn])
		self.vehicles = gb.aggregate({
			"degree":[np.size, np.average, np.std, np.min, np.max],
			"speed":[np.size, np.average, np.std, np.min, np.max], 
			"avg_speed":[np.average, np.std, np.min, np.max], 
			"num_stops":[np.average, np.std, np.min, np.max],
			"x":[np.min, np.max], "y":[np.min, np.max],
			"com_size":[np.min, np.max, np.average, np.std]}).reset_index()
	
	def analyseNumberOfCommunityChanges(self, vehicleColumn):	
		# calculated number of community changes calculated as number of unique com_id for a vehicle
		self.numberOfCommunityChanges = self.df.groupby([vehicleColumn, "com_id"]).size().reset_index().groupby(vehicleColumn).size().reset_index()
		print('numberOfCommunityChanges', self.numberOfCommunityChanges)
	
	def calculateDistance(self, x0, y0, x1, y1):
		deltaX = x0 - x1
		deltaY = y0 - y1
		distance = np.sqrt((x0 - x1)*(x0 - x1) + ( y0 - y1)*( y0 - y1))
		return distance

	def writeVehiclesToFile(self, filename):
		out = open(filename, 'w')
		self.vehicles.to_csv(out, sep='\t')
	
	def writeCommunityChangesToFile(self, filename):
		out = open(filename, 'w')
		self.numberOfCommunityChanges.to_csv(out, sep='\t')

	def writeCommunitiesToFile(self, filename):
		out = open(filename, 'w')
		self.communities.to_csv(out, sep='\t')
	
	def plotCommunitySize(self, dirname):
		# community size per step : avg, min, max 
		column = 'com_size'
		plt.figure();
		bp = self.communities.boxplot(column=[column], by='step')
		plt.savefig(os.path.join(dirname, column + "_boxplot.png"))
		# number of communities per step
		plt.figure();
		numberOfCommunities = self.df.groupby('step').com_id.nunique()
		numberOfCommunities.plot(title=str("Number of communities per step"))
		plt.savefig(os.path.join(dirname, "num_of_com_per_step.png"))
		# number of vehicles per step
		plt.figure();
		numberOfVehicles = self.df.groupby('step').node_id.nunique()
		numberOfVehicles.plot(title=str("Number of vehicles per step"))
		plt.savefig(os.path.join(dirname, "num_of_veh_per_step.png"))
		

# def analyseVehicles(self, self.df, column, outputDir):
# 	self.df1 = DataFrame({'number_of_steps' : self.df.groupby(['node_id','com_id']).size()}).reset_index()
# 	self.df2 = DataFrame({
# 		'number_of_communities' : self.df1.groupby(['node_id']).size(),
# 		'first_step': self.df.groupby(['node_id']).aggregate({"step":min})['step'],
# 		'avg_degree': self.df.groupby('node_id').aggregate({"degree":np.average})["degree"],
# 		'avg_number_of_steps': self.df1.groupby(['node_id']).aggregate({'number_of_steps':np.average})['number_of_steps'],
# 		'total_number_of_steps': self.df1.groupby(['node_id']).aggregate({'number_of_steps':np.sum})['number_of_steps']
# 		}).reset_index()
# 	print "number of vehicles: {0}".format(len(self.df2)) 
# 	self.df2 = self.df2.sort(['first_step'],ascending=[True])
# 	# print self.df2.head()
# 	# plot
# 	# self.df.groupby('node_id').plot(x="first_step", y="avg_degree", title=str("degree"))
# 	self.df2.plot(x='node_id', y='number_of_communities', title=str("number of communities per vehicle"))
# 	plt.savefig(os.path.join(outputDir, column + "_num_of_communities.png"))
# 	# plt.show()
# 	print self.df2.describe()
# 	outputfile = os.path.join(outputDir, "analysis_"+column+".tsv")
# 	out = open(outputfile, 'w')
# 	out.write("Total number of communities\t{0}\n".format(len(self.df.groupby('com_id'))))
# 	out.write("Avg number of community changes per vehicle\t{0}\n".format(np.average(self.df2['number_of_communities'])))
# 	return

