#!/bin/sh
mkdir -p shared/stats
cp -a modules/onionperf/stats/*.csv shared/stats/
cp -a modules/legacy/stats/*.csv shared/stats/
cp -a modules/connbidirect/stats/connbidirect2.csv shared/stats/
cp -a modules/advbwdist/stats/advbwdist.csv shared/stats/
cp -a modules/hidserv/stats/hidserv.csv shared/stats/
cp -a modules/clients/stats/clients*.csv shared/stats/
cp -a modules/clients/stats/userstats-combined.csv shared/stats/
cp -a modules/webstats/stats/webstats.csv shared/stats/

mkdir -p shared/RData
cp -a modules/clients/RData/*.RData shared/RData/
cp -a modules/webstats/RData/*.RData shared/RData/

