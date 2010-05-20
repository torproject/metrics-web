options(warn = -1)
suppressPackageStartupMessages(library("ggplot2"))

# Ensure that both input files exist
if (file.exists("stats/dirreq-stats") &
    file.exists("stats/bridge-stats")) {

  # Read direct user numbers
  dirreq <- read.csv("stats/dirreq-stats", header = TRUE,
    stringsAsFactors = FALSE)
  trustedSub <- subset(dirreq,
    directory %in% "8522EB98C91496E80EC238E732594D1509158E77")
  trustedSub[na.omit(trustedSub$share) == 0,3:length(trustedSub)] <- NA
  trusted <- data.frame(date = trustedSub$date,
    floor(trustedSub[3:(length(trustedSub) - 1)] / trustedSub$share * 10))

  # Read bridge user numbers
  bridge <- read.csv("stats/bridge-stats", header = TRUE,
    stringsAsFactors = FALSE)
  bridge <- bridge[1:length(bridge$date)-1,]
  bridge <- data.frame(date = bridge[,1],
    floor(bridge[,2:length(bridge[1,])]))

  # Melt both data frames and append them
  bridge_melted <- data.frame(melt(bridge, id.vars = "date", na.rm = TRUE),
    source = "bridge")
  direct_melted <- data.frame(melt(trusted, id.vars = "date", na.rm = TRUE),
    source = "direct")
  both_melted <- rbind(bridge_melted, direct_melted)

  # Merge data source (bridge or direct)
  country_day <- aggregate(both_melted$value,
    by = list(date = both_melted$date, country = both_melted$variable), sum)

  # Merge months
  month_mean <- aggregate(country_day$x,
    by = list(month = substr(country_day$date, 1, 7),
    country = country_day$country), mean)
  month_max <- aggregate(country_day$x,
    by = list(month = substr(country_day$date, 1, 7),
    country = country_day$country), max)

  # Convert to final matrices
  month_peak <- t(matrix(month_max$x,
    ncol = length(unique(month_max$country)),
    dimnames = list(unique(month_max$month),
    as.vector(unique(month_max$country)))))
  month_avg <- t(matrix(floor(month_mean$x),
    ncol = length(unique(month_mean$country)),
    dimnames = list(unique(month_mean$month),
    as.vector(unique(month_mean$country)))))

  # Write to disk
  write.csv(month_peak, "website/csv/monthly-users-peak.csv", quote = FALSE)
  write.csv(month_avg, "website/csv/monthly-users-average.csv",
    quote = FALSE)
}

