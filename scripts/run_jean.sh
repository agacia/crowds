#!/bin/sh

if [ $1 ]; then NAME=$1; fi
echo "# Name "$NAME

if [ $2 ]; then WEIGHT=$2; fi
echo "# WEIGHT "$WEIGHT

if [ $3 ]; then NUM=$3; fi
echo "# NUM "$NUM



/home/users/agrzybek/VNCBoston/crowds/scripts/run_and_analyse_crowds.sh run_algorithm 2 2400 "MobileSandSharc" cluster run "/work/users/agrzybek/congestion/output/${NAME}_${WEIGHT}_${NUM}" 0 2400 "sdsd" "/work/users/agrzybek/congestion/data/vanet_probeData_v15-30+200_17032014.dgs" "SCORE_THR" "MAX_SIZE_ID" "MAX_LIFE_ID" "SAMPLING_RATE" "IMG_FILE_TYPE" "VEGA_SPEC" "VEGA_PATH" "/home/users/agrzybek/VNCBoston/crowds/crowds-${NAME}.jar" ${WEIGHT}

