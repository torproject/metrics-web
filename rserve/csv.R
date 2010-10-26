export_networksize <- function(path) {
  drv <- dbDriver("PostgreSQL")
  con <- dbConnect(drv, user = dbuser, password = dbpassword, dbname = db)
  q <- "SELECT date, avg_running AS relays FROM network_size"
  rs <- dbSendQuery(con, q)
  relays <- fetch(rs, n = -1)
  q <- "SELECT date, avg_running AS bridges FROM bridge_network_size"
  rs <- dbSendQuery(con, q)
  bridges <- fetch(rs, n = -1)
  dbDisconnect(con)
  dbUnloadDriver(drv)
  networksize <- rbind(melt(relays, "date"), melt(bridges, "date"))
  networksize <- cast(networksize, date ~ variable)
  networksize <- networksize[order(networksize$date), ]
  write.csv(networksize, path, quote = FALSE, row.names = FALSE)
}

export_versions <- function(path) {
  drv <- dbDriver("PostgreSQL")
  con <- dbConnect(drv, user = dbuser, password = dbpassword, dbname = db)
  q <- "SELECT date, version, relays FROM relay_versions"
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
      "FROM relay_platforms ORDER BY date")
  rs <- dbSendQuery(con, q)
  platforms <- fetch(rs, n = -1)
  dbDisconnect(con)
  dbUnloadDriver(drv)
  write.csv(platforms, path, quote = FALSE, row.names = FALSE)
}

export_bandwidth <- function(path) {
  drv <- dbDriver("PostgreSQL")
  con <- dbConnect(drv, user = dbuser, password = dbpassword, dbname = db)
  q <- "SELECT date, bwadvertised FROM total_bandwidth"
  rs <- dbSendQuery(con, q)
  bw_desc <- fetch(rs, n = -1)
  q <- paste("SELECT date, read, written FROM total_bwhist",
      "WHERE date < (SELECT MAX(date) FROM total_bwhist) - 1")
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

export_dirbytes <- function(path) {
  drv <- dbDriver("PostgreSQL")
  con <- dbConnect(drv, user = dbuser, password = dbpassword, dbname = db)
  q <- paste("SELECT date, read / 86400 AS read,",
      "written / 86400 AS written, dirread / 86400 AS dirread,",
      "dirwritten / 86400 AS dirwritten FROM total_bwhist",
      "WHERE date < (SELECT MAX(date) FROM total_bwhist) - 1",
      "ORDER BY date")
  rs <- dbSendQuery(con, q)
  bw_hist <- fetch(rs, n = -1)
  dbDisconnect(con)
  dbUnloadDriver(drv)
  write.csv(bw_hist, path, quote = FALSE, row.names = FALSE)
}

export_relayflags <- function(path) {
  drv <- dbDriver("PostgreSQL")
  con <- dbConnect(drv, user = dbuser, password = dbpassword, dbname = db)
  q <- paste("SELECT date, avg_running AS running, avg_exit AS exit,",
      "avg_guard AS guard, avg_fast AS fast, avg_stable AS stable",
      "FROM network_size ORDER BY date")
  rs <- dbSendQuery(con, q)
  relayflags <- fetch(rs, n = -1)
  dbDisconnect(con)
  dbUnloadDriver(drv)
  write.csv(relayflags, path, quote = FALSE, row.names = FALSE)
}

export_relayflags_hour <- function(path) {
  drv <- dbDriver("PostgreSQL")
  con <- dbConnect(drv, user = dbuser, password = dbpassword, dbname = db)
   q <- paste("SELECT validafter, avg_running AS running,",
      "avg_exit AS exit, avg_guard AS guard, avg_fast AS fast,",
      "avg_stable AS stable FROM network_size_hour ORDER BY validafter")
  rs <- dbSendQuery(con, q)
  relayflags <- fetch(rs, n = -1)
  dbDisconnect(con)
  dbUnloadDriver(drv)
  write.csv(relayflags, path, quote = FALSE, row.names = FALSE)
}

export_new_users <- function(path) {
  drv <- dbDriver("PostgreSQL")
  con <- dbConnect(drv, user = dbuser, password = dbpassword, dbname = db)
  q <- paste("SELECT date, country, 6 * requests AS newusers",
      "FROM dirreq_stats",
      "WHERE source = '68333D0761BCF397A587A0C0B963E4A9E99EC4D3'",
      "OR source = 'F2044413DAC2E02E3D6BCF4735A19BCA1DE97281'",
      "ORDER BY date, country")
  rs <- dbSendQuery(con, q)
  newusers <- fetch(rs, n = -1)
  dbDisconnect(con)
  dbUnloadDriver(drv)
  newusers <- cast(newusers, date ~ country, value = "newusers")
  names(newusers)[names(newusers) == "zy"] <- "all"
  write.csv(newusers, path, quote = FALSE, row.names = FALSE)
}

export_direct_users <- function(path) {
  drv <- dbDriver("PostgreSQL")
  con <- dbConnect(drv, user = dbuser, password = dbpassword, dbname = db)
  q <- paste("SELECT date, country,",
      "FLOOR(10 * requests / share) AS directusers",
      "FROM dirreq_stats WHERE share >= 1",
      "AND source = '8522EB98C91496E80EC238E732594D1509158E77'",
      "ORDER BY date, country")
  rs <- dbSendQuery(con, q)
  directusers <- fetch(rs, n = -1)
  dbDisconnect(con)
  dbUnloadDriver(drv)
  directusers <- cast(directusers, date ~ country, value = "directusers")
  names(directusers)[names(directusers) == "zy"] <- "all"
  write.csv(directusers, path, quote = FALSE, row.names = FALSE)
}

export_bridge_users <- function(path) {
  drv <- dbDriver("PostgreSQL")
  con <- dbConnect(drv, user = dbuser, password = dbpassword, dbname = db)
  q <- paste("SELECT date, country, users AS bridgeusers",
      "FROM bridge_stats",
      "WHERE date < (SELECT MAX(date) FROM bridge_stats)",
      "ORDER BY date, country")
  rs <- dbSendQuery(con, q)
  bridgeusers <- fetch(rs, n = -1)
  dbDisconnect(con)
  dbUnloadDriver(drv)
  bridgeusers <- cast(bridgeusers, date ~ country, value = "bridgeusers")
  names(bridgeusers)[names(bridgeusers) == "zy"] <- "all"
  write.csv(bridgeusers, path, quote = FALSE, row.names = FALSE)
}

export_gettor <- function(path) {
  drv <- dbDriver("PostgreSQL")
  con <- dbConnect(drv, user = dbuser, password = dbpassword, dbname = db)
  q <- "SELECT date, bundle, downloads FROM gettor_stats"
  rs <- dbSendQuery(con, q)
  downloads <- fetch(rs, n = -1)
  dbDisconnect(con)
  dbUnloadDriver(drv)
  downloads_total <- downloads[downloads$bundle != "none", ]
  downloads_total <- aggregate(downloads_total$downloads,
      by = list(date = downloads_total$date), sum)
  downloads_en <- downloads[grep("*_en", downloads$bundle), ]
  downloads_en <- aggregate(downloads_en$downloads,
      by = list(date = downloads_en$date), sum)
  downloads_zh_cn <- downloads[grep("*_zh_cn", downloads$bundle), ]
  downloads_zh_cn <- aggregate(downloads_zh_cn$downloads,
      by = list(date = downloads_zh_cn$date), sum)
  downloads_fa <- downloads[grep("*_fa", downloads$bundle), ]
  downloads_fa <- aggregate(downloads_fa$downloads,
      by = list(date = downloads_fa$date), sum)
  downloads <- rbind(
      data.frame(date = downloads_total$date,
        bundle = "total", downloads = downloads_total$x),
      data.frame(date = downloads_en$date,
        bundle = "en", downloads = downloads_en$x),
      data.frame(date = downloads_zh_cn$date,
        bundle = "zh_cn", downloads = downloads_zh_cn$x),
      data.frame(date = downloads_fa$date,
        bundle = "fa", downloads = downloads_fa$x))
  downloads <- cast(downloads, date ~ bundle, value = "downloads")
  downloads <- downloads[order(downloads$date), ]
  write.csv(downloads, path, quote = FALSE, row.names = FALSE)
}

export_torperf <- function(path) {
  drv <- dbDriver("PostgreSQL")
  con <- dbConnect(drv, user = dbuser, password = dbpassword, dbname = db)
  q <- paste("SELECT source, date, q1, md, q3 FROM torperf_stats",
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
  q <- paste("SELECT date, country,",
      "FLOOR(10 * requests / share) AS users",
      "FROM dirreq_stats WHERE share >= 1",
      "AND source = '8522EB98C91496E80EC238E732594D1509158E77'",
      "ORDER BY date, country")
  rs <- dbSendQuery(con, q)
  trusted <- fetch(rs, n = -1)
  q <- paste("SELECT date, country, FLOOR(users) AS users",
      "FROM bridge_stats",
      "WHERE date < (SELECT MAX(date) FROM bridge_stats)",
      "ORDER BY date, country")
  rs <- dbSendQuery(con, q)
  bridge <- fetch(rs, n = -1)
  dbDisconnect(con)
  dbUnloadDriver(drv)
  users <- rbind(bridge, trusted)
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

