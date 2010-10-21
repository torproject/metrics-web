#!/bin/sh
dir=`pwd`
R CMD /home/metrics/R/x86_64-pc-linux-gnu-library/2.11/Rserve/libs/Rserve-bin.so --RS-conf $dir/Rserv.conf >> rserve.log 2>&1
