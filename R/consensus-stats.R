options(warn = -1)
suppressPackageStartupMessages(library("ggplot2"))

consensuses <- read.csv("stats/consensus-stats", header = TRUE,
    stringsAsFactors = FALSE);
consensuses <- consensuses[1:length(consensuses$date)-1,]
write.csv(data.frame(date = consensuses$date,
  relays = consensuses$running, bridges = consensuses$brunning),
  "website/csv/networksize.csv", quote = FALSE, row.names = FALSE)
write.csv(data.frame(date = consensuses$date,
  all = consensuses$running, exit = consensuses$exit),
  "website/csv/exit.csv", quote = FALSE, row.names = FALSE)

plot_consensus <- function(directory, filename, title, limits, rows, breaks,
    labels) {
  c <- melt(consensuses[rows], id = "date")
  ggplot(c, aes(x = as.Date(date, "%Y-%m-%d"), y = value,
    colour = variable)) + geom_line() + #stat_smooth() +
    scale_x_date(name = "", limits = limits) +
    #paste("\nhttp://metrics.torproject.org/ -- last updated:",
    #  date(), "UTC"),
    scale_y_continuous(name = "",
    limits = c(0, max(c$value, na.rm = TRUE))) +
    scale_colour_hue("", breaks = breaks, labels = labels) +
    opts(title = title)
  ggsave(filename = paste(directory, filename, sep = ""),
    width = 8, height = 5, dpi = 72)
}

plot_pastdays <- function(directory, filenamePart, titlePart, days, rows,
    breaks, labels) {
  for (day in days) {
    end <- Sys.Date()
    start <- seq(from = end, length = 2, by = paste("-", day, " days",
      sep = ""))[2]
    plot_consensus(directory, paste(filenamePart, "-", day, "d.png",
      sep = ""), paste(titlePart, "(past", day, "days)\n"), c(start, end),
      rows, breaks, labels)
  }
}

plot_years <- function(directory, filenamePart, titlePart, years, rows,
    breaks, labels) {
  for (year in years) {
    plot_consensus(directory, paste(filenamePart, "-", year, ".png",
      sep = ""), paste(titlePart, " (", year, ")\n", sep = ""),
      as.Date(c(paste(year, "-01-01", sep = ""),
      paste(year, "-12-31", sep = ""))), rows, breaks, labels)
  }
}

plot_quarters <- function(directory, filenamePart, titlePart, years,
    quarters, rows, breaks, labels) {
  for (year in years) {
    for (quarter in quarters) {
      start <- as.Date(paste(year, "-", (quarter - 1) * 3 + 1, "-01",
        sep = ""))
      end <- seq(seq(start, length = 2, by = "3 months")[2], length = 2,
        by = "-1 day")[2]
      plot_consensus(directory, paste(filenamePart, "-", year, "-q",
        quarter, ".png",
        sep = ""), paste(titlePart, " (Q", quarter, " ", year, ")\n",
        sep = ""), c(start, end), rows, breaks, labels)
    }
  }
}

plot_months <- function(directory, filenamePart, titlePart, years, months,
    rows, breaks, labels) {
  for (year in years) {
    for (month in months) {
      start <- as.Date(paste(year, "-", month, "-01", sep = ""))
      end <- seq(seq(start, length = 2, by = "1 month")[2], length = 2,
        by = "-1 day")[2]
      plot_consensus(directory, paste(filenamePart, "-", year, "-",
        format(start, "%m"), ".png", sep = ""), paste(titlePart,
        " (", format(start, "%B"), " ", year, ")\n", sep = ""),
        c(start, end), rows, breaks, labels)
    }
  }
}

plot_all <- function(directory, filenamePart, titlePart, rows, breaks,
    labels) {
  plot_consensus(directory, paste(filenamePart, "-all.png", sep = ""),
    paste(titlePart, " (all data)\n", sep = ""),
    as.Date(c(min(consensuses$date), max(consensuses$date))), rows,
    breaks, labels)
}

# TODO these need to be updated manually
plot_current <- function(directory, filenamePart, titlePart, rows, breaks,
    labels) {
  plot_pastdays(directory, filenamePart, titlePart, c(30, 90, 180), rows,
    breaks, labels)
  plot_years(directory, filenamePart, titlePart, "2010", rows, breaks,
    labels)
  plot_quarters(directory, filenamePart, titlePart, "2010", 1, rows,
    breaks, labels)
  plot_months(directory, filenamePart, titlePart, "2010", 2, rows, breaks,
    labels)
  plot_all(directory, filenamePart, titlePart, rows, breaks, labels)
}

plot_current("website/graphs/networksize/", "networksize",
  "Number of relays and bridges", c(1, 5, 7),
  c("running", "brunning"), c("Relays", "Bridges"))
plot_current("website/graphs/exit/", "exit", "Number of exit relays",
  c(1, 5, 2), c("running", "exit"), c("All relays", "Exit relays"))

