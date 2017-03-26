#!/bin/sh
#
# Expecting the submodule path as argument
#

cd $1

for x in  metrics-lib collector onionoo ; do
    cd $1/$x
    src/main/resources/bootstrap-development.sh
    if  ! [ -d lib ] ; then
        mkdir lib
    fi;
    ant clean docs
done;
