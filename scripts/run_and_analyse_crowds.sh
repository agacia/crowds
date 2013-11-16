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
CROWDS_PROG="/Users/agatagrzybek/workspace/crowds/crowds.jar"
DGS_PATH="/Users/agatagrzybek/Google Drive/PhD/workshop/sumo_scenarios/Luxembourg_6-8/fcd2vanet/graph_10s.dgs"
OUTPUT_DIR="/Users/agatagrzybek/workspace/crowds/output/08102013"
ANALYSE_SCRIPT="/Users/agatagrzybek/workspace/crowds/python/analyse_crowds.py"
PLOT_SCRIPT="/Users/agatagrzybek/workspace/crowds/python/plot_crowds.py"
GETGROUPS_SCRIPT="/Users/agatagrzybek/workspace/crowds/python/vegaGetGroups.py"
GENIMGS_SCRIPT="/Users/agatagrzybek/workspace/crowds/python/vegaGenerateImgs.py"
LOG_FILE="crowds_log.txt"
GRAPH_FILES=""
DELTA=0.05
NUM_ITER=1;
START_STEP=0;
END_STEP=10;
INDEX_START=0;
INDEX_STOP=7;
COMMUNITY_INDEX=8;
GRAPH_AVERAGE_FILE="avg_graph.txt"
COMMUNITY_FILE="communities.csv"
COMMUNITY_ANALYSIS_FILE="analysis_community_sum.txt"
VEHICLE_ANALYSIS_FILE="analysis_vehicles_sum.txt"
SIZE_ANALYSIS_FILE="analysis_size_sum.txt"
NUM_RUNS=2;
AVG_GRAPH_FILES=""
ALGS="Leung;MobileLeung"
MOBILITY_METRICS="distance"
DO_RUN=print;
SCORE_THR=-1;
MAX_SIZE_ID=-1;
MAX_LIFE_ID=-1;
SAMPLING_RATE=10;
IMG_FILE_TYPE=".png"
VEGA_PATH="/Users/agatagrzybek/Documents/PhD/Phd_workshop/vega/bin/vg2png"
VEGA_PATH_SVG="/Users/agatagrzybek/Documents/PhD/Phd_workshop/vega/bin/vg2svg"
VEGA_SPEC="/Users/agatagrzybek/workspace/crowds/vega/vega_spec_vanet.json"

# ALGS=('Leung' 'MobileLeung')
# ALGS=('Leung' 'EpidemicCommunityAlgorithm' 'MobileLeung' 'Sharc' 'SawSharc' 'DynSharc' 'NewSawSharc' 'SandSharc')
# NUM_ALGS=${#ALGS[@]}


if [ $5 ]; then ENV=$5; fi
echo "# ENVIRONMENT "$ENV 

if [ $ENV = "cluster" ]; then

	# module display matplotlib/1.2.1-goolf-1.4.10-Python-2.7.3
	# /opt/apps/HPCBIOS/modules/vis/matplotlib/1.2.1-goolf-1.4.10-Python-2.7.3
	# module load matplotlib/1.2.1-goolf-1.4.10-Python-2.7.3
	CROWDS_PROG="/home/users/agrzybek/VNCBoston/crowds/crowds.jar"
	DGS_PATH="/work/users/agrzybek/VNCBoston/data/fcd_0-30_full.dgs"
	OUTPUT_DIR="/work/users/agrzybek/VNCBoston/output/10102013"
	PLOT_SCRIPT="/home/users/agrzybek/VNCBoston/crowds/python/plot_crowds.py"
	ANALYSE_SCRIPT="/home/users/agrzybek/VNCBoston/crowds/python/analyse_crowds.py"
	GETGROUPS_SCRIPT="/home/users/agrzybek/VNCBoston/crowds/python/vegaGetGroups.py"
	GENIMGS_SCRIPT="/home/users/agrzybek/VNCBoston/crowds/python/vegaGenerateImgs.py"
	START_STEP=0
	END_STEP=1200;
fi

echo "Number of arguments: "$#

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

if [ $# -gt 7 -a $8 ]; then INDEX_START=$8; fi
echo "# INDEX_START "$INDEX_START 

if [ $# -gt 8 -a $9 ]; then INDEX_STOP=$9; fi
echo "# INDEX_STOP "$INDEX_STOP

if [ $# -gt 9 -a $10 ]; then MOBILITY_METRICS="${10}"; fi
echo "# MOBILITY_METRICS "$MOBILITY_METRICS

if [ $# -gt 10 -a $11 ]; then DGS_PATH="${11}"; fi
echo "# DGS_PATH "$DGS_PATH

if [ $# -gt 11 -a $12 ]; then SCORE_THR="${12}"; fi
echo "# SCORE_THR "$SCORE_THR

if [ $# -gt 12 -a $13 ]; then MAX_SIZE_ID="${13}"; fi
echo "# MAX_SIZE_ID "$MAX_SIZE_ID

if [ $# -gt 13 -a $14 ]; then MAX_LIFE_ID="${14}"; fi
echo "# MAX_LIFE_ID "$MAX_LIFE_ID

if [ $# -gt 14 -a $15 ]; then SAMPLING_RATE="${15}"; fi
echo "# SAMPLING_RATE "$SAMPLING_RATE

if [ $# -gt 15 -a $16 ]; then IMG_FILE_TYPE="${16}"; fi
echo "# IMG_FILE_TYPE "$IMG_FILE_TYPE

if [ $# -gt 16 -a $17 ]; then VEGA_SPEC="${17}"; fi
echo "# VEGA_SPEC "$VEGA_SPEC

if [ $# -gt 17 -a $18 ]; then VEGA_PATH="${18}"; fi
echo "# VEGA_PATH "$VEGA_PATH



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
	OUTPUT_ALG=$OUTPUT_DIR"/"$ALGO"/"
	if [ ! -d $OUTPUT_ALG  -o "$CMD" = "all" ]; then
		mkdir $OUTPUT_ALG
	fi

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
		
		if [ "$CMD" = "run_algorithm"  -o "$CMD" = "all" ]; then
			if [ "$ALGO" = "MobileLeungDSD" ]; then
				ALGO="MobileLeung"
				MOBILITY_METRICS="dsd"
			fi
			if [ "$ALGO" = "MobileLeungSDSD" ]; then
				ALGO="MobileLeung"
				MOBILITY_METRICS="sdsd"
			fi
			if [ "$MOBILITY_METRICS" = "" ]; then
				MOBILITY_METRICS="distance"
			fi
			if [ "$DO_RUN" = "run" -a $ENV = "mac" ]; then 
				java -Xmx4096m -jar $CROWDS_PROG --inputFile "${DGS_PATH}" --outputDir $OUTPUT --delta $DELTA --numberOfIterations $NUM_ITER --startStep $START_STEP --endStep $END_STEP --algorithm $ALGO --mobilityMetrics $MOBILITY_METRICS > $LOG_PATH; 
				echo
				echo "java -Xmx4096m -jar $CROWDS_PROG --inputFile \"$DGS_PATH\" --outputDir ${OUTPUT} --delta ${DELTA} --numberOfIterations ${NUM_ITER} --startStep ${START_STEP} --endStep ${END_STEP} --algorithm ${ALGO} --mobilityMetrics $MOBILITY_METRICS > ${LOG_PATH}"
			fi 
			if [ "$DO_RUN" = "run" -a $ENV = "cluster" ]; then 
				java -Xmx4096m -jar "$CROWDS_PROG" --inputFile "${DGS_PATH}" --outputDir $OUTPUT --delta $DELTA --numberOfIterations $NUM_ITER --startStep $START_STEP --endStep $END_STEP --algorithm $ALGO --mobilityMetrics $MOBILITY_METRICS; 
				echo
				echo "oarsub -t besteffort -t idempotent -l nodes=1/cpu=1/core=4,walltime=24:00:0 'java -Xmx4096m -jar $CROWDS_PROG --inputFile \"$DGS_PATH\" --outputDir ${OUTPUT} --delta ${DELTA} --numberOfIterations ${NUM_ITER} --startStep ${START_STEP} --endStep ${END_STEP} --algorithm $ALGO --mobilityMetrics $MOBILITY_METRICS'"
			fi 
			if [ "$DO_RUN" = "print" ]; then
				echo
				echo "java -Xmx4096m -jar $CROWDS_PROG --inputFile \"$DGS_PATH\" --outputDir ${OUTPUT} --delta ${DELTA} --numberOfIterations ${NUM_ITER} --startStep ${START_STEP} --endStep ${END_STEP} --algorithm ${ALGO} --mobilityMetrics $MOBILITY_METRICS > ${LOG_PATH}"
			fi
		fi
	done		

	if [ "$CMD" = "calculate_avg_graph" -o "$CMD" = "plot_comparison" -o "$CMD" = "all" ]; then
		# # calculate avg_graoh
		# echo "# Calculating average avg_graph for all iterations..."
		TITLE="Number of vehicles" 
		COLUMNS="step avg_com_size max_com_size min_com_size com_count std_com_dist com_modularity"
		AVG_GRAPH_FILES=$AVG_GRAPH_FILES$OUTPUT_ALG$GRAPH_AVERAGE_FILE" "
		if [ $CMD = "calculate_avg_graph"  -o "$CMD" = "all" ]; then
			echo 
			echo "python ${PLOT_SCRIPT} --title \"${TITLE}\" --indexStart \"${INDEX_START}\" --indexStop \"${INDEX_STOP}\"  --inputFiles \"${GRAPH_FILES}\" --labels \"${ITER_LABELS}\" --outputDir ${OUTPUT_ALG} --columns \"${COLUMNS}\" --type average --filename \"${GRAPH_AVERAGE_FILE}\""
			if [ "$DO_RUN" = "run" ]; then  python ${PLOT_SCRIPT} --indexStart "${INDEX_START}" --indexStop "${INDEX_STOP}" --title "${TITLE}" --inputFiles "${GRAPH_FILES}" --labels "${ITER_LABELS}" --outputDir ${OUTPUT_ALG} --columns "${COLUMNS}" --type average --filename "${GRAPH_AVERAGE_FILE}"; fi
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
		if [ $CMD = "calculate_avg_community_sum" -o "$CMD" = "all" ]; then
			# echo "# Calculating average community_analysis_sum.txt for each algorithm..."
			COLUMNS="group_count avg_size max_size max_size_id max_size_step max_size_lifetime avg_lifetime max_lifetime max_lifetime_id"
			echo 
			echo "python ${PLOT_SCRIPT} --indexStart \"${INDEX_START}\"  --title ${TITLE} --inputFiles \"${AVG_COMMUNITY_ANALYSIS_FILES}\" --labels \"${ITER_LABELS}\" --outputDir \"${OUTPUT_ALG}/\" --columns \"${COLUMNS}\" --type average --filename \"${COMMUNITY_ANALYSIS_FILE}\""
			if [ "$DO_RUN" = "run" -a  $CMD = "calculate_avg_community_sum" ]; then 
				python ${PLOT_SCRIPT} --indexStart "${INDEX_START}" --indexStop "${INDEX_STOP}" --title "${TITLE}" --inputFiles "${AVG_COMMUNITY_ANALYSIS_FILES}" --labels "${ITER_LABELS}" --outputDir "${OUTPUT_ALG}" --columns "${COLUMNS}" --type average --filename "${COMMUNITY_ANALYSIS_FILE}"; fi
		fi
	fi

	if [ $CMD = "analyse_vehicles" -o $CMD = "calculate_avg_vehicle_sum"  -o "$CMD" = "all" ]; then
		TYPE="vehicles"
		# echo "# Analysing of "$TYPE" from communities.csv for each iteration..."
		for i in `seq 1 ${NUM_RUNS}`; do
			OUTPUT_COM=$OUTPUT_DIR"/"$i"/"$ALGO
			AVG_VEHICLES_ANALYSIS_FILES=$AVG_VEHICLE_ANALYSIS_FILES$OUTPUT_COM"/"$VEHICLE_ANALYSIS_FILE" "
			if [ $CMD = "analyse_vehicles"  -o "$CMD" = "all" ]; then
				echo
				echo "python ${ANALYSE_SCRIPT} --inputFile \"${OUTPUT_COM}/${COMMUNITY_FILE}\" --outputDir \"${OUTPUT_COM}/\" --type ${TYPE}"
				if [ "$DO_RUN" = "run" ]; then  python $ANALYSE_SCRIPT --inputFile $OUTPUT_COM"/"$COMMUNITY_FILE  --outputDir "${OUTPUT_COM}/" --type $TYPE; fi
			fi		
		done
		if [ $CMD = "calculate_avg_vehicle_sum" -o "$CMD" = "all" ]; then
			# echo "# Calculating average vehicle_analysis_sum.txt for each algorithm..."
			COLUMNS="vehicles avg_traveled_steps avg_connected_steps avg_percent_connected avg_degree avg_joins avg_disconnections avg_changes_community avg_steps_in_each_community avg_stability"
			echo 
			echo "python ${PLOT_SCRIPT} --indexStart \"${INDEX_START}\"  --title ${TITLE} --inputFiles \"${AVG_VEHICLES_ANALYSIS_FILES}\" --labels \"${ITER_LABELS}\" --outputDir \"${OUTPUT_ALG}\" --columns \"${COLUMNS}\" --type average --filename \"${VEHICLE_ANALYSIS_FILE}\""
			if [ "$DO_RUN" = "run" -a  $CMD = "calculate_avg_vehicle_sum" ]; then 
				python ${PLOT_SCRIPT} --indexStart "${INDEX_START}" --indexStop "${INDEX_STOP}" --title "${TITLE}" --inputFiles "${AVG_VEHICLES_ANALYSIS_FILES}" --labels "${ITER_LABELS}" --outputDir "${OUTPUT_ALG}" --columns "${COLUMNS}" --type average --filename "${VEHICLE_ANALYSIS_FILE}"; fi
		fi
	fi

	if [ $CMD = "analyse_size" -o $CMD = "calculate_avg_size_sum"  -o "$CMD" = "all" ]; then
		TYPE="size"
		# echo "# Analysing of "$TYPE" from communities.csv for each iteration..."
		for i in `seq 1 ${NUM_RUNS}`; do
			OUTPUT_COM=$OUTPUT_DIR"/"$i"/"$ALGO
			AVG_SIZE_ANALYSIS_FILES=$AVG_SIZE_ANALYSIS_FILES$OUTPUT_COM"/"$SIZE_ANALYSIS_FILE" "
			if [ $CMD = "analyse_size"  -o "$CMD" = "all" ]; then
				echo
				echo "python ${ANALYSE_SCRIPT} --inputFile \"${OUTPUT_COM}/${COMMUNITY_FILE}\" --outputDir \"${OUTPUT_COM}/\" --type ${TYPE}"
				if [ "$DO_RUN" = "run" ]; then  python $ANALYSE_SCRIPT --inputFile $OUTPUT_COM"/"$COMMUNITY_FILE  --outputDir "${OUTPUT_COM}/" --type $TYPE; fi
			fi
		done
		if [ $CMD = "calculate_avg_size_sum" -o "$CMD" = "all" ]; then
			# echo "# Calculating average vehicle_analysis_sum.txt for each algorithm..."
			COLUMNS="vehicles avg_traveled_steps avg_connected_steps avg_percent_connected avg_degree avg_joins avg_disconnections avg_changes_community avg_steps_in_each_community avg_stability"
			echo 
			echo "python ${PLOT_SCRIPT} --indexStart \"${INDEX_START}\"  --title ${TITLE} --inputFiles \"${AVG_VEHICLES_ANALYSIS_FILES}\" --labels \"${ITER_LABELS}\" --outputDir \"${OUTPUT_ALG}\" --columns \"${COLUMNS}\" --type average --filename \"${VEHICLE_ANALYSIS_FILE}\""
			if [ "$DO_RUN" = "run" -a  $CMD = "calculate_avg_size_sum" ]; then 
				python ${PLOT_SCRIPT} --indexStart "${INDEX_START}" --indexStop "${INDEX_STOP}" --title "${TITLE}" --inputFiles "${AVG_VEHICLES_ANALYSIS_FILES}" --labels "${ITER_LABELS}" --outputDir "${OUTPUT_ALG}" --columns "${COLUMNS}" --type average --filename "${VEHICLE_ANALYSIS_FILE}"; fi
		fi
	fi

	if [ "$CMD" = "vega" -o "$CMD" = "vega_groups" -o "$CMD" = "vega_imgs" ]; then
		for i in `seq 1 ${NUM_RUNS}`; do
			OUTPUT_COM=$OUTPUT_DIR"/"$i"/"$ALGO
			OUTPUT_GROUPS=$OUTPUT_COM"/groups"
			if [ ! -d $OUTPUT_GROUPS ]; then
				echo "mkdir "$OUTPUT_GROUPS
				mkdir $OUTPUT_GROUPS
			fi
			OUTPUT_IMGS=$OUTPUT_COM"/imgs"
			if [ ! -d $OUTPUT_IMGS ]; then
				mkdir $OUTPUT_IMGS
			fi
			if [ "$CMD" = "vega" -o "$CMD" = "vega_groups" ]; then
				TYPE="community"
				echo
				echo "python ${GETGROUPS_SCRIPT} --inputFile ${OUTPUT_COM}/${COMMUNITY_FILE} --outputDir ${OUTPUT_GROUPS}/ --groupIndex ${COMMUNITY_INDEX} --type ${TYPE} --delta_threshold ${SCORE_THR} --maxSizeCommunityId ${MAX_SIZE_ID} --maxLifetimeCommunityId ${MAX_LIFE_ID} --samplingRate ${SAMPLING_RATE}"
				python ${GETGROUPS_SCRIPT} --inputFile ${OUTPUT_COM}/${COMMUNITY_FILE} --outputDir ${OUTPUT_GROUPS}/ --groupIndex ${COMMUNITY_INDEX} --type ${TYPE} --delta_threshold ${SCORE_THR} --maxSizeCommunityId ${MAX_SIZE_ID} --maxLifetimeCommunityId ${MAX_LIFE_ID} --samplingRate ${SAMPLING_RATE}
			fi
			if [ "$CMD" = "vega" -o "$CMD" = "vega_imgs" ]; then	
				echo
				echo "python ${GENIMGS_SCRIPT} --inputDir ${OUTPUT_GROUPS} --outputDir ${OUTPUT_IMGS}/ --spec ${VEGA_SPEC} --vegaPath ${VEGA_PATH} --fileType ${IMG_FILE_TYPE}"
				python ${GENIMGS_SCRIPT} --inputDir ${OUTPUT_GROUPS} --outputDir ${OUTPUT_IMGS}/  --spec ${VEGA_SPEC} --vegaPath ${VEGA_PATH} --fileType ${IMG_FILE_TYPE}
			fi
		done
	fi

done # end algo loop

if [ "$CMD" = "plot_comparison"  -o "$CMD" = "all" ]; then
	# # Plot comparison of algorithms
	echo
	echo "python ${PLOT_SCRIPT} --indexStart ${INDEX_START} --indexStop ${INDEX_STOP} --title \"${TITLE}\" --inputFiles \"${AVG_GRAPH_FILES}\" --labels \"${ALGO_LABELS}\" --outputDir \"${OUTPUT_DIR}/\" --type graph"
	if [ "$DO_RUN" = "run" ]; then python ${PLOT_SCRIPT} --indexStart ${INDEX_START} --indexStop ${INDEX_STOP}  --title "${TITLE}" --inputFiles "${AVG_GRAPH_FILES}" --labels "${ALGO_LABELS}" --outputDir "${OUTPUT_DIR}/" --type graph; fi
fi



