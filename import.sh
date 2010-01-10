#!/bin/sh
java -cp bin/:lib/commons-codec-1.4.jar Main import
R --slave < graphs.R

