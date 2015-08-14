require(reshape)
r <- read.csv("userstats-ranges.csv", stringsAsFactors = FALSE)
r <- melt(r, id.vars = c("date", "country"))
r <- data.frame(date = r$date, node = "relay", country = r$country,
  transport = "", version = "",
  variable = ifelse(r$variable == "maxusers", "upper", "lower"),
  value = floor(r$value))
u <- read.csv("userstats.csv", stringsAsFactors = FALSE)
u <- melt(u, id.vars = c("date", "node", "country", "transport",
  "version"))
u <- data.frame(date = u$date, node = u$node, country = u$country,
  transport = u$transport, version = u$version,
  variable = ifelse(u$variable == "frac", "frac", "clients"),
  value = u$value)
c <- rbind(r, u)
c <- cast(c, date + node + country + transport + version ~ variable)
c <- c[order(as.Date(c$date), c$node, c$country, c$transport, c$version), ]
write.csv(c, "clients.csv", quote = FALSE, row.names = FALSE, na = "")

