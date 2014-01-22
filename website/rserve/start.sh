#!/bin/sh
dir=`pwd`
R CMD Rserve --no-save --RS-conf $dir/Rserv.conf >> rserve.log 2>&1
