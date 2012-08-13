countrynames <- function(countries) {
  sapply(countries, countryname)
}

write_direct_users <- function(start, end, path) {
  drv <- dbDriver("PostgreSQL")
  con <- dbConnect(drv, user = dbuser, password = dbpassword, dbname = db)
  q <- paste("SELECT date, country, r, bwp, brn, bwn, brp, bwr, brr ",
      "FROM user_stats WHERE date >= '", start, "' AND date <= '", end,
      "' AND date < current_date - 3 ORDER BY date, country", sep = "")
  rs <- dbSendQuery(con, q)
  u <- fetch(rs, n = -1)
  dbDisconnect(con)
  dbUnloadDriver(drv)
  d <- data.frame(date = u$date, country = u$country,
       directusers = floor(u$r * (u$bwp * u$brn / u$bwn - u$brp) /
               (u$bwr * u$brn / u$bwn - u$brr) / 10))
  d <- aggregate(d$directusers, by = list(country = d$country), mean)
  total <- d[d$country == "zy", "x"]
  d <- d[!(d$country %in% c("zy", "??", "a1", "a2", "o1", "ap", "eu")), ]
  d <- data.frame(country = d$country, directusers = d$x)
  d <- d[order(d$directusers, decreasing = TRUE), ]
  d <- d[1:10, ]
  d <- data.frame(
    cc = as.character(d$country),
    country = sub('the ', '', countrynames(as.character(d$country))),
    abs = round(d$directusers),
    rel = round(100 * d$directusers / total, 2))
  write.csv(d, path, quote = FALSE, row.names = FALSE)
}

write_censorship_events <- function(start, end, path) {
  drv <- dbDriver("PostgreSQL")
  con <- dbConnect(drv, user = dbuser, password = dbpassword, dbname = db)
  q <- paste("SELECT date, country, r, bwp, brn, bwn, brp, bwr, brr ",
      "FROM user_stats WHERE date >= '", start, "' AND date <= '", end,
      "' AND date < current_date - 1", sep = "")
  rs <- dbSendQuery(con, q)
  u <- fetch(rs, n = -1)
  dbDisconnect(con)
  dbUnloadDriver(drv)
  u <- data.frame(date = u$date, country = u$country,
       users = u$r * (u$bwp * u$brn / u$bwn - u$brp) /
               (u$bwr * u$brn / u$bwn - u$brr) / 10)
  dates <- seq(from = as.Date(start, "%Y-%m-%d"),
      to = as.Date(end, "%Y-%m-%d"), by="1 day")
  missing <- setdiff(dates, u$date)
  r <- read.csv(
    "/srv/metrics.torproject.org/web/detector/direct-users-ranges.csv",
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

