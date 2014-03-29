# fcd scripts
rsync -avzu /Users/agatagrzybek/workspace/crowds/scripts/* gaia-cluster:/home/users/agrzybek/VNCBoston/fcd2vanet/scripts/

rsync -avzu /Users/agatagrzybek/workspace/crowds/scripts/run_test.sh gaia-cluster:/home/users/agrzybek/VNCBoston/fcd2vanet/scripts/


rsync -avzu /Users/agatagrzybek/workspace/fcd2vanet/scripts/* gaia-cluster:/home/users/agrzybek/VNCBoston/fcd2vanet/scripts/


# crowds
# bin
rsync  -avzu /Users/agatagrzybek/workspace/crowds/crowds-*.jar  gaia-cluster:VNCBoston/crowds/


# scripts
rsync -avzu /Users/agatagrzybek/workspace/crowds/scripts/* gaia-cluster:/home/users/agrzybek/VNCBoston/crowds/scripts/

# python
rsync -avzu /Users/agatagrzybek/workspace/crowds/python/* gaia-cluster:/home/users/agrzybek/VNCBoston/crowds/python/

rsync -avzu /Users/agatagrzybek/workspace/crowds/vega/* gaia-cluster:/home/users/agrzybek/VNCBoston/crowds/vega/

 
# input 
scp /Users/agatagrzybek/workspace/Jean/Agata/probeData_v15-30_avg_direction_clean_300.dgs gaia-cluster:/work/users/agrzybek/congestion/data/
#rsync -avzu  /Users/agatagrzybek/workspace/Jean/Agata/dgs_probeData_v8-20_avg.dgs gaia-cluster:/work/users/agrzybek/congestion/data/
scp /Users/agatagrzybek/workspace/Jean/Agata/vanet_probeData_v15-30+300_17032014.dgs gaia-cluster:/work/users/agrzybek/congestion/data/
# output

mkdir 20022014
mv OAR* 
mv only_dsd_* 20022014

rsync -avzu gaia-cluster:/work/users/agrzybek/congestion/output/26032014 /Users/agatagrzybek/workspace/crowds/output/Jean/cluster


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

