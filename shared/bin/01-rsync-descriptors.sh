#!/bin/sh
rsync -arz --delete --exclude 'relay-descriptors/votes' --exclude 'relay-descriptors/microdescs' metrics.torproject.org::metrics-recent shared/in

