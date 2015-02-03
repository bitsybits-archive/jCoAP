#!/bin/bash
# run "./runTests.sh" for verbose output
# for short output, run
# ./runTests.sh | grep FAILED
# ./runTests.sh | grep PASSED
#master branch
SERVER_CLASS="org.ws4d.coap.test.PlugtestServer"
CLIENT_CLASS="org.ws4d.coap.test.PlugtestClient"
CLASSPATH="bin/:../ws4d-jcoap/bin"
LOG_DIR="log"
REF_LOG_DIR="logref"

EXEC_SERVER=""
EXEC_CLIENT=""

build_exec_string() {
  TEST=$1

  EXEC_SERVER="java -cp $CLASSPATH $SERVER_CLASS $TEST > $LOG_DIR/${TEST}_SERVER.txt &"
  EXEC_CLIENT="java -cp $CLASSPATH $CLIENT_CLASS $TEST > $LOG_DIR/${TEST}_CLIENT.txt &"
}

run_test() {
  TEST=$1

  echo "Running Test $TEST"
  build_exec_string $TEST
  eval $EXEC_SERVER
  eval $EXEC_CLIENT
  sleep 3
  killall java >/dev/null 2>/dev/null
  sleep 1

  diff -i -b -B -q $LOG_DIR/${TEST}_SERVER.txt $REF_LOG_DIR/${TEST}_SERVER.txt
  if [ ! $? -eq 0 ]
  then
    echo "FAILED Server $TEST"
  else
    echo "PASSED Server $TEST"
  fi

  diff -i -b -B -q $LOG_DIR/${TEST}_CLIENT.txt $REF_LOG_DIR/${TEST}_CLIENT.txt
  if [ ! $? -eq 0 ]
  then
    echo "FAILED Client $TEST"
  else
    echo "PASSED Client $TEST"
  fi
}

killall java >/dev/null 2>/dev/null
sleep 1

mkdir -p $LOG_DIR

# CoAP CORE Tests
for i in 1 2 3 4 5 6 7 8 9
do
  run_test TD_COAP_CORE_0$i
done

for i in 10 11 12 13 14 15 16
do
  run_test TD_COAP_CORE_$i
done

# CoAP Link Tests
#for i in 1 2
#do
#  run_test TD_COAP_LINK_0$i
#done

# CoAP Block Tests
#for i in 1 2 3 4
#do
#  run_test TD_COAP_BLOCK_0$i
#done

# CoAP Observation Tests
#for i in 1 2 3 4 5
#do
#  run_test TD_COAP_OBS_0$i
#done
