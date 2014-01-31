require(reshape)
t <- read.csv("stats/advbwdist-validafter.csv", stringsAsFactors = FALSE)
t <- t[t$valid_after < paste(Sys.Date() - 1, "23:59:59"), ]
t <- aggregate(list(advbw = as.numeric(t$advbw)),
  by = list(date = as.Date(cut.Date(as.Date(t$valid_after), "day")),
  isexit = !is.na(t$isexit), relay = ifelse(is.na(t$relay), -1, t$relay),
  percentile = ifelse(is.na(t$percentile), -1, t$percentile)),
  FUN = median)
t <- data.frame(date = t$date, isexit = ifelse(t$isexit, "t", ""),
  relay = ifelse(t$relay < 0, NA, t$relay),
  percentile = ifelse(t$percentile < 0, NA, t$percentile),
  advbw = floor(t$advbw))
t <- t[order(t$date, t$isexit, t$relay, t$percentile), ]
write.csv(t, "stats/advbwdist.csv", quote = FALSE, row.names = FALSE,
  na = "")

