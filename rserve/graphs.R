plot_networksize <- function(start, end, path, dpi) {
  drv <- dbDriver("PostgreSQL")
  con <- dbConnect(drv, user = dbuser, password = dbpassword, dbname = db)
  q <- paste("SELECT date, avg_running AS relays FROM network_size ",
      "WHERE date >= '", start, "' AND date <= '", end, "'", sep = "")
  rs <- dbSendQuery(con, q)
  relays <- fetch(rs, n = -1)
  q <- paste("SELECT date, avg_running AS bridges ",
      "FROM bridge_network_size WHERE date >= '", start,
      "' AND date <= '", end, "'", sep = "")
  rs <- dbSendQuery(con, q)
  bridges <- fetch(rs, n = -1)
  dbDisconnect(con)
  dbUnloadDriver(drv)
  dates <- seq(from = as.Date(start, "%Y-%m-%d"),
      to = as.Date(end, "%Y-%m-%d"), by="1 day")
  missing <- setdiff(dates, as.Date(relays$date, origin = "1970-01-01"))
  if (length(missing) > 0)
    relays <- rbind(relays,
        data.frame(date = as.Date(missing, origin = "1970-01-01"),
        relays = NA))
  missing <- setdiff(dates, bridges$date)
  if (length(missing) > 0)
    bridges <- rbind(bridges,
        data.frame(date = as.Date(missing, origin = "1970-01-01"),
        bridges = NA))
  relays <- melt(relays, id = "date")
  bridges <- melt(bridges, id = "date")
  networksize <- rbind(relays, bridges)
  ggplot(networksize, aes(x = as.Date(date, "%Y-%m-%d"), y = value,
    colour = variable)) + geom_line(size = 1) +
    scale_x_date(name = paste("\nThe Tor Project - ",
        "https://metrics.torproject.org/", sep = ""), format =
        c("%d-%b", "%d-%b", "%b-%Y", "%b-%Y", "%Y", "%Y")[
        cut(as.numeric(max(as.Date(networksize$date, "%Y-%m-%d")) -
        min(as.Date(networksize$date, "%Y-%m-%d"))),
        c(0, 10, 56, 365, 730, 5000, Inf), labels=FALSE)]) +
    scale_y_continuous(name = "", limits = c(0, max(networksize$value,
        na.rm = TRUE))) +
    scale_colour_hue("", breaks = c("relays", "bridges"),
        labels = c("Relays", "Bridges")) +
    opts(title = "Number of relays\n")
  ggsave(filename = path, width = 8, height = 5, dpi = as.numeric(dpi))
}

plot_relaycountries <- function(start, end, country, path, dpi) {
  drv <- dbDriver("PostgreSQL")
  con <- dbConnect(drv, user = dbuser, password = dbpassword, dbname = db)
  if (country == "all") {
    q <- paste("SELECT date, avg_running AS relays FROM network_size ",
        "WHERE date >= '", start, "' AND date <= '", end,
        "' AND date < (SELECT MAX(date) FROM network_size)", sep = "")
  } else {
    q <- paste("SELECT date, relays FROM relay_countries ",
        "WHERE date >= '", start, "' AND date <= '", end,
        "' AND date < (SELECT MAX(date) FROM relay_countries) ",
        "AND country = '", country, "'", sep = "")
  }
  rs <- dbSendQuery(con, q)
  u <- fetch(rs, n = -1)
  if (length(u$date) == 0)
    u <- data.frame(date = as.Date(start), relays = 0)
  dbDisconnect(con)
  dbUnloadDriver(drv)
  dates <- seq(from = as.Date(start, "%Y-%m-%d"),
      to = as.Date(end, "%Y-%m-%d"), by="1 day")
  missing <- setdiff(dates, u$date)
  if (length(missing) > 0)
    u <- rbind(u,
        data.frame(date = as.Date(missing, origin = "1970-01-01"),
        relays = NA))
  peoples <- data.frame(country = c("ae", "au", "bh", "br", "ca", "cn",
    "cu", "de", "dj", "dz", "eg", "et", "fr", "gb", "il", "ir", "it",
    "iq", "jo", "jp", "kp", "kr", "kw", "lb", "ly", "ma", "mm", "om",
    "pl", "ps", "qa", "ru", "sa", "sd", "se", "sy", "tn", "tm", "us",
    "uz", "vn", "ye"),
    people = c("U.A.E.", "Australian", "Bahraini", "Brazilian",
    "Canadian", "Chinese", "Cuban", "German", "Djiboutian", "Algerian",
    "Egyptian", "Ethiopian", "French", "U.K.", "Israeli", "Iranian",
    "Italian", "Iraqi", "Jordanian", "Japanese", "North Korean",
    "South Korean", "Kuwaiti", "Lebanese", "Libyan", "Moroccan",
    "Burmese", "Omani", "Polish", "Palestinian", "Qatari", "Russian",
    "Saudi", "Sudanese", "Swedish", "Syrian", "Tunisian", "Turkmen",
    "U.S.", "Uzbek", "Vietnamese", "Yemeni"),
    stringsAsFactors = FALSE)
  title <- ifelse(country == "all",
    "Number of relays in all countries\n",
    paste("Number of", peoples[peoples$country == country, "people"],
      "relays\n"))
  formatter <- function(x, ...) { format(x, scientific = FALSE, ...) }
  ggplot(u, aes(x = as.Date(date, "%Y-%m-%d"), y = relays)) +
    geom_line(size = 1) +
    scale_x_date(name = paste("\nThe Tor Project - ",
        "https://metrics.torproject.org/", sep = ""), format =
        c("%d-%b", "%d-%b", "%b-%Y", "%b-%Y", "%Y", "%Y")[
        cut(as.numeric(max(as.Date(u$date, "%Y-%m-%d")) -
        min(as.Date(u$date, "%Y-%m-%d"))),
        c(0, 10, 56, 365, 730, 5000, Inf), labels=FALSE)]) +
    scale_y_continuous(name = "", limits = c(0, max(u$relays,
        na.rm = TRUE)), formatter = formatter) +
    opts(title = title)
  ggsave(filename = path, width = 8, height = 5, dpi = as.numeric(dpi))
}

plot_versions <- function(start, end, path, dpi) {
  drv <- dbDriver("PostgreSQL")
  con <- dbConnect(drv, user = dbuser, password = dbpassword, dbname = db)
  q <- paste("SELECT date, version, relays FROM relay_versions ",
      "WHERE date >= '", start, "' AND date <= '", end, "'", sep = "")
  rs <- dbSendQuery(con, q)
  versions <- fetch(rs, n = -1)
  dbDisconnect(con)
  dbUnloadDriver(drv)
  known_versions <- c("0.1.0", "0.1.1", "0.1.2", "0.2.0", "0.2.1",
        "0.2.2", "0.2.3")
  versions <- versions[versions$version %in% known_versions, ]
  visible_versions <- sort(unique(versions$version))
  versions <- rbind(data.frame(
    date = as.Date(rep(end, 7)),
    version = known_versions,
    relays = rep(NA, 7)), versions)
  ggplot(versions, aes(x = as.Date(date, "%Y-%m-%d"), y = relays,
      colour = version)) +
    geom_line(size = 1) +
    scale_x_date(name = paste("\nThe Tor Project - ",
        "https://metrics.torproject.org/", sep = ""), format =
        c("%d-%b", "%d-%b", "%b-%Y", "%b-%Y", "%Y", "%Y")[
        cut(as.numeric(max(as.Date(versions$date, "%Y-%m-%d")) -
        min(as.Date(versions$date, "%Y-%m-%d"))),
        c(0, 10, 56, 365, 730, 5000, Inf), labels=FALSE)]) +
    scale_y_continuous(name = "",
      limits = c(0, max(versions$relays, na.rm = TRUE))) +
    scale_colour_hue(name = "Tor version", h.start = 280,
      breaks = visible_versions, labels = visible_versions) +
    opts(title = "Relay versions\n")
  ggsave(filename = path, width = 8, height = 5, dpi = as.numeric(dpi))
}

plot_platforms <- function(start, end, path, dpi) {
  drv <- dbDriver("PostgreSQL")
  con <- dbConnect(drv, user=dbuser, password=dbpassword, dbname=db)
  q <- paste("SELECT date, avg_linux, avg_darwin, avg_bsd, avg_windows, ",
      "avg_other FROM relay_platforms WHERE date >= '", start,
      "' AND date <= '", end, "'", sep = "")
  rs <- dbSendQuery(con, q)
  platforms <- fetch(rs, n = -1)
  dbDisconnect(con)
  dbUnloadDriver(drv)
  platforms <- melt(platforms, id = "date")
  ggplot(platforms, aes(x = as.Date(date, "%Y-%m-%d"), y = value,
      colour = variable)) +
    geom_line(size = 1) +
    scale_x_date(name = paste("\nThe Tor Project - ",
        "https://metrics.torproject.org/", sep = ""), format =
        c("%d-%b", "%d-%b", "%b-%Y", "%b-%Y", "%Y", "%Y")[
        cut(as.numeric(max(as.Date(platforms$date, "%Y-%m-%d")) -
        min(as.Date(platforms$date, "%Y-%m-%d"))),
        c(0, 10, 56, 365, 730, 5000, Inf), labels=FALSE)]) +
    scale_y_continuous(name = "",
      limits = c(0, max(platforms$value, na.rm = TRUE))) +
    scale_colour_hue(name = "Platform", h.start = 180,
      breaks = c("avg_linux", "avg_darwin", "avg_bsd", "avg_windows",
          "avg_other"),
      labels = c("Linux", "Darwin", "FreeBSD", "Windows", "Other")) +
    opts(title = "Relay platforms\n")
  ggsave(filename = path, width = 8, height = 5, dpi = as.numeric(dpi))
}

plot_bandwidth <- function(start, end, path, dpi) {
  drv <- dbDriver("PostgreSQL")
  con <- dbConnect(drv, user = dbuser, password = dbpassword, dbname = db)
  q <- paste("SELECT date, bwadvertised FROM total_bandwidth ",
      "WHERE date >= '", start, "' AND date <= '", end, "'", sep = "")
  rs <- dbSendQuery(con, q)
  bw_desc <- fetch(rs, n = -1)
  q <- paste("SELECT date, read, written FROM total_bwhist ",
      "WHERE date >= '", start, "' AND date <= '", end, "' ",
      "AND date < (SELECT MAX(date) FROM total_bwhist) - 1 ", sep = "")
  rs <- dbSendQuery(con, q)
  bw_hist <- fetch(rs, n = -1)
  dbDisconnect(con)
  dbUnloadDriver(drv)
  bandwidth <- rbind(data.frame(date = bw_desc$date,
      value = bw_desc$bwadvertised, variable = "bwadv"),
    data.frame(date = bw_hist$date, value = (bw_hist$read +
      bw_hist$written) / (2 * 86400), variable = "bwhist"))
  ggplot(bandwidth, aes(x = as.Date(date, "%Y-%m-%d"), y = value / 2^20,
      colour = variable)) +
    geom_line(size = 1) +
    scale_x_date(name = paste("\nThe Tor Project - ",
        "https://metrics.torproject.org/", sep = ""), format =
        c("%d-%b", "%d-%b", "%b-%Y", "%b-%Y", "%Y", "%Y")[
        cut(as.numeric(max(as.Date(bandwidth$date, "%Y-%m-%d")) -
        min(as.Date(bandwidth$date, "%Y-%m-%d"))),
        c(0, 10, 56, 365, 730, 5000, Inf), labels=FALSE)]) +
    scale_y_continuous(name="Bandwidth (MiB/s)",
        limits = c(0, max(bandwidth$value, na.rm = TRUE) / 2^20)) +
    scale_colour_hue(name = "", h.start = 90,
        breaks = c("bwadv", "bwhist"),
        labels = c("Advertised bandwidth", "Bandwidth history")) +
    opts(title = "Total relay bandwidth", legend.position = "top")
  ggsave(filename = path, width = 8, height = 5, dpi = as.numeric(dpi))
}

plot_dirbytes <- function(start, end, path, dpi) {
  drv <- dbDriver("PostgreSQL")
  con <- dbConnect(drv, user = dbuser, password = dbpassword, dbname = db)
  q <- paste("SELECT date, dr, dw, brp, bwp, brd, bwd FROM user_stats",
      "WHERE country = 'zy' AND bwp / bwd <= 3",
      "AND date < (SELECT MAX(date) FROM user_stats) - 1 ORDER BY date")
  rs <- dbSendQuery(con, q)
  dir <- fetch(rs, n = -1)
  dbDisconnect(con)
  dbUnloadDriver(drv)
  dir <- data.frame(date = dir$date,
      dirread = floor(dir$dr * dir$brp / dir$brd / 86400),
      dirwrite = floor(dir$dw * dir$bwp / dir$bwd / 86400))
  dir <- na.omit(dir)
  dir <- melt(dir, id = "date")
  ggplot(dir, aes(x = as.Date(date, "%Y-%m-%d"), y = value / 2^20,
      colour = variable)) +
    geom_line(size = 1) +
    scale_x_date(name = paste("\nThe Tor Project - ",
        "https://metrics.torproject.org/", sep = ""), format =
        c("%d-%b", "%d-%b", "%b-%Y", "%b-%Y", "%Y", "%Y")[
        cut(as.numeric(max(as.Date(dir$date, "%Y-%m-%d")) -
        min(as.Date(dir$date, "%Y-%m-%d"))),
        c(0, 10, 56, 365, 730, 5000, Inf), labels=FALSE)]) +
    scale_y_continuous(name="Bandwidth (MiB/s)",
        limits = c(0, max(dir$value, na.rm = TRUE) / 2^20)) +
    scale_colour_hue(name = "",
        breaks = c("dirwrite", "dirread"),
        labels = c("Written dir bytes", "Read dir bytes")) +
    opts(title = "Number of bytes spent on answering directory requests",
        legend.position = "top")
  ggsave(filename = path, width = 8, height = 5, dpi = as.numeric(dpi))
}

plot_relayflags <- function(start, end, flags, granularity, path, dpi) {
  drv <- dbDriver("PostgreSQL")
  con <- dbConnect(drv, user = dbuser, password = dbpassword, dbname = db)
  if (granularity == 'day') {
    columns <- paste("avg_", tolower(flags), sep = "", collapse = ", ")
    q <- paste("SELECT date, ", columns, " FROM network_size ",
        "WHERE date >= '", start, "' AND date <= '", end, "'", sep = "")
    rs <- dbSendQuery(con, q)
    networksize <- fetch(rs, n = -1)
    dbDisconnect(con)
    dbUnloadDriver(drv)
    networksize <- melt(networksize, id = "date")
    networksize <- rbind(data.frame(
      date = as.Date(rep(end, 5)),
      variable = paste("avg_", c("running", "exit", "guard", "fast",
        "stable"), sep = ""),
      value = rep(NA, 5)), networksize)
    ggplot(networksize, aes(x = as.Date(date, "%Y-%m-%d"), y = value,
      colour = variable)) + geom_line(size = 1) +
      scale_x_date(name = paste("\nThe Tor Project - ",
          "https://metrics.torproject.org/", sep = ""), format =
          c("%d-%b", "%d-%b", "%b-%Y", "%b-%Y", "%Y", "%Y")[
          cut(as.numeric(max(as.Date(networksize$date, "%Y-%m-%d")) -
          min(as.Date(networksize$date, "%Y-%m-%d"))),
          c(0, 10, 56, 365, 730, 5000, Inf), labels=FALSE)]) +
      scale_y_continuous(name = "", limits = c(0, max(networksize$value,
          na.rm = TRUE))) +
      scale_colour_hue(name = "Relay flags", h.start = 280,
          breaks = paste("avg_", tolower(flags), sep = ""),
          labels = flags) +
      opts(title = "Number of relays with relay flags assigned\n")
  } else {
    columns <- paste("avg_", tolower(flags), sep = "", collapse = ", ")
    q <- paste("SELECT validafter, ", columns, " FROM network_size_hour ",
        "WHERE DATE(validafter) >= '", start,
        "' AND DATE(validafter) <= '", end, "'", sep = "")
    rs <- dbSendQuery(con, q)
    networksize <- fetch(rs, n = -1)
    dbDisconnect(con)
    dbUnloadDriver(drv)
    networksize <- melt(networksize, id = "validafter")
    networksize <- rbind(data.frame(
      validafter = as.POSIXct(rep(paste(end, "00:00:00"), 5)),
      variable = paste("avg_", c("running", "exit", "guard", "fast",
        "stable"), sep = ""),
      value = rep(NA, 5)), networksize)
    ggplot(networksize, aes(x = as.POSIXct(validafter), y = value,
      colour = variable)) + geom_line(size = 1) +
      scale_x_datetime(name = paste("\nThe Tor Project - ",
          "https://metrics.torproject.org/", sep = "")) +
      scale_y_continuous(name = "", limits = c(0, max(networksize$value,
          na.rm = TRUE))) +
      scale_colour_hue(name = "Relay flags", h.start = 280,
          breaks = paste("avg_", tolower(flags), sep = ""),
          labels = flags) +
      opts(title = "Number of relays with relay flags assigned\n")
  }
  ggsave(filename = path, width = 8, height = 5, dpi = as.numeric(dpi))
}

plot_direct_users <- function(start, end, country, path, dpi) {
  drv <- dbDriver("PostgreSQL")
  con <- dbConnect(drv, user = dbuser, password = dbpassword, dbname = db)
  q <- paste("SELECT date, r, bwp, brn, bwn, brp, bwr, brr ",
      "FROM user_stats WHERE date >= '", start, "' AND date <= '", end,
      "' AND date < (SELECT MAX(date) FROM user_stats) - 1 ",
      " AND country = '", ifelse(country == "all", "zy", country), "'",
      sep = "")
  rs <- dbSendQuery(con, q)
  u <- fetch(rs, n = -1)
  dbDisconnect(con)
  dbUnloadDriver(drv)
  u <- data.frame(date = u$date,
       users = u$r * (u$bwp * u$brn / u$bwn - u$brp) /
               (u$bwr * u$brn / u$bwn - u$brr) / 10)
  dates <- seq(from = as.Date(start, "%Y-%m-%d"),
      to = as.Date(end, "%Y-%m-%d"), by="1 day")
  missing <- setdiff(dates, u$date)
  if (length(missing) > 0)
    u <- rbind(u,
        data.frame(date = as.Date(missing, origin = "1970-01-01"),
        users = NA))
  peoples <- data.frame(country = c("ae", "au", "bh", "br", "ca", "cn",
    "cu", "de", "dj", "dz", "eg", "et", "fr", "gb", "il", "ir", "it",
    "iq", "jo", "jp", "kp", "kr", "kw", "lb", "ly", "ma", "mm", "om",
    "pl", "ps", "qa", "ru", "sa", "sd", "se", "sy", "tn", "tm", "us",
    "uz", "vn", "ye"),
    people = c("U.A.E.", "Australian", "Bahraini", "Brazilian",
    "Canadian", "Chinese", "Cuban", "German", "Djiboutian", "Algerian",
    "Egyptian", "Ethiopian", "French", "U.K.", "Israeli", "Iranian",
    "Italian", "Iraqi", "Jordanian", "Japanese", "North Korean",
    "South Korean", "Kuwaiti", "Lebanese", "Libyan", "Moroccan",
    "Burmese", "Omani", "Polish", "Palestinian", "Qatari", "Russian",
    "Saudi", "Sudanese", "Swedish", "Syrian", "Tunisian", "Turkmen",
    "U.S.", "Uzbek", "Vietnamese", "Yemeni"),
    stringsAsFactors = FALSE)
  title <- ifelse(country == "all",
    "Total directly connecting Tor users (all data)\n",
    paste("Directly connecting",
    peoples[peoples$country == country, "people"], "Tor users\n"))
  formatter <- function(x, ...) { format(x, scientific = FALSE, ...) }
  ggplot(u, aes(x = as.Date(date, "%Y-%m-%d"), y = users)) +
    geom_line(size = 1) +
    scale_x_date(name = paste("\nThe Tor Project - ",
        "https://metrics.torproject.org/", sep = ""), format =
        c("%d-%b", "%d-%b", "%b-%Y", "%b-%Y", "%Y", "%Y")[
        cut(as.numeric(max(as.Date(u$date, "%Y-%m-%d")) -
        min(as.Date(u$date, "%Y-%m-%d"))),
        c(0, 10, 56, 365, 730, 5000, Inf), labels=FALSE)]) +
    scale_y_continuous(name = "", limits = c(0, max(u$users,
        na.rm = TRUE)), formatter = formatter) +
    opts(title = title)
  ggsave(filename = path, width = 8, height = 5, dpi = as.numeric(dpi))
}

plot_bridge_users <- function(start, end, country, path, dpi) {
  drv <- dbDriver("PostgreSQL")
  con <- dbConnect(drv, user = dbuser, password = dbpassword, dbname = db)
  q <- paste("SELECT date, users FROM bridge_stats ",
      "WHERE date >= '", start, "' AND date <= '", end, "' ",
      "AND date < (SELECT MAX(date) FROM bridge_stats) ",
      " AND country = '", ifelse(country == "all", "zy", country), "'",
      sep = "")
  rs <- dbSendQuery(con, q)
  bridgeusers <- fetch(rs, n = -1)
  dbDisconnect(con)
  dbUnloadDriver(drv)
  dates <- seq(from = as.Date(start, "%Y-%m-%d"),
      to = as.Date(end, "%Y-%m-%d"), by="1 day")
  missing <- setdiff(dates, bridgeusers$date)
  if (length(missing) > 0)
    bridgeusers <- rbind(bridgeusers,
        data.frame(date = as.Date(missing, origin = "1970-01-01"),
        users = NA))
  peoples <- data.frame(country = c("ae", "au", "bh", "br", "ca", "cn",
    "cu", "de", "dj", "dz", "eg", "et", "fr", "gb", "il", "ir", "it",
    "iq", "jo", "jp", "kp", "kr", "kw", "lb", "ly", "ma", "mm", "om",
    "pl", "ps", "qa", "ru", "sa", "sd", "se", "sy", "tn", "tm", "us",
    "uz", "vn", "ye"),
    people = c("U.A.E.", "Australian", "Bahraini", "Brazilian",
    "Canadian", "Chinese", "Cuban", "German", "Djiboutian", "Algerian",
    "Egyptian", "Ethiopian", "French", "U.K.", "Israeli", "Iranian",
    "Italian", "Iraqi", "Jordanian", "Japanese", "North Korean",
    "South Korean", "Kuwaiti", "Lebanese", "Libyan", "Moroccan",
    "Burmese", "Omani", "Polish", "Palestinian", "Qatari", "Russian",
    "Saudi", "Sudanese", "Swedish", "Syrian", "Tunisian", "Turkmen",
    "U.S.", "Uzbek", "Vietnamese", "Yemeni"),
    stringsAsFactors = FALSE)
  title <- ifelse(country == "all",
    "Total users via bridges (all data)\n",
    paste(peoples[peoples$country == country, "people"],
    "users via bridges\n"))
  formatter <- function(x, ...) { format(x, scientific = FALSE, ...) }
  ggplot(bridgeusers, aes(x = as.Date(date, "%Y-%m-%d"), y = users)) +
    geom_line(size = 1) +
    scale_x_date(name = paste("\nThe Tor Project - ",
        "https://metrics.torproject.org/", sep = ""), format =
        c("%d-%b", "%d-%b", "%b-%Y", "%b-%Y", "%Y", "%Y")[
        cut(as.numeric(max(as.Date(bridgeusers$date, "%Y-%m-%d")) -
        min(as.Date(bridgeusers$date, "%Y-%m-%d"))),
        c(0, 10, 56, 365, 730, 5000, Inf), labels=FALSE)]) +
    scale_y_continuous(name = "", limits = c(0, max(bridgeusers$users,
        na.rm = TRUE)), formatter = formatter) +
    opts(title = title)
  ggsave(filename = path, width = 8, height = 5, dpi = as.numeric(dpi))
}

plot_gettor <- function(start, end, bundle, path, dpi) {
  drv <- dbDriver("PostgreSQL")
  con <- dbConnect(drv, user = dbuser, password = dbpassword, dbname = db)
  condition <- ifelse(bundle == "all", "<> 'none'",
      paste("LIKE 'tor-%browser-bundle_", tolower(bundle), "'", sep = ""))
  q <- paste("SELECT date, SUM(downloads) AS downloads ",
      "FROM gettor_stats WHERE bundle ", condition, " AND date >= '",
      start, "' AND date <= '", end, "' GROUP BY date", sep = "")
  rs <- dbSendQuery(con, q)
  downloads <- fetch(rs, n = -1)
  dbDisconnect(con)
  dbUnloadDriver(drv)
  dates <- seq(from = as.Date(start, "%Y-%m-%d"),
      to = as.Date(end, "%Y-%m-%d"), by="1 day")
  missing <- setdiff(dates, downloads$date)
  if (length(missing) > 0)
    downloads <- rbind(downloads,
        data.frame(date = as.Date(missing, origin = "1970-01-01"),
        downloads = NA))
  title <- ifelse(bundle == "all",
    "Total packages requested from GetTor per day\n",
    paste("Tor Browser Bundles (", bundle,
    ") requested from GetTor per day\n", sep = ""))
  ggplot(downloads, aes(x = as.Date(date, "%Y-%m-%d"), y = downloads)) +
    geom_line(size = 1) +
    scale_x_date(name = paste("\nThe Tor Project - ",
        "https://metrics.torproject.org/", sep = ""), format =
        c("%d-%b", "%d-%b", "%b-%Y", "%b-%Y", "%Y", "%Y")[
        cut(as.numeric(max(as.Date(downloads$date, "%Y-%m-%d")) -
        min(as.Date(downloads$date, "%Y-%m-%d"))),
        c(0, 10, 56, 365, 730, 5000, Inf), labels=FALSE)]) +
    scale_y_continuous(name = "", limits = c(0, max(downloads$downloads,
        na.rm = TRUE))) +
    opts(title = title)
  ggsave(filename = path, width = 8, height = 5, dpi = as.numeric(dpi))
}

plot_torperf <- function(start, end, source, filesize, path, dpi) {
  drv <- dbDriver("PostgreSQL")
  con <- dbConnect(drv, user = dbuser, password = dbpassword, dbname = db)
  q <- paste("SELECT date, q1, md, q3 FROM torperf_stats ",
      "WHERE source = '", paste(source, filesize, sep = "-"),
      "' AND date >= '", start, "' AND date <= '", end, "'", sep = "")
  rs <- dbSendQuery(con, q)
  torperf <- fetch(rs, n = -1)
  dbDisconnect(con)
  dbUnloadDriver(drv)
  dates <- seq(from = as.Date(start, "%Y-%m-%d"),
      to = as.Date(end, "%Y-%m-%d"), by="1 day")
  missing <- setdiff(dates, torperf$date)
  if (length(missing) > 0)
    torperf <- rbind(torperf,
        data.frame(date = as.Date(missing, origin = "1970-01-01"),
        q1 = NA, md = NA, q3 = NA))
  colours <- data.frame(source = c("all", "siv", "moria", "torperf"),
      colour = c("#FF8C00", "#0000EE", "#EE0000", "#00CD00"),
      stringsAsFactors = FALSE)
  colour <- colours[colours$source == source, "colour"]
  filesizes <- data.frame(filesizes = c("5mb", "1mb", "50kb"),
      label = c("5 MiB", "1 MiB", "50 KiB"), stringsAsFactors = FALSE)
  filesizeStr <- filesizes[filesizes$filesize == filesize, "label"]
  maxY <- max(torperf$q3, na.rm = TRUE)
  ggplot(torperf, aes(x = as.Date(date, "%Y-%m-%d"), y = md/1e3,
      fill = "line")) +
    geom_line(colour = colour, size = 0.75) +
    geom_ribbon(data = torperf, aes(x = date, ymin = q1/1e3,
      ymax = q3/1e3, fill = "ribbon")) +
    scale_x_date(name = paste("\nThe Tor Project - ",
        "https://metrics.torproject.org/", sep = ""), format =
        c("%d-%b", "%d-%b", "%b-%Y", "%b-%Y", "%Y", "%Y")[
        cut(as.numeric(max(as.Date(torperf$date, "%Y-%m-%d")) -
        min(as.Date(torperf$date, "%Y-%m-%d"))),
        c(0, 10, 56, 365, 730, 5000, Inf), labels=FALSE)]) +
    scale_y_continuous(name = "", limits = c(0, maxY) / 1e3) +
    scale_fill_manual(name = paste("Measured times on",
        ifelse(source == "all", "all sources", source), "per day"),
      breaks = c("line", "ribbon"),
      labels = c("Median", "1st to 3rd quartile"),
      values = paste(colour, c("", "66"), sep = "")) +
    opts(title = paste("Time in seconds to complete", filesizeStr,
        "request"), legend.position = "top")
  ggsave(filename = path, width = 8, height = 5, dpi = as.numeric(dpi))
}

plot_torperf_failures <- function(start, end, source, filesize, path,
    dpi) {
  drv <- dbDriver("PostgreSQL")
  con <- dbConnect(drv, user = dbuser, password = dbpassword, dbname = db)
  q <- paste("SELECT date, timeouts, failures, requests ",
      "FROM torperf_stats WHERE source = '",
      paste(source, filesize, sep = "-"),
      "' AND date >= '", start, "' AND date <= '", end, "'", sep = "")
  rs <- dbSendQuery(con, q)
  torperf <- fetch(rs, n = -1)
  dbDisconnect(con)
  dbUnloadDriver(drv)
  dates <- seq(from = as.Date(start, "%Y-%m-%d"),
      to = as.Date(end, "%Y-%m-%d"), by="1 day")
  missing <- setdiff(dates, torperf$date)
  if (length(missing) > 0)
    torperf <- rbind(torperf,
        data.frame(date = as.Date(missing, origin = "1970-01-01"),
        timeouts = NA, failures = NA, requests = NA))
  colours <- data.frame(source = c("all", "siv", "moria", "torperf"),
      colour = c("#FF8C00", "#0000EE", "#EE0000", "#00CD00"),
      stringsAsFactors = FALSE)
  colour <- colours[colours$source == source, "colour"]
  filesizes <- data.frame(filesizes = c("5mb", "1mb", "50kb"),
      label = c("5 MiB", "1 MiB", "50 KiB"), stringsAsFactors = FALSE)
  filesizeStr <- filesizes[filesizes$filesize == filesize, "label"]
  torperf <- rbind(data.frame(date = torperf$date,
      value = ifelse(torperf$requests > 0,
                     torperf$timeouts / torperf$requests, 0),
      variable = "timeouts"),
    data.frame(date = torperf$date,
      value = ifelse(torperf$requests > 0,
                     torperf$failures / torperf$requests, 0),
      variable = "failures"))
  ggplot(torperf, aes(x = as.Date(date, "%Y-%m-%d"), y = value,
    colour = variable)) +
    geom_point(size = 2) +
    scale_x_date(name = paste("\nThe Tor Project - ",
        "https://metrics.torproject.org/", sep = ""), format =
        c("%d-%b", "%d-%b", "%b-%Y", "%b-%Y", "%Y", "%Y")[
        cut(as.numeric(max(as.Date(torperf$date, "%Y-%m-%d")) -
        min(as.Date(torperf$date, "%Y-%m-%d"))),
        c(0, 10, 56, 365, 730, 5000, Inf), labels=FALSE)]) +
    scale_y_continuous(name = "", formatter = "percent") +
    scale_colour_hue(name = paste("Problems encountered on",
        ifelse(source == "all", "all sources", source)),
        h.start = 45, breaks = c("timeouts", "failures"),
        labels = c("Timeouts", "Failures")) +
    opts(title = paste("Timeouts and failures of", filesizeStr,
        "requests"), legend.position = "top")
  ggsave(filename = path, width = 8, height = 5, dpi = as.numeric(dpi))
}

plot_connbidirect <- function(start, end, path, dpi) {
  drv <- dbDriver("PostgreSQL")
  con <- dbConnect(drv, user = dbuser, password = dbpassword, dbname = db)
  q <- paste("SELECT DATE(statsend) AS date, readnum, writenum, bothnum ",
      "FROM connbidirect WHERE DATE(statsend) >= '", start,
      "' AND DATE(statsend) <= '", end, "'", sep = "")
  rs <- dbSendQuery(con, q)
  c <- fetch(rs, n = -1)
  dbDisconnect(con)
  dbUnloadDriver(drv)
  connbidirect <- data.frame(date = c$date, c[, 2:4] /
      (c$readnum + c$writenum + c$bothnum))
  connbidirect <- melt(connbidirect, id = "date")
  ggplot(connbidirect, aes(x = as.Date(date, "%Y-%m-%d"), y = value,
      colour = variable)) +
    geom_point(size = 2.5) +
    scale_x_date(name = paste("\nThe Tor Project - ",
        "https://metrics.torproject.org/", sep = ""), format =
        c("%d-%b", "%d-%b", "%b-%Y", "%b-%Y", "%Y", "%Y")[
        cut(as.numeric(max(as.Date(connbidirect$date, "%Y-%m-%d")) -
        min(as.Date(connbidirect$date, "%Y-%m-%d"))),
        c(0, 10, 56, 365, 730, 5000, Inf), labels=FALSE)]) +
    scale_y_continuous(name = "", formatter = "percent") +
    scale_colour_hue("", breaks = c("readnum", "writenum", "bothnum"),
        labels = c("Mostly reading", "Mostly writing",
        "Both reading and writing")) +
    opts(title = "Fraction of connections used uni-/bidirectionally",
        legend.position = "top")
  ggsave(filename = path, width = 8, height = 5, dpi = as.numeric(dpi))
}

## TODO The bandwidth history shouldn't be based on the consensus weights
## which aren't bandwidths anymore, but either on the advertised bandwidth
## contained in server descriptors or better on the bandwidth history
## reported in extra-info descriptors.
plot_routerdetail <- function(fingerprint, path) {
  drv <- dbDriver("PostgreSQL")
  con <- dbConnect(drv, user = dbuser, password = dbpassword, dbname = db)
  q <- paste("SELECT AVG(bandwidth)::INTEGER AS bw, ",
      "DATE(validafter) AS date ",
      "FROM statusentry WHERE fingerprint = '", fingerprint, "' ",
      "AND validafter > CURRENT_TIMESTAMP AT TIME ZONE 'UTC' - ",
      "interval '1 week' GROUP BY DATE(validafter)", sep = "")
  rs <- dbSendQuery(con, q)
  routerdetail <- fetch(rs, n = -1)
  ## TODO We should add NA's for missing dates.
  dbDisconnect(con)
  dbUnloadDriver(drv)
  ggplot(routerdetail, aes(x = as.Date(date, "%Y-%m-%d"), y = bw)) +
    geom_line(size = 1) +
    scale_x_date(name = paste("\nThe Tor Project - ",
        "https://metrics.torproject.org/", sep = ""), format =
        c("%d-%b", "%d-%b", "%b-%Y", "%b-%Y", "%Y", "%Y")[
        cut(as.numeric(max(as.Date(routerdetail$date, "%Y-%m-%d")) -
        min(as.Date(routerdetail$date, "%Y-%m-%d"))),
        c(0, 10, 56, 365, 730, 5000, Inf), labels=FALSE)]) +
    scale_y_continuous(name = "") +
    opts(title = paste("Bandwidth history for ", fingerprint, "\n",
        sep = ""))
  ggsave(filename = path, width = 8, height = 5, dpi = 72)
}

