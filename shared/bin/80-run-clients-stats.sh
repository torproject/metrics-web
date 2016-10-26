#!/bin/sh

cd modules/clients/

echo `date` "Parsing descriptors."
ant | grep "\[java\]"

for i in $(ls out/*.sql)
do
  echo `date` "Importing $i."
  psql -f $i userstats
done

echo `date` "Exporting results."
psql -c 'COPY (SELECT * FROM estimated) TO STDOUT WITH CSV HEADER;' userstats > userstats.csv
psql -c 'COPY (SELECT * FROM combined) TO STDOUT WITH CSV HEADER;' userstats > userstats-combined.csv

echo `date` "Running censorship detector."
R --slave -f userstats-detector.R > /dev/null 2>&1
python detector.py

echo `date` "Merging censorship detector results."
R --slave -f merge-clients.R > /dev/null 2>&1

echo `date` "Splitting results file."
R --slave -f split-clients.R > /dev/null 2>&1
mkdir -p stats/
cp clients*.csv stats/
cp userstats-combined.csv stats/

echo `date` "Terminating."

cd ../../

