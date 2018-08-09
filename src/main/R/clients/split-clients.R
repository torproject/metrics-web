dir.create("RData", showWarnings = FALSE)

c <- read.csv("clients.csv", stringsAsFactors = FALSE)
data <- c[c$node == 'relay', !(names(c) %in% c("node"))]
save(data, file = "RData/clients-relay.RData")
data <- c[c$node == 'bridge', !(names(c) %in% c("node"))]
save(data, file = "RData/clients-bridge.RData")

u <- read.csv("userstats-combined.csv", stringsAsFactors = FALSE)
data <- u[, !(names(u) %in% c("node", "version"))]
save(data, file = "RData/userstats-bridge-combined.RData")

