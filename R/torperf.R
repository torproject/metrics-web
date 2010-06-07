options(warn = -1)
suppressPackageStartupMessages(library("ggplot2"))

if (file.exists("stats/torperf-stats")) {

  t <- read.csv("stats/torperf-stats", colClasses = c("character", "Date",
    "integer", "integer", "integer"))
  write.csv(t, "website/csv/torperf.csv", quote = FALSE, row.names = FALSE)

  intervals <- c("12m", "6m", "2w")
  intervalsStr <- c("-12 months", "-6 months", "-2 weeks")

  for (intervalInd in 1:length(intervals)) {
    interval <- intervals[intervalInd]
    intervalStr <- intervalsStr[intervalInd]

    end <- seq(from = Sys.Date(), length = 2, by = "-1 day")[2]
    start <- seq(seq(from = end, length = 2,
        by=intervalStr)[2], length=2, by="1 day")[2]

    dates <- seq(from = start, to = end, by="1 day")

    sources <- c("siv", "moria", "torperf")
    colors <- c("#0000EE", "#EE0000", "#00CD00")
    sizes <- c("5mb", "1mb", "50kb")
    sizePrint <- c("5 MiB", "1 MiB", "50 KiB")

    for (sizeInd in 1:length(sizes)) {
      size <- sizes[sizeInd]
      sizePr <- sizePrint[sizeInd]
      for (sourceInd in 1:length(sources)) {
        sourceStr <- paste(sources[sourceInd], size, sep = "-")
        sourceName <- sources[sourceInd]

        u <- t[t$source == sourceStr & t$date >= start & t$date <= end, 2:5]
        missing <- setdiff(dates, u$date)
        if (length(missing) > 0) {
          u <- rbind(u, data.frame(date = as.Date(missing, origin = "1970-01-01"),
              q1 = NA, md = NA, q3 = NA))
        }
        maxy <- max(t[t$source %in% paste(sources, "-", size, sep = ""),5],
            na.rm = TRUE)
        ggplot(u, aes(x = as.Date(date), y = md/1e3, fill = "line")) +
          geom_line(colour = colors[sourceInd], size = 0.75) +
          geom_ribbon(data = u, aes(x = date, ymin = q1/1e3,
            ymax = q3/1e3, fill = "ribbon")) +
          scale_x_date(name = "") +
          scale_y_continuous(name = "", limits = c(0, maxy / 1e3)) +
          coord_cartesian(ylim = c(0, 0.8 * maxy / 1e3)) +
          scale_fill_manual(name = paste("Measured times on",
              sources[sourceInd], "per day"),
            breaks = c("line", "ribbon"),
            labels = c("Median", "1st to 3rd quartile"),
            values = paste(colors[sourceInd], c("", "66"), sep = "")) +
          opts(title = paste("Time in seconds to complete", sizePr, "request"), legend.position = "top")
        ggsave(filename = paste("website/graphs/torperf/torperf-", size, "-",
            sourceName, "-", interval, ".png", sep = ""), width = 8, height = 5,
            dpi = 72)
      }
    }
  }
}

