#!/bin/sh

module load matplotlib/1.2.1-goolf-1.4.10-Python-2.7.3
python /home/users/agrzybek/VNCBoston/crowds/python/analyse_crowds.py --inputFile "/work/users/agrzybek/VNCBoston/output/20131113/1/communities.csv" --outputDir "/work/users/agrzybek/VNCBoston/output/20131113/2/" --type communities --groupIndex 8 --groupType community