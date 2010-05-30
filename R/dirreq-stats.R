options(warn = -1)
suppressPackageStartupMessages(library("ggplot2"))

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

plot_current <- function(directory, filenamePart, titlePart, data,
    countries) {
  plot_alldata(directory, filenamePart, titlePart, data, countries)
  plot_pastdays(directory, filenamePart, titlePart, c(30, 90, 180), data,
    countries)
  today <- as.POSIXct(Sys.Date(), tz = "GMT")
  one_week_ago <- seq(from = today, length = 2, by = "-7 days")[2]
  year_today <- format(today, "%Y")
  year_one_week_ago <- format(one_week_ago, "%Y")
  quarter_today <- 1 + floor((as.numeric(format(today, "%m")) - 1) / 3)
  quarter_one_week_ago <- 1 + floor((as.numeric(format(one_week_ago,
    "%m")) - 1) / 3)
  month_today <- as.numeric(format(today, "%m"))
  month_one_week_ago <- as.numeric(format(one_week_ago, "%m"))
  plot_years(directory, filenamePart, titlePart, union(year_today,
    year_one_week_ago), data, countries)
  if (year_today == year_one_week_ago) {
    plot_quarters(directory, filenamePart, titlePart, year_today,
      union(quarter_today, quarter_one_week_ago), data, countries)
  } else {
    plot_quarters(directory, filenamePart, titlePart, year_today,
      quarter_today, data, countries)
    plot_quarters(directory, filenamePart, titlePart, year_one_week_ago,
      quarter_one_week_ago, data, countries)
  }
  if (year_today == year_one_week_ago) {
    plot_months(directory, filenamePart, titlePart, year_today,
      union(month_today, month_one_week_ago), data, countries)
  } else {
    plot_months(directory, filenamePart, titlePart, year_today,
      month_today, data, countries)
    plot_months(directory, filenamePart, titlePart, year_one_week_ago,
      month_one_week_ago, data, countries)
  }
}

countries <- data.frame(code = c("bh", "cn", "cu", "et", "ir", "mm", "sa",
  "sy", "tn", "tm", "uz", "vn", "ye"), people = c("Bahraini", "Chinese",
  "Cuban", "Ethiopian", "Iranian", "Burmese", "Saudi", "Syrian",
  "Tunisian", "Turkmen", "Uzbek", "Vietnamese", "Yemeni"), filename =
  c("bahrain", "china", "cuba", "ethiopia", "iran", "burma", "saudi",
  "syria", "tunisia", "turkmenistan", "uzbekistan", "vietnam", "yemen"),
  stringsAsFactors = FALSE)

if (file.exists("stats/dirreq-stats")) {
  dirreq <- read.csv("stats/dirreq-stats", header = TRUE,
    stringsAsFactors = FALSE)
  gabelmooSub <- subset(dirreq, directory %in%
    c("68333D0761BCF397A587A0C0B963E4A9E99EC4D3",
      "F2044413DAC2E02E3D6BCF4735A19BCA1DE97281"))
  gabelmoo <- data.frame(date = gabelmooSub$date,
    gabelmooSub[3:(length(gabelmooSub) - 1)] * 6)
  trustedSub <- dirreq[dirreq$directory ==
    "8522EB98C91496E80EC238E732594D1509158E77",]
  trustedSub[!is.na(trustedSub$share) & trustedSub$share < 0.01,
    3:length(trustedSub)] <- NA
  trusted <- data.frame(date = trustedSub$date,
    floor(trustedSub[3:(length(trustedSub) - 1)] / trustedSub$share * 10))

  write.csv(gabelmoo, "website/csv/new-users.csv", quote = FALSE,
    row.names = FALSE)
  write.csv(trusted, "website/csv/recurring-users.csv", quote = FALSE,
    row.names = FALSE)

  plot_current("website/graphs/new-users/", "-new",
    "New or returning, directly connecting", gabelmoo, countries)
  plot_current("website/graphs/direct-users/", "-direct",
    "Recurring, directly connecting", trusted, countries)
}

