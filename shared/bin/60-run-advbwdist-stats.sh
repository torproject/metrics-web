#!/bin/sh
cd modules/advbwdist/
ant | grep "\[java\]"
R --slave -f aggregate.R
cd ../../

