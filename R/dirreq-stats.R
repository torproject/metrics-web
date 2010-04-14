options(warn = -1)
suppressPackageStartupMessages(library("ggplot2"))

dirreq <- read.csv("stats/dirreq-stats", header = TRUE,
  stringsAsFactors = FALSE)
moria1Sub <- subset(dirreq,
  directory %in% "9695DFC35FFEB861329B9F1AB04C46397020CE31")
moria1 <- data.frame(date = moria1Sub$date,
  moria1Sub[3:(length(moria1Sub) - 1)] * 6)
trustedSub <- subset(dirreq,
  directory %in% "8522EB98C91496E80EC238E732594D1509158E77")
trusted <- data.frame(date = trustedSub$date,
  floor(trustedSub[3:(length(trustedSub) - 1)] / trustedSub$share * 10))

write.csv(moria1, "website/csv/new-users.csv", quote = FALSE,
  row.names = FALSE)
write.csv(trusted, "website/csv/recurring-users.csv", quote = FALSE,
  row.names = FALSE)

plot_dirreq <- function(directory, filename, title, limits, data, code) {
  c <- data.frame(date = data$date, users = data[[code]])
  ggplot(c, aes(x = as.Date(date, "%Y-%m-%d"), y = users)) +
    geom_line() +
    scale_x_date(name = "", limits = limits) +
    scale_y_continuous(name = "",
    limits = c(0, max(c$users, na.rm = TRUE))) +
    opts(title = title)
  ggsave(filename = paste(directory, filename, sep = ""),
    width = 8, height = 5, dpi = 72)
}

plot_alldata <- function(directory, filenamePart, titlePart, data,
    countries) {
  for (country in 1:length(countries$code)) {
    code <- countries[country, 1]
    people <- countries[country, 2]
    filename <- countries[country, 3]
    end <- Sys.Date()
    start <- as.Date(data$date[1])
    plot_dirreq(directory, paste(filename, filenamePart, "-all.png",
      sep = ""), paste(titlePart, people, "Tor users (all data)\n"),
      c(start, end), data, code)
  }
}

plot_pastdays <- function(directory, filenamePart, titlePart, days, data,
    countries) {
  for (day in days) {
    for (country in 1:length(countries$code)) {
      code <- countries[country, 1]
      people <- countries[country, 2]
      filename <- countries[country, 3]
      end <- Sys.Date()
      start <- seq(from = end, length = 2, by = paste("-", day, " days",
        sep = ""))[2]
      plot_dirreq(directory, paste(filename, filenamePart, "-", day,
        "d.png", sep = ""), paste(titlePart, people, "Tor users (past",
        day, "days)\n"), c(start, end), data, code)
    }
  }
}

plot_years <- function(directory, filenamePart, titlePart, years, data,
    countries) {
  for (year in years) {
    for (country in 1:length(countries$code)) {
      code <- countries[country, 1]
      people <- countries[country, 2]
      filename <- countries[country, 3]
      plot_dirreq(directory, paste(filename, filenamePart, "-", year,
        ".png", sep = ""), paste(titlePart, " ", people, " Tor users (",
        year, ")\n", sep = ""), as.Date(c(paste(year, "-01-01", sep = ""),
        paste(year, "-12-31", sep = ""))), data, code)
    }
  }
}

plot_quarters <- function(directory, filenamePart, titlePart, years,
    quarters, data, countries) {
  for (year in years) {
    for (quarter in quarters) {
      for (country in 1:length(countries$code)) {
        code <- countries[country, 1]
        people <- countries[country, 2]
        filename <- countries[country, 3]
        start <- as.Date(paste(year, "-", (quarter - 1) * 3 + 1, "-01",
          sep = ""))
        end <- seq(seq(start, length = 2, by = "3 months")[2], length = 2,
          by = "-1 day")[2]
        plot_dirreq(directory, paste(filename, filenamePart, "-", year,
          "-q", quarter, ".png", sep = ""), paste(titlePart, " ", people,
          " Tor users (Q", quarter, " ", year, ")\n", sep = ""),
          c(start, end), data, code)
      }
    }
  }
}

plot_months <- function(directory, filenamePart, titlePart, years, months,
    data, countries) {
  for (year in years) {
    for (month in months) {
      for (country in 1:length(countries$code)) {
        code <- countries[country, 1]
        people <- countries[country, 2]
        filename <- countries[country, 3]
        start <- as.Date(paste(year, "-", month, "-01", sep = ""))
        end <- seq(seq(start, length = 2, by = "1 month")[2], length = 2,
          by = "-1 day")[2]
        plot_dirreq(directory, paste(filename, filenamePart, "-", year,
          "-", format(start, "%m"), ".png", sep = ""), paste(titlePart,
          " ", people, " Tor users (", format(start, "%B"), " ", year,
          ")\n", sep = ""), c(start, end), data, code)
      }
    }
  }
}

# TODO these need to be updated manually
plot_current <- function(directory, filenamePart, titlePart, data,
    countries) {
  plot_alldata(directory, filenamePart, titlePart, data, countries)
  plot_pastdays(directory, filenamePart, titlePart, c(30, 90, 180), data,
    countries)
  plot_years(directory, filenamePart, titlePart, "2010", data, countries)
  plot_quarters(directory, filenamePart, titlePart, "2010", 2, data,
    countries)
  plot_months(directory, filenamePart, titlePart, "2010", 4, data,
    countries)
}

countries <- data.frame(code = c("bh", "cn", "cu", "et", "ir", "mm", "sa",
  "sy", "tn", "tm", "uz", "vn", "ye"), people = c("Bahraini", "Chinese",
  "Cuban", "Ethiopian", "Iranian", "Burmese", "Saudi", "Syrian",
  "Tunisian", "Turkmen", "Uzbek", "Vietnamese", "Yemeni"), filename =
  c("bahrain", "china", "cuba", "ethiopia", "iran", "burma", "saudi",
  "syria", "tunisia", "turkmenistan", "uzbekistan", "vietnam", "yemen"),
  stringsAsFactors = FALSE)

plot_current("website/graphs/new-users/", "-new",
  "New or returning, directly connecting", moria1, countries)
plot_current("website/graphs/direct-users/", "-direct",
  "Recurring, directly connecting", trusted, countries)

