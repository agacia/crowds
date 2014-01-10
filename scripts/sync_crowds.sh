# fcd scripts
rsync -avzu /Users/agatagrzybek/workspace/crowds/scripts/* gaia-cluster:/home/users/agrzybek/VNCBoston/fcd2vanet/scripts/

rsync -avzu /Users/agatagrzybek/workspace/crowds/scripts/run_test.sh gaia-cluster:/home/users/agrzybek/VNCBoston/fcd2vanet/scripts/


rsync -avzu /Users/agatagrzybek/workspace/fcd2vanet/scripts/* gaia-cluster:/home/users/agrzybek/VNCBoston/fcd2vanet/scripts/


# crowds
# bin
rsync  -avzu /Users/agatagrzybek/workspace/crowds/crowds.jar  gaia-cluster:VNCBoston/crowds/crowds-instant.jar
# scripts
rsync -avzu /Users/agatagrzybek/workspace/crowds/scripts/* gaia-cluster:/home/users/agrzybek/VNCBoston/crowds/scripts/
# python
rsync -avzu /Users/agatagrzybek/workspace/crowds/python/* gaia-cluster:/home/users/agrzybek/VNCBoston/crowds/python/

rsync -avzu /Users/agatagrzybek/workspace/crowds/vega/* gaia-cluster:/home/users/agrzybek/VNCBoston/crowds/vega/

 
# output 

rsync -avzu  /Users/agatagrzybek/workspace/Jean/Agata/dgs_probeData_v8-20_avg.dgs gaia-cluster:/work/users/agrzybek/congestion/data/

rsync -avzu gaia-cluster:/work/users/agrzybek/congestion/output/8-20_avg /Users/agatagrzybek/workspace/crowds/output/Jean/cluster/
rsync -avzu gaia-cluster:/work/users/agrzybek/congestion/output/8-20_instant /Users/agatagrzybek/workspace/crowds/output/Jean/cluster/ 
rsync -avzu gaia-cluster:/work/users/agrzybek/congestion/output/8-20_no_mob /Users/agatagrzybek/workspace/crowds/output/Jean/cluster/

# Lux

rsync -avzu gaia-cluster:/work/users/agrzybek/crowds/output/20131220/1/Leung/groups /Users/agatagrzybek/workspace/crowds/output/cluster20131220/1/Leung/
rsync -avzu gaia-cluster:/work/users/agrzybek/crowds/output/20131220/1/MobileLeungSDSD/groups /Users/agatagrzybek/workspace/crowds/output/cluster20131220/1/MobileLeungSDSD/
rsync -avzu gaia-cluster:/work/users/agrzybek/crowds/output/20131220/1/SandSharc/groups /Users/agatagrzybek/workspace/crowds/output/cluster20131220/1/SandSharc/
rsync -avzu gaia-cluster:/work/users/agrzybek/crowds/output/20131220/1/MobileSandSharc/groups /Users/agatagrzybek/workspace/crowds/output/cluster20131220/1/MobileSandSharc/
rsync -avzu gaia-cluster:/work/users/agrzybek/crowds/output/20131220/1/Crowdz/groups /Users/agatagrzybek/workspace/crowds/output/cluster20131220/1/Crowdz/ 

rsync -avzu gaia-cluster:/work/users/agrzybek/crowds/output/20131220/1/Leung/analysis_node_id.tsv /Users/agatagrzybek/workspace/crowds/output/cluster20131220/1/Leung/
rsync -avzu gaia-cluster:/work/users/agrzybek/crowds/output/20131220/1/MobileLeungSDSD/analysis_node_id.tsv /Users/agatagrzybek/workspace/crowds/output/cluster20131220/1/MobileLeungSDSD/
rsync -avzu gaia-cluster:/work/users/agrzybek/crowds/output/20131220/1/SandSharc/analysis_node_id.tsv /Users/agatagrzybek/workspace/crowds/output/cluster20131220/1/SandSharc/
rsync -avzu gaia-cluster:/work/users/agrzybek/crowds/output/20131220/1/MobileSandSharc/analysis_node_id.tsv /Users/agatagrzybek/workspace/crowds/output/cluster20131220/1/MobileSandSharc/
rsync -avzu gaia-cluster:/work/users/agrzybek/crowds/output/20131220/1/Crowdz/analysis_node_id.tsv /Users/agatagrzybek/workspace/crowds/output/cluster20131220/1/Crowdz/ 

rsync -avzu gaia-cluster:/work/users/agrzybek/crowds/output/20131220/1/Leung/analysis_step.tsv /Users/agatagrzybek/workspace/crowds/output/cluster20131220/1/Leung/
rsync -avzu gaia-cluster:/work/users/agrzybek/crowds/output/20131220/1/MobileLeungSDSD/analysis_step.tsv /Users/agatagrzybek/workspace/crowds/output/cluster20131220/1/MobileLeungSDSD/
rsync -avzu gaia-cluster:/work/users/agrzybek/crowds/output/20131220/1/SandSharc/analysis_step.tsv /Users/agatagrzybek/workspace/crowds/output/cluster20131220/1/SandSharc/
rsync -avzu gaia-cluster:/work/users/agrzybek/crowds/output/20131220/1/MobileSandSharc/analysis_step.tsv /Users/agatagrzybek/workspace/crowds/output/cluster20131220/1/MobileSandSharc/
rsync -avzu gaia-cluster:/work/users/agrzybek/crowds/output/20131220/1/Crowdz/analysis_step.tsv /Users/agatagrzybek/workspace/crowds/output/cluster20131220/1/Crowdz/ 

rsync -avzu gaia-cluster:/work/users/agrzybek/crowds/output/20131220/1/Leung/*.png /Users/agatagrzybek/workspace/crowds/output/cluster20131220/1/Leung/
rsync -avzu gaia-cluster:/work/users/agrzybek/crowds/output/20131220/1/MobileLeungSDSD/*.png /Users/agatagrzybek/workspace/crowds/output/cluster20131220/1/MobileLeungSDSD/
rsync -avzu gaia-cluster:/work/users/agrzybek/crowds/output/20131220/1/SandSharc/*.png /Users/agatagrzybek/workspace/crowds/output/cluster20131220/1/SandSharc/
rsync -avzu gaia-cluster:/work/users/agrzybek/crowds/output/20131220/1/MobileSandSharc/*.png /Users/agatagrzybek/workspace/crowds/output/cluster20131220/1/MobileSandSharc/
rsync -avzu gaia-cluster:/work/users/agrzybek/crowds/output/20131220/1/Crowdz/*.png /Users/agatagrzybek/workspace/crowds/output/cluster20131220/1/Crowdz/ 

