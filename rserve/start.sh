#!/bin/sh

###########################################################
# NOTE                                                    #
# RUN THIS AS ROOT - Rserve will run as user rserve       #
###########################################################

#Output stderr and stdout to rserve log.

dir=`pwd`
R CMD Rserve --RS-conf $dir/Rserv.conf >> ../rserve.log 2>&1
