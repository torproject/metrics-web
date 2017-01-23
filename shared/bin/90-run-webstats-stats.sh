#!/bin/sh

cd modules/webstats/

ant run | grep "\[java\]"

R --slave -f src/main/resources/write-RData.R > /dev/null 2>&1

cd ../../

