countrynames <- function(countries) {
  sapply(countries, countryname)
}

write_userstats <- function(start, end, node, path) {
  end <- min(end, as.character(Sys.Date()))
  c <- read.csv(paste("/srv/metrics.torproject.org/metrics/shared/stats/",
                "clients.csv", sep = ""), stringsAsFactors = FALSE)
  c <- c[c$date >= start & c$date <= end & c$country != '' &
         c$transport == '' & c$version == '' & c$node == node, ]
  u <- data.frame(country = c$country, users = c$clients,
                  stringsAsFactors = FALSE)
  u <- u[!is.na(u$users), ]
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
    rel = sprintf("%.2f", round(100 * u$users / total, 2)))
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
  c <- read.csv(paste("/srv/metrics.torproject.org/metrics/shared/stats/",
                "clients.csv", sep = ""), stringsAsFactors = FALSE)
  c <- c[c$date >= start & c$date <= end & c$country != '' &
         c$transport == '' & c$version == '' & c$node == 'relay', ]
  r <- data.frame(date = c$date, country = c$country,
                  upturn = ifelse(!is.na(c$upper) &
                                  c$clients > c$upper, 1, 0),
                  downturn = ifelse(!is.na(c$lower) &
                                    c$clients <= c$lower, 1, 0))
  r <- aggregate(r[, c("upturn", "downturn")],
    by = list(country = r$country), sum)
  r <- r[(r$country %in% names(countrylist)), ]
  r <- r[order(r$downturn, r$upturn, decreasing = TRUE), ]
  r <- r[1:10, ]
  r <- data.frame(cc = r$country,
    country = sub('the ', '', countrynames(as.character(r$country))),
    downturns = r$downturn,
    upturns = r$upturn)
  write.csv(r, path, quote = FALSE, row.names = FALSE)
}

