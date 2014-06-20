#! /usr/bin/env python
import os
import sys


jar="/Users/agatagrzybek/workspace/crowds/crowdz.jar"
jar="/Users/agatagrzybek/workspace/crowds/crowdz-instant.jar"
jar="/Users/agatagrzybek/workspace/crowds/crowdz-timemean.jar"
jar="/Users/agatagrzybek/workspace/crowds/crowdz-test.jar"
# dgsfilename="/Users/agatagrzybek/workspace/ovnis/scenarios/Kirchberg/sumoOutput-accident/400-400-200/Kirchberg-400-400-200.dgs"
# dgsfilename="/Users/agatagrzybek/workspace/ovnis/scenarios/Kirchberg/sumoOutput-accident/900-50-50/Kirchberg-accident-900-50-50-50PR.dgs"
# dgsfilename="/Users/agatagrzybek/workspace/ovnis/scenarios/Kirchberg/sumoOutput-accident/900-50-50/Kirchberg-accident-900-50-50-100PR.dgs"
dgsfilename="/Users/agatagrzybek/Dropbox/Agata/processed/vanet_probeData_v15-30+200_17032014.dgs"
inputdir="/Users/agatagrzybek/workspace/crowds/output/eclipse"
startstep=0
endstep=600
algorithm="MobileSandSharc"
congestion="CongestionMeasure"
congestion_threshold=5 # congestionSpeedThreshold=0 # congestionSpeedThreshold
history_length=10 # speedHistoryLength, speedHistoryLength
# speed_type = "instant"
# speed_type = "timemean"
speed_type = "spacetimemean"
# targetdir="/Users/agatagrzybek/www/vis/node-js-sample/public/data/Kirchberg/05042014-instant-900-50-50-50PR-emergence"
targetdir="/Users/agatagrzybek/www/vis/node-js-sample/public/data/Manhattan/10042014-emergence"
# targetdir="/Users/agatagrzybek/www/vis/node-js-sample/public/data/Kirchberg/01042014-900-50-50-50PR"
# targetdir="/Users/agatagrzybek/www/vis/node-js-sample/public/data/Kirchberg/06042014-900-50-50-50PR-emergence"
# targetdir="/Users/agatagrzybek/www/vis/node-js-sample/public/data/Kirchberg/05042014-test"

pythonscript="/Users/agatagrzybek/workspace/crowds/python/analyse_crowds.py"
pythonscript_analyse="/Users/agatagrzybek/Documents/PhD/Phd_workshop/fce-kirchberg/python/analyse.py"
# run crowds with different parameters
speed_history_lengths=range(90,100,20)
congestion_thresholds=range(11,12,2)
print "speed_history_lengths", speed_history_lengths
print "congestion_thresholds", congestion_thresholds

call = ""
for history_length in speed_history_lengths:
	for congestion_threshold in congestion_thresholds:
		dirname="avgspeed_thres{0}_hist{1}".format(congestion_threshold, history_length)
		outputdir = os.path.join(targetdir,dirname)
		os.system("mkdir {0}".format(outputdir))
		# run sim
		call = "java -Xmx4096m -jar {0} --inputFile \"{1}\" --outputDir {2}/ --startStep {3} --endStep {4} --algorithm {5} --congestion {6} --congestionSpeedThreshold {7} --speedHistoryLength {8} --speedType {9} > {2}/log.txt".format(jar, dgsfilename, outputdir, startstep, endstep, algorithm, congestion, congestion_threshold, history_length, speed_type)
		print call
		print ""
		os.system(call)
		# commnities_pandas
		command="communities_pandas"
		call = "python {0} --outputDir \"{1}\" --type {2}".format(pythonscript, outputdir, command)
		print call
		print ""
		os.system(call)
		# analyse communities
		#call = "python {0} --inputFile {1} --outputDir {2}".format(pythonscript_analyse, os.path.join(outputdir, "communities.csv"), outputdir)
		#print call
