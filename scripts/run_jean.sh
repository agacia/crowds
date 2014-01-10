#!/bin/sh

if [ $1 ]; then NAME=$1; fi
echo "# Name "$NAME

/home/users/agrzybek/VNCBoston/crowds/scripts/run_and_analyse_crowds.sh run_algorithm 5 2400 "MobileSandSharc" cluster run "/work/users/agrzybek/congestion/output/8-20_no_mob" 0 2400 "sdsd" "/work/users/agrzybek/congestion/data/dgs_probeData_v8-20_avg.dgs" "SCORE_THR" "MAX_SIZE_ID" "MAX_LIFE_ID" "SAMPLING_RATE" "IMG_FILE_TYPE" "VEGA_SPEC" "VEGA_PATH" "/home/users/agrzybek/VNCBoston/crowds/crowds-no-mob.jar"

