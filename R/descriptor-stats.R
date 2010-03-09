options(warn = -1)
suppressPackageStartupMessages(library("ggplot2"))

versions <- read.csv("stats/version-stats", header = TRUE,
    colClasses = c(date = "Date"))
platforms <- read.csv("stats/platform-stats", header = TRUE,
    colClasses = c(date = "Date"))
bandwidth <- read.csv("stats/bandwidth-stats", header = TRUE,
    colClasses = c(date = "Date"))
write.csv(versions, "website/csv/versions.csv", quote = FALSE,
  row.names = FALSE)
write.csv(platforms, "website/csv/platforms.csv", quote = FALSE,
  row.names = FALSE)
write.csv(bandwidth, "website/csv/bandwidth.csv", quote = FALSE,
  row.names = FALSE)

plot_versions <- function() {
  v <- melt(versions, id = "date")
  ggplot(v, aes(x = date, y = value, colour = variable)) +
    geom_line(size = 1) +
    scale_x_date(name = "") + scale_y_continuous(name = "",
        limits = c(0, max(v$value, na.rm = TRUE))) +
    scale_colour_brewer(name = "Tor version",
        breaks = rev(names(versions)[2:length(names(versions))]),
        labels = c("other",
            substr(rev(names(versions)[2:(length(names(versions)) - 1)]),
            2, 6))) +
    opts(title = "Relay versions\n")
  ggsave(filename = "website/graphs/descriptors/versions.png",
    width = 8, height = 5, dpi = 72)
}

plot_platforms <- function() {
  p <- melt(platforms, id = "date")
  ggplot(p, aes(x = date, y = value, colour = variable)) +
    geom_line(size = 1) +
    scale_x_date(name = "") + scale_y_continuous(name = "",
        limits = c(0, max(p$value, na.rm = TRUE))) +
    scale_colour_brewer(name = "Platform",
        breaks = rev(names(platforms)[2:length(names(platforms))]),
        labels = rev(names(platforms)[2:length(names(platforms))])) +
    opts(title = "Relay platforms\n")
  ggsave(filename = "website/graphs/descriptors/platforms.png",
    width = 8, height = 5, dpi = 72)
}

plot_bandwidth <- function() {
  ggplot(bandwidth, aes(x = date, y = advbw / 1024)) + geom_line() +
    scale_x_date(name = "") +
    scale_y_continuous(name = "Bandwidth (MiB/s)",
        limits = c(0, max(bandwidth$advbw / 1024, na.rm = TRUE))) +
    opts(title = "Total advertised bandwidth\n")
  ggsave(filename = "website/graphs/descriptors/bandwidth.png",
    width = 8, height = 5, dpi = 72)
}

plot_versions()
plot_platforms()
plot_bandwidth()

