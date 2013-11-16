./run_and_analyse_crowds.sh 
	CMD		<all|run_algorithm|calculate_avg_graph|plot_comparison|analyse_communities|calculate_avg_community_sum|vega|vega_groups|vega_imgs> 
	NUM_RUNS	<0-100> 
	END_STEP	<0-1200> 
	ALGS		"<Leung;EpidemicCommunityAlgorithm;MobileLeung;Sharc;SawSharc;DynSharc;NewSawSharc;SandSharc>" 
	ENV		<mac|cluster> 
	DO_RUN		<run|print>
	OUTPUT_DIR	<PATH>
	INDEX_START	<0-END_STEP>
	INDEX_STOP	<INDEX_START-INDEX_STOP>
	MOBILITY_METRICS
	DGS_PATH	<path to dgs file>
	SCORE_THR	0 // above the thershold communiy will be considered as community
	MAX_SIZE_ID	0 // id of community with maximum size - get metadata 
	MAX_LIFE_ID	0 // as above but lifetime
	SAMPLING_RATE	2 // sampling of steps for groups
	IMG_FILE_TYPE	png // or svg
	VEGA_SPEC	<path to vega spec>
	VEGA_PATH	<path to vega>

e.g:

--- vega

-- small test
/Users/agatagrzybek/workspace/crowds/scripts/run_and_analyse_crowds.sh vega 1 10 "MobileLeungDSD;MobileLeungSDSD" mac run "/Users/agatagrzybek/workspace/crowds/output/14112013" 0 1200 "" "" 0 0 0 2 .png /Users/agatagrzybek/workspace/crowds/vega/vega_spec_vanet.json /Users/agatagrzybek/Documents/PhD/Phd_workshop/vega/bin/vg2png


--- analyse vehicles:
-mac
/Users/agatagrzybek/workspace/crowds/scripts/run_and_analyse_crowds.sh analyse_vehicles 1 10 "MobileLeung" mac run "/Users/agatagrzybek/workspace/crowds/output/eclipse" 0 1200 sdsd

-cluster
/home/users/agrzybek/VNCBoston/crowds/scripts/run_and_analyse_crowds.sh analyse_vehicles 1 1200 "MobileLeung" cluster print "/work/users/agrzybek/VNCBoston/output/20131113" 0 1200 sdsd

oarsub -t besteffort -t idempotent -l nodes=1/cpu=1/core=4,walltime=12:00:0 '/home/users/agrzybek/VNCBoston/crowds/scripts/run_and_analyse_crowds.sh analyse_vehicles 1 1200 "MobileLeung" cluster print "/work/users/agrzybek/VNCBoston/output/20131113" 0 1200 sdsd'

#!/bin/sh


