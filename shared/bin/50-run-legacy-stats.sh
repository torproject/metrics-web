#!/bin/sh
cd modules/legacy/
ant | grep "\[java\]"
psql -U metrics tordir -c 'SELECT * FROM refresh_all();'
mkdir -p stats
psql -c 'COPY (SELECT * FROM stats_servers) TO STDOUT WITH CSV HEADER;' tordir > stats/servers.csv
psql -c 'COPY (SELECT * FROM stats_bandwidth) TO STDOUT WITH CSV HEADER;' tordir > stats/bandwidth.csv
cd ../../

