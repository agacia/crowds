#!/bin/sh

CMD="all";
# commands:
# all
# run_algorithm - run crovds
# calculate_avg_graph
# plot_comparison
#analyse_communities
#calculate_avg_community_sum

#mac
ENV="mac"
CROWDS_PROG="/Users/agatagrzybek/GraphStreamWorkspace/crowds/crowds.jar"
DGS_PATH="/Users/agatagrzybek/Google Drive/PhD/workshop/sumo_scenarios/Luxembourg_6-8/fcd2vanet/graph_10s.dgs"
OUTPUT_DIR="/Users/agatagrzybek/GraphStreamWorkspace/crowds/output/08102013"
ANALYSE_SCRIPT="/Users/agatagrzybek/GraphStreamWorkspace/crowds/python/analyse_crowds.py"
PLOT_SCRIPT="/Users/agatagrzybek/GraphStreamWorkspace/crowds/python/plot_crowds.py"
LOG_FILE="crowds_log.txt"
GRAPH_FILES=""
DELTA=0.05
NUM_ITER=1;
START_STEP=0;
END_STEP=10;
COMMUNITY_INDEX=8;
GRAPH_AVERAGE_FILE="avg_graph.txt"
COMMUNITY_FILE="communities.csv"
COMMUNITY_ANALYSIS_FILE="analysis_community_sum.txt"
NUM_RUNS=2;
AVG_GRAPH_FILES=""
ALGS="Leung;MobileLeung"
DO_RUN=print
# ALGS=('Leung' 'MobileLeung')
# ALGS=('Leung' 'EpidemicCommunityAlgorithm' 'MobileLeung' 'Sharc' 'SawSharc' 'DynSharc' 'NewSawSharc' 'SandSharc')
# NUM_ALGS=${#ALGS[@]}



if [ $5 ]; then ENV=$5; fi
echo "# ENVIRONMENT "$ENV 

if [ $ENV = "cluster" ]; then
	CROWDS_PROG="/home/users/agrzybek/VNCBoston/crowds/crowds.jar"
	DGS_PATH="/work/users/agrzybek/VNCBoston/data/fcd_0-30_full.dgs"
	OUTPUT_DIR="/work/users/agrzybek/VNCBoston/output/10102013"
	PLOT_SCRIPT="/home/users/agrzybek/VNCBoston/crowds/python/plot_crowds.py"
	ANALYSE_SCRIPT="/home/users/agrzybek/VNCBoston/crowds/python/analyse_crowds.py"
	START_STEP=0
	END_STEP=1200;
fi

if [ $1 ]; then CMD=$1; fi
echo "# Command "$CMD

if [ $2 ]; then NUM_RUNS=${2}; fi
echo "# NUM_RUNS "$NUM_RUNS

if [ $3 ]; then END_STEP=$3; fi
echo "# END_STEP "$END_STEP

if [ $4 ]; then ALGS=$4; fi
echo "# ALGORITHMS "$ALGS 

if [ $6 ]; then DO_RUN=$6; fi
echo "# DO_RUN "$DO_RUN 

if [ $7 ]; then OUTPUT_DIR=$7; fi
echo "# OUTPUT_DIR "$OUTPUT_DIR

# for each algorithm
# for i in `seq 1 ${NUM_ALGS}`; do
IFS=";"
#for i in $(echo $ALGS | tr ";" "\n"); do
for ALGO in $ALGS; do
	# ALGO=${ALGS[$i-1]}
	GRAPH_FILES=""
	ITER_LABELS=""
	ALGO_LABELS=$ALGO_LABELS"/"$ALGO"/ "
	AVG_COMMUNITY_ANALYSIS_FILES=""
	# echo "\n# Algorithm "$ALGO":"
	unset IFS
	sequence=$(seq 1 $NUM_RUNS)
	# echo $sequence
	for i in $sequence; do
		if [ ! -d $OUTPUT_DIR ]; then
			mkdir $OUTPUT_DIR
		fi
		# echo "Iter ${i}:"
		ITER_LABELS=$ITER_LABELS$i"/ "
		OUTPUT_ITER=$OUTPUT_DIR"/"${i}
		OUTPUT=$OUTPUT_ITER"/"$ALGO"/"
		if [ ! -d $OUTPUT_ITER ]; then
		   mkdir $OUTPUT_ITER
		fi
		if [ ! -d $OUTPUT ]; then
			mkdir $OUTPUT
		fi
		LOG_PATH=$OUTPUT$LOG_FILE
		GRAPH_FILES="${GRAPH_FILES}${OUTPUT}graph.txt "
		
		if [ "$CMD"  =  "run_algorithm"  -o "$CMD" = "all" ]; then
			if [ "$DO_RUN" = "run" -a $ENV = "mac" ]; then 
				java -Xmx4096m -jar $CROWDS_PROG --inputFile "${DGS_PATH}" --outputDir $OUTPUT --delta $DELTA --numberOfIterations $NUM_ITER --startStep $START_STEP --endStep $END_STEP --algorithm $ALGO > $LOG_PATH; 
				echo
				echo "java -Xmx4096m -jar $CROWDS_PROG --inputFile \"$DGS_PATH\" --outputDir ${OUTPUT} --delta ${DELTA} --numberOfIterations ${NUM_ITER} --startStep ${START_STEP} --endStep ${END_STEP} --algorithm ${ALGO} > ${LOG_PATH}"
			fi 
			if [ "$DO_RUN" = "run" -a $ENV = "cluster" ]; then 
				#oarsub -l core=1,walltime=48 'java -Xmx4096m -jar "$CROWDS_PROG" --inputFile "${DGS_PATH}" --outputDir $OUTPUT --delta $DELTA --numberOfIterations $NUM_ITER --startStep $START_STEP --endStep $END_STEP --algorithm $ALGO'; 
				echo
				echo "oarsub -l core=1,walltime=48 'java -Xmx4096m -jar $CROWDS_PROG --inputFile \"$DGS_PATH\" --outputDir ${OUTPUT} --delta ${DELTA} --numberOfIterations ${NUM_ITER} --startStep ${START_STEP} --endStep ${END_STEP}'"
			fi 
			if [ "$DO_RUN" = "print" ]; then
				echo
				echo "java -Xmx4096m -jar $CROWDS_PROG --inputFile \"$DGS_PATH\" --outputDir ${OUTPUT} --delta ${DELTA} --numberOfIterations ${NUM_ITER} --startStep ${START_STEP} --endStep ${END_STEP} --algorithm ${ALGO} > ${LOG_PATH}"
			fi
		fi
	done		


	if [ "$CMD" = "calculate_avg_graph" -o "$CMD" = "plot_comparison" -o "$CMD" = "all" ]; then
		# # calculate avg_graoh
		# echo "# Calculating average avg_graph for all iterations..."
		TITLE="Number of vehicles" 
		OUTPUT_ALG=$OUTPUT_DIR"/"$ALGO"/"
		if [ ! -d $OUTPUT_ITER  -o "$CMD" = "all" ]; then
			mkdir $OUTPUT_ALG
		fi
		COLUMNS="step avg_com_size max_com_size min_com_size com_count std_com_dist"
		AVG_GRAPH_FILES=$AVG_GRAPH_FILES$OUTPUT_ALG$GRAPH_AVERAGE_FILE" "
		if [ $CMD = "calculate_avg_graph"  -o "$CMD" = "all" ]; then
			echo 
			echo "python ${PLOT_SCRIPT} --title \"${TITLE}\" --inputFiles \"${GRAPH_FILES}\" --labels \"${ITER_LABELS}\" --outputDir ${OUTPUT_ALG} --columns \"${COLUMNS}\" --type average --filename \"${GRAPH_AVERAGE_FILE}\""
			if [ "$DO_RUN" = "run" ]; then  python ${PLOT_SCRIPT} --title "${TITLE}" --inputFiles "${GRAPH_FILES}" --labels "${ITER_LABELS}" --outputDir ${OUTPUT_ALG} --columns "${COLUMNS}" --type average --filename "${GRAPH_AVERAGE_FILE}"; fi
		fi
	fi

	if [ $CMD = "analyse_communities" -o $CMD = "calculate_avg_community_sum"  -o "$CMD" = "all" ]; then
		# # Analyse  communities from communities.csv
		TYPE="communities"
		# echo "# Analysing of "$TYPE" from communities.csv for each iteration..."
		for i in `seq 1 ${NUM_RUNS}`; do
			OUTPUT_COM=$OUTPUT_DIR"/"$i"/"$ALGO
			AVG_COMMUNITY_ANALYSIS_FILES=$AVG_COMMUNITY_ANALYSIS_FILES$OUTPUT_COM"/"$COMMUNITY_ANALYSIS_FILE" "
			if [ $CMD = "analyse_communities"  -o "$CMD" = "all" ]; then
				echo
				echo "python ${ANALYSE_SCRIPT} --inputFile \"${OUTPUT_COM}/${COMMUNITY_FILE}\" --outputDir \"${OUTPUT_COM}/\" --type ${TYPE} --groupIndex ${COMMUNITY_INDEX} --groupType community"
				if [ "$DO_RUN" = "run" ]; then python $ANALYSE_SCRIPT --inputFile $OUTPUT_COM"/"$COMMUNITY_FILE  --outputDir "${OUTPUT_COM}/" --type $TYPE --groupIndex $COMMUNITY_INDEX --groupType community; fi
			fi
		done
		if [ $CMD = "calculate_avg_community_sum"  -o "$CMD" = "all" ]; then
			# echo "# Calculating average community_analysis_sum.txt for each algorithm..."
			COLUMNS="group_count avg_size max_size max_size_id max_size_step max_size_lifetime avg_lifetime max_lifetime max_lifetime_id"
			echo 
			echo "python ${PLOT_SCRIPT} --title ${TITLE} --filename ${COMMUNITY_ANALYSIS_FILE}  --inputFiles \"${OUTPUT_COM}/${COMMUNITY_FILE}\" --labels \"${ITER_LABELS}\" --outputDir \"${OUTPUT_ALG}/\" --columns \"${COLUMNS}\" --type average --filename \"${COMMUNITY_ANALYSIS_FILE}\""
			if [ "$DO_RUN" = "run" ]; then python ${PLOT_SCRIPT} --title "${TITLE}" --filename $COMMUNITY_ANALYSIS_FILE --inputFiles "${AVG_COMMUNITY_ANALYSIS_FILES}" --labels "${ITER_LABELS}" --outputDir "${OUTPUT_ALG}" --columns "${COLUMNS}" --type average --filename "${COMMUNITY_ANALYSIS_FILE}"; fi
		fi
	fi

	if [ $CMD = "analyse_vehicles" -o $CMD = "calculate_avg_vehicle_sum"  -o "$CMD" = "all" ]; then
		TYPE="vehicles"
		# echo "# Analysing of "$TYPE" from communities.csv for each iteration..."
		for i in `seq 1 ${NUM_RUNS}`; do
			OUTPUT_COM=$OUTPUT_DIR"/"$i"/"$ALGO
			# AVG_VEHICLES_ANALYSIS_FILES=$AVG_VEHICLE_ANALYSIS_FILES$OUTPUT_COM"/"$VEHICLE_ANALYSIS_FILE" "
			if [ $CMD = "analyse_vehicles"  -o "$CMD" = "all" ]; then
				echo
				echo "python ${ANALYSE_SCRIPT} --inputFile \"${OUTPUT_COM}/${COMMUNITY_FILE}\" --outputDir \"${OUTPUT_COM}/\" --type ${TYPE}"
				if [ "$DO_RUN" = "run" ]; then  python $ANALYSE_SCRIPT --inputFile $OUTPUT_COM"/"$COMMUNITY_FILE  --outputDir "${OUTPUT_COM}/" --type $TYPE; fi
			fi		
		done
	fi

	if [ $CMD = "analyse_size" -o $CMD = "calculate_avg_size_sum"  -o "$CMD" = "all" ]; then
		TYPE="size"
		# echo "# Analysing of "$TYPE" from communities.csv for each iteration..."
		for i in `seq 1 ${NUM_RUNS}`; do
			OUTPUT_COM=$OUTPUT_DIR"/"$i"/"$ALGO
			# AVG_SIZE_ANALYSIS_FILES=$AVG_SIZE_ANALYSIS_FILES$OUTPUT_COM"/"$SIZE_ANALYSIS_FILE" "
			if [ $CMD = "analyse_size"  -o "$CMD" = "all" ]; then
				echo
				echo "python ${ANALYSE_SCRIPT} --inputFile \"${OUTPUT_COM}/${COMMUNITY_FILE}\" --outputDir \"${OUTPUT_COM}/\" --type ${TYPE}"
				if [ "$DO_RUN" = "run" ]; then  python $ANALYSE_SCRIPT --inputFile $OUTPUT_COM"/"$COMMUNITY_FILE  --outputDir "${OUTPUT_COM}/" --type $TYPE; fi
			fi
		done
	fi
	
done 

if [ "$CMD" = "plot_comparison"  -o "$CMD" = "all" ]; then
	# # Plot comparison of algorithms
	echo
	echo "python ${PLOT_SCRIPT} --title \"${TITLE}\" --inputFiles \"${AVG_GRAPH_FILES}\" --labels \"${ALGO_LABELS}\" --outputDir \"${OUTPUT_DIR}/\" --type graph"
	if [ "$DO_RUN" = "run" ]; then python ${PLOT_SCRIPT} --title "${TITLE}" --inputFiles "${AVG_GRAPH_FILES}" --labels "${ALGO_LABELS}" --outputDir "${OUTPUT_DIR}/" --type graph; fi
fi
