# fcd scripts
rsync -avzu /Users/agatagrzybek/workspace/crowds/scripts/* gaia-cluster:/home/users/agrzybek/VNCBoston/fcd2vanet/scripts/

rsync -avzu /Users/agatagrzybek/workspace/crowds/scripts/run_test.sh gaia-cluster:/home/users/agrzybek/VNCBoston/fcd2vanet/scripts/

# crowds
# bin
rsync  -avzu /Users/agatagrzybek/workspace/crowds/crowds.jar  gaia-cluster:VNCBoston/crowds/



# python
rsync -avzu /Users/agatagrzybek/workspace/crowds/python/* gaia-cluster:/home/users/agrzybek/VNCBoston/crowds/python/
# scripts
rsync -avzu /Users/agatagrzybek/workspace/crowds/scripts/* gaia-cluster:/home/users/agrzybek/VNCBoston/crowds/scripts/

rsync -avzu /Users/agatagrzybek/workspace/crowds/vega/* gaia-cluster:/home/users/agrzybek/VNCBoston/crowds/vega/

 
# output 
rsync -avzu gaia-cluster:/work/users/agrzybek/VNCBoston/output/10102013/*.png /Users/agatagrzybek/workspace/crowds/output/cluster/
rsync -avzu gaia-cluster:/work/users/agrzybek/VNCBoston/output/10102013/*.png /Users/agatagrzybek/workspace/crowds/output/cluster/900-1200

scp gaia-cluster:/work/users/agrzybek/VNCBoston/output/20131113/0/* /Users/agatagrzybek/workspace/crowds/output/cluster13112013/ 

 



