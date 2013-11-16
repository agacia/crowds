#!/bin/sh

#oarsub -l core=1,walltime=12 'java -Xmx4096m -jar /home/users/agrzybek/VNCBoston/crowds/crowds.jar --inputFile "/work/users/agrzybek/VNCBoston/data/fcd_0-30_full.dgs" --outputDir /work/users/agrzybek/VNCBoston/output/10102013/2/Leung/ --delta 0.05 --numberOfIterations 1 --startStep 0 --endStep 1200 --algorithm Leung'


oarsub -l core=1,walltime=12 'java -Xmx4096m -jar /home/users/agrzybek/VNCBoston/crowds/crowds.jar --inputFile "/work/users/agrzybek/VNCBoston/data/fcd_0-30_full.dgs" --outputDir /work/users/agrzybek/VNCBoston/output/shortest/ --delta 0.05 --numberOfIterations 1 --startStep 0 --endStep 1200 --algorithm Leung --goal ASPL'

