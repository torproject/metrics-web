options(warn = -1)
suppressPackageStartupMessages(library("ggplot2"))

gettor <- read.csv("stats/gettor-stats", header = TRUE,
    stringsAsFactors = FALSE);
start <- as.Date(min(gettor$date))
end <- seq(from = Sys.Date(), length = 2, by = "-1 day")[2]
total <- data.frame(date = gettor$date,
  packages = rowSums(gettor[2:length(gettor)]) - gettor$none)
en <- data.frame(date = gettor$date,
  packages = gettor$tor.browser.bundle_en + gettor$tor.im.browser.bundle_en)
zh_cn <- data.frame(date = gettor$date,
  packages = gettor$tor.browser.bundle_zh_cn +
  gettor$tor.im.browser.bundle_zh_cn)
fa <- data.frame(date = gettor$date,
  packages = gettor$tor.browser.bundle_fa + gettor$tor.im.browser.bundle_fa)

write.csv(data.frame(date = gettor$date,
  total = rowSums(gettor[2:length(gettor)]) - gettor$none,
  en = gettor$tor.browser.bundle_en + gettor$tor.im.browser.bundle_en,
  zh_cn = gettor$tor.browser.bundle_zh_cn +
    gettor$tor.im.browser.bundle_zh_cn,
  fa = gettor$tor.browser.bundle_fa + gettor$tor.im.browser.bundle_fa),
  "website/csv/gettor.csv", quote = FALSE, row.names = FALSE)

plot_packages <- function(filename, title, data) {
  ggplot(data, aes(x = as.Date(date, "%Y-%m-%d"), y = packages)) + geom_line() +
    scale_x_date(name = "", limits = c(start, end)) +
    scale_y_continuous(name = "",
    limits = c(0, max(data$packages, na.rm = TRUE))) +
    opts(title = paste(title, "\n", sep = ""))
  ggsave(filename = paste("website/graphs/gettor/", filename, sep = ""),
    width = 8, height = 5, dpi = 72)
}

plot_packages("gettor-total.png",
  "Total packages requested from GetTor per day", total)
plot_packages("gettor-en.png",
  "Tor Browser Bundles (en) requested from GetTor per day", en)
plot_packages("gettor-zh_cn.png",
  "Tor Browser Bundles (zh_CN) requested from GetTor per day", zh_cn)
plot_packages("gettor-fa.png",
  "Tor Browser Bundles (fa) requested from GetTor per day", fa)

