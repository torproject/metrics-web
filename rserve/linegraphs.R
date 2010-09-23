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
plot_versions_line <- function(start, end, path) {
  drv <- dbDriver("PostgreSQL")
  con <- dbConnect(drv, user=dbuser, password=dbpassword, dbname=db)
  q <- paste("SELECT date, version, relays FROM relay_versions ",
      "WHERE date >= '", start, "' AND date <= '", end, "'", sep = "")
  rs <- dbSendQuery(con, q)
  v <- fetch(rs,n=-1)
  colours <- data.frame(version = c("0.1.0", "0.1.1", "0.1.2", "0.2.0",
    "0.2.1", "0.2.2", "0.2.3"), colour = c("#B4674D", "#C0448F",
    "#1F75FE", "#FF7F49", "#1CAC78", "#5D76CB", "#FF496C"),
    stringsAsFactors = FALSE)
  colours <- colours[colours$version %in% unique(v$version), "colour"]
  ggplot(v, aes(x = as.Date(date, "%Y-%m-%d"), y = relays,
      colour = version)) +
    geom_line(size=1) +
    scale_x_date(name = paste("\nThe Tor Project - ",
        "https://metrics.torproject.org/", sep = "")) +
    scale_y_continuous(name= "",
      limits = c(0, max(v$relays, na.rm = TRUE))) +
    scale_colour_manual(name = "Tor version", values = colours) +
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
  q1 <- paste("SELECT date, bwadvertised FROM total_bandwidth ",
      "WHERE date >= '", start, "' AND date <= '", end, "'", sep = "")
  rs1 <- dbSendQuery(con, q1)
  bw_desc <- fetch(rs1, n = -1)
  q2 <- paste("SELECT date, read, written FROM total_bwhist ",
      "WHERE date >= '", start, "' AND date <= '", end, "'", sep = "")
  rs2 <- dbSendQuery(con, q2)
  bw_hist <- fetch(rs2, n = -1)
  bandwidth <- rbind(data.frame(date = bw_desc$date,
      value = bw_desc$bwadvertised, variable = "bwadv"),
    data.frame(date = bw_hist$date, value = (bw_hist$read +
      bw_hist$written) / (2 * 86400), variable = "bwhist"))
  ggplot(bandwidth, aes(x = as.Date(date, "%Y-%m-%d"), y = value / 2^20,
      colour = variable)) +
    geom_line(size=1) +
    scale_x_date(name = paste("\nThe Tor Project - ",
        "https://metrics.torproject.org/", sep = "")) +
    scale_y_continuous(name="Bandwidth (MiB/s)",
        limits = c(0, max(bandwidth$value, na.rm = TRUE) / 2^20)) +
    scale_colour_hue(name = "", breaks = c("bwadv", "bwhist"),
        labels = c("Advertised bandwidth", "Bandwidth history")) +
    opts(title = "Total relay bandwidth", legend.position = "top")
  ggsave(filename = path, width = 8, height = 5, dpi = 72)
  dbDisconnect(con)
  dbUnloadDriver(drv)
}

