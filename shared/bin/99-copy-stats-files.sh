#!/bin/sh
mkdir -p shared/stats
cp -a modules/legacy/stats/*.csv shared/stats/
cp -a modules/connbidirect/stats/connbidirect2.csv shared/stats/
cp -a modules/advbwdist/stats/advbwdist.csv shared/stats/
cp -a modules/hidserv/stats/hidserv.csv shared/stats/
cp -a modules/clients/stats/clients.csv shared/stats/
cp -a modules/clients/stats/userstats-combined.csv shared/stats/

