#!/bin/sh

#mac
CROWDS_PROG="/Users/agatagrzybek/GraphStreamWorkspace/crowds/crowds.jar"
DGS_PATH="/Users/agatagrzybek/Google Drive/PhD/workshop/sumo_scenarios/Luxembourg_6-8/fcd2vanet/graph_10s.dgs"
OUTPUT_DIR="/Users/agatagrzybek/GraphStreamWorkspace/crowds/output/08102013"
LOG_FILE="crowds_log.txt"
ANALYSE_SCRIPT="/Users/agatagrzybek/GraphStreamWorkspace/crowds/python/analyse_crowds.py"
PLOT_SCRIPT="/Users/agatagrzybek/GraphStreamWorkspace/crowds/python/plot_crowds.py"
GRAPH_FILES=""
DELTA=0.05
NUM_ITER=1;
START_STEP=0;
END_STEP=10;
COMMUNITY_INDEX=8;
GRAPH_AVERAGE_FILE="avg_graph.txt"
COMMUNITY_FILE="communities.csv"
COMMUNITY_ANALYSIS_FILE="analysis_community_sum.txt"

# cluster
# CROWDS_PROG="/home/users/agrzybek/VNCBoston/crowds/crowds.jar"
# DGS_PATH="/work/users/agrzybek/VNCBoston/data/fcd_0-30_full.dgs"
# OUTPUT_DIR="/work/users/agrzybek/VNCBoston/output/08102013"
# LOG_FILE="crowds_log.txt"
# PLOT_SCRIPT="/home/users/agrzybek/VNCBoston/crowds/scripts/plot_crowds.py"
# ANALYSE_SCRIPT="/home/users/agrzybek/VNCBoston/crowds/scripts/analyse_crowds.py"
# GRAPH_FILES=""
# DELTA=0.05
# NUM_ITER=1;
# START_STEP=0;
# END_STEP=1200;
# COMMUNITY_INDEX=8;

NUM_RUNS=2;
ALGS=('Leung' 'EpidemicCommunityAlgorithm')

NUM_ALGS=${#ALGS[@]}
AVG_GRAPH_FILES=""

# for each algorithm
for i in `seq 1 ${NUM_ALGS}`; do
	ALGO=${ALGS[$i-1]}
	GRAPH_FILES=""
	ITER_LABELS=""
	ALGO_LABELS=$ALGO_LABELS$ALGO"/ "
	echo "\nAlgorithm "$ALGO":"
	AVG_COMMUNITY_ANALYSIS_FILES=""

	# run crowds
	for i in `seq 1 ${NUM_RUNS}`; do
		echo "Iteration ${i}... running crowds.jar"
		ITER_LABELS=$ITER_LABELS$i"/ "
		OUTPUT_ITER=$OUTPUT_DIR"/"$i
		OUTPUT=$OUTPUT_ITER"/"$ALGO"/"
		mkdir $OUTPUT_ITER
		mkdir $OUTPUT
		LOG_PATH=$OUTPUT$LOG_FILE
		GRAPH_FILES="${GRAPH_FILES}${OUTPUT}graph.txt "
		# java -Xmx4096m -jar $CROWDS_PROG --inputFile "${DGS_PATH}" --outputDir $OUTPUT --delta $DELTA --numberOfIterations $NUM_ITER --startStep $START_STEP --endStep $END_STEP --algorithm $ALGO > $LOG_PATH  
		echo "java -Xmx4096m -jar $CROWDS_PROG --inputFile \"$DGS_PATH\" --outputDir $OUTPUT --delta $DELTA --numberOfIterations $NUM_ITER --startStep $START_STEP --endStep $END_STEP --algorithm $ALGO > $LOG_PATH "
	done		
	
	# # calculate avg_graoh
	echo "Calculating average avg_graph for all iterations..."
	TITLE="Number of vehicles" 
	OUTPUT_ALG=$OUTPUT_DIR"/"$ALGO"/"
	mkdir $OUTPUT_ALG
	COLUMNS="step avg_com_size max_com_size min_com_size com_count std_com_dist"
	python ${PLOT_SCRIPT} --title "${TITLE}" --inputFiles "${GRAPH_FILES}" --labels "${ITER_LABELS}" --outputDir ${OUTPUT_ALG} --columns "${COLUMNS}" --type average --filename "${GRAPH_AVERAGE_FILE}"
	AVG_GRAPH_FILES=$AVG_GRAPH_FILES$OUTPUT_ALG$GRAPH_AVERAGE_FILE" "

	# # analyse  communities from communities.csv
	# TYPE="communities"
	# echo "Analysing of "$TYPE" from communities.csv for each iteration..."
	# for i in `seq 1 ${NUM_RUNS}`; do
	# 	OUTPUT_COM=$OUTPUT_DIR"/"$i"/"$ALGO
	# 	python $ANALYSE_SCRIPT --inputFile $OUTPUT_COM"/"$COMMUNITY_FILE  --outputDir "${OUTPUT_COM}/" --type $TYPE --groupIndex $COMMUNITY_INDEX --groupType community  
	# 	AVG_COMMUNITY_ANALYSIS_FILES=$AVG_COMMUNITY_ANALYSIS_FILES$OUTPUT_COM"/"$COMMUNITY_ANALYSIS_FILE" "
	# done
	# echo "Calculating average community_analysis_sum.txt for each algorithm..."
	# COLUMNS="group_count avg_size max_size max_size_id max_size_step max_size_lifetime avg_lifetime max_lifetime max_lifetime_id"
	# python ${PLOT_SCRIPT} --title "${TITLE}" --filename $COMMUNITY_ANALYSIS_FILE --inputFiles "${AVG_COMMUNITY_ANALYSIS_FILES}" --labels "${ITER_LABELS}" --outputDir "${OUTPUT_ALG}" --columns "${COLUMNS}" --type average --filename "${COMMUNITY_ANALYSIS_FILE}"

	# # analyse vehicles
	# TYPE="vehicles"
	# echo "Analysing of "$TYPE" from communities.csv for each iteration..."
	# 	for i in `seq 1 ${NUM_RUNS}`; do
	# 	OUTPUT_COM=$OUTPUT_DIR"/"$i"/"$ALGO
	# 	python $ANALYSE_SCRIPT --inputFile $OUTPUT_COM"/"$COMMUNITY_FILE  --outputDir "${OUTPUT_COM}/" --type $TYPE
	# 	# AVG_VEHICLES_ANALYSIS_FILES=$AVG_VEHICLE_ANALYSIS_FILES$OUTPUT_COM"/"$VEHICLE_ANALYSIS_FILE" "
	# done
	
	# TYPE="size"
	# echo "Analysing of "$TYPE" from communities.csv for each iteration..."
	# for i in `seq 1 ${NUM_RUNS}`; do
	# 	OUTPUT_COM=$OUTPUT_DIR"/"$i"/"$ALGO
	# 	python $ANALYSE_SCRIPT --inputFile $OUTPUT_COM"/"$COMMUNITY_FILE  --outputDir "${OUTPUT_COM}/" --type $TYPE  
	# 	# AVG_SIZE_ANALYSIS_FILES=$AVG_SIZE_ANALYSIS_FILES$OUTPUT_COM"/"$SIZE_ANALYSIS_FILE" "
	# done

	
done 

# plot comparison of algorithms
echo "Output files: "$AVG_FILES
echo "Algo labels: "$ALGO_LABELS
echo "Plotting avgerages avg_graph.txt for all algorithms"
python ${PLOT_SCRIPT} --title "${TITLE}" --inputFiles "${AVG_GRAPH_FILES}" --labels "${ALGO_LABELS}" --outputDir "${OUTPUT_DIR}/" --type graph

