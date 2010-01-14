bridge <- read.csv("stats/bridge-stats", header = TRUE, stringsAsFactors = FALSE)

plot_bridges <- function(country, people, filename, color) {
  sums <- aggregate(bridge[[country]], list(Date = bridge$date), sum)
  sums <- sums[1:(length(sums$Date)-1),]
  end <- seq(from = Sys.Date(), length = 2, by = "-1 day")[2]
  start <- seq(seq(from = end, length = 2,
      by="-6 months")[2], length=2, by="1 day")[2]
  dates <- seq(from = start, to = end, by="1 day")
  datesStr <- as.character(dates)
  data <- c()
  for (i in datesStr) {
    data <- c(data, ifelse(i %in% sums$Date,
        sums$x[sums$Date == i], NA))
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
  par(mar = c(2.6, 3.9, 2.1, 0))
  plot(data, ylim=c(0, max(na.omit(data))), type="l", col=color, lwd=2, axes=FALSE, frame=FALSE, main=paste(people, "Tor users via bridges"), xlab="", ylab="")
  axis(1, at=monthticks - 0.5, labels=FALSE, lwd=0, lwd.ticks=1)
  axis(1, at=c(1, length(data)), labels=FALSE, lwd=1, lwd.ticks=0)
  axis(1, at=monthat, lwd=0, labels=monthlabels)
  axis(2, las=1, lwd=0, lwd.ticks=1)
  axis(2, las=1, at=c(min(na.omit(data)), max(na.omit(data))), lwd.ticks=0, labels=FALSE)
  dev.off()
}
# TODO find better colors
plot_bridges("bh", "Bahraini", "website/graphs/bahrain-bridges.png", "red")
plot_bridges("cn", "Chinese", "website/graphs/china-bridges.png", "red")
plot_bridges("cu", "Cuban", "website/graphs/cuba-bridges.png", "red")
plot_bridges("et", "Ethiopian", "website/graphs/ethiopia-bridges.png", "red")
plot_bridges("ir", "Iranian", "website/graphs/iran-bridges.png", "green3")
plot_bridges("mm", "Burmese", "website/graphs/burma-bridges.png", "blue4")
plot_bridges("sa", "Saudi", "website/graphs/saudi-bridges.png", "red")
plot_bridges("sy", "Syrian", "website/graphs/syria-bridges.png", "red")
plot_bridges("tn", "Tunisian", "website/graphs/tunisia-bridges.png", "red")
plot_bridges("tm", "Turkmen", "website/graphs/turkmenistan-bridges.png", "red")
plot_bridges("uz", "Uzbek", "website/graphs/uzbekistan-bridges.png", "red")
plot_bridges("vn", "Vietnamese", "website/graphs/vietnam-bridges.png", "gold2")
plot_bridges("ye", "Yemeni", "website/graphs/yemen-bridges.png", "gold2")

