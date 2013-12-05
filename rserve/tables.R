countrynames <- function(countries) {
  sapply(countries, countryname)
}

write_userstats <- function(start, end, node, path) {
  end <- min(end, as.character(Sys.Date()))
  u <- read.csv(paste("/srv/metrics.torproject.org/task-8462-graphs/",
    "task-8462/userstats.csv", sep = ""),
    stringsAsFactors = FALSE)
  u <- u[u$date >= start & u$date <= end & u$country != '' &
         u$transport == '' & u$version == '' & u$node == node,
         c("country", "users")]
  u <- aggregate(list(users = u$users), by = list(country = u$country),
                 mean)
  total <- sum(u$users)
  u <- u[!(u$country %in% c("zy", "??", "a1", "a2", "o1", "ap", "eu")), ]
  u <- u[order(u$users, decreasing = TRUE), ]
  u <- u[1:10, ]
  u <- data.frame(
    cc = as.character(u$country),
    country = sub('the ', '', countrynames(as.character(u$country))),
    abs = round(u$users),
    rel = round(100 * u$users / total, 2))
  write.csv(u, path, quote = FALSE, row.names = FALSE)
}

write_userstats_relay <- function(start, end, path) {
  write_userstats(start, end, 'relay', path)
}

write_userstats_bridge <- function(start, end, path) {
  write_userstats(start, end, 'bridge', path)
}

write_userstats_censorship_events <- function(start, end, path) {
  end <- min(end, as.character(Sys.Date()))
  u <- read.csv(paste("/srv/metrics.torproject.org/task-8462-graphs/",
    "task-8462/userstats.csv", sep = ""),
    stringsAsFactors = FALSE)
  u <- u[u$date >= start & u$date <= end & u$country != '' &
         u$transport == '' & u$version == '' & u$node == 'relay',
         c("date", "country", "users")]
  r <- read.csv(
    "/srv/metrics.torproject.org/web/detector/userstats-ranges.csv",
    stringsAsFactors = FALSE)
  r <- r[r$date >= start & r$date <= end,
      c("date", "country", "minusers", "maxusers")]
  r <- cast(rbind(melt(u, id.vars = c("date", "country")),
      melt(r, id.vars = c("date", "country"))))
  r <- na.omit(r[r$users < r$minusers | r$users > r$maxusers, ])
  r <- data.frame(date = r$date, country = r$country,
    upturn = ifelse(r$users > r$maxusers, 1, 0),
    downturn = ifelse(r$users < r$minusers, 1, 0))
  r <- aggregate(r[, c("upturn", "downturn")],
    by = list(country = r$country), sum)
  r <- r[!(r$country %in% c("zy", "??", "a1", "a2", "o1", "ap", "eu")), ]
  r <- r[order(r$downturn, r$upturn, decreasing = TRUE), ]
  r <- r[1:10, ]
  r <- data.frame(cc = r$country,
    country = sub('the ', '', countrynames(as.character(r$country))),
    downturns = r$downturn,
    upturns = r$upturn)
  write.csv(r, path, quote = FALSE, row.names = FALSE)
}

