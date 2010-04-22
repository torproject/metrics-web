options(warn = -1)
suppressPackageStartupMessages(library("ggplot2"))

args <- commandArgs()

days <- args[4]
fname <- args[5]

consensuses <- read.csv("/tmp/consensus-stats", header = TRUE,
    stringsAsFactors = FALSE);
consensuses <- consensuses[1:length(consensuses$date)-1,]

plot_consensus <- function(directory, filename, title, limits, rows, breaks,
    labels) {
  c <- melt(consensuses[rows], id = "date")
  ggplot(c, aes(x = as.Date(date, "%Y-%m-%d"), y = value,
    colour = variable)) + geom_line() +
    scale_x_date(name = "", limits = limits) +
    scale_y_continuous(name = "",
    limits = c(0, max(c$value, na.rm = TRUE))) +
    scale_colour_hue("", breaks = breaks, labels = labels) +
    opts(title = title)
  ggsave(filename = filename,
    width = 8, height = 5, dpi = 72)
}

plot_pastdays <- function(directory, filenamePart, titlePart, days, rows,
    breaks, labels) {
  for (day in days) {
    end <- Sys.Date()
    start <- seq(from = end, length = 2, by = paste("-", day, " days",
      sep = ""))[2]
    plot_consensus(directory, filenamePart,
      paste(titlePart, "(past", day, "days)\n"), c(start, end),
      rows, breaks, labels)
  }
}

plot_current <- function(directory, filenamePart, titlePart, rows, breaks,
    labels) {
  plot_pastdays(directory, filenamePart, titlePart, as.numeric(days), rows,
    breaks, labels)
}

plot_current("", fname,
  "Number of relays and bridges", c(1, 5, 7),
  c("running", "brunning"), c("Relays", "Bridges"))

