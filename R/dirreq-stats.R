dirreq <- read.csv("stats/dirreq-stats", header = TRUE, stringsAsFactors = FALSE)
moria1 <- subset(dirreq, directory %in% "moria1")
trusted <- subset(dirreq, directory %in% "trusted")

plot_moria1 <- function(country, people, filename, color) {
  start <- seq(seq(from=Sys.Date(), length=2,
      by="-6 months")[2], length=2, by="1 day")[2]
  dates <- seq(from = start, to = Sys.Date(), by="1 day")
  datesStr <- as.character(dates)
  data <- c()
  for (i in datesStr) {
    data <- c(data, ifelse(i %in% moria1$date,
        moria1[[country]][moria1$date == i] * 6, NA))
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
  png(filename, width=600, height=400)
  par(mar = c(4.1, 3.9, 2.1, 0))
  plot(data, ylim=c(0, max(0, na.omit(data))), type="l", col=color, lwd=2,
      axes=FALSE, frame=FALSE, xlab=paste("Last updated:",
      as.POSIXlt(Sys.time(), "UTC"), "UTC"),
      ylab="", main=paste("New or returning, directly connecting", people,
      "Tor users"))
  axis(1, at=monthticks - 0.5, labels=FALSE, lwd=0, lwd.ticks=1)
  axis(1, at=c(1, length(data)), labels=FALSE, lwd=1, lwd.ticks=0)
  axis(1, at=monthat, lwd=0, labels=monthlabels)
  axis(2, las=1, lwd=0, lwd.ticks=1)
  axis(2, las=1, at=c(min(max(0, na.omit(data)), na.omit(data)), max(0, na.omit(data))), lwd.ticks=0, labels=FALSE)
  dev.off()
}
plot_moria1("bh", "Bahraini", "website/graphs/bahrain-new.png", "red")
plot_moria1("cn", "Chinese", "website/graphs/china-new.png", "red")
plot_moria1("cu", "Cuban", "website/graphs/cuba-new.png", "red")
plot_moria1("et", "Ethiopian", "website/graphs/ethiopia-new.png", "red")
plot_moria1("ir", "Iranian", "website/graphs/iran-new.png", "green3")
plot_moria1("mm", "Burmese", "website/graphs/burma-new.png", "blue4")
plot_moria1("sa", "Saudi", "website/graphs/saudi-new.png", "red")
plot_moria1("sy", "Syrian", "website/graphs/syria-new.png", "red")
plot_moria1("tn", "Tunisian", "website/graphs/tunisia-new.png", "red")
plot_moria1("tm", "Turkmen", "website/graphs/turkmenistan-new.png", "red")
plot_moria1("uz", "Uzbek", "website/graphs/uzbekistan-new.png", "red")
plot_moria1("vn", "Vietnamese", "website/graphs/vietnam-new.png", "gold2")
plot_moria1("ye", "Yemeni", "website/graphs/yemen-new.png", "gold2")

plot_trusted <- function(country, people, filename, color) {
  start <- seq(seq(from=Sys.Date(), length=2,
      by="-6 months")[2], length=2, by="1 day")[2]
  dates <- seq(from = start, to = Sys.Date(), by="1 day")
  datesStr <- as.character(dates)
  data <- c()
  for (i in datesStr) {
    data <- c(data, ifelse(i %in% trusted$date,
        trusted[[country]][trusted$date == i] /
        trusted$share[trusted$date == i] * 10, NA))
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
      format(as.POSIXct(dates[monthticks[length(monthticks) - 1] + 1]),
      "%b %y"))
  monthat <- c()
  for (i in 1:(length(monthticks) - 1))
    monthat <- c(monthat, (monthticks[i] + monthticks[i + 1]) / 2)
  png(filename, width=600, height=400)
  par(mar = c(4.1, 3.9, 2.1, 0))
  plot(data, ylim=c(0, max(0, na.omit(data))), type="l", col=color, lwd=2,
      axes=FALSE, frame=FALSE, xlab=paste("Last updated:",
      as.POSIXlt(Sys.time(), "UTC"), "UTC"),
      ylab="", main=paste("Recurring, directly connecting", people,
      "Tor users"))
  axis(1, at=monthticks - 0.5, labels=FALSE, lwd=0, lwd.ticks=1)
  axis(1, at=c(1, length(data)), labels=FALSE, lwd=1, lwd.ticks=0)
  axis(1, at=monthat, lwd=0, labels=monthlabels)
  axis(2, las=1, lwd=0, lwd.ticks=1)
  axis(2, las=1, at=c(min(max(0, na.omit(data)), na.omit(data)), max(0, na.omit(data))), lwd.ticks=0, labels=FALSE)
  dev.off()
}

plot_trusted("bh", "Bahraini", "website/graphs/bahrain-direct.png", "red")
plot_trusted("cn", "Chinese", "website/graphs/china-direct.png", "red")
plot_trusted("cu", "Cuban", "website/graphs/cuba-direct.png", "red")
plot_trusted("et", "Ethiopian", "website/graphs/ethiopia-direct.png", "red")
plot_trusted("ir", "Iranian", "website/graphs/iran-direct.png", "green3")
plot_trusted("mm", "Burmese", "website/graphs/burma-direct.png", "blue4")
plot_trusted("sa", "Saudi", "website/graphs/saudi-direct.png", "red")
plot_trusted("sy", "Syrian", "website/graphs/syria-direct.png", "red")
plot_trusted("tn", "Tunisian", "website/graphs/tunisia-direct.png", "red")
plot_trusted("tm", "Turkmen", "website/graphs/turkmenistan-direct.png", "red")
plot_trusted("uz", "Uzbek", "website/graphs/uzbekistan-direct.png", "red")
plot_trusted("vn", "Vietnamese", "website/graphs/vietnam-direct.png", "gold2")
plot_trusted("ye", "Yemeni", "website/graphs/yemen-direct.png", "gold2")

