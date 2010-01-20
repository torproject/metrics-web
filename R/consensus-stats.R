consensuses <- read.csv("stats/consensus-stats", header=TRUE,
    stringsAsFactors=FALSE);
end <- seq(from = Sys.Date(), length = 2, by = "-1 day")[2]
start <- seq(seq(from = end, length = 2,
    by="-6 months")[2], length=2, by="1 day")[2]
dates <- seq(from = start, to = end, by="1 day")
datesStr <- as.character(dates)
exitNum <- c()
runningNum <- c()
brunningNum <- c()
for (i in datesStr) {
  exitNum <- c(exitNum, ifelse(i %in% consensuses$date,
      consensuses$exit[consensuses$date == i], NA))
  runningNum <- c(runningNum, ifelse(i %in% consensuses$date,
      consensuses$running[consensuses$date == i], NA))
  brunningNum <- c(brunningNum, ifelse(i %in% consensuses$date,
      consensuses$brunning[consensuses$date == i], NA))
}
firstdays <- c()
for (i in datesStr)
  if (format(as.POSIXct(i, tz="GMT"), "%d") == "01")
    firstdays <- c(firstdays, i)
monthticks <- which(datesStr %in% firstdays)
monthlabels <- c()
for (i in monthticks[1:(length(monthticks) - 2)])
  monthlabels <- c(monthlabels,
      format(as.POSIXct(dates[i + 1], tz="GMT"), "%b"))
monthlabels <- c(monthlabels,
    format(as.POSIXct(dates[monthticks[length(monthticks) - 1] + 1]), "%b %y"))
monthat <- c()
for (i in 1:(length(monthticks) - 1))
  monthat <- c(monthat, (monthticks[i] + monthticks[i + 1]) / 2)

png("website/graphs/exit.png", width=600, height=400)
par(mar = c(4.1, 3.9, 2.1, 4.1))
runningCol <- "red"
exitCol <- "darkgreen"
plot(runningNum, ylim=c(0, max(na.omit(runningNum))), type="l",
    col=runningCol, lwd=2, axes=FALSE, frame=FALSE,
    main=paste("Number of exit relays"),
    xlab=paste("Last updated:", as.POSIXlt(Sys.time(), "UTC")), ylab="")
lines(exitNum, col=exitCol, lwd=2)
mtext("All relays", side=4, line=0, las=1,
    at=tail(na.omit(runningNum), n=1), col=runningCol)
mtext("Exit relays", side=4, line=0, las=1,
    at=tail(na.omit(exitNum), n=1), col=exitCol)
axis(1, at=monthticks - 0.5, labels=FALSE, lwd=0, lwd.ticks=1)
axis(1, at=c(1, length(exitNum)), labels=FALSE, lwd=1, lwd.ticks=0)
axis(1, at=monthat, lwd=0, labels=monthlabels)
axis(2, las=1, lwd=0, lwd.ticks=1)
axis(2, at=c(min(na.omit(runningNum)), max(na.omit(runningNum))), lwd.ticks=0, labels=FALSE)
axis(2, at=c(min(na.omit(exitNum)), max(na.omit(exitNum))), lwd.ticks=0, labels=FALSE)
dev.off()

png("website/graphs/networksize.png", width=600, height=400)
par(mar = c(4.1, 3.9, 2.1, 4.1))
runningCol <- "red"
brunningCol <- "blue"
plot(runningNum, ylim=c(0, max(na.omit(runningNum))), type="l",
    col=runningCol, lwd=2, axes=FALSE, frame=FALSE,
    main=paste("Number of relays and bridges"),
    xlab=paste("Last updated:", as.POSIXlt(Sys.time(), "UTC")), ylab="")
lines(brunningNum, col=brunningCol, lwd=2)
mtext("Relays", side=4, line=0, las=1,
    at=tail(na.omit(runningNum), n=1), col=runningCol)
mtext("Bridges", side=4, line=0, las=1,
    at=tail(na.omit(brunningNum), n=1), col=brunningCol)
axis(1, at=monthticks - 0.5, labels=FALSE, lwd=0, lwd.ticks=1)
axis(1, at=c(1, length(exitNum)), labels=FALSE, lwd=1, lwd.ticks=0)
axis(1, at=monthat, lwd=0, labels=monthlabels)
axis(2, las=1, lwd=0, lwd.ticks=1)
axis(2, at=c(min(na.omit(runningNum)), max(na.omit(runningNum))), lwd.ticks=0, labels=FALSE)
axis(2, at=c(min(na.omit(brunningNum)), max(na.omit(brunningNum))), lwd.ticks=0, labels=FALSE)
dev.off()

