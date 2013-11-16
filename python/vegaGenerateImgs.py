import os
import sys
from optparse import OptionParser
import fileinput
from scipy import spatial
from numpy import * 
from pylab import *
import re
from matplotlib.mlab import csv2rec
import json
from pprint import pprint

parser = OptionParser("usage: %prog [options]")
parser.add_option('--inputDir', help=("Input file"), type="string", dest='inputDir')
parser.add_option('--spec', help=(""), type="string", dest='spec')
parser.add_option('--vegaPath', help=("vegaPath"), type="string", dest='vegaPath')
parser.add_option('--fileType', help=("fileType"), type="string", dest='fileType')
parser.add_option('--outputDir', help=("Output dir name"), type="string", dest='outputDir')
(options, args) = parser.parse_args()
print options

# check set options
if not options.inputDir:
	print "Usage: generateImgs --inputDir <FILE> " 
	print "Exiting..."
	exit()


def getFiles(dirName):
	files = []
	for dirname, dirnames, filenames in os.walk(dirName):
		print "os {0} {1} {2}".format(dirname, dirnames, filenames);
		for filename in filenames:
			if filename != '.DS_Store' and "groups" in filename and ".tsv" in filename:
				files.append(filename)
	return files

# --------------

vegaPathToSvg = "/Users/agatagrzybek/PhD/Phd_workshop/vega/bin/vg2svg"
vegaPathToPng = "/Users/agatagrzybek/PhD/Phd_workshop/vega/bin/vg2png"
vegaPath = vegaPathToPng
if options.vegaPath:
	vegaPath = options.vegaPath
specPath = options.spec
ext = ".png"
if options.fileType:
	ext = options.fileType
files = getFiles(options.inputDir)
if ext == ".svg":
	vegaPath = vegaPathToSvg
if options.vegaPath:
	vegaPath = options.vegaPath

i = 0
add_nulls = lambda number, zero_count : "{0:0{1}d}".format(number, zero_count)
files.sort()

for fileName in files:
	# "data/" +		
	# urlPath = options.inputDir + "/" + fileName
	urlPath = fileName
	# print "options.inputDir " + options.inputDir
	# print "fileName " + fileName
	# print "urlPath" + urlPath
	# os.system("cd "+ options.inputDir )
	# modify json for new url
	spec = {}
	with open(specPath) as data_file:    
		spec = json.load(data_file)
		dataSets = spec["data"]
		for dataSet in dataSets:
			print "Modifying spec file {0} with data url {1}".format(specPath, urlPath)
			dataSet["url"] = urlPath 
	with open(specPath, 'w') as data_file:
		json.dump(spec, data_file)
	outputFilePath = options.outputDir + "img_" + add_nulls(i,4) + ext
	print "{0} {1} {2}".format(vegaPath,specPath ,outputFilePath)
	callVega = vegaPath + " --base file://" + options.inputDir + " "  +  specPath + " " + outputFilePath
	print "Generating img {0}".format(outputFilePath)
	os.system(callVega)
	i += 1