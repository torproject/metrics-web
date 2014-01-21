#!/bin/sh
rsync -arz --delete --exclude 'relay-descriptors/votes' metrics.torproject.org::metrics-recent shared/in

