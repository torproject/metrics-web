require(reshape)
t <- read.csv("stats/advbwdist-validafter.csv",
  colClasses = c("character", "logical", "integer", "integer", "integer"),
  stringsAsFactors = FALSE)

currSysDate <- paste(Sys.Date() - 1, "23:59:59")
t <- t[t$valid_after < currSysDate, ]
t$date <- as.factor(substr(t$valid_after, 1, 10))
t$isexit <- !is.na(t$isexit)
t$relay <- ifelse(is.na(t$relay), -1, t$relay)
t$percentile <- ifelse(is.na(t$percentile), -1, t$percentile)

t <- aggregate(list(advbw = t$advbw), by = list(date = t$date,
    isexit = t$isexit, relay = t$relay, percentile = t$percentile),
    FUN = median)

t$isexit <- ifelse(t$isexit, "t", "")
t$relay <- ifelse(t$relay < 0, NA, t$relay)
t$percentile <- ifelse(t$percentile < 0, NA, t$percentile)
t$advbw <- floor(t$advbw)

t <- t[order(t$date, t$isexit, t$relay, t$percentile), ]

write.csv(t, "stats/advbwdist.csv", quote = FALSE, row.names = FALSE, na = "")

