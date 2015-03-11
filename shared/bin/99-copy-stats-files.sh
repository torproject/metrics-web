#!/bin/sh
mkdir -p shared/stats
cp -a modules/legacy/stats/*.csv shared/stats/
cp -a modules/advbwdist/stats/advbwdist.csv shared/stats/
cp -a modules/hidserv/stats/hidserv.csv shared/stats/

