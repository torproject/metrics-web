##Pre-loaded libraries and graphing functions to speed things up

library("RPostgreSQL")
library("DBI")
library("ggplot2")
library("proto")
library("grid")
library("reshape")
library("plyr")
library("digest")

db = "tordir"
dbuser = "metrics"
dbpassword= ""

source('graphs.R')
source('csv.R')
