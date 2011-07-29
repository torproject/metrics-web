countrynames <- function(countries) {
  sapply(countries, countryname)
}

write_direct_users <- function(start, end, path) {
  drv <- dbDriver("PostgreSQL")
  con <- dbConnect(drv, user = dbuser, password = dbpassword, dbname = db)
  q <- paste("SELECT date, country, r, bwp, brn, bwn, brp, bwr, brr ",
      "FROM user_stats WHERE date >= '", start, "' AND date <= '", end,
      "' AND date < (SELECT MAX(date) FROM user_stats) - 1 ",
      "ORDER BY date, country", sep = "")
  rs <- dbSendQuery(con, q)
  u <- fetch(rs, n = -1)
  dbDisconnect(con)
  dbUnloadDriver(drv)
  d <- data.frame(date = u$date, country = u$country,
       directusers = floor(u$r * (u$bwp * u$brn / u$bwn - u$brp) /
               (u$bwr * u$brn / u$bwn - u$brr) / 10))
  d <- aggregate(d$directusers, by = list(country = d$country), mean)
  total <- d[d$country == "zy", "x"]
  d <- d[d$country != "zy", ]
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

