options(scipen = 15)

export_networksize <- function(path) {
  s <- read.csv("/srv/metrics.torproject.org/web/stats/servers.csv",
    stringsAsFactors = FALSE)
  s <- s[s$flag == '' & s$country == '' & s$version == '' &
         s$platform == '' & s$ec2bridge == '',
         c("date", "relays", "bridges")]
  write.csv(s, path, quote = FALSE, row.names = FALSE)
}

export_cloudbridges <- function(path) {
  s <- read.csv("/srv/metrics.torproject.org/web/stats/servers.csv",
    stringsAsFactors = FALSE)
  s <- s[s$flag == '' & s$country == '' & s$version == '' &
         s$platform == '' & s$ec2bridge == 't', ]
  cloudbridges <- data.frame(date = s$date, cloudbridges = s$bridges)
  write.csv(cloudbridges, path, quote = FALSE, row.names = FALSE)
}

export_relaycountries <- function(path) {
  s <- read.csv("/srv/metrics.torproject.org/web/stats/servers.csv",
    stringsAsFactors = FALSE)
  s <- s[s$flag == '' & s$country != '' & s$version == '' &
         s$platform == '' & s$ec2bridge == '',
         c("date", "country", "relays")]
  write.csv(s, path, quote = FALSE, row.names = FALSE)
}

export_versions <- function(path) {
  s <- read.csv("/srv/metrics.torproject.org/web/stats/servers.csv",
    stringsAsFactors = FALSE)
  s <- s[s$flag == '' & s$country == '' & s$version != '' &
         s$platform == '' & s$ec2bridge == '',
         c("date", "version", "relays")]
  versions <- cast(s, date ~ version, value = "relays")
  versions <- versions[order(versions$date), ]
  write.csv(versions, path, quote = FALSE, row.names = FALSE)
}

export_platforms <- function(path) {
  s <- read.csv("/srv/metrics.torproject.org/web/stats/servers.csv",
    stringsAsFactors = FALSE)
  s <- s[s$flag == '' & s$country == '' & s$version == '' &
         s$platform != '' & s$ec2bridge == '',
         c("date", "platform", "relays")]
  s <- data.frame(date = s$date,
                  platform = ifelse(s$platform == 'FreeBSD', 'bsd',
                  tolower(s$platform)), relays = s$relays)
  s <- cast(s, date ~ platform, value = "relays")
  platforms <- s[order(s$date), ]
  write.csv(platforms, path, quote = FALSE, row.names = FALSE)
}

export_bandwidth <- function(path) {
  b <- read.csv("/srv/metrics.torproject.org/web/stats/bandwidth.csv",
    stringsAsFactors = FALSE)
  b <- b[b$isexit == '' & b$isguard == '', ]
  b <- data.frame(date = as.Date(b$date, "%Y-%m-%d"),
                  bwadv = b$advbw,
                  bwhist = floor((b$bwread + b$bwwrite) / 2))
  b <- b[order(b$date), ]
  write.csv(b, path, quote = FALSE, row.names = FALSE)
}

export_bwhist_flags <- function(path) {
  b <- read.csv("/srv/metrics.torproject.org/web/stats/bandwidth.csv",
    stringsAsFactors = FALSE)
  b <- b[b$isexit != '' & b$isguard != '' & !is.na(b$bwread) &
         !is.na(b$bwwrite), ]
  b <- data.frame(date = as.Date(b$date, "%Y-%m-%d"),
                  isexit = b$isexit == 't', isguard = b$isguard == 't',
                  read = b$bwread, written = b$bwwrite)
  write.csv(b, path, quote = FALSE, row.names = FALSE)
}

export_dirbytes <- function(path) {
  b <- read.csv("/srv/metrics.torproject.org/web/stats/bandwidth.csv",
    stringsAsFactors = FALSE)
  b <- b[b$isexit == '' & b$isguard == '' & !is.na(b$dirread) &
         !is.na(b$dirwrite), ]
  b <- data.frame(date = as.Date(b$date, "%Y-%m-%d"),
                  dirread = b$dirread, dirwrite = b$dirwrite)
  b <- b[order(b$date), ]
  write.csv(b, path, quote = FALSE, row.names = FALSE)
}

export_relayflags <- function(path) {
  s <- read.csv("/srv/metrics.torproject.org/web/stats/servers.csv",
    stringsAsFactors = FALSE)
  s <- s[s$country == '' & s$version == '' & s$platform == '' &
         s$ec2bridge == '', ]
  s <- data.frame(date = as.Date(s$date, "%Y-%m-%d"),
                  flag = ifelse(s$flag == '', 'running', tolower(s$flag)),
                  relays = s$relays)
  s <- cast(s, date ~ flag, value = "relays")
  relayflags <- s[order(s$date), ]
  write.csv(relayflags, path, quote = FALSE, row.names = FALSE)
}

export_torperf <- function(path) {
  t <- read.csv("/srv/metrics.torproject.org/web/stats/torperf.csv",
    stringsAsFactors = FALSE)
  t <- data.frame(
     source = paste(ifelse(t$source == '', 'all', t$source),
                    ifelse(t$size == 50 * 1024, '50kb',
                           ifelse(t$size == 1024 * 1024, '1mb', '5mb')),
                    sep = '-'),
     date = as.Date(t$date, "%Y-%m-%d"),
     q1 = t$q1, md = t$md, q3 = t$q3)
  torperf <- t[order(t$source, t$date), ]
  write.csv(torperf, path, quote = FALSE, row.names = FALSE)
}

export_torperf_failures <- function(path) {
  t <- read.csv("/srv/metrics.torproject.org/web/stats/torperf.csv",
    stringsAsFactors = FALSE)
  t <- data.frame(
     source = paste(ifelse(t$source == '', 'all', t$source),
                    ifelse(t$size == 50 * 1024, '50kb',
                           ifelse(t$size == 1024 * 1024, '1mb', '5mb')),
                    sep = '-'),
     date = as.Date(t$date, "%Y-%m-%d"),
     timeouts = t$timeouts, failures = t$failures, requests = t$requests)
  torperf <- t[order(t$source, t$date), ]
  write.csv(torperf, path, quote = FALSE, row.names = FALSE)
}

export_connbidirect <- function(path) {
  c <- read.csv("/srv/metrics.torproject.org/web/stats/connbidirect.csv",
    stringsAsFactors = FALSE)
  write.csv(format(c, trim = TRUE, scientific = FALSE), path,
      quote = FALSE, row.names = FALSE)
}

export_bandwidth_flags <- function(path) {
  b <- read.csv("/srv/metrics.torproject.org/web/stats/bandwidth.csv",
    stringsAsFactors = FALSE)
  b <- b[b$isexit != '' & b$isguard != '', ]
  b <- data.frame(date = as.Date(b$date, "%Y-%m-%d"),
                  isexit = b$isexit == 't', isguard = b$isguard == 't',
                  advbw = b$advbw,
                  bwhist = floor((b$bwread + b$bwwrite) / 2))
  b <- rbind(
    data.frame(b[b$isguard == TRUE, ], flag = "guard"),
    data.frame(b[b$isexit == TRUE, ], flag = "exit"))
  b <- data.frame(date = b$date, advbw = b$advbw, bwhist = b$bwhist,
                  flag = b$flag)
  b <- aggregate(list(advbw = b$advbw, bwhist = b$bwhist),
                 by = list(date = b$date, flag = b$flag), FUN = sum,
                 na.rm = TRUE, na.action = NULL)
  b <- melt(b, id.vars = c("date", "flag"))
  b <- data.frame(date = b$date, type = b$variable, flag = b$flag,
                  value = b$value)
  b <- b[b$value > 0, ]
  write.csv(b, path, quote = FALSE, row.names = FALSE)
}

export_userstats <- function(path) {
  c <- read.csv("/srv/metrics.torproject.org/web/stats/clients.csv",
    stringsAsFactors = FALSE)
  c <- data.frame(date = c$date, node = c$node, country = c$country,
                  transport = c$transport, version = c$version,
                  frac = c$frac, users = c$clients)
  write.csv(format(c, trim = TRUE, scientific = FALSE), path,
      quote = FALSE, row.names = FALSE)
}

help_export_monthly_userstats <- function(path, aggr_fun) {
  c <- read.csv("/srv/metrics.torproject.org/web/stats/clients.csv",
    stringsAsFactors = FALSE)
  c <- c[c$country != '' & c$transport == '' & c$version == '', ]
  u <- data.frame(date = c$date, country = c$country, users = c$clients,
                  stringsAsFactors = FALSE)
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

export_userstats_detector <- function(path) {
  c <- read.csv("/srv/metrics.torproject.org/web/stats/clients.csv",
    stringsAsFactors = FALSE)
  c <- c[c$country != '' & c$transport == '' & c$version == '' &
         c$node == 'relay', ]
  u <- data.frame(country = c$country, date = c$date, users = c$clients,
                  stringsAsFactors = FALSE)
  u <- rbind(u, data.frame(country = "zy",
                aggregate(list(users = u$users),
                          by = list(date = u$date), sum)))
  u <- data.frame(date = u$date, country = u$country,
                  users = floor(u$users))
  u <- cast(u, date ~ country, value = "users")
  names(u)[names(u) == "zy"] <- "all"
  write.csv(u, path, quote = FALSE, row.names = FALSE)
}

