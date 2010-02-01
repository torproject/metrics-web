library(ggplot2)
consensuses <- read.csv("stats/consensus-stats", header=TRUE,
    stringsAsFactors=FALSE);

plot_consensus <- function(filename, title, limits, rows, breaks,
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
  ggsave(filename = paste("website/graphs/", filename, sep = ""),
    width = 8, height = 5, dpi = 72)
}

plot_pastdays <- function(filenamePart, titlePart, days, rows, breaks, labels) {
  for (day in days) {
    end <- seq(from = Sys.Date(), length = 2, by = "-1 day")[2]
    start <- seq(from = end, length = 2, by = paste("-", day, " days", sep = ""))[2]
    plot_consensus(paste(filenamePart, "-", day, "d.png", sep = ""),
      paste(titlePart, "(past", day, "days)\n"), c(start, end),
      rows, breaks, labels)
  }
}

plot_years <- function(filenamePart, titlePart, years, rows, breaks,
    labels) {
  for (year in years) {
    plot_consensus(paste(filenamePart, "-", year, ".png", sep = ""),
      paste(titlePart, " (", year, ")\n", sep = ""),
      as.Date(c(paste(year, "-01-01", sep = ""),
      paste(year, "-12-31", sep = ""))), rows, breaks, labels)
  }
}

plot_quarters <- function(filenamePart, titlePart, years, quarters, rows,
    breaks, labels) {
  for (year in years) {
    for (quarter in quarters) {
      start <- as.Date(paste(year, "-", (quarter - 1) * 3 + 1, "-01",
        sep = ""))
      end <- seq(seq(start, length = 2, by = "3 months")[2], length = 2,
        by = "-1 day")[2]
      plot_consensus(paste(filenamePart, "-", year, "-q", quarter, ".png",
        sep = ""), paste(titlePart, " (Q", quarter, " ", year, ")\n",
        sep = ""), c(start, end), rows, breaks, labels)
    }
  }
}

plot_months <- function(filenamePart, titlePart, years, months, rows,
    breaks, labels) {
  for (year in years) {
    for (month in months) {
      start <- as.Date(paste(year, "-", month, "-01", sep = ""))
      end <- seq(seq(start, length = 2, by = "1 month")[2], length = 2,
        by = "-1 day")[2]
      plot_consensus(paste(filenamePart, "-", year, "-",
        format(start, "%m"), ".png", sep = ""), paste(titlePart,
        " (", format(start, "%B"), " ", year, ")\n", sep = ""),
        c(start, end), rows, breaks, labels)
    }
  }
}

# TODO these need to be updated manually

plot_pastdays("networksize", "Number of relays and bridges",
  c(30, 90, 180), c(1, 5, 7), c("running", "brunning"),
  c("Relays", "Bridges"))
plot_years("networksize", "Number of relays and bridges",
  "2010", c(1, 5, 7), c("running", "brunning"),
  c("Relays", "Bridges"))
plot_quarters("networksize", "Number of relays and bridges",
  "2010", 1, c(1, 5, 7), c("running", "brunning"),
  c("Relays", "Bridges"))
plot_months("networksize", "Number of relays and bridges",
  "2010", 2, c(1, 5, 7), c("running", "brunning"),
  c("Relays", "Bridges"))

plot_pastdays("exit", "Number of exit relays",
  c(30, 90, 180), c(1, 5, 2), c("running", "exit"),
  c("All relays", "Exit relays"))
plot_years("exit", "Number of exit relays",
  "2010", c(1, 5, 2), c("running", "exit"),
  c("All relays", "Exit relays"))
plot_quarters("exit", "Number of exit relays",
  "2010", 1, c(1, 5, 2), c("running", "exit"),
  c("All relays", "Exit relays"))
plot_months("exit", "Number of exit relays",
  "2010", 2, c(1, 5, 2), c("running", "exit"),
  c("All relays", "Exit relays"))

