import os
import sys
from optparse import OptionParser
import fileinput
from scipy import spatial
from numpy import * 
from pylab import *
import re
from matplotlib.mlab import csv2rec

parser = OptionParser("usage: %prog [options]")
parser.add_option('--inputFile', help=("Input file"), type="string", dest='inputFile')
parser.add_option('--groupIndex', help=("Column Index in tsv file of group"), type="int", dest='groupIndex')
parser.add_option('--outputDir', help=("Output dir name"), type="string", dest='outputDir')
parser.add_option('--type', help=("Group type"), type="string", dest='type')
parser.add_option('--samplingRate', help=("Sampling rate"), type="int", dest='samplingRate')
parser.add_option('--delta_threshold', help=("Delta threshold"), type="int", dest='delta_threshold')
parser.add_option('--maxSizeCommunityId', help=("maxSizeCommunityId"), type="string", dest='maxSizeCommunityId')
parser.add_option('--maxLifetimeCommunityId', help=("maxLifetimeCommunityId"), type="string", dest='maxLifetimeCommunityId')
(options, args) = parser.parse_args()
print options

# check set options
if not options.inputFile:
	print "Usage: getGroups --inputFile <FILE> " 
	print "Exiting..."
	exit()
if options.samplingRate:
	samplingRate = options.samplingRate
else:
	samplingRate = 10
	
# ---------------------------------
add_nulls = lambda number, zero_count : "{0:0{1}d}".format(number, zero_count)

def processLine(line):	
	global step
	global globalStep
	global communities
	global vehiclesCount
	global currentVehiclesCount
	global outputGroupFile
	global outGroups
	global colors
	global colorIndex
	global selectedStep 
	global samplingRate 
	global ymax
	startstep = -1
	data = line.split(separator)
	now = int(data[0])
	# if len(data) != 12:
	# 	print "Warning! Skipping wrong line {1}, {0}".format(len(data),line)
	# 	return
	if step >= startstep: 
		if (now != step):
			if step == -1:
				globalStep += now
			else:
				globalStep += 1
			step = now
			if globalStep%samplingRate == 0:
				print "starting writing step {0}".format(globalStep)
				filename = outputGroupFile+add_nulls(globalStep,4)+".tsv"
				outGroups = open(filename, 'w')
				# input: step	node_id	x	y	degree  neighbors	cc_id	cc_size	com_id	com_score	com_size 	speed
				outGroups.write("step\tid\tx\ty\tdegree\tcom_id\tcos_score\tcom_size\tcc_size\tmetadata\tcolor\tspeed\tnum_stops\n")
			currentVehiclesCount = 0

		# add data
		vehiclesCount += 1
		currentVehiclesCount += 1
		# step	node id	x	y	degree	neighbors	community id	community score	community size	connected component id
		step = float(data[0])
		vehicleId = str(data[1])	
		x = float(data[2])
		y = float(data[3])	
		degree = float(data[4])
		# neighbors = str(data[5]).split(separator2)
		currentVehiclesCount += 1
		communityId = str(data[8]).strip()
		communityScore = 0
		if options.type == "community":
			communityScore = float(data[9])
		comSize = float(data[10])
		ccSize = float(data[7])
		speed = float(data[12])
		numOfStops = float(data[13])
		# size = 30
		if (communityId not in communities.keys()): 
			colorIndex += 1
			communityIndex[communityId] = colorIndex
		if degree > 0:
			communities[communityId] = colors[communityIndex[communityId]%len(colors)]
		else:
			communities[communityId] = "#707070"

		# size = 10
		if globalStep%samplingRate == 0:
			metadata = "regular"
			if  options.delta_threshold and options.delta_threshold != -1 and communityScore <= options.delta_threshold:
				metadata = "cut"
			if options.maxSizeCommunityId and options.maxSizeCommunityId != -1 and communityId == options.maxSizeCommunityId:
				metadata = "maxSize"
			if options.maxLifetimeCommunityId and options.maxLifetimeCommunityId != -1 and communityId == options.maxLifetimeCommunityId:
				metadata = "maxLifetime"
				
			# y = ymax-y
			outGroups.write("{0}\t{1}\t{2}\t{3}\t{4}\t{5}\t{6}\t{7}\t{8}\t{9}\t{10}\t{11}\t{12}\n".format(
				step,vehicleId,x,y,degree,communityId,communityScore,comSize,ccSize,metadata,communities[communityId],speed, numOfStops))
			# print "{0}\t{1}\t{2}\t{3}\t{4}\t{5}\t{6}\t{7}\n".format(vehicleId,x,y,communityId,communityScore,communities[communityId],size,metadata)
			# print line
	return 0

def getIndexes(line):
	global indexes
	indexes = line.split(separator)
	return indexes

#----------------------------------
colors = ["#707070","#F400A1", "#E5AA70", "#4D5D53", "#E0B582", "#4F7942", "#FF2800", "#6C541E", "#B22222", "#CE2029", "#E25822", "#FC8EAC", "#664423", "#F7E98E", "#EEDC82", "#A2006D", "#FFFAF0", "#FFBF00", "#FF1493", "#CCFF00", "#FF004F", "#014421", "#228B22", "#A67B5B", "#856D4D", "#0072BB", "#86608E", "#9EFD38", "#D473D4", "#FD6C9E", "#4E1609", "#C72C48", "#F64A8A", "#77B5FE", "#8806CE", "#AC1E44", "#A6E7FF", "#FF00FF", "#C154C1", "#FF77FF", "#C74375", "#E48400", "#CC6666", "#D71868", "#85BB65", "#664C28", "#967117", "#00009C", "#E5CCC9", "#EFDFBB", "#E1A95F", "#555D50", "#C2B280", "#1B1B1B", "#614051", "#F0EAD6", "#1034A6", "#7DF9FF", "#FF003F", "#00FFFF", "#00FF00", "#6F00FF", "#F4BBFF", "#CCFF00", "#BF00FF", "#3F00FF", "#8F00FF", "#FFFF00", "#50C878", "#6C3082", "#1B4D3E", "#B48395", "#AB4E52", "#563C5C", "#96C8A2", "#44D7A8", "#C19A6B", "#801818", "#B53389", "#DE5285", "#232B2B", "#E68FAC", "#DFFF00", "#7FFF00", "#DE3163", "#FFB7C5", "#904535", "#DE6FA1", "#A8516E", "#AA381E", "#856088", "#7B3F00", "#D2691E", "#FFA700", "#98817B", "#E34234", "#D2691E", "#E4D00A", "#9EA91F", "#7F1734", "#FBCCE7", "#030507", "#0047AB", "#D2691E", "#965A3E", "#6F4E37", "#9BDDFF", "#F88379", "#002E63", "#8C92AC", "#B87333", "#DA8A67", "#AD6F69", "#CB6D51", "#996666", "#FF3800", "#FF7F50", "#F88379", "#FF4040", "#893F45", "#FBEC5D", "#B31B1B", "#6495ED", "#FFF8DC", "#FFF8E7", "#FFBCD9", "#FFFDD0", "#DC143C", "#BE0032", "#00FFFF", "#00B7EB", "#58427C", "#FFD300", "#FFFF31", "#F0E130", "#00008B", "#666699", "#654321", "#5D3954", "#A40000", "#08457E", "#986960", "#CD5B45", "#008B8B", "#536878", "#B8860B", "#A9A9A9", "#013220", "#00416A", "#1A2421", "#BDB76B", "#483C32", "#734F96", "#534B4F", "#543D37", "#8B008B", "#003366", "#4A5D23", "#556B2F", "#FF8C00", "#9932CC", "#779ECB", "#03C03C", "#966FD6", "#C23B22", "#E75480", "#003399", "#4F3A3C", "#872657", "#8B0000", "#E9967A", "#560319", "#8FBC8F", "#3C1414", "#8CBED6", "#483D8B", "#2F4F4F", "#177245", "#918151", "#FFA812", "#483C32", "#CC4E5C", "#00CED1", "#D1BEA8", "#9400D3", "#9B870C", "#00703C", "#555555", "#D70A53", "#A9203E", "#EF3038", "#E9692C", "#DA3287", "#FAD6A5", "#B94E48", "#704241", "#C154C1", "#004B49", "#F5C74C", "#9955BB", "#CC00CC", "#D473D4", "#355E3B", "#FFCBA4", "#FF1493", "#A95C68", "#843F5B", "#FF9933", "#00BFFF", "#4A646C", "#7E5E60", "#66424D", "#BA8759", "#1560BD", "#C19A6B", "#EDC9AF", "#EA3C53", "#B9F2FF", "#696969", "#9B7653", "#1E90FF", "#92A1CF", "#ACE1AF", "#007BA7", "#2F847C", "#B2FFFF", "#4997D0", "#DE3163", "#EC3B83", "#007BA7", "#2A52BE", "#6D9BC3", "#007AA5", "#E03C31", "#A0785A", "#F7E7CE", "#36454F", "#A8BB19", "#7CB9E8", "#C9FFE5", "#B284BE", "#5D8AA8", "#00308F", "#72A0C1", "#AF002A", "#F0F8FF", "#E32636", "#C46210", "#EFDECD", "#E52B50", "#D69CBB", "#3B7A57", "#FFBF00", "#FF7E00", "#FF033E", "#9966CC", "#A4C639", "#F2F3F4", "#CD9575", "#665D1E", "#915C83", "#841B2D", "#FAEBD7", "#008000", "#8DB600", "#FBCEB1", "#00FFFF", "#7FFFD4", "#4B5320", "#3B444B", "#8F9779", "#E9D66B", "#B2BEB5", "#87A96B", "#FF9966", "#A52A2A", "#FDEE00", "#6E7F80", "#568203", "#007FFF", "#F0FFFF", "#89CFF0", "#A1CAF1", "#F4C2C2", "#FEFEFA", "#FF91AF", "#21ABCD", "#FAE7B5", "#FFE135", "#E0218A", "#7C0A02", "#848482", "#98777B", "#BCD4E6", "#9F8170", "#F5F5DC", "#2E5894", "#9C2542", "#FFE4C4", "#3D2B1F", "#967117", "#CAE00D", "#648C11", "#FE6F5E", "#BF4F51", "#000000", "#3D0C02", "#253529", "#3B3C36", "#FFEBCD", "#A57164", "#318CE7", "#ACE5EE", "#FAF0BE", "#0000FF", "#1F75FE", "#0093AF", "#0087BD", "#333399", "#0247FE", "#A2A2D0", "#6699CC", "#0D98BA", "#126180", "#8A2BE2", "#5072A7", "#4F86F7", "#1C1CF0", "#DE5D83", "#79443B", "#0095B6", "#E3DAC9", "#CC0000", "#006A4E", "#873260", "#0070FF", "#B5A642", "#CB4154", "#1DACD6", "#66FF00", "#BF94E4", "#C32148", "#1974D2", "#FF007F", "#08E8DE", "#D19FE8", "#F4BBFF", "#FF55A3", "#FB607F", "#004225", "#CD7F32", "#737000", "#964B00", "#A52A2A", "#664423", "#1B4D3E", "#FFC1CC", "#E7FEFF", "#F0DC82", "#7BB661", "#480607", "#800020", "#DEB887", "#CC5500", "#E97451", "#8A3324", "#BD33A4", "#702963", "#536872", "#5F9EA0", "#91A3B0", "#006B3C", "#ED872D", "#E30022", "#FFF600", "#A67B5B", "#4B3621", "#1E4D2B", "#A3C1AD", "#C19A6B", "#EFBBCC", "#78866B", "#FFEF00", "#FF0800", "#E4717A", "#00BFFF", "#592720", "#C41E3A", "#00CC99", "#960018", "#D70040", "#EB4C42", "#FF0038", "#FFA6C9", "#B31B1B", "#99BADD", "#ED9121", "#00563F", "#062A78", "#703642", "#C95A49"]

communityIndex = {}
communities = {}
vehiclesCount = 0
currentVehiclesCount = 0
step = -1
globalStep = 0
separator = '\t'
separator2 = ','
outputGroupFile = os.path.join(options.outputDir, "groups_")
colorIndex = 0
indexes = []
i = 0
step = -1
selectedStep = 3600


print "Reading file {0}".format(options.inputFile)

#data = np.genfromtxt(options.inputFile, dtype=None, delimiter=separator, names=True)
# data = csv2rec(options.inputFile, delimiter=separator)
#ymax = amax(data['y'])

for line in fileinput.input(options.inputFile):
	if i == 0:
		getIndexes(line)
	if i > 0:
		processLine(line)
	i += 1

# summariseAnalysis(2,2)

# def findColors():
# 	text = '#A8BB19 '
# 	regex = '#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})'
# 	m = re.findall(regex, text)
# 	print m




