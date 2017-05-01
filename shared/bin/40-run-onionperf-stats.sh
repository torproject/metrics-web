#!/bin/sh
cd modules/onionperf/
ant | grep "\[java\]" | grep -Ev " DEBUG | INFO "
cd ../../

