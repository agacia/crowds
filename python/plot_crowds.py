import os
import sys
from optparse import OptionParser
import fileinput
from scipy import spatial
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from matplotlib.mlab import csv2rec
import re
from pylab import *
import numpy
from numpy import * 
from operator import itemgetter, attrgetter
from matplotlib.finance import candlestick

parser = OptionParser("usage: %prog [options]")
parser.add_option('--inputFile', help=("Input file"), type="string", dest='inputFile')
parser.add_option('--filename', help=("File name to calculate aggregate"), type="string", dest='filename')
parser.add_option('--title', help=("Title"), type="string", dest='title')
parser.add_option('--inputFiles', help=("Input files"), type="string", dest='inputFiles')
parser.add_option('--labels', help=("Label titles"), type="string", dest='labelTitles')
parser.add_option('--outputDir', help=("Output dir"), type="string", dest='outputDir')
parser.add_option('--inputDir', help=("Input dir"), type="string", dest='inputDir')
parser.add_option('--columns', help=("Columns for average"), type="string", dest='columns')
parser.add_option('--type', help=("Type"), type="string", dest='type')

(options, args) = parser.parse_args()

print "plot {0}".format(options)

#----------------

linestyles = ['-.', '-', '--', ':']
colors = ('b', 'g', 'r', 'c', 'm', 'y', 'k')
# markers = []
# for m in Line2D.markers:
#     try:
#         if len(m) == 1 and m != ' ':
#             markers.append(m)
#     except TypeError:
#         pass
# styles = markers + [
#     r'$\lambda$',
#     r'$\bowtie$',
#     r'$\circlearrowleft$',
#     r'$\clubsuit$',
#     r'$\checkmark$']
styles = [
	'o',
	'v',
	'^',
	'1',
    r'$\lambda$',
    r'$\bowtie$',
    r'$\circlearrowleft$',
    r'$\clubsuit$',
    r'$\checkmark$']

def getFiles(dirName, ext):
	files = []
	for dirname, dirnames, filenames in os.walk(dirName):
		for filename in filenames:
			if filename != '.DS_Store':
				if filename.endswith(ext):
					files.append(dirname +"/"+ filename)
	return files

def getDirs(dirName, ext):
	files = []
	for dirname, dirnames, filenames in os.walk(dirName):
		for filename in filenames:
			if filename != '.DS_Store':
				if filename.endswith(ext):
					files.append(dirname +"/")
	return files


def plotComparison(plottedColumn, files):
	mpl.rcParams['figure.figsize'] = 10, 5
	mpl.rcParams['font.size'] = 12
	fig, ax = plt.subplots()
	plt.grid(True, which = 'both')

	axisNum = 0
	for fileName in files:
		# print "Parsing {0}".format(fileName)
		input = open(fileName, 'r')
		input.close()
		data = csv2rec(fileName, delimiter=delimiterStr)
		#"column\taverage\tmin\tmax\n")
		columns = data['column']
		averages = data['average']
		mins = data['min']
		maxs = data['max']
		index = 0
		for column in columns:
			if column == plottedColumn:
				break
			index += 1
		yStr = averages[index]
		yStr = re.sub(r'[\[\]]', '', yStr)
		y = yStr.split(',')
		x = range(len(y))
		axisNum += 1
		color = colors[axisNum % len(colors)]
		linestyle = linestyles[axisNum % len(linestyles)]
		# print "printing {0} {1} {2} {3} ".format(len(y), legendTitles[axisNum-1], color, linestyle)
		ax.plot(x, y, linestyle=linestyle, color=color, linewidth=2.0, markersize=10, label="{0}".format(legendTitles[axisNum-1]))	
	legend = ax.legend(loc='best')
	legend.get_frame().set_alpha(0.5)
	for label in legend.get_texts():
	    label.set_fontsize('large')
	for label in legend.get_lines():
	    label.set_linewidth(2)  # the legend line width
	if  plottedColumn == "gs_average_community_size":
		ytitle = "Average community size"
	if plottedColumn == "gs_max_community_size":
		ytitle = "Maximum community size"
	if plottedColumn == "com_modularity":
		ytitle = "Average modularity"
	plt.ylabel(ytitle)
	plt.xlabel(xtitle)
	fig.autofmt_xdate(bottom=0.2, rotation=0, ha='left')
	outputfile = options.outputDir + "commare_" + plottedColumn + ".png"
	plt.savefig(outputfile)



def plotMultipleSeries(inputData, xColumn, plottedColumnsSingle, plottedColumnMultiple, legends, xtitle, ytitle, seriesType = "line", sortColumn=None):
	if len(inputData) < 1:
		print "No data to plot"
		return

	mpl.rcParams['figure.figsize'] = 10, 5
	mpl.rcParams['font.size'] = 12
	fig, ax = plt.subplots()
	plt.grid(True, which = 'both')

	if sortColumn:
		for label,data in inputData.items():
			inputData[label] = numpy.sort(inputData[label], order=sortColumn)

	#read x column	
	label = inputData.keys()[0]
	data = inputData[label]
	x = data[xColumn]
			
	# read single columns
	axis = {}
	# print "Reading single data from any data set, e.g. {0}".format(label)
	for column in plottedColumnsSingle:
		axis[legends[column]] = data[column]

	for column in plottedColumnMultiple:
		for label,data in inputData.items():
			# print "label " + label
			label = "{0}{1}".format(legends[column], label)
			label = re.sub(r'[\[\]]', '', label)
			axis[label] = data[column]
	
	# read multiple columns for each given label (data from different files)
	axisNum = 0		
	for label,y in axis.items():
		color = colors[axisNum % len(colors)]
		if seriesType == "line":
			linestyle = linestyles[axisNum % len(linestyles)]
			print "printing line {0} {1} {2}".format(label, color, linestyle)
			ax.plot(x, y, linestyle=linestyle, color=color, linewidth=2.0, markersize=10, label="{0}".format(label))	
			legend = ax.legend(loc='best')
			legend.get_frame().set_alpha(0.5)
			for label in legend.get_texts():
			    label.set_fontsize('large')
			for label in legend.get_lines():
			    label.set_linewidth(2) 
		if seriesType == "scatterplot":
			print "printing scatterplot {0} {1}".format(label, color)
			style = styles[axisNum % len(styles)]
			# s=data['steps_count']**2
			ax.scatter(x, y, c=color, alpha=0.5, marker = style)
		axisNum += 1
	
	
	
	plt.ylabel(ytitle)
	plt.xlabel(xtitle)
	fig.autofmt_xdate(bottom=0.2, rotation=0, ha='left')
	outputfile = options.outputDir + xtitle + "-" + ytitle + ".png"
	plt.savefig(outputfile)

	
add_nulls = lambda number, zero_count : "{0:0{1}d}".format(number, zero_count)

def fixTabs(filename):
	file = open(filename, 'r')
	content = file.read()
	file.close()
	# print content
	content = re.sub(r"[\t]+", "\t", content)
	file = open(filename, 'w')
	file.write(content)
	file.close()
	
def readData(files, labels):
	data = {}
	if len(files) != len(labels):
		print "ERROR. Input files: {0}, inpute labels: {1}".format(len(files), len(labels))
		return data
	# find name of data set
	dataSetName = ""
	for label in labels:
		# print "Looking for {0}".format(label)
		label = label.strip()
		if (label != ""):
			for fileName in files:
				# print "In {0}".format(fileName)
				if label in fileName:
					dataSetName = re.sub(r'/', '', label)
					if dataSetName == "EpidemicCommunityAlgorithm":
						dataSetName = "Epidemic"
					break
			fixTabs(fileName)
			data[dataSetName] = csv2rec(fileName, delimiter=delimiterStr)
	return data

def calculateAverages(inputData, columns, outputfile):
	# print "calculateAverages " + str(len(inputData))
	if len(inputData) > 0:
		sumData = inputData[inputData.keys()[0]]
		count = 1
		for label,data in inputData.items():
			for column in columns:
				# print "sumData[column]"
				# print sumData[column]
				sumData[column] += data[column]
				# print "sumData[column]"
				# print sumData[column]
			count += 1
		# print "sumData:"
		# print sumData	
		for column in columns:
			sumData[column] = sumData[column]/count
		# print "avgData"
		# print sumData
		file = open(outputfile, 'w')
		file.close()
		# print "outputfile" + outputfile
		# names = delimiterStr.join(sumData.dtype.names)
		# names = np.array(sumData.dtype.names)
		# print names
		# print sumData
		# print len(names)
		# print len(sumData)
		# print np.insert(sumData, 1, names, 0)  
		
		np.savetxt(outputfile, sumData, delimiter=delimiterStr, fmt="%s")
		file = open(outputfile, 'r')
		content = file.read()
		file.close()
		# print content
		# content = re.sub(r"[# ]+", "", content)
		file = open(outputfile, 'w')
		names = delimiterStr.join(sumData.dtype.names) + "\n"
		file.write(names)
		file.write(content)
		file.close()
		#print "average file saved to outputfile " + outputfile


# ---------

ytitle = "Value"
xtitle = "Step"
globalStep = -1
delimiterStr = '\t'		

if options.inputDir:

	modularityOfIteration = []
	sumModularityAtOfIteration = []
	avgModularityAtSteps = []
	maxModularity = 0
	plottedStepsStart = 0
	plottedStepsStop = 1200

	ext = "community_stats.tsv"
	files = getFiles(options.inputDir, ext)
	files.sort()
	
	legendTitles = ['delta=0.05', 'delta=0.25', 'delta=0.5']
	# plotComparison("gs_average_community_size", files)
	# plotComparison("gs_max_community_size", files)
	plotComparison("com_modularity", files)

if options.inputFiles:
	
	inputFiles = options.inputFiles.split(' ')
	inputLabels = options.labelTitles.split(' ')
	inputData = readData(inputFiles, inputLabels)

	# graph.txt
	if options.type=="graph":
		plotMultipleSeries(inputData, "step", ["nodes", "singletons", "connected"], [], 
			{"nodes":"Vehicles", "singletons":"Singletons", "connected": "Connected"}, "Simulation step", "Number of vehicles" )
		plotMultipleSeries(inputData, "step", ["nodes", "singletons", "connected"], ["com_count", "cc_count"], 
			{"nodes":"Vehicles", "singletons":"Singletons", "connected": "Connected", "com_count" : "Communities - ", "cc_count" : "CC - " }, "Simulation step", "Number" )
		plotMultipleSeries(inputData, "step", [], ["com_count"], {"com_count" : ""}, "Simulation step", "Number of communities" )
		plotMultipleSeries(inputData, "step", [], ["max_com_size"], {"max_com_size" : ""}, "Simulation step", "Maximum community size" )
		plotMultipleSeries(inputData, "step", [], ["avg_com_size"], {"avg_com_size" : ""}, "Simulation step", "Average community size" )
		plotMultipleSeries(inputData, "step", ["avg_degree"], ["avg_com_size", "avg_cc_size"], {"avg_degree" : "Average degree", "avg_com_size" : "Community size - ", "avg_cc_size" : "CC size - "}, "Simulation step", "Degree and number" )

	# vehicleanalysis.tsv
	if options.type == "vehicles":
		plotMultipleSeries(inputData, "start_step", [], ["changes_community"], {"changes_community":""}, "Simulation step", "Changes of community", "scatterplot", "start_step" )
		plotMultipleSeries(inputData, "start_step", [], ["stabilitycom"], {"stabilitycom":""}, "Simulation step", "Stability", "scatterplot", "start_step" )
		plotMultipleSeries(inputData, "start_step", [], ["percent_in_community"], {"percent_in_community":""}, "Simulation step", "percent in community", "scatterplot", "start_step" )
	
	if options.type == "average":
		calculateAverages(inputData, options.columns.split(' '), options.outputDir+options.filename)


