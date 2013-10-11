./run_and_analyse_crowds.sh 
	CMD		<all|run_algorithm|calculate_avg_graph|plot_comparison|analyse_communities|calculate_avg_community_sum> 
	NUM_RUNS	<0-100> 
	END_STEP	<0-1200> 
	ALGS		"<Leung;EpidemicCommunityAlgorithm;MobileLeung;Sharc;SawSharc;DynSharc;NewSawSharc;SandSharc>" 
	ENV		<mac|cluster> 
	DO_RUN		<run|print>
	OUTPUT_DIR	<PATH>

e.g:
-mac
./run_and_analyse_crowds.sh run_algorithm 3 10 "Leung;MobileLeung;Sharc" mac print "/Users/agatagrzybek/GraphStreamWorkspace/crowds/output/10102013"

-cluster

./run_and_analyse_crowds.sh "run_algorithm" 3 1200 "Leung;EpidemicCommunityAlgorithm;MobileLeung;Sharc;DynSharc" "cluster" "run" "/work/users/agrzybek/VNCBoston/output/10102013"
