options(warn = -1)
suppressPackageStartupMessages(library("ggplot2"))

bridge <- read.csv("stats/bridge-stats", header = TRUE,
  stringsAsFactors = FALSE)
write.csv(bridge, "website/csv/bridge-users.csv", quote = FALSE,
  row.names = FALSE)

plot_bridges <- function(filename, title, limits, code) {
  c <- data.frame(date = bridge$date, users = bridge[[code]])
  ggplot(c, aes(x = as.Date(date, "%Y-%m-%d"), y = users)) +
    geom_line() + scale_x_date(name = "", limits = limits) +
    scale_y_continuous(name = "", limits = c(0, max(bridge[[code]],
    na.rm = TRUE))) +
    opts(title = title)
  ggsave(filename = paste("website/graphs/", filename, sep = ""),
    width = 8, height = 5, dpi = 72)
}

plot_pastdays <- function(days, countries) {
  for (day in days) {
    for (country in 1:length(countries$code)) {
      code <- countries[country, 1]
      people <- countries[country, 2]
      filename <- countries[country, 3]
      end <- seq(from = Sys.Date(), length = 2, by = "-1 day")[2]
      start <- seq(from = end, length = 2, by = paste("-", day, " days",
        sep = ""))[2]
      plot_bridges(paste(filename, "-bridges-", day, "d.png", sep = ""),
        paste(people, "Tor users via bridges (past", day, "days)\n"),
        c(start, end), code)
    }
  }
}

plot_years <- function(years, countries) {
  for (year in years) {
    for (country in 1:length(countries$code)) {
      code <- countries[country, 1]
      people <- countries[country, 2]
      filename <- countries[country, 3]
      plot_bridges(paste(filename, "-bridges-", year, ".png", sep = ""),
        paste(people, " Tor users via bridges (", year, ")\n", sep = ""),
        as.Date(c(paste(year, "-01-01", sep = ""), paste(year, "-12-31",
        sep = ""))), code)
    }
  }
}

plot_quarters <- function(years, quarters, countries) {
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
        plot_bridges(paste(filename, "-bridges-", year, "-q", quarter,
          ".png", sep = ""), paste(people, " Tor users via bridges (Q",
          quarter, " ", year, ")\n", sep = ""), c(start, end), code)
      }
    }
  }
}

plot_months <- function(years, months, countries) {
  for (year in years) {
    for (month in months) {
      for (country in 1:length(countries$code)) {
        code <- countries[country, 1]
        people <- countries[country, 2]
        filename <- countries[country, 3]
        start <- as.Date(paste(year, "-", month, "-01", sep = ""))
        end <- seq(seq(start, length = 2, by = "1 month")[2], length = 2,
          by = "-1 day")[2]
        plot_bridges(paste(filename, "-bridges-", year, "-",
          format(start, "%m"), ".png", sep = ""), paste(people,
          " Tor users via bridges (", format(start, "%B"), " ", year,
          ")\n", sep = ""), c(start, end), code)
      }
    }
  }
}

# TODO these need to be updated manually
plot_current <- function(countries) {
  plot_pastdays(c(30, 90, 180), countries)
  plot_years("2010", countries)
  plot_quarters("2010", 1, countries)
  plot_months("2010", 1:2, countries)
}

countries <- data.frame(code = c("bh", "cn", "cu", "et", "ir", "mm", "sa",
  "sy", "tn", "tm", "uz", "vn", "ye"), people = c("Bahraini", "Chinese",
  "Cuban", "Ethiopian", "Iranian", "Burmese", "Saudi", "Syrian",
  "Tunisian", "Turkmen", "Uzbek", "Vietnamese", "Yemeni"), filename =
  c("bahrain", "china", "cuba", "ethiopia", "iran", "burma", "saudi",
  "syria", "tunisia", "turkmenistan", "uzbekistan", "vietnam", "yemen"),
  stringsAsFactors = FALSE)

plot_current(countries)

