plot_networksize_line <- function(start, end, path) {
  drv <- dbDriver("PostgreSQL")
  con <- dbConnect(drv, user=dbuser, password=dbpassword, dbname=db)
  q <- paste("SELECT date, avg_running, avg_exit, avg_guard ",
      "FROM network_size WHERE date >= '", start, "' AND date <= '", end,
      "'", sep = "")
  rs <- dbSendQuery(con, q)
  networksize <- fetch(rs,n=-1)
  networksize <- melt(networksize, id="date")
  ggplot(networksize, aes(x = as.Date(date, "%Y-%m-%d"), y = value,
    colour = variable)) + geom_line(size=1) +
    scale_x_date(name = paste("\nThe Tor Project - ",
        "https://metrics.torproject.org/", sep = "")) +
    scale_y_continuous(name="", limits = c(0, max(networksize$value,
        na.rm = TRUE))) +
    scale_colour_hue("",breaks=c("avg_running","avg_exit","avg_guard"),
        labels=c("Total","Exit","Guard")) +
    opts(title = "Number of relays\n")
  ggsave(filename=path, width=8, height=5, dpi=72)
  dbDisconnect(con)
  dbUnloadDriver(drv)
}
# TODO Instead of displaying pre-defined versions, we could prune all
# versions with 0 relays in the requested interval and only show versions
# with 1 or more relays. We should manually define colors for versions in
# this case, or people will get confused when a version changes its color
# only because they pick a different interval.
plot_versions_line <- function(start, end, path) {
  drv <- dbDriver("PostgreSQL")
  con <- dbConnect(drv, user=dbuser, password=dbpassword, dbname=db)
  q <- paste("SELECT * FROM relay_versions WHERE date >= '", start,
      "' AND date <= '", end, "'", sep = "")
  rs <- dbSendQuery(con, q)
  v <- fetch(rs,n=-1)
  v <- v[, c("date", "0.1.2", "0.2.0", "0.2.1", "0.2.2")]
  v <- melt(v, id="date")
  ggplot(v, aes(x=as.Date(date, "%Y-%m-%d"), y = value, colour=variable)) +
    geom_line(size=1) +
    scale_x_date(name = paste("\nThe Tor Project - ",
        "https://metrics.torproject.org/", sep = "")) +
    scale_y_continuous(name= "",
      limits = c(0, max(v$value, na.rm = TRUE))) +
    scale_colour_brewer(name = "Tor version") +
    opts(title = "Relay versions\n")
  ggsave(filename=path, width=8,height=5,dpi=72)
  dbDisconnect(con)
  dbUnloadDriver(drv)
}
plot_platforms_line <- function(start, end, path) {
  drv <- dbDriver("PostgreSQL")
  con <- dbConnect(drv, user=dbuser, password=dbpassword, dbname=db)
  q <- paste("SELECT date, avg_linux, avg_darwin, avg_bsd, avg_windows, ",
      "avg_other FROM relay_platforms WHERE date >= '", start,
      "' AND date <= '", end, "'", sep = "")
  rs <- dbSendQuery(con, q)
  p <- fetch(rs,n=-1)
  p <- melt(p, id="date")
  ggplot(p, aes(x=as.Date(date, "%Y-%m-%d"), y=value, colour=variable)) +
    geom_line(size=1) +
    scale_x_date(name = paste("\nThe Tor Project - ",
        "https://metrics.torproject.org/", sep = "")) +
    scale_y_continuous(name="",
      limits=c(0,max(p$value, na.rm=TRUE))) +
    scale_colour_brewer(name="Platform", breaks=c("avg_linux",
        "avg_darwin", "avg_bsd", "avg_windows", "avg_other"),
      labels=c("Linux", "Darwin", "FreeBSD", "Windows", "Other")) +
    opts(title = "Relay platforms\n")
  ggsave(filename=path,width=8,height=5,dpi=72)
  dbDisconnect(con)
  dbUnloadDriver(drv)
}
plot_bandwidth_line <- function(start, end, path) {
  drv <- dbDriver("PostgreSQL")
  con <- dbConnect(drv, user=dbuser, password=dbpassword, dbname=db)
  # TODO As soon as the database schema has bwadvertised, switch to that
  # and update the graph title!
  q <- paste("SELECT bwobserved, date FROM total_bandwidth ",
      "WHERE date >= '", start, "' AND date <= '", end, "'", sep = "")
  rs <- dbSendQuery(con, q)
  bandwidth <- fetch(rs,n=-1)
  bandwidth <- melt(bandwidth, id="date")
  ggplot(bandwidth, aes(x = as.Date(date, "%Y-%m-%d"), y = value / 2^20)) +
    geom_line(size=1) +
    scale_x_date(name = paste("\nThe Tor Project - ",
        "https://metrics.torproject.org/", sep = "")) +
    scale_y_continuous(name="Bandwidth (MiB/s)",
        limits = c(0, max(bandwidth$value, na.rm = TRUE) / 2^20)) +
    opts(title = "Total observed bandwidth\n")
  ggsave(filename = path, width = 8, height = 5, dpi = 72)
  dbDisconnect(con)
  dbUnloadDriver(drv)
}

