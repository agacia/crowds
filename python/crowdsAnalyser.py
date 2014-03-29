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
		self.startIndex = 	0
		self.indexStep = 1
		self.postfixLenghth = 1

	def readFile(self, filename):
		print "Reading file {0}".format(filename)
		self.filename = filename  
		self.df = pd.read_csv(filename, sep="\t")
		print("Reading file", filename)
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
		print "gb", gb
		# self.df["congested"] = self.moreThan(gb["num_stops"], 1)
		self.communities = gb.aggregate({
			"degree":[np.mean, np.std, np.amin, np.amax],
			"speed":[np.mean, np.std, np.amin, np.amax], 
			"timeMeanSpeed":[np.mean, np.std, np.amin, np.amax], 
			"num_stops":[sum, np.mean, np.amin, np.amax],
			"x":[np.min, np.max], "y":[np.min, np.max],
			# "congested":[sum],
			"com_size":[np.size]}).reset_index()

		print  self.communities.columns

		self.communities["congested"] = self.communities["num_stops"]["sum"]
		print ("self.df.len", len(self.df))
		# add column with community range calculated as distance between min position of a vehicle and maximum position of vehicle 
		self.communities["range"] = self.calculateDistance(self.communities.x.amin,self.communities.y.amin,self.communities.x.amax,self.communities.y.amax)
		# print (self.communities["timeMeanSpeed"])
		# print ("gb.len", len(gb), "self.communities.len", len(self.communities))
		#self.communities["num_stops_count"] = self.moreThan(gb["num_stops"], 1)
		self.communities["num_stops_count"] = self.communities["range"]
		# print("num_stops_count", self.communities["num_stops_count"])
		# for name,group in gb:
		# 	num_stops_count = 0
		# 	series = group["num_stops"]
		# 	print "----"
		# 	print group["num_stops"]
		# 	lala = np.nonzero(group["num_stops"])
		# 	print (lala[0], len(lala[0]))

	def analyseSteps(self):
		print "df",self.df
		gb = self.df.groupby(['step', 'com_id'])
		print "gb",gb
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


	def analyseVehicles(self, vehicleColumn, dirname):
		# group by step and com_id to get statistics for each community at every step
		gb = self.df.groupby(vehicleColumn)
		self.vehicles = gb.aggregate({
			"degree":[np.size, np.mean, np.std, np.min, np.max],
			"speed":[np.mean, np.std, np.min, np.max], 
			"timeMeanSpeed":[np.mean, np.std, np.min, np.max], 
			"num_stops":[np.mean, np.std, np.min, np.max],
			"x":[np.min, np.max], "y":[np.min, np.max],
			"com_size":[np.min, np.max, np.mean, np.std]}).reset_index()
		# print "===="
		# print "vehicles"
		# print self.vehicles.head();

		gb2 = self.df.groupby([vehicleColumn,"com_id"])
		self.vehiclesCom = gb2.aggregate({
			"com_size":[np.size, np.mean]}).reset_index()
		# print "===="
		# print "vehiclesCom"
		# print self.vehiclesCom.head(23);
		print self.vehiclesCom.columns.values
		self.vehiclesCom.columns = [' '.join(col).strip() for col in self.vehiclesCom.columns.values]
		# print self.vehiclesCom.head(23);

		# print self.vehiclesCom
		# print self.vehiclesCom["com_size"]["mean"]
		gb3 = self.vehiclesCom.groupby(vehicleColumn)

		self.vehiclesComSum = gb3.aggregate({
			"com_id":[np.size],
			"com_size size":[np.mean, np.sum], # avg number of steps in a community , total number of steps of a vehicle
			"com_size mean":[np.mean] # avg size of a community
		})
		self.vehiclesComSum.columns = [ "avg_steps_in_com", "steps", "avg_com_size", "num_changes" ]

		# self.vehiclesComSum2 = gb3.aggregate({
		# 	"com_size":[np.mean]
		# })
		print "===="
		print "vehiclesComSum", self.vehiclesComSum.head()
		# print self.vehiclesComSum.head(23);
		
		plt.figure()
		self.vehiclesComSum.num_changes.hist(bins=300)
		# plt.show()
		plt.savefig(os.path.join(dirname, "hist-numb-changes.png"))

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

