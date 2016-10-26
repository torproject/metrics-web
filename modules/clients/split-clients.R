u <- read.csv("clients.csv", stringsAsFactors = FALSE)
write.csv(u[u$node == 'relay', names(u) != "node"], 'clients-relay.csv',
  quote = FALSE, row.names = FALSE, na = '')
write.csv(u[u$node == 'bridge', names(u) != "node"], 'clients-bridge.csv',
  quote = FALSE, row.names = FALSE, na = '')

