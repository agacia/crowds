import matplotlib
matplotlib.use('Agg') # headless mode
import matplotlib.pyplot as plt
import numpy as np
import time
from matplotlib.mlab import csv2rec
import math
from math import exp, pow
import pandas as pd

def readFile(filename, sep1, sep2):
	dataFile = open(filename, 'r')
	rows = dataFile.read().split(sep1)
	dataFile.close()
	columns = []
	for row in rows:
		rowElems = row.split(sep2)
		if len(columns) == 0:
			for rowElem in rowElems:
				columns.append([])
		for i in range(0,len(rowElems)):
			columns[i].append(int(rowElems[i]))
	return columns

def readCVSFile(filename, sep, names = None):
	columns = csv2rec(filename, delimiter=sep, names=None)
	return columns

# ------------------

filename = "sample.cvs"
filename = "communities.csv"
vehicle_id=22504
link_id=1379
# vehicle_id=69950
# vehicle_id=65056
# data = readCVSFile(filename, '\t', None)
df = pd.read_csv(filename, sep="\t")

# df = df[df['node_id']==vehicle_id]
df = df[df['link_id']==link_id]
x = df["step"]
y1 = df["timeMeanSpeed"]
y2 = df["speed"]
# y3 = df["avg_speed"]
title = "Mean travel time and speed of a vehicle {0}".format(vehicle_id)
xtitle = 'time'
ytitle = 'speed'

# -----
# fig = plt.figure()
# # rect = fig.patch
# # rect.set_facecolor('black')
# ax1 = fig.add_subplot(1,1,1, axisbg='white') # height, width, chart # 

# ax1.plot(x, y2, 'r', linewidth=1, label="Speed")
# # ax1.set_title("Speed")
# ax1.set_xlabel("Time")
# ax1.set_ylabel("Speed")

# # ax1.plot(x, y3, 'b', linewidth=1, label="Avg speed")
# # ax2 = fig.add_subplot(2,1,2, axisbg='white') # height, width, chart # 
# ax1.plot(x, y1, 'g', linewidth=2, label="Time mean speed")
# # ax2.set_title("Time mean speed")
# ax1.set_xlabel("Time")
# ax1.set_ylabel("Time mean speed")
# plt.legend(loc='best')
# # color of ticks for axes
# # ax1.tick_params(axis='x', color='c') # color -> only ticks, colors -> also text
# # ax1.tick_params(axis='x', colors='c')
# # ax1.tick_params(axis='y', colors='c')
# # ax1.spines['bottom'].set_color('w')
# # ax1.spines['top'].set_color('w')
# # ax1.spines['left'].set_color('w')
# # ax1.spines['right'].set_color('w')
# # ax1.xaxis.label.set_color('c')
# # ax1.yaxis.label.set_color('c')
# plt.show()
# outputfile = xtitle + "-" + ytitle + "_" + str(vehicle_id) + ".png"
# plt.savefig(outputfile)		

#-------------
colors = ['c','b','r','y','g','m']
# grouped = df.groupby("link_id")
grouped = df.groupby("node_id")
fig = plt.figure()
plt.figure(1)
print "num of groups:",len(grouped)
for i,value in enumerate(grouped):
	name,group = value
	# print name,group["timeMeanSpeed"]
	color = colors[i%len(colors)]
	print i, name, color
	plt.scatter(group["step"], group["timeMeanSpeed"], s=20, c=color, marker='.', label=name, lw = 0)
	for i, value in enumerate(zip(group["maxHistoryRecords"],group["step"],group["timeMeanSpeed"])):
		# print str(value)#[i],group["timeMeanSpeed"][i])
		plt.annotate(value[0] if value[0]!=-1 else "", (value[1],value[2]))
outputfile = xtitle + "-" + ytitle + "_scatterplot_vehicle_" + str(vehicle_id) + ".png"
outputfile = xtitle + "-" + ytitle + "_scatterplot_link_" + str(link_id) + ".png"
plt.savefig(outputfile)	
