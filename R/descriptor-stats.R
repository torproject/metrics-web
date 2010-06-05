# R script to plot relay versions, platforms, and advertised bandwidth.
# Run from ERNIE's base directory as "R --slave < R/descriptor.stats.R".

# Suppress all warnings, so that only errors are written to stdout. This
# is useful when executing this script from cron and having it mail out a
# notification only when there's an actual problem.
options(warn = -1)

# Import library ggplot2 that is used for plotting. Suppress package
# startup messages for the same reason as suppressing warnings.
suppressPackageStartupMessages(library("ggplot2"))

# Define a function to plot relay versions. Right now, there are no
# parameters for this function. In the future, a possible parameter would
# be the time interval to be plotted on the x axis.
plot_versions <- function() {

  # Transform data frame versions into a data frame that can be processed
  # by ggplot2. In particular, versions has one row per date and multiple
  # columns for the number of relays running a particular Tor version at
  # that date. What we need for plotting is a single data point per row
  # with additional columns for classification, e.g., which version this
  # date point belongs to. Add commands "print(versions)" and "print(v)"
  # for an example.
  v <- melt(versions, id = "date")

  # Start plotting the data in data frame v.
  ggplot(v,

    # Tell ggplot2 how to understand the data in data frame v. The date
    # shall be plotted on the x axis, the value on the y axis, and the
    # row called variable shall be used to distinguish data sets by color.
    aes(x = date, y = value, colour = variable)) +

    # So far, ggplot2 only knows how to understand the data, but not how
    # to visualize them. Draw a line from the data with line size 1.
    geom_line(size = 1) +

    # Override the default x axis which would display a label "date" with
    # an x axis that has no label. This line can be commented out.
    scale_x_date(name = "") +

    # Override the default y axis with label "value" with one that has no
    # label and that starts at the origin. Note that the max() function is
    # told to remove NA values. These lines can be commented out.
    scale_y_continuous(name = "",
        limits = c(0, max(v$value, na.rm = TRUE))) +

    # Override the categorization by relay version to use a different
    # color scheme (brewer instead of hue), have a different legend title
    # ("Tor versions" instead of "variable") and display custom legend
    # labels ("0.2.2" instead of "X0.2.2"). These lines can be commented
    # out.
    scale_colour_brewer(name = "Tor version",
        breaks = rev(names(versions)[2:length(names(versions))]),
        labels = c("other",
            substr(rev(names(versions)[2:(length(names(versions)) - 1)]),
            2, 6))) +

    # Add a graph title. This line can be commented out together with the
    # '+' character in the last non-comment line.
    opts(title = "Relay versions\n")

  # Save the generated graph to the following path with given width,
  # height, and resolution.
  ggsave(filename = "website/graphs/descriptors/versions.png",
      width = 8, height = 5, dpi = 72)
}

# Define a function to plot relay platforms. See the similar function
# plot_versions() for details.
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

# Define a function to plot advertised bandwidth. See the similar function
# plot_versions() for details.
plot_bandwidth <- function() {
  ggplot(bandwidth, aes(x = date, y = advbw / 1024)) + geom_line() +
    scale_x_date(name = "") +
    scale_y_continuous(name = "Bandwidth (MiB/s)",
        limits = c(0, max(bandwidth$advbw / 1024, na.rm = TRUE))) +
    opts(title = "Total advertised bandwidth\n")
  ggsave(filename = "website/graphs/descriptors/bandwidth.png",
      width = 8, height = 5, dpi = 72)
}

# If a CSV file with version data exists, ...
if (file.exists("stats/version-stats")) {

  # Read in the file, declare that the first line has the column names,
  # and define the type of the first column as Date.
  versions <- read.csv("stats/version-stats", header = TRUE,
      colClasses = c(date = "Date"))

  # Write the same data to disk without putting in quotes around strings
  # and without adding row numbers. This file can be downloaded by others
  # to run their own evaluations.
  write.csv(versions, "website/csv/versions.csv", quote = FALSE,
    row.names = FALSE)

  # Call the function defined above to plot relay versions.
  plot_versions()
}

# If a CSV file with platform data exists, read it, copy it to the
# website, and plot a platform graph.
if (file.exists("stats/platform-stats")) {
  platforms <- read.csv("stats/platform-stats", header = TRUE,
      colClasses = c(date = "Date"))
  write.csv(platforms, "website/csv/platforms.csv", quote = FALSE,
    row.names = FALSE)
  plot_platforms()
}

# If a CSV file with bandwidth data exists, read it, copy it to the
# website, and plot a bandwidth graph.
if (file.exists("stats/bandwidth-stats")) {
  bandwidth <- read.csv("stats/bandwidth-stats", header = TRUE,
      colClasses = c(date = "Date"))
  write.csv(bandwidth, "website/csv/bandwidth.csv", quote = FALSE,
    row.names = FALSE)
  plot_bandwidth()
}

