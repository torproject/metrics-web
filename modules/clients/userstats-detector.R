library("reshape")
export_userstats_detector <- function(path) {
  c <- read.csv("userstats.csv", stringsAsFactors = FALSE)
  c <- c[c$country != '' & c$transport == '' & c$version == '' &
         c$node == 'relay', ]
  u <- data.frame(country = c$country, date = c$date, users = c$users,
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
export_userstats_detector("userstats-detector.csv")

