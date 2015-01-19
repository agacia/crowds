import os
import math
import random
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt

import matplotlib.font_manager as font_manager
# from pandas.tools.plotting import bootstrap_plot


class CrowdsAnalyser:
	"store file metadata"
	def __init__(self):
		self.filename = None
		self.communities = None
		self.startIndex = 	0
		self.indexStep = 1
		self.postfixLenghth = 1
		self.no_runs = 2
		self.average_span = 60
		self.labels = {
			"SandSharc_oryg": "NoStability",
			"SandSharc_link_duration": "Link",
			"SandSharc_mobility_instant": "Mobility instant",
			"SandSharc_mobility_timemean": "Mobility timemean",
			"SandSharc_hybrid_timemean_modifications" : "SandSharc_hybrid_timemean_modifications",
			"NewSawSharc_oryg" : "NewSawSharc_oryg_modifications",
			"SandSharc_hybrid_instant": "Link + mobility instant",
			"SandSharc_hybrid_timemean": "Link + mobility timemean", 
			"MobileSandSharc_oryg":"MobileSandSharc_oryg", 
			"SandSharc_ag": "SandSharc_ag",
			"Crowdz":"Crowdz", 
			"StableCrowdz": "StableCrowdz"}
		self.colors = {
			"SandSharc_oryg": "c",
			"SandSharc_link_duration":"m",
			"SandSharc_mobility_instant":"r",
			"SandSharc_mobility_timemean": "b",
			"SandSharc_hybrid_timemean_modifications" : "g",
			"SandSharc_hybrid_instant": "y",
			"SandSharc_hybrid_timemean": "g",  
			"NewSawSharc_oryg" : "g",
			"MobileSandSharc_oryg":"m", 
			"Crowdz":"b", 
			"StableCrowdz":"g",
			"SandSharc_ag": "b",
			"Crowdz":"b"}

	def readFile(self, filename):
		self.filename = filename  
		self.df = pd.read_csv(filename, sep="\t")
		print "Read file {0}".format(filename)
		return self.df

	def readFiles(self, dir, filename, algorithm):
		# print("Reading dir", dir, "files ", filename)
		self.results = {}
		for root, dirnames, filenames in os.walk(dir):
			for dirname in dirnames:
				if self.is_number(dirname):
					filepath = os.path.join(dir,dirname,algorithm,filename)
					print ("reading",dirname,filepath)
					if os.path.isfile(filepath):
						print ("is file",dirname,filepath)
						self.results[dirname] = pd.read_csv(filepath, skiprows=[1,2], sep="\t")
						print "Read", self.results[dirname].head()
			break
		print ("Read results", len(self.results))
		return
	
	def is_number(self, s):
	    try:
	        float(s)
	        return True
	    except ValueError:
	        return False

	def rand_data(self, integ):
		'''
		Function that generates 'integ' random values between [0.,1.)
		'''
		rand_dat = [random.random() for _ in range(integ)]
		return rand_dat

	def analyseCommunities(self, stepColumn, communityColumn):
		# group by step and com_id to get statistics for each community at every step
		gb = self.df.groupby([stepColumn, communityColumn])
		# self.df["congested"] = self.moreThan(gb["num_stops"], 1)
		self.communities = gb.aggregate({
			"degree":[np.mean, np.std, np.amin, np.amax],
			"speed":[np.mean, np.std, np.amin, np.amax], 
			"timeMeanSpeed":[np.mean, np.std, np.amin, np.amax], 
			"num_stops":[sum, np.mean, np.amin, np.amax],
			"x":[np.min, np.max], "y":[np.min, np.max],
			# "congested":[sum],
			"com_size":[np.size]}).reset_index()
		self.communities["congested"] = self.communities["num_stops"]["sum"]
		# add column with community range calculated as distance between min position of a vehicle and maximum position of vehicle 
		self.communities["range"] = self.calculateDistance(self.communities.x.amin,self.communities.y.amin,self.communities.x.amax,self.communities.y.amax)
		self.communities["num_stops_count"] = self.communities["range"]
		
	def analyseSteps(self):
		gb = self.df.groupby(['step', 'com_id'])
		communities = gb.aggregate({
			"timeMeanSpeed":[np.size, np.mean, np.std, np.amin, np.amax], 
			"com_size":[np.size]}).reset_index()
		
		print ("com speed std", communities['timeMeanSpeed']['std'].tail(100))
		# print ("com size", communities['com_size']['size'].tail(100))
		self.df_steps = pd.DataFrame(
			{
				"step": communities['step'],
				"com_id": communities['com_id'],
				"timeMeanSpeed": communities['timeMeanSpeed']['mean'],
				"timeMeanSpeed_std": np.ma.masked_array(communities['timeMeanSpeed']['std'],np.isnan(communities['timeMeanSpeed']['std'])),
				"com_size": communities['com_size']['size']
				# "com_size_std": np.ma.masked_array(communities['com_size']['size'], np.isnan(communities['com_size']['std']))
				# "com_size_max": communities['com_size']['amax']
			
			})
		grouped = self.df_steps.groupby('step', as_index=True)
		
		# for name, group in grouped:
		# 	a = group['avg_speed_std']
		# 	mm = np.mean(np.ma.masked_array(a,np.isnan(a)))
		# 	print (name,mm)
		
		self.steps = grouped.aggregate( {
			"timeMeanSpeed":[np.mean],
			"timeMeanSpeed_std":[np.mean],
			"com_size": [np.mean, np.std, np.size, np.amax, sum]
			});
		# (np.mean) #.reset_index()
	
	def compareSteps(self):
		print ("comparing results of", len(self.results), "runs")	
		pieces = []
		# for i,result in self.results.items():
			# print ("re",result)
			# pieces.append(result)
		pieces = [self.results["1"], self.results["2"]]
		self.concatenated = pd.concat(pieces, axis=1)
		# print self.concatenated.index


	def analyseVehicles(self, vehicleColumn):
		# group by step and com_id to get statistics for each community at every step
		gb = self.df.groupby(vehicleColumn)
		self.vehicles = gb.aggregate({
			"degree":[np.size, np.mean, np.std, np.min, np.max],
			"speed":[np.mean, np.std, np.min, np.max], 
			"timeMeanSpeed":[np.mean, np.std, np.min, np.max], 
			"num_stops":[np.mean, np.std, np.min, np.max],
			"x":[np.min, np.max], "y":[np.min, np.max],
			"com_size":[np.min, np.max, np.mean, np.std]}).reset_index()
		gb2 = self.df.groupby([vehicleColumn,"com_id"])
		self.vehiclesCom = gb2.aggregate({
			"com_size":[np.size, np.mean]}).reset_index()
		self.vehiclesCom.columns = [' '.join(col).strip() for col in self.vehiclesCom.columns.values]

		gb3 = self.vehiclesCom.groupby(vehicleColumn)
		self.vehiclesComSum = gb3.aggregate({
			"com_id":[np.size],
			"com_size size":[np.mean, np.sum], # avg number of steps in a community , total number of steps of a vehicle
			"com_size mean":[np.mean] # avg size of a community
		})
		self.vehiclesComSum.columns = [ "avg_steps_in_com", "steps", "avg_com_size", "num_changes" ]
		return self.vehiclesComSum


	def plot_vehicle_hist(self, dirname):
		plt.figure()
		self.vehiclesComSum.hist(bins=300, sharey=True)
		plt.ylim([0,180])
		plt.savefig(os.path.join(dirname, "histograms.png"))
		# fig, axes = plt.subplots(nrows=1, ncols=4, figsize=(12, 4))
		# for i,var in enumerate(self.vehiclesComSum.columns):
		# 	self.vehiclesComSum[var].hist(bins=300, ax=axes[i], sharey=True)
		# 	axes[i].set_ylabel(var)
		# plt.figure()
		# self.vehiclesComSum.steps.hist(bins=300)
		# plt.savefig(os.path.join(dirname, "hist-steps.png"))
		# plt.figure()
		# self.vehiclesComSum.avg_com_size.hist(bins=300)
		# plt.savefig(os.path.join(dirname, "hist-avg_com_size.png"))


	def analyseModularity(self, step_column, column_name, dirname):
		# group by step and com_id to get statistics for each community at every step
		plt.figure()
		self.df.plot(x=step_column, y=column_name)
		plt.savefig(os.path.join(dirname, "column_"+column_name+".png"))

	def analyseLinks(self, column, dirname):
		# group by step and com_id to get statistics for each community at every step
		# links = self.df[["step", "link_id", "com_id"]]
		self.df.head()
		gb = self.df.groupby(["link_id", "step"])
		self.links = gb.com_id.nunique()
		self.links.head()
		print self.links
		path = os.path.join(dirname, "links.tsv")
		print "writing to {0}".format(path)
		out = open(os.path.join(path), "w")
		self.links.to_csv(out, sep='\t')
		
		# for name, group in gb.groups:
		# 	print name, group
		
	def moreThan(self, column, thres):
		biggerThan = lambda x: (x > thres)
		print "before transformed", biggerThan
		#transformed = column.apply(sum)
		#print "transformed", transformed
		#return transformed
		return column

	def calculateDistance(self, x0, y0, x1, y1):
		deltaX = x0 - x1
		deltaY = y0 - y1
		distance = np.sqrt((x0 - x1)*(x0 - x1) + ( y0 - y1)*( y0 - y1))
		return distance

	def writeVehiclesToFile(self, outputDir):
		# out = open(os.path.join(outputDir, "vehicles_pandas.tsv"), 'w')
		# self.vehicles.to_csv(out, sep='\t')
		path = os.path.join(outputDir, "num_com_changes_pandas.tsv")
		print "writing to {0}".format(path)
		out = open(os.path.join(path), "w")
		self.vehiclesComSum.to_csv(out, sep='\t')
		
		path = os.path.join(outputDir, "veh_com_pandas.tsv")
		print "writing to {0}".format(path)
		out = open(os.path.join(path), "w")
		self.vehiclesCom.to_csv(out, sep='\t')

	def writeModularityToFile(self, column_name, outputDir):
		# out = open(os.path.join(outputDir, "vehicles_pandas.tsv"), 'w')
		# self.vehicles.to_csv(out, sep='\t')
		path = os.path.join(outputDir, "column_"+column_name+".tsv")
		print "writing to {0}".format(path)
		out = open(os.path.join(path), "w")
		avg_mod = np.mean(self.df[column_name])
		out.write("mean\t%.2f" % avg_mod)
		

	def writeStepsToFile(self, filename):
		out = open(filename, 'w')
		print ("writing" , filename, "self.steps",self.steps.tail())
		self.steps.to_csv(out, sep='\t')
		# out = open(filename+"2", 'w')
		# self.df_steps.to_csv(out, sep='\t')

	def writeStepsComparisonToFile(self, filename):
		out = open(filename, 'w')
		print ("writing to" , filename)
		# print("self.steps",self.steps.head())
		# self.steps.to_csv(out, sep='\t')
		# out = open(filename+"2", 'w')
		# self.df_steps.to_csv(out, sep='\t')

		# plt.figure();
		# # avg_speed = np.mean(columns)
		# dfm = pd.DataFrame( {
		# 	"timeMeanSpeed": self.getMean("('avg_speed', 'mean')"),
		# 	"avg_speed_std": self.getMean("('avg_speed_std', 'mean')"),
		# 	"avg_com_count": self.getMean("('com_size', 'size')"),
		# 	"avg_com_size": self.getMean("('com_size', 'mean')"),
		# 	"max_com_size": self.getMean("('com_size', 'amax')"),
		# 	"std_com_size": self.getMean("('com_size', 'std')")});
		dfm = pd.DataFrame( {
			"timeMeanSpeed": self.getMean("timeMeanSpeed"),
			"timeMeanSpeed_std": self.getMean("timeMeanSpeed_std"),
			"avg_com_size_avg": self.getMean("com_size"), # avg
			"avg_com_size_std": self.getMean("com_size.1"), # std
			"avg_com_count": self.getMean("com_size.2"), # size
			"avg_com_size_max": self.getMean("com_size.3"), # amax
			"avg_com_size_sum": self.getMean("com_size.4")}); # sum
		print ("dfm",type(dfm))
		dfm.to_csv(out, sep='\t')
		

	def writeComChangesComparisonToFile(self, outputDir):
		out = open(os.path.join(outputDir, "comparison_changes.tsv"), 'w')
		print ("writing to" , os.path.join(outputDir, "comparison_changes.tsv"))
		self.changes_comparison = pd.DataFrame( {
			"num_changes": self.getMean("num_changes"),
			"avg_steps_in_com": self.getMean("avg_steps_in_com"), 
			"steps": self.getMean("steps"),
			"avg_com_size": self.getMean("avg_com_size")
		});
		# print ("self.changes_comparison",type(self.changes_comparison))
		self.changes_comparison.to_csv(out, sep='\t')
		plt.figure()
		self.changes_comparison.num_changes.hist(bins=50)
		plt.savefig(os.path.join(outputDir, "hist-numb-changes.png"))
		
		plt.figure()
		self.changes_comparison.avg_steps_in_com.hist(bins=50)
		plt.savefig(os.path.join(outputDir, "hist-numb-avg_steps_in_com.png"))
		
		plt.figure()
		self.changes_comparison.steps.hist(bins=50)
		plt.savefig(os.path.join(outputDir, "hist-numb-steps.png"))

		plt.figure()
		self.changes_comparison.avg_com_size.hist(bins=50)
		plt.savefig(os.path.join(outputDir, "hist-numb-avg_com_size.png"))


		# dfm.plot(title=str("comparison-all"), label="com size")
		# plt.legend(loc='best')
		# plt.savefig(os.path.join(dirname, "comparison-all.png"))

	def create_scatter_plot(self, axarr, i, measures_names, means):
		for j,name in enumerate(measures_names):
			# print "j",j
			axarr[j].scatter([i], means[j], s=20, c='b', marker='o', label=name)

	def create_bar_plot(self, ylabel, metric, groups, algorithms, averages, plotfile):
		N = len(groups)
		ind = np.arange(N)  # the x locations for the groups
		width = 0.25       # the width of the bars
		fig, ax = plt.subplots()
		colors = ["r","y","b"]
		rects = []
		means = {}
		std = {}
		j = 0
		k = 0
		for algorithm in algorithms:
			means[algorithm] = []
			for i in range(len(groups)):
				means[algorithm].append(averages[metric][j])
				j += 1
			# print algorithm, "means", means[algorithm]
			# std[algorithm] =  (2, 3, 4)
			rects.append(ax.bar(ind + width * k, means[algorithm], width, color=colors[k])) #, yerr=std[algorithm])
			k += 1
		ax.set_ylabel(ylabel)
		ax.set_title(ylabel + ' by speed and algorithm')
		ax.set_xticks(ind+width)
		ax.set_xticklabels( tuple(groups) )
		ax.legend( tuple(rects), tuple(algorithms) )
		plt.savefig(plotfile)

	def compareAlgorithmsNumberOfChanges(self, dirname, algorithms, filename):
		# read average algorithm results
		self.results = {}
		for algorithm in algorithms:
			filepath = os.path.join(dirname,algorithm,filename)
			print ("reading file ", filepath)
			index = float(algorithm[-self.postfixLenghth:len(algorithm)])
			self.results[index] = pd.read_csv(filepath, sep="\t")
		print ("Read", len(self.results), "algorithms", algorithms)
		
		measures_names = ['avg_com_size','avg_steps_in_com','num_changes','steps']
		# calculate averages
		self.averages = {}
		self.averages['algorithms'] = []
		for name in measures_names:
			self.averages[name] = []
		index = self.startIndex
		out = open(os.path.join(dirname, "algotirhms_comparison_changes.txt"), 'w')
		out.write("{0}\t{1}\t{2}\t{3}\t{4}\n".format('algorithm','avg_com_size','avg_steps_in_com','num_changes','steps'))
		for i in range(0,len(self.results)):
			result = self.results[index]
			means = [round(np.mean(result[x]),2) for x in measures_names]
			for name,mean in zip(measures_names,means):
				self.averages[name].append(mean)
			self.averages['algorithms'].append(algorithms[i])
			out.write("{0}\t{1}\n".format(algorithms[i], "\t".join([str(round(x,2)) for x in means])))
			index = round(index + self.indexStep, 2)

	def compareAlgorithms(self, dirname, algorithms, filename):
		# read average algorithm results
		self.results = {}
		for algorithm in algorithms:
			filepath = os.path.join(dirname,algorithm,filename)
			print ("reading file ", filepath)
			index = float(algorithm[-self.postfixLenghth:len(algorithm)])
			self.results[index] = pd.read_csv(filepath, sep="\t")
			print "Read", self.results[index].head()
		# print ("Read", len(self.results), "algorithms", algorithms)
		
		algorithm_names = ['nsim 100', 'nsim 200', 'nsim 300', 'sr 100', 'sr 200', 'sr 300', 'dsd 100', 'dsd 200', 'dsd 300']
		# measures_names = ['avg_speed','avg_speed_std','avg_com_size','max_com_size','std_com_size','avg_com_count']
		measures_names = ['timeMeanSpeed','timeMeanSpeed_std','avg_com_size_avg', 'avg_com_size_max', 'avg_com_size_std',	'avg_com_count']
		# measures_names = ["avg_com_count",	"avg_com_size",	"timeMeanSpeed",	"avg_speed_std",	"max_com_size",	"std_com_size"]
		measures_titles = ['Average speed','Average std speed','Average community size',
		'Maximum community size','Std community size','Average community count']
		
		# calculate averages
		self.averages = {}
		self.averages['algorithms'] = []
		for name in measures_names:
			self.averages[name] = []
		
		out = open(os.path.join(dirname, "algotirhms_comparison.txt"), 'w')
		out.write("{0}\t{1}\t{2}\t{3}\t{4}\t{5}\t{6}\n".format("algorithm","timeMeanSpeed","avg_speed_std","avg_size","max_com_size","std_com_size","avg_count"))
		
		plt.figure(1)
		f, axarr = plt.subplots(6, sharex=True)
		for i,name in enumerate(measures_names):
			axarr[i].set_title(name)

		index = self.startIndex
		# for each algorithm 
		for i in range(0,len(self.results)):
			result = self.results[index]
			means = [round(np.mean(result[x]),2) for x in measures_names]
			
			# avg_speed = np.mean(result['avg_speed'])
			# avg_speed_std = np.mean(result['avg_speed_std'])
			# avg_com_size = np.mean(result['avg_com_size'])
			# max_com_size = np.mean(result['max_com_size'])
			# std_com_size = np.mean(result['std_com_size'])
			# avg_com_count = np.mean(result['avg_com_count'])
			# print algorithms[i]
			for name,mean in zip(measures_names,means):
				self.averages[name].append(mean)
			self.averages['algorithms'].append(algorithms[i])

			out.write("{0}\t{1}\n".format(algorithms[i], "\t".join([str(round(x,2)) for x in means])))
			
			self.create_scatter_plot(axarr, i, measures_names, means)

			# plt_avg_speed = axarr[0].scatter([i],[avg_speed], s=20, c='b', marker='o', label=" avg_speed")
			# plt_avg_speed_std = axarr[1].scatter([i],[avg_speed_std], s=20, c='r', marker='o', label=" avg_speed")
			# plt_avg_com_size = axarr[2].scatter([i],[avg_com_size], s=20, c='g', marker='o', label=" avg_com_size")
			# plt_avg_count = axarr[3].scatter([i],[avg_com_count], s=20, c='y', marker='o', label=" avg_count")
			# plt_avg_max_com_size = axarr[4].scatter([i],[max_com_size], s=20, c='y', marker='o', label=" max_com_size")
			# plt_avg_std_dev_size = axarr[5].scatter([i],[std_com_size], s=20, c='y', marker='o', label=" std_com_size")
			index = round(index + self.indexStep, 2)
		
		# plt.legend((plt_avg_speed, plt_avg_speed_std, plt_avg_com_size, plt_avg_count, plt_avg_max_com_size, plt_avg_std_dev_size),
  #          ('avg speed', 'avg speed std', 'avg_com_size', 'avg_count',"max_com_size","std_com_size"),
  #          scatterpoints=1,
  #          loc='best',
  #          ncol=3,
  #          fontsize=8)
		
		axarr[5].set_xticklabels( tuple(algorithm_names) )
		plt.savefig(os.path.join(dirname, "avg-scatter-comparison-algorithms-avgspeed.png"))
		

		# groups = ['100', '200', '300']
		# algorithms = ["nsim", "sr", "dsd"]
		# for metric,title in zip(measures_names,measures_titles):
		# 	filename = os.path.join(dirname, "avg-barplot-comparison-algorithms-"+metric+".png")
		# 	print "plotting to ", filename
		# 	self.create_bar_plot(title, metric, groups, algorithms, self.averages, filename)

		# linear plots 

		# plt.figure()
		# plt_avg_speed = plt.plot(range(0,len(self.results)),self.averages['avg_speed'], label=" avg_speed")
		# plt.legend(loc='best')
		# plt.xtitle="Algorithm"
		# plt.savefig(os.path.join(dirname, "avg-comparison-algorithms-avg_speed.png"))

		# plt.figure()
		# plt_avg_speed = plt.plot(range(0,len(self.results)),self.averages['avg_speed'], label=" avg_speed_std")
		# plt.savefig(os.path.join(dirname, "avg-comparison-algorithms-avg_speed_std.png"))
		
		# plt.figure()
		# plt_avg_com_size = plt.plot(range(0,len(self.results)),self.averages['avg_com_size'], label=" avg_com_size")
		# plt.savefig(os.path.join(dirname, "avg-comparison-algorithms-avg_com_size.png"))
		
		# plt.figure()
		# plt_max_com_size = plt.plot(range(0,len(self.results)),self.averages['max_com_size'], label=" max_com_size")
		# plt.savefig(os.path.join(dirname, "avg-comparison-algorithms-max_com_size.png"))
		
		# plt.figure()
		# plt_std_com_size = plt.plot(range(0,len(self.results)),self.averages['std_com_size'], label=" std_com_size")
		# plt.savefig(os.path.join(dirname, "avg-comparison-algorithms-std_com_size.png"))
		
		# plt.figure()
		# plt_avg_count = plt.plot(range(0,len(self.results)),self.averages['avg_count'], label=" avg_count")
		# plt.savefig(os.path.join(dirname, "avg-comparison-algorithms-avg_count.png"))
		
	
	def writeCommunityChangesToFile(self, filename):
		out = open(filename, 'w')
		self.numberOfCommunityChanges.to_csv(out, sep='\t')

	def writeCommunitiesToFile(self, filename):
		out = open(filename, 'w')
		print ("writing to ", filename)
		self.communities.columns = ['_'.join(col).strip() for col in self.communities.columns.values]
		print  "columns",self.communities.columns
		self.communities.to_csv(out, sep='\t')

	
	def plotComparison(self, dirname):

		# avg_speed_mean	avg_speed_std_mean	com_size_mean	com_size_std	com_size_size	com_size_amax	com_size_sum
		self.plot_results("timeMeanSpeed", "Average speed in a community", dirname)
		self.plot_results("timeMeanSpeed_std", "Average stdev of speed in a community", dirname)
		self.plot_results("com_size", "Average community size", dirname)
		self.plot_results("com_size.1", "Average stddev community size", dirname)
		self.plot_results("com_size.2", "Average number of communities", dirname)
		self.plot_results("com_size.3", "Average max community size", dirname)
		self.plot_results("com_size.4", "Number of vehicles", dirname)
		# plt.figure();
		# for i,result in self.results.items():
		# 	result.plot(title=str("Count"), x='step', y="('com_size', 'size')", label="run "+str(i)) 
		# plt.legend(loc='best')
		# plt.savefig(os.path.join(dirname, "comparison-count.png"))
		
	def plot_results(self, columnname, title, dirname):
		plt.figure();
		for i,result in self.results.items():
			result=result.rename(columns = {'Unnamed: 0':'step'})
			result = result.fillna(0)
			result[columnname].astype('float32')
			result.plot(title=str(title), x='step', y=columnname, label="run "+str(i)) 
		plt.legend(loc='best')
		plt.savefig(os.path.join(dirname, "comparison-"+columnname+".png"))
		

	def getMean(self, columnname):
		a  = []
		for index,result in self.results.items():
			# print "result.keys", result.keys()
			# print ("appending",result[columnname])
			a.append(result[columnname])
		df = pd.DataFrame(a)
		# if len(self.results) == 5:
		# 	df = pd.DataFrame([self.results['1'][columnname],self.results['2'][columnname],
		# 		self.results['3'][columnname],self.results['4'][columnname],
		# 		self.results['5'][columnname]])
		# if len(self.results) == 10:
		# 	df = pd.DataFrame([self.results['1'][columnname],self.results['2'][columnname],
		# 		self.results['3'][columnname],self.results['4'][columnname],
		# 		self.results['5'][columnname],self.results['6'][columnname],self.results['7'][columnname],
		# 		self.results['8'][columnname],self.results['9'][columnname],self.results['10'][columnname],
		# 		])
		dfm = df.mean(axis=0)
		return dfm

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
	
	def plot_histograms(self, column_name, input_dir, filename):
		plt.figure()
		self.vehiclesComSum.num_changes.hist(bins=300, ylim=(0,100))
		plt.savefig(os.path.join(dirname, "hist-num-changes.png"))
		plt.figure()

	def plot_modularity(self, column_name, plot_title, inputfile):
		# filepath = os.path.join(inputfile)
		df = self.readFile(inputfile)
		df[column_name].plot(title=plot_title)
		plt.savefig(inputfile+".png")	
	
	def write_stability(self, inputdir, filename, titles, start_step, stop_step):
		folders = range(0, self.no_runs)
		stats = {}
		# stats_all = {}
		statnames = ["mean"]

		# values = {name: {name: list() for name in statnames} for name in groupnames}
		out = open(os.path.join(inputdir, "stability.csv"), 'w')
		
		step_column = 'step'
		for title in titles:	
			out.write(title + '\n')
			stats[title] = pd.DataFrame()
			for folder in folders:
				inputfile = os.path.join(inputdir, title, str(folder), filename)
				print inputfile
				df = self.readFile(inputfile)
				if start_step != None and stop_step != None:
					# start_step = df[step_column][0]
					# stop_step = df[step_column][len(df)-1]
					print "start_step, stop_step", start_step, stop_step
					df = df[df[step_column] >= start_step] 
					df = df[df[step_column] <= stop_step]
					self.df = df
				vehs = self.analyseVehicles("node_id")
				stats[title][str(folder)] = vehs.mean()
			print title, stats[title]

			out.write("\nstats_stability_all\n")
			self.print_summary_df(stats[title], out)
			# out.write("\nstats_stability_all\n")
			# self.print_summary_df(stats_all[title], out)

	def analyse_link_lifetime(self, inputdir, inputfile, start_step, stop_step):

		out_contacts = open(os.path.join(inputdir, "contacts.csv"), 'w')
		out_average_lifetime = open(os.path.join(inputdir, "average_lifetime.csv"), 'w')
			
		df = pd.read_csv(inputfile, sep="\t")
		print "read df "
		print df.head()
		step_column = 'step'
		if start_step != None and stop_step != None:
			# start_step = df[step_column][0]
			# stop_step = df[step_column][len(df)-1]
			print "start_step, stop_step", start_step, stop_step
			df = df[df[step_column] >= start_step] 
			df = df[df[step_column] <= stop_step]
		
		df['contact_number'] = df['link_duration'] == 1
				
		edge_column = 'edge_id'
		gb = df.groupby(edge_column)
		contact_time = gb.size()

		contacts = gb.aggregate({'contact_number':sum})
		contacts["contact_time"] = contact_time	
		contacts["average_lifetime"] = contacts["contact_time"] / contacts["contact_number"]

		print contacts.head(), type(contacts)
		contacts.to_csv(out_contacts, '\t')

		all_average_lifetime = np.mean(contacts["average_lifetime"])
		out_average_lifetime.write("all_average_lifetime\t{0}".format(all_average_lifetime))
		print "writing to {0}".format(out_average_lifetime)

	def analyse_communities_lifetime(self, inputdir, column_name, titles, start_step, stop_step):
		folders = range(0, self.no_runs)
		stats_lifetime = {}
		stats_lifetime_all = {}
		stats = {}
	
		# values = {name: {name: list() for name in statnames} for name in groupnames}
		out = open(os.path.join(inputdir, "community_lifetime.csv"), 'w')
		for title in titles:	
			out.write(title + '\n')
			stats_lifetime[title] = pd.DataFrame()
			stats_lifetime_all[title] = pd.DataFrame()
			stats[title] = {}
			stats[title]['one_second_com'] = []
			stats[title]['more_one_second_com'] = []
			stats[title]['more_one_second_com_singl'] = []
			stats[title]['more_one_second_com_nosingl'] = []
			step_column = "step"
			com_column = "com_id"

			for folder in folders:
				inputfile = os.path.join(inputdir, title, str(folder), "crowds_communities.csv")
				print inputfile
				df = self.readFile(inputfile)
				
				if start_step != None and stop_step != None:
					# start_step = df[step_column][0]
					# stop_step = df[step_column][len(df)-1]
					print "start_step, stop_step", start_step, stop_step
					df = df[df[step_column] >= start_step] 
					df = df[df[step_column] <= stop_step]
	
				# comlife = df.groupby([column_name, "step"]).aggregate({"node_id":[np.size]}).reset_index()
				# comlife.columns = [' '.join(col).strip() for col in comlife.columns.values]
				# gb = comlife.groupby(column_name)
				# life = gb.aggregate({"step":[np.size]})
				# life.columns = [' '.join(col).strip() for col in life.columns.values]
				# life = life[life["step size"] > 1]
				# mean_lifetime = np.mean(life)
				# plt.figure()
				# life.hist(bins=20, sharey=True)
				# plt.savefig(os.path.join(inputdir,title, str(folder), "histograms_lifetime.png"))
##
				df = df[[step_column, com_column, "timeMeanSpeed", "degree", "speed", "com_size"]]
				gb = df.groupby([com_column, step_column])
				communities = gb.aggregate({
					"degree":[np.mean, np.std, np.amin, np.amax],
					"speed":[np.mean, np.std, np.amin, np.amax], 
					"timeMeanSpeed":[np.mean, np.std, np.amin, np.amax], 
					"com_size":[np.size]}).reset_index()
				communities.columns = ['_'.join(col).strip() for col in communities.columns.values]
				gb = communities.groupby([com_column+"_"])
				data = gb.aggregate(np.mean)
				data["num_steps"] = gb.size()
				data = data.reset_index()

				comunities_longer_lifetime = data[data["num_steps"] > 1]
				comunities_second_lifetime = data[data["num_steps"] == 1]
				stats[title]['one_second_com'].append(len(comunities_second_lifetime))
				stats[title]['more_one_second_com'].append(len(comunities_longer_lifetime))
				comunities_with_one_vehicle = comunities_longer_lifetime[comunities_longer_lifetime["com_size_size"]==1]
				comunities_with_more_vehicles = comunities_longer_lifetime[comunities_longer_lifetime["com_size_size"]>1]
				stats[title]['more_one_second_com_singl'].append(len(comunities_with_one_vehicle))
				stats[title]['more_one_second_com_nosingl'].append(len(comunities_with_more_vehicles))

				data_selected = comunities_with_more_vehicles[["timeMeanSpeed_mean", "timeMeanSpeed_std", "com_size_size", "num_steps"]]
				means_selected = np.mean(data_selected)
				# mean_lifetime = means["num_steps"]
				stats_lifetime[title][str(folder)] = means_selected 
				data_all = data[["timeMeanSpeed_mean", "timeMeanSpeed_std", "com_size_size", "num_steps"]]
				means_all = np.mean(data_all)				
				stats_lifetime_all[title][str(folder)] = means_all
				
			print title, stats_lifetime[title]

			out.write("\nstats_lifetime\n")
			self.print_summary_df(stats_lifetime[title], out)
			out.write("\nstats_lifetime_all\n")
			self.print_summary_df(stats_lifetime_all[title], out)
			out.write("\nstats\n")

			self.print_summary_dict(stats[title], out)

	def print_summary_df(self, df, out):
		df_summary = pd.DataFrame()
		df_summary["min_val"] = df.min(axis=1)
		df_summary["mean_val"] = df.mean(axis=1)
		df_summary["max_val"] = df.max(axis=1)
		df_summary["std_val"] = df.std(axis=1)
		df_summary.to_csv(out, '\t')

	def print_summary_dict(self, dictionary, out):
		for name, values in dictionary.items():
			stats = "{0}\t{1}\t{2}\t{3}\t{4}\n".format(name, np.amin(values), np.mean(values), np.amax(values), np.std(values))
			out.write(stats)


	def get_color(self, title):
		color = random.choice(self.colors.values())
		if title in self.colors.keys():
			color = self.colors[title]
		if "hybrid" in title:
			color = 'g'
		elif "mobility" in title:
			color = 'b'
		elif "linkduration" in title:
			color = 'r'
		elif "nostab" in title:
			color = 'c'
		return color


	def get_label(self, title):
		label = title
		if title in self.labels:
			label = self.labels[title]

		if "hybrid" in title:
			label = 'Hybrid'
		elif "mobility" in title:
			label = 'Mobility'
		elif "linkduration" in title:
			label = 'Link duration'
		elif "nostab" in title:
			label = 'None'
		return label


	def floor_number(self, number, span):
		return (number // span) * span


	def plot_modularities(self, column_name, plot_title, titlenames,  inputdir, filename, start_step, stop_step):
		folders = range(0, self.no_runs)
		statnames = ["mean"]
		plt.rc('text', usetex=True)
		plt.rc('font', family='serif')
		fig = plt.figure()
		fig, ax = plt.subplots()
		for title in titlenames:	
			mod_df = pd.DataFrame()
			print title, mod_df.head()
			for folder in folders:
				filepath = os.path.join(inputdir, title, str(folder), filename)
				print filepath
				df = self.readFile(filepath)
				step_column = 'step'
				if start_step == None:
					start_step = df[step_column][0]
				if stop_step == None:
					stop_step = df[step_column][len(df)-1]
				mod_df[str(folder)] = df[column_name]
				if column_name == "weighted_com_modularity" and "nostab" in title:
					mod_df[str(folder)] = df["com_modularity"]
			df = pd.DataFrame()
			df["min_mod"] = mod_df.min(axis=1)
			df["mean_mod"] = mod_df.mean(axis=1)
			df["max_mod"] = mod_df.max(axis=1)
			df = df.ix[1:]
			y = df
			
			#smooth 
			smooth = True
			# smooth = False
			
			start = start_step 
			stop = stop_step
			if smooth:
				df['step2'] = self.floor_number(df.index, self.average_span)
				y = df.groupby('step2').aggregate(np.mean).reset_index()
				
				start = start_step / self.average_span
				stop = stop_step / self.average_span

			y = y.ix[start:stop]
			columns = ["min_mod", "mean_mod", "max_mod"]
			color = self.get_color(title)
			styles = [color+'--',color+'-',color+'--']
			linewidths = [1, 2, 1]
			for col, style, lw in zip(df.columns, styles, linewidths):
				if col == "mean_mod":
					label = self.get_label(title)
					y[col].plot(style=style, lw=lw, ax=ax, legend=True, label=label)
				else:
					y[col].plot(style=style, lw=lw, ax=ax, legend=False)
			# df.min_mod.plot(title=str("Min Modularity"), style='-',  legend=True)
			# df.mean_mod.plot(title=str(title), style={"c": "blue"}, legend=True)
			# df.max_mod.plot(title=str("Max Modularity"), style='-', legend=True)
		ax.set_ylabel(plot_title)
		ax.set_xlabel('Simulation time (min)')
		os.system("mkdir " + os.path.join(inputdir, "plots_"+str(len(titlenames))))
		plt.savefig(os.path.join(inputdir, "plots_"+str(len(titlenames)), "plot_"+column_name+".pdf"))	
	
	def get_axes_ranges(self, df, xlabel, ylabels):
	    xmin = 0
	    ymin = 0
	    xmax = 0
	    ymax = 0
	    x = max(df[xlabel])
	    if x > xmax:
	        xmax = x
	    for ylabel in ylabels:
	        y = max(df[ylabel])
	        if y > ymax:
	            ymax = y
	    return [xmin, xmax, ymin, ymax]

	def analyse_graph(self, filename, outputdir):
		df = self.readFile(filename)
		print df[:5]
		

		columns = ["nodes", "edges", "avg_degree"] #, "com_count", "max_com_size", "min_com_size", "std_com_dist"]
		if "avg_edge_weight" in df.columns:
			columns.append("avg_edge_weight")
		histdf = pd.DataFrame()
		for column in columns:
			plt.figure()
			df[column].plot()
			histdf[column] = df[column]
			plt.savefig(os.path.join(outputdir, "graph_"+column+".png"))



		histdf.mean().to_csv(os.path.join(outputdir,"graph_mean.csv"), sep='\t')
		histdf.sum().to_csv(os.path.join(outputdir,"graph_sum.csv"), sep='\t')
		histdf.min().to_csv(os.path.join(outputdir,"graph_min.csv"), sep='\t')
		histdf.max().to_csv(os.path.join(outputdir,"graph_max.csv"), sep='\t')
		histdf.std().to_csv(os.path.join(outputdir,"graph_std.csv"), sep='\t')
		
		plt.figure()
		histdf.plot()
		plt.savefig(os.path.join(outputdir, "graph_all.png"))

	def analyse_graphs(self, inputdir):
		folders = ["Highway", "Highway_congestion"]
		title = "higways"
		# folders = ["Manhattan"]
		# title = "Manhattan"
		# folders = ["Manhattan_avgspeed90"]
		# title = "Manhattan"
		columns = ["nodes", "edges", "avg_degree"] #, "com_count", "max_com_size", "min_com_size", "std_com_dist"]
		fig, axes = plt.subplots(nrows=len(columns), ncols=1, figsize=(12, 8))
		# check for egde weight column in df
		filename = os.path.join(inputdir, folders[0], "MobileSandSharc_oryg", "0", "crowds_graph.csv")
		df = self.readFile(filename)
		weight_column = "avg_edge_weight"
		if weight_column in df.columns:
			fig, axes = plt.subplots(nrows=len(columns)+2, ncols=1, figsize=(12, 12))
		

		for folder in folders:
			print "plotting " + folder
			filename = os.path.join(inputdir, folder, "MobileSandSharc_oryg", "0", "crowds_graph.csv")
			df = self.readFile(filename)
			
			histdf = pd.DataFrame()
			i = 0
			for i,column in enumerate(columns):
				df[column].plot(ax=axes[i], label=folder, legend=True)
				axes[i].set_ylabel(column)
			# read mobility similarity
			if weight_column in df:
				i += 1
				df[weight_column].plot(ax=axes[i], label=folder, legend=True)
				axes[i].set_ylabel("mobility_similarity")
			# read linkduration
			filename = os.path.join(inputdir, folder, "SandSharc_oryg", "0", "crowds_graph.csv")
			df = self.readFile(filename)
			if weight_column in df:
				i += 1
				df[weight_column].plot(ax=axes[i], label=folder, legend=True)
				axes[i].set_ylabel("link_duration")

		plt.savefig(os.path.join(inputdir, "graphs_compare_"+title+".png"))


# def analyseVehicles(self, self.df, column, outputDir):
# 	self.df1 = DataFrame({'number_of_steps' : self.df.groupby(['node_id','com_id']).size()}).reset_index()
# 	self.df2 = DataFrame({
# 		'number_of_communities' : self.df1.groupby(['node_id']).size(),
# 		'first_step': self.df.groupby(['node_id']).aggregate({"step":min})['step'],
# 		'avg_degree': self.df.groupby('node_id').aggregate({"degree":np.mean})["degree"],
# 		'avg_number_of_steps': self.df1.groupby(['node_id']).aggregate({'number_of_steps':np.mean})['number_of_steps'],
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
# 	out.write("Avg number of community changes per vehicle\t{0}\n".format(np.mean(self.df2['number_of_communities'])))
# 	return

