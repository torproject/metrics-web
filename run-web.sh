#!/bin/sh
for i in $(ls shared/bin/[0-9]* | sort); do ./$i; done

