#!/bin/sh
java -cp bin/:lib/commons-codec-1.4.jar Main download
R --slave < R/graphs.R

