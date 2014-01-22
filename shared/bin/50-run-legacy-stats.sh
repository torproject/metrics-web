#!/bin/sh
cd modules/legacy/
ant | grep "\[java\]"
psql -U metrics tordir -c 'SELECT * FROM refresh_all();'
psql -c 'COPY (SELECT * FROM stats_servers) TO STDOUT WITH CSV HEADER;' tordir > stats/servers.csv
psql -c 'COPY (SELECT * FROM stats_bandwidth) TO STDOUT WITH CSV HEADER;' tordir > stats/bandwidth.csv
psql -c 'COPY (SELECT * FROM stats_torperf) TO STDOUT WITH CSV HEADER;' tordir > stats/torperf.csv
psql -c 'COPY (SELECT * FROM stats_connbidirect) TO STDOUT WITH CSV HEADER;' tordir > stats/connbidirect.csv
cd ../../

