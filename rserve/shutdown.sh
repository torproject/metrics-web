#!/bin/sh

echo "library(Rserve)
c <- RSconnect()
RSshutdown(c)" | R --slave
