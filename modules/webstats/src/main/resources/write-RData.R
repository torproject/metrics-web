dir.create("RData", showWarnings = FALSE)

d <- read.csv("stats/webstats.csv", stringsAsFactors = FALSE)
d <- d[d$request_type %in% c('tbid', 'tbsd', 'tbup', 'tbur'), ]
data <- aggregate(list(count = d$count),
    by = list(log_date = as.Date(d$log_date), request_type = d$request_type),
    FUN = sum)
save(data, file = "RData/webstats-tb.RData")

