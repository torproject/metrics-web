#!/bin/sh
# TODO is there a better way to suppress Ant's output?
ant -q | grep -Ev "^$|^BUILD SUCCESSFUL|^Total time: "

