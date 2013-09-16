#!/bin/bash
wget -qO direct-users.csv --no-check-certificate https://metrics.torproject.org/csv/direct-users.csv
wget -qO userstats-detector.csv --no-check-certificate https://metrics.torproject.org/csv/userstats-detector.csv
python detector.py
cat short_censorship_report.txt | mail -E -s 'Possible censorship events' tor-censorship-events@lists.torproject.org

