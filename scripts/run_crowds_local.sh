
#!/bin/bash

java -Xmx4096m -jar /Users/agatagrzybek/GraphStreamWorkspace/crovds/crovds.jar \
--inputFile "/Users/agatagrzybek/Google Drive/PhD/workshop/sumo_scenarios/Luxembourg_6-8/fcd2vanet/graph_10s.dgs" \
--outputDir "output/Leung" \
--delta 0.05 \
--numberOfIterations 1 \
--startStep 0 \
--endStep 10 \
--algorithm "Leung" > output/Leung/log.txt 


