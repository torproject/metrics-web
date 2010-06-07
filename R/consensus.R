options(warn = -1)
suppressPackageStartupMessages(library("ggplot2"))

args <- commandArgs()
days <- args[4]
fname <- args[5]

c <- read.csv("/tmp/consensus-stats", header = TRUE,
    stringsAsFactors = FALSE);
c <- c[1:length(c$date)-1,c("date", "running", "brunning")]
c <- melt(c, id = "date")

day <- as.numeric(days)
end <- Sys.Date()
start <- seq(from = end, length = 2, by = paste("-", day, " days",
    sep = ""))[2]
limits <- c(start, end)
png(filename = fname, unit = "in", width = 8, height = 5, res = 72)
ggplot(c, aes(x = as.Date(date, "%Y-%m-%d"), y = value,
  colour = variable)) + geom_line() +
  scale_x_date(name = "", limits = limits) +
  scale_y_continuous(name = "",
  limits = c(0, max(c$value, na.rm = TRUE))) +
  scale_colour_hue("", breaks = c("running", "brunning"),
      labels = c("Relays", "Bridges")) +
  opts(title = paste("Number of relays and bridges (past", day,
      "days)\n"))
invisible(dev.off())

