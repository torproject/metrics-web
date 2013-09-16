export_networksize <- function(path) {
  drv <- dbDriver("PostgreSQL")
  con <- dbConnect(drv, user = dbuser, password = dbpassword, dbname = db)
  q <- paste("SELECT date, avg_running AS relays FROM network_size",
      "WHERE date < current_date - 1")
  rs <- dbSendQuery(con, q)
  relays <- fetch(rs, n = -1)
  q <- paste("SELECT date, avg_running AS bridges",
      "FROM bridge_network_size WHERE date < current_date - 1")
  rs <- dbSendQuery(con, q)
  bridges <- fetch(rs, n = -1)
  dbDisconnect(con)
  dbUnloadDriver(drv)
  networksize <- rbind(melt(relays, "date"), melt(bridges, "date"))
  networksize <- cast(networksize, date ~ variable)
  networksize <- networksize[order(networksize$date), ]
  write.csv(networksize, path, quote = FALSE, row.names = FALSE)
}

export_cloudbridges <- function(path) {
  drv <- dbDriver("PostgreSQL")
  con <- dbConnect(drv, user = dbuser, password = dbpassword, dbname = db)
  q <- paste("SELECT date, avg_running_ec2 AS cloudbridges",
      "FROM bridge_network_size WHERE date < current_date - 1",
      "ORDER BY date")
  rs <- dbSendQuery(con, q)
  cloudbridges <- fetch(rs, n = -1)
  dbDisconnect(con)
  dbUnloadDriver(drv)
  write.csv(cloudbridges, path, quote = FALSE, row.names = FALSE)
}

export_relaycountries <- function(path) {
  drv <- dbDriver("PostgreSQL")
  con <- dbConnect(drv, user = dbuser, password = dbpassword, dbname = db)
  q <- paste("SELECT date, country, relays FROM relay_countries",
      "WHERE date < current_date - 1 ORDER BY date, country")
  rs <- dbSendQuery(con, q)
  relays <- fetch(rs, n = -1)
  dbDisconnect(con)
  dbUnloadDriver(drv)
  write.csv(relays, path, quote = FALSE, row.names = FALSE)
}

export_versions <- function(path) {
  drv <- dbDriver("PostgreSQL")
  con <- dbConnect(drv, user = dbuser, password = dbpassword, dbname = db)
  q <- paste("SELECT date, version, relays FROM relay_versions",
      "WHERE date < current_date - 1")
  rs <- dbSendQuery(con, q)
  versions <- fetch(rs, n = -1)
  dbDisconnect(con)
  dbUnloadDriver(drv)
  versions <- cast(versions, date ~ version, value = "relays")
  versions <- versions[order(versions$date), ]
  write.csv(versions, path, quote = FALSE, row.names = FALSE)
}

export_platforms <- function(path) {
  drv <- dbDriver("PostgreSQL")
  con <- dbConnect(drv, user = dbuser, password = dbpassword, dbname = db)
  q <- paste("SELECT date, avg_linux AS linux, avg_darwin AS darwin,",
      "avg_bsd AS bsd, avg_windows AS windows, avg_other AS other",
      "FROM relay_platforms WHERE date < current_date - 1 ORDER BY date")
  rs <- dbSendQuery(con, q)
  platforms <- fetch(rs, n = -1)
  dbDisconnect(con)
  dbUnloadDriver(drv)
  write.csv(platforms, path, quote = FALSE, row.names = FALSE)
}

export_bandwidth <- function(path) {
  drv <- dbDriver("PostgreSQL")
  con <- dbConnect(drv, user = dbuser, password = dbpassword, dbname = db)
  q <- paste("SELECT date, bwadvertised FROM total_bandwidth",
      "WHERE date < current_date - 3")
  rs <- dbSendQuery(con, q)
  bw_desc <- fetch(rs, n = -1)
  q <- paste("SELECT date, read, written FROM total_bwhist",
      "WHERE date < current_date - 3")
  rs <- dbSendQuery(con, q)
  bw_hist <- fetch(rs, n = -1)
  dbDisconnect(con)
  dbUnloadDriver(drv)
  bandwidth <- rbind(data.frame(date = bw_desc$date,
      value = bw_desc$bwadvertised, variable = "bwadv"),
    data.frame(date = bw_hist$date, value = floor((bw_hist$read +
      bw_hist$written) / (2 * 86400)), variable = "bwhist"))
  bandwidth <- cast(bandwidth, date ~ variable, value = "value")
  bandwidth <- bandwidth[order(bandwidth$date), ]
  write.csv(bandwidth, path, quote = FALSE, row.names = FALSE)
}

export_bwhist_flags <- function(path) {
  drv <- dbDriver("PostgreSQL")
  con <- dbConnect(drv, user = dbuser, password = dbpassword, dbname = db)
  q <- paste("SELECT date, isexit, isguard, read / 86400 AS read,",
      "written / 86400 AS written",
      "FROM bwhist_flags WHERE date < current_date - 3",
      "ORDER BY date, isexit, isguard")
  rs <- dbSendQuery(con, q)
  bw <- fetch(rs, n = -1)
  dbDisconnect(con)
  dbUnloadDriver(drv)
  write.csv(bw, path, quote = FALSE, row.names = FALSE)
}

export_dirbytes <- function(path) {
  drv <- dbDriver("PostgreSQL")
  con <- dbConnect(drv, user = dbuser, password = dbpassword, dbname = db)
  q <- paste("SELECT date, dr, dw, brp, bwp, brd, bwd FROM user_stats",
      "WHERE country = 'zy' AND bwp / bwd <= 3",
      "AND date < current_date - 3 ORDER BY date")
  rs <- dbSendQuery(con, q)
  dir <- fetch(rs, n = -1)
  dbDisconnect(con)
  dbUnloadDriver(drv)
  dir <- data.frame(date = dir$date,
      dirread = floor(dir$dr * dir$brp / dir$brd / 86400),
      dirwrite = floor(dir$dw * dir$bwp / dir$bwd / 86400))
  dir <- na.omit(dir)
  write.csv(dir, path, quote = FALSE, row.names = FALSE)
}

export_relayflags <- function(path) {
  drv <- dbDriver("PostgreSQL")
  con <- dbConnect(drv, user = dbuser, password = dbpassword, dbname = db)
  q <- paste("SELECT date, avg_running AS running, avg_exit AS exit,",
      "avg_guard AS guard, avg_fast AS fast, avg_stable AS stable,",
      "avg_hsdir AS hsdir",
      "FROM network_size WHERE date < current_date - 1 ORDER BY date")
  rs <- dbSendQuery(con, q)
  relayflags <- fetch(rs, n = -1)
  dbDisconnect(con)
  dbUnloadDriver(drv)
  write.csv(relayflags, path, quote = FALSE, row.names = FALSE)
}

export_direct_users <- function(path) {
  drv <- dbDriver("PostgreSQL")
  con <- dbConnect(drv, user = dbuser, password = dbpassword, dbname = db)
  q <- paste("SELECT date, country, r, bwp, brn, bwn, brp, bwr, brr",
      "FROM user_stats WHERE date < current_date - 3",
      "ORDER BY date, country")
  rs <- dbSendQuery(con, q)
  u <- fetch(rs, n = -1)
  dbDisconnect(con)
  dbUnloadDriver(drv)
  directusers <- data.frame(date = u$date, country = u$country,
       directusers = floor(u$r * (u$bwp * u$brn / u$bwn - u$brp) /
               (u$bwr * u$brn / u$bwn - u$brr) / 10))
  directusers <- cast(directusers, date ~ country, value = "directusers")
  names(directusers)[names(directusers) == "zy"] <- "all"
  write.csv(directusers, path, quote = FALSE, row.names = FALSE)
}

export_bridge_users <- function(path) {
  drv <- dbDriver("PostgreSQL")
  con <- dbConnect(drv, user = dbuser, password = dbpassword, dbname = db)
  q <- paste("SELECT date, country, users AS bridgeusers",
      "FROM bridge_stats WHERE date < current_date - 3",
      "ORDER BY date, country")
  rs <- dbSendQuery(con, q)
  bridgeusers <- fetch(rs, n = -1)
  dbDisconnect(con)
  dbUnloadDriver(drv)
  bridgeusers <- cast(bridgeusers, date ~ country, value = "bridgeusers")
  names(bridgeusers)[names(bridgeusers) == "zy"] <- "all"
  write.csv(bridgeusers, path, quote = FALSE, row.names = FALSE)
}

export_torperf <- function(path) {
  drv <- dbDriver("PostgreSQL")
  con <- dbConnect(drv, user = dbuser, password = dbpassword, dbname = db)
  q <- paste("SELECT source, date, q1, md, q3 FROM torperf_stats",
      "WHERE date < current_date - 1 ORDER BY source, date")
  rs <- dbSendQuery(con, q)
  torperf <- fetch(rs, n = -1)
  dbDisconnect(con)
  dbUnloadDriver(drv)
  write.csv(torperf, path, quote = FALSE, row.names = FALSE)
}

export_torperf_failures <- function(path) {
  drv <- dbDriver("PostgreSQL")
  con <- dbConnect(drv, user = dbuser, password = dbpassword, dbname = db)
  q <- paste("SELECT source, date, timeouts, failures, requests",
      "FROM torperf_stats WHERE date < current_date - 1",
      "ORDER BY source, date")
  rs <- dbSendQuery(con, q)
  torperf <- fetch(rs, n = -1)
  dbDisconnect(con)
  dbUnloadDriver(drv)
  write.csv(torperf, path, quote = FALSE, row.names = FALSE)
}

help_export_monthly_users <- function(path, aggr_fun) {
  drv <- dbDriver("PostgreSQL")
  con <- dbConnect(drv, user = dbuser, password = dbpassword, dbname = db)
  q <- paste("SELECT date, country, r, bwp, brn, bwn, brp, bwr, brr",
      "FROM user_stats WHERE date < current_date - 3",
      "ORDER BY date, country")
  rs <- dbSendQuery(con, q)
  u <- fetch(rs, n = -1)
  direct <- data.frame(date = u$date, country = u$country,
       users = u$r * (u$bwp * u$brn / u$bwn - u$brp) /
               (u$bwr * u$brn / u$bwn - u$brr) / 10)
  q <- paste("SELECT date, country, FLOOR(users) AS users",
      "FROM bridge_stats WHERE date < current_date - 3",
      "ORDER BY date, country")
  rs <- dbSendQuery(con, q)
  bridge <- fetch(rs, n = -1)
  dbDisconnect(con)
  dbUnloadDriver(drv)
  users <- rbind(bridge, direct)
  users <- aggregate(users$users,
      by = list(date = users$date, country = users$country), sum)
  users <- aggregate(users$x, by = list(month = substr(users$date, 1, 7),
      country = users$country), aggr_fun)
  users <- cast(users, country ~ month, value = "x")
  users[users$country == "zy", 1] <- "all"
  users[, 2:length(users)] <- floor(users[, 2:length(users)])
  write.csv(users, path, quote = FALSE, row.names = FALSE)
}

export_monthly_users_peak <- function(path) {
  help_export_monthly_users(path, max)
}

export_monthly_users_average <- function(path) {
  help_export_monthly_users(path, mean)
}

export_connbidirect <- function(path) {
  drv <- dbDriver("PostgreSQL")
  con <- dbConnect(drv, user = dbuser, password = dbpassword, dbname = db)
  q <- paste("SELECT DATE(statsend) AS date, source, belownum AS below,",
      "readnum AS read, writenum AS write, bothnum AS \"both\"",
      "FROM connbidirect WHERE DATE(statsend) < current_date - 1",
      "ORDER BY 1, 2")
  rs <- dbSendQuery(con, q)
  c <- fetch(rs, n = -1)
  dbDisconnect(con)
  dbUnloadDriver(drv)
  write.csv(format(c, trim = TRUE, scientific = FALSE), path, 
      quote = FALSE, row.names = FALSE)
}

export_dirreq_stats <- function(path) {
  drv <- dbDriver("PostgreSQL")
  con <- dbConnect(drv, user = dbuser, password = dbpassword, dbname = db)
  q <- paste("SELECT date, r, bwp, brp, bwn, brn, bwr, brr ",
      "FROM user_stats WHERE date < current_date - 3",
      "AND country = 'zy' ORDER BY date", sep = "")
  rs <- dbSendQuery(con, q)
  u <- fetch(rs, n = -1)
  dbDisconnect(con)
  dbUnloadDriver(drv)
  u <- data.frame(date = u$date,
       requests = u$r,
       fraction = (u$bwr * u$brn / u$bwn - u$brr) /
                (u$bwp * u$brn / u$bwn - u$brp),
       users = u$r * (u$bwp * u$brn / u$bwn - u$brp) /
               (u$bwr * u$brn / u$bwn - u$brr) / 10)
  write.csv(format(u, trim = TRUE, scientific = FALSE), path,
      quote = FALSE, row.names = FALSE)
}

export_bandwidth_flags <- function(path) {
  drv <- dbDriver("PostgreSQL")
  con <- dbConnect(drv, user = dbuser, password = dbpassword, dbname = db) 
  q <- paste("SELECT date, isexit, isguard, bwadvertised AS value",
      "FROM bandwidth_flags WHERE date < current_date - 3")
  rs <- dbSendQuery(con, q)
  bw_desc <- fetch(rs, n = -1) 
  q <- paste("SELECT date, isexit, isguard,",
      "(read + written) / (2 * 86400) AS value",
      "FROM bwhist_flags WHERE date < current_date - 3")
  rs <- dbSendQuery(con, q)
  bw_hist <- fetch(rs, n = -1) 
  dbDisconnect(con)
  dbUnloadDriver(drv)
  bandwidth <- rbind(data.frame(bw_desc, type = "advbw"),
      data.frame(bw_hist, type = "bwhist"))
  bandwidth <- rbind(
    data.frame(bandwidth[bandwidth$isguard == TRUE, ], flag = "guard"),
    data.frame(bandwidth[bandwidth$isexit == TRUE, ], flag = "exit"))
  bandwidth <- aggregate(list(value = bandwidth$value),
    by = list(date = bandwidth$date, type = bandwidth$type,
    flag = bandwidth$flag), FUN = sum)
  write.csv(format(bandwidth, trim = TRUE, scientific = FALSE), path,
      quote = FALSE, row.names = FALSE)
}

export_userstats <- function(path) {
  u <- read.csv(paste("/srv/metrics.torproject.org/task-8462-graphs/",
    "task-8462/userstats.csv", sep = ""),
    stringsAsFactors = FALSE)
  write.csv(format(u, trim = TRUE, scientific = FALSE), path,
      quote = FALSE, row.names = FALSE)
}

help_export_monthly_userstats <- function(path, aggr_fun) {
  u <- read.csv(paste("/srv/metrics.torproject.org/task-8462-graphs/",
    "task-8462/userstats.csv", sep = ""),
    stringsAsFactors = FALSE)
  u <- u[u$country != '' & u$transport == '' & u$version == '',
         c("date", "country", "users")]
  u <- aggregate(list(users = u$users),
                      by = list(date = u$date, country = u$country), sum)
  u <- aggregate(list(users = u$users),
                      by = list(country = u$country,
                                month = substr(u$date, 1, 7)), aggr_fun)
  u <- rbind(u, data.frame(country = "zy",
                aggregate(list(users = u$users),
                          by = list(month = u$month), sum)))
  u <- cast(u, country ~ month, value = "users")
  u[u$country == "zy", "country"] <- "all"
  u[, 2:length(u)] <- floor(u[, 2:length(u)])
  write.csv(u, path, quote = FALSE, row.names = FALSE)
}

export_monthly_userstats_peak <- function(path) {
  help_export_monthly_userstats(path, max)
}

export_monthly_userstats_average <- function(path) {
  help_export_monthly_userstats(path, mean)
}

