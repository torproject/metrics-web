require(reshape2)
require(ggplot2)
require(RColorBrewer)
require(scales)
require(dplyr)
require(tidyr)
require(readr)

countrylist <- list(
  "ad" = "Andorra",
  "ae" = "the United Arab Emirates",
  "af" = "Afghanistan",
  "ag" = "Antigua and Barbuda",
  "ai" = "Anguilla",
  "al" = "Albania",
  "am" = "Armenia",
  "an" = "the Netherlands Antilles",
  "ao" = "Angola",
  "aq" = "Antarctica",
  "ar" = "Argentina",
  "as" = "American Samoa",
  "at" = "Austria",
  "au" = "Australia",
  "aw" = "Aruba",
  "ax" = "the Aland Islands",
  "az" = "Azerbaijan",
  "ba" = "Bosnia and Herzegovina",
  "bb" = "Barbados",
  "bd" = "Bangladesh",
  "be" = "Belgium",
  "bf" = "Burkina Faso",
  "bg" = "Bulgaria",
  "bh" = "Bahrain",
  "bi" = "Burundi",
  "bj" = "Benin",
  "bl" = "Saint Bartelemey",
  "bm" = "Bermuda",
  "bn" = "Brunei",
  "bo" = "Bolivia",
  "bq" = "Bonaire, Sint Eustatius and Saba",
  "br" = "Brazil",
  "bs" = "the Bahamas",
  "bt" = "Bhutan",
  "bv" = "the Bouvet Island",
  "bw" = "Botswana",
  "by" = "Belarus",
  "bz" = "Belize",
  "ca" = "Canada",
  "cc" = "the Cocos (Keeling) Islands",
  "cd" = "the Democratic Republic of the Congo",
  "cf" = "Central African Republic",
  "cg" = "Congo",
  "ch" = "Switzerland",
  "ci" = "Côte d'Ivoire",
  "ck" = "the Cook Islands",
  "cl" = "Chile",
  "cm" = "Cameroon",
  "cn" = "China",
  "co" = "Colombia",
  "cr" = "Costa Rica",
  "cu" = "Cuba",
  "cv" = "Cape Verde",
  "cw" = "Curaçao",
  "cx" = "the Christmas Island",
  "cy" = "Cyprus",
  "cz" = "the Czech Republic",
  "de" = "Germany",
  "dj" = "Djibouti",
  "dk" = "Denmark",
  "dm" = "Dominica",
  "do" = "the Dominican Republic",
  "dz" = "Algeria",
  "ec" = "Ecuador",
  "ee" = "Estonia",
  "eg" = "Egypt",
  "eh" = "the Western Sahara",
  "er" = "Eritrea",
  "es" = "Spain",
  "et" = "Ethiopia",
  "fi" = "Finland",
  "fj" = "Fiji",
  "fk" = "the Falkland Islands (Malvinas)",
  "fm" = "the Federated States of Micronesia",
  "fo" = "the Faroe Islands",
  "fr" = "France",
  "ga" = "Gabon",
  "gb" = "the United Kingdom",
  "gd" = "Grenada",
  "ge" = "Georgia",
  "gf" = "French Guiana",
  "gg" = "Guernsey",
  "gh" = "Ghana",
  "gi" = "Gibraltar",
  "gl" = "Greenland",
  "gm" = "Gambia",
  "gn" = "Guinea",
  "gp" = "Guadeloupe",
  "gq" = "Equatorial Guinea",
  "gr" = "Greece",
  "gs" = "South Georgia and the South Sandwich Islands",
  "gt" = "Guatemala",
  "gu" = "Guam",
  "gw" = "Guinea-Bissau",
  "gy" = "Guyana",
  "hk" = "Hong Kong",
  "hm" = "Heard Island and McDonald Islands",
  "hn" = "Honduras",
  "hr" = "Croatia",
  "ht" = "Haiti",
  "hu" = "Hungary",
  "id" = "Indonesia",
  "ie" = "Ireland",
  "il" = "Israel",
  "im" = "the Isle of Man",
  "in" = "India",
  "io" = "the British Indian Ocean Territory",
  "iq" = "Iraq",
  "ir" = "Iran",
  "is" = "Iceland",
  "it" = "Italy",
  "je" = "Jersey",
  "jm" = "Jamaica",
  "jo" = "Jordan",
  "jp" = "Japan",
  "ke" = "Kenya",
  "kg" = "Kyrgyzstan",
  "kh" = "Cambodia",
  "ki" = "Kiribati",
  "km" = "Comoros",
  "kn" = "Saint Kitts and Nevis",
  "kp" = "North Korea",
  "kr" = "the Republic of Korea",
  "kw" = "Kuwait",
  "ky" = "the Cayman Islands",
  "kz" = "Kazakhstan",
  "la" = "Laos",
  "lb" = "Lebanon",
  "lc" = "Saint Lucia",
  "li" = "Liechtenstein",
  "lk" = "Sri Lanka",
  "lr" = "Liberia",
  "ls" = "Lesotho",
  "lt" = "Lithuania",
  "lu" = "Luxembourg",
  "lv" = "Latvia",
  "ly" = "Libya",
  "ma" = "Morocco",
  "mc" = "Monaco",
  "md" = "the Republic of Moldova",
  "me" = "Montenegro",
  "mf" = "Saint Martin",
  "mg" = "Madagascar",
  "mh" = "the Marshall Islands",
  "mk" = "Macedonia",
  "ml" = "Mali",
  "mm" = "Burma",
  "mn" = "Mongolia",
  "mo" = "Macau",
  "mp" = "the Northern Mariana Islands",
  "mq" = "Martinique",
  "mr" = "Mauritania",
  "ms" = "Montserrat",
  "mt" = "Malta",
  "mu" = "Mauritius",
  "mv" = "the Maldives",
  "mw" = "Malawi",
  "mx" = "Mexico",
  "my" = "Malaysia",
  "mz" = "Mozambique",
  "na" = "Namibia",
  "nc" = "New Caledonia",
  "ne" = "Niger",
  "nf" = "Norfolk Island",
  "ng" = "Nigeria",
  "ni" = "Nicaragua",
  "nl" = "the Netherlands",
  "no" = "Norway",
  "np" = "Nepal",
  "nr" = "Nauru",
  "nu" = "Niue",
  "nz" = "New Zealand",
  "om" = "Oman",
  "pa" = "Panama",
  "pe" = "Peru",
  "pf" = "French Polynesia",
  "pg" = "Papua New Guinea",
  "ph" = "the Philippines",
  "pk" = "Pakistan",
  "pl" = "Poland",
  "pm" = "Saint Pierre and Miquelon",
  "pn" = "the Pitcairn Islands",
  "pr" = "Puerto Rico",
  "ps" = "the Palestinian Territory",
  "pt" = "Portugal",
  "pw" = "Palau",
  "py" = "Paraguay",
  "qa" = "Qatar",
  "re" = "Reunion",
  "ro" = "Romania",
  "rs" = "Serbia",
  "ru" = "Russia",
  "rw" = "Rwanda",
  "sa" = "Saudi Arabia",
  "sb" = "the Solomon Islands",
  "sc" = "the Seychelles",
  "sd" = "Sudan",
  "se" = "Sweden",
  "sg" = "Singapore",
  "sh" = "Saint Helena",
  "si" = "Slovenia",
  "sj" = "Svalbard and Jan Mayen",
  "sk" = "Slovakia",
  "sl" = "Sierra Leone",
  "sm" = "San Marino",
  "sn" = "Senegal",
  "so" = "Somalia",
  "sr" = "Suriname",
  "ss" = "South Sudan",
  "st" = "São Tomé and Príncipe",
  "sv" = "El Salvador",
  "sx" = "Sint Maarten",
  "sy" = "the Syrian Arab Republic",
  "sz" = "Swaziland",
  "tc" = "Turks and Caicos Islands",
  "td" = "Chad",
  "tf" = "the French Southern Territories",
  "tg" = "Togo",
  "th" = "Thailand",
  "tj" = "Tajikistan",
  "tk" = "Tokelau",
  "tl" = "East Timor",
  "tm" = "Turkmenistan",
  "tn" = "Tunisia",
  "to" = "Tonga",
  "tr" = "Turkey",
  "tt" = "Trinidad and Tobago",
  "tv" = "Tuvalu",
  "tw" = "Taiwan",
  "tz" = "the United Republic of Tanzania",
  "ua" = "Ukraine",
  "ug" = "Uganda",
  "um" = "the United States Minor Outlying Islands",
  "us" = "the United States",
  "uy" = "Uruguay",
  "uz" = "Uzbekistan",
  "va" = "Vatican City",
  "vc" = "Saint Vincent and the Grenadines",
  "ve" = "Venezuela",
  "vg" = "the British Virgin Islands",
  "vi" = "the United States Virgin Islands",
  "vn" = "Vietnam",
  "vu" = "Vanuatu",
  "wf" = "Wallis and Futuna",
  "ws" = "Samoa",
  "xk" = "Kosovo",
  "ye" = "Yemen",
  "yt" = "Mayotte",
  "za" = "South Africa",
  "zm" = "Zambia",
  "zw" = "Zimbabwe")

countryname <- function(country) {
  res <- countrylist[[country]]
  if (is.null(res))
    res <- "no-man's-land"
  res
}

# Helper function that takes date limits as input and returns major breaks as
# output. The main difference to the built-in major breaks is that we're trying
# harder to align major breaks with first days of weeks (Sundays), months,
# quarters, or years.
custom_breaks <- function(input) {
  scales_index <- cut(as.numeric(max(input) - min(input)),
    c(-1, 7, 12, 56, 180, 600, 2000, Inf), labels = FALSE)
  from_print_format <- c("%F", "%F", "%Y-W%U-7", "%Y-%m-01", "%Y-01-01",
    "%Y-01-01", "%Y-01-01")[scales_index]
  from_parse_format <- ifelse(scales_index == 3, "%Y-W%U-%u", "%F")
  by <- c("1 day", "2 days", "1 week", "1 month", "3 months", "1 year",
    "2 years")[scales_index]
  seq(as.Date(as.character(min(input), from_print_format),
    format = from_parse_format), max(input), by = by)
}

# Helper function that takes date limits as input and returns minor breaks as
# output. As opposed to the built-in minor breaks, we're not just adding one
# minor break half way through between two major breaks. Instead, we're plotting
# a minor break for every day, week, month, or quarter between two major breaks.
custom_minor_breaks <- function(input) {
  scales_index <- cut(as.numeric(max(input) - min(input)),
    c(-1, 7, 12, 56, 180, 600, 2000, Inf), labels = FALSE)
  from_print_format <- c("%F", "%F", "%F", "%Y-W%U-7", "%Y-%m-01", "%Y-01-01",
    "%Y-01-01")[scales_index]
  from_parse_format <- ifelse(scales_index == 4, "%Y-W%U-%u", "%F")
  by <- c("1 day", "1 day", "1 day", "1 week", "1 month", "3 months",
    "1 year")[scales_index]
  seq(as.Date(as.character(min(input), from_print_format),
    format = from_parse_format), max(input), by = by)
}

# Helper function that takes breaks as input and returns labels as output. We're
# going all ISO-8601 here, though we're not just writing %Y-%m-%d everywhere,
# but %Y-%m or %Y if all breaks are on the first of a month or even year.
custom_labels <- function(breaks) {
  if (all(format(breaks, format = "%m-%d") == "01-01", na.rm = TRUE)) {
    format(breaks, format = "%Y")
  } else {
    if (all(format(breaks, format = "%d") == "01", na.rm = TRUE)) {
      format(breaks, format = "%Y-%m")
    } else {
      format(breaks, format = "%F")
    }
  }
}

# Helper function to format numbers in non-scientific notation with spaces as
# thousands separator.
formatter <- function(x, ...) {
  format(x, ..., scientific = FALSE, big.mark = " ")
}

theme_update(
  # Make plot title centered, and leave some room to the plot.
  plot.title = element_text(hjust = 0.5, margin = margin(b = 11)),

  # Leave a little more room to the right for long x axis labels.
  plot.margin = margin(5.5, 11, 5.5, 5.5)
)

# Set the default line size of geom_line() to 1.
update_geom_defaults("line", list(size = 1))

copyright_notice <- "The Tor Project - https://metrics.torproject.org/"

stats_dir <- "/srv/metrics.torproject.org/metrics/shared/stats/"

no_data_available_dir <- "/srv/metrics.torproject.org/metrics/src/main/R/rserver/"

# Helper function that copies the appropriate no data object to filename.
copy_no_data <- function(filename) {
  len <- nchar(filename)
  extension <- substr(filename, len - 3, len)
  if (".csv" == extension) {
    write("# No data available for the given parameters.", file=filename)
  } else {
    file.copy(paste(no_data_available_dir, "no-data-available", extension,
      sep = ""), filename)
  }
}

# Helper function wrapping calls into error handling.
robust_call <- function(wrappee, filename) {
  tryCatch(eval(wrappee), error = function(e) copy_no_data(filename),
     finally = if (!file.exists(filename) || file.size(filename) == 0) {
       copy_no_data(filename)
       })
}

# Write the result of the given FUN, typically a prepare_ function, as .csv file
# to the given path_p.
write_data <- function(FUN, ..., path_p) {
  FUN(...) %>%
    write.csv(path_p, quote = FALSE, row.names = FALSE, na = "")
}

# Disable readr's automatic progress bar.
options(readr.show_progress = FALSE)

prepare_networksize <- function(start_p = NULL, end_p = NULL) {
  read_csv(file = paste(stats_dir, "networksize.csv", sep = ""),
      col_types = cols(
        date = col_date(format = ""),
        relays = col_double(),
        bridges = col_double())) %>%
    filter(if (!is.null(start_p)) date >= as.Date(start_p) else TRUE) %>%
    filter(if (!is.null(end_p)) date <= as.Date(end_p) else TRUE)
}

plot_networksize <- function(start_p, end_p, path_p) {
  prepare_networksize(start_p, end_p) %>%
    gather(variable, value, -date) %>%
    complete(date = full_seq(date, period = 1),
      variable = c("relays", "bridges")) %>%
    ggplot(aes(x = date, y = value, colour = variable)) +
    geom_line() +
    scale_x_date(name = "", breaks = custom_breaks,
      labels = custom_labels, minor_breaks = custom_minor_breaks) +
    scale_y_continuous(name = "", labels = formatter, limits = c(0, NA)) +
    scale_colour_hue("", breaks = c("relays", "bridges"),
        labels = c("Relays", "Bridges")) +
    ggtitle("Number of relays") +
    labs(caption = copyright_notice)
  ggsave(filename = path_p, width = 8, height = 5, dpi = 150)
}

prepare_versions <- function(start_p = NULL, end_p = NULL) {
  read_csv(paste(stats_dir, "versions.csv", sep = ""),
      col_types = cols(
        date = col_date(format = ""),
        version = col_character(),
        relays = col_double())) %>%
    filter(if (!is.null(start_p)) date >= as.Date(start_p) else TRUE) %>%
    filter(if (!is.null(end_p)) date <= as.Date(end_p) else TRUE)
}

plot_versions <- function(start_p, end_p, path_p) {
  s <- prepare_versions(start_p, end_p)
  known_versions <- unique(s$version)
  getPalette <- colorRampPalette(brewer.pal(12, "Paired"))
  colours <- data.frame(breaks = known_versions,
    values = rep(brewer.pal(min(12, length(known_versions)), "Paired"),
                 len = length(known_versions)),
    stringsAsFactors = FALSE)
  versions <- s[s$version %in% known_versions, ]
  visible_versions <- sort(unique(versions$version))
  versions <- versions %>%
    complete(date = full_seq(date, period = 1), nesting(version)) %>%
    ggplot(aes(x = date, y = relays, colour = version)) +
    geom_line() +
    scale_x_date(name = "", breaks = custom_breaks,
      labels = custom_labels, minor_breaks = custom_minor_breaks) +
    scale_y_continuous(name = "", labels = formatter, limits = c(0, NA)) +
    scale_colour_manual(name = "Tor version",
      values = colours[colours$breaks %in% visible_versions, 2],
      breaks = visible_versions) +
    ggtitle("Relay versions") +
    labs(caption = copyright_notice)
  ggsave(filename = path_p, width = 8, height = 5, dpi = 150)
}

prepare_platforms <- function(start_p = NULL, end_p = NULL) {
  read_csv(file = paste(stats_dir, "platforms.csv", sep = ""),
      col_types = cols(
        date = col_date(format = ""),
        platform = col_factor(levels = NULL),
        relays = col_double())) %>%
    filter(if (!is.null(start_p)) date >= as.Date(start_p) else TRUE) %>%
    filter(if (!is.null(end_p)) date <= as.Date(end_p) else TRUE) %>%
    mutate(platform = tolower(platform)) %>%
    dcast(date ~ platform, value.var = "relays")
}

plot_platforms <- function(start_p, end_p, path_p) {
  prepare_platforms(start_p, end_p) %>%
    gather(platform, relays, -date) %>%
    complete(date = full_seq(date, period = 1), nesting(platform)) %>%
    ggplot(aes(x = date, y = relays, colour = platform)) +
    geom_line() +
    scale_x_date(name = "", breaks = custom_breaks,
      labels = custom_labels, minor_breaks = custom_minor_breaks) +
    scale_y_continuous(name = "", labels = formatter, limits = c(0, NA)) +
    scale_colour_manual(name = "Platform",
      breaks = c("linux", "macos", "bsd", "windows", "other"),
      labels = c("Linux", "macOS", "BSD", "Windows", "Other"),
      values = c("linux" = "#56B4E9", "macos" = "#333333", "bsd" = "#E69F00",
          "windows" = "#0072B2", "other" = "#009E73")) +
    ggtitle("Relay platforms") +
    labs(caption = copyright_notice)
  ggsave(filename = path_p, width = 8, height = 5, dpi = 150)
}

prepare_dirbytes <- function(start_p = NULL, end_p = NULL) {
  read_csv(file = paste(stats_dir, "bandwidth.csv", sep = ""),
      col_types = cols(
        date = col_date(format = ""),
        isexit = col_logical(),
        isguard = col_logical(),
        bwread = col_skip(),
        bwwrite = col_skip(),
        dirread = col_double(),
        dirwrite = col_double())) %>%
    filter(if (!is.null(start_p)) date >= as.Date(start_p) else TRUE) %>%
    filter(if (!is.null(end_p)) date <= as.Date(end_p) else TRUE) %>%
    filter(is.na(isexit)) %>%
    filter(is.na(isguard)) %>%
    mutate(dirread = dirread * 8 / 1e9,
      dirwrite = dirwrite * 8 / 1e9) %>%
    select(date, dirread, dirwrite)
}

plot_dirbytes <- function(start_p, end_p, path_p) {
  prepare_dirbytes(start_p, end_p) %>%
    gather(variable, value, -date) %>%
    complete(date = full_seq(date, period = 1), nesting(variable)) %>%
    ggplot(aes(x = date, y = value, colour = variable)) +
    geom_line() +
    scale_x_date(name = "", breaks = custom_breaks,
      labels = custom_labels, minor_breaks = custom_minor_breaks) +
    scale_y_continuous(name = "", labels = function(x) sprintf("%.1f Gbit/s", x),
      limits = c(0, NA)) +
    scale_colour_hue(name = "",
        breaks = c("dirwrite", "dirread"),
        labels = c("Written dir bytes", "Read dir bytes")) +
    ggtitle("Number of bytes spent on answering directory requests") +
    labs(caption = copyright_notice) +
    theme(legend.position = "top")
  ggsave(filename = path_p, width = 8, height = 5, dpi = 150)
}

prepare_relayflags <- function(start_p = NULL, end_p = NULL, flag_p = NULL) {
  read_csv(file = paste(stats_dir, "relayflags.csv", sep = ""),
      col_types = cols(
        date = col_date(format = ""),
        flag = col_factor(levels = NULL),
        relays = col_double())) %>%
    filter(if (!is.null(start_p)) date >= as.Date(start_p) else TRUE) %>%
    filter(if (!is.null(end_p)) date <= as.Date(end_p) else TRUE) %>%
    filter(if (!is.null(flag_p)) flag %in% flag_p else TRUE)
}

plot_relayflags <- function(start_p, end_p, flag_p, path_p) {
  prepare_relayflags(start_p, end_p, flag_p) %>%
    complete(date = full_seq(date, period = 1), flag = unique(flag)) %>%
    ggplot(aes(x = date, y = relays, colour = flag)) +
    geom_line() +
    scale_x_date(name = "", breaks = custom_breaks,
      labels = custom_labels, minor_breaks = custom_minor_breaks) +
    scale_y_continuous(name = "", labels = formatter, limits = c(0, NA)) +
    scale_colour_manual(name = "Relay flags", values = c("#E69F00",
        "#56B4E9", "#009E73", "#EE6A50", "#000000", "#0072B2"),
        breaks = flag_p, labels = flag_p) +
    ggtitle("Number of relays with relay flags assigned") +
    labs(caption = copyright_notice)
  ggsave(filename = path_p, width = 8, height = 5, dpi = 150)
}

prepare_torperf <- function(start_p = NULL, end_p = NULL, server_p = NULL,
    filesize_p = NULL) {
  read_csv(file = paste(stats_dir, "torperf-1.1.csv", sep = ""),
      col_types = cols(
        date = col_date(format = ""),
        filesize = col_double(),
        source = col_character(),
        server = col_character(),
        q1 = col_double(),
        md = col_double(),
        q3 = col_double(),
        timeouts = col_skip(),
        failures = col_skip(),
        requests = col_skip())) %>%
    filter(if (!is.null(start_p)) date >= as.Date(start_p) else TRUE) %>%
    filter(if (!is.null(end_p)) date <= as.Date(end_p) else TRUE) %>%
    filter(if (!is.null(server_p)) server == server_p else TRUE) %>%
    filter(if (!is.null(filesize_p))
        filesize == ifelse(filesize_p == "50kb", 50 * 1024,
        ifelse(filesize_p == "1mb", 1024 * 1024, 5 * 1024 * 1024)) else
        TRUE) %>%
    transmute(date, filesize, source, server, q1 = q1 / 1e3, md = md / 1e3,
      q3 = q3 / 1e3)
}

plot_torperf <- function(start_p, end_p, server_p, filesize_p, path_p) {
  prepare_torperf(start_p, end_p, server_p, filesize_p) %>%
    filter(source != "") %>%
    complete(date = full_seq(date, period = 1), nesting(source)) %>%
    ggplot(aes(x = date, y = md, ymin = q1, ymax = q3, fill = source)) +
    geom_ribbon(alpha = 0.5) +
    geom_line(aes(colour = source), size = 0.75) +
    scale_x_date(name = "", breaks = custom_breaks,
      labels = custom_labels, minor_breaks = custom_minor_breaks) +
    scale_y_continuous(name = "", labels = unit_format(unit = "s"),
      limits = c(0, NA)) +
    scale_fill_hue(name = "Source") +
    scale_colour_hue(name = "Source") +
    ggtitle(paste("Time to complete",
        ifelse(filesize_p == "50kb", "50 KiB",
        ifelse(filesize_p == "1mb", "1 MiB", "5 MiB")),
        "request to", server_p, "server")) +
    labs(caption = copyright_notice) +
    theme(legend.position = "top")
  ggsave(filename = path_p, width = 8, height = 5, dpi = 150)
}

prepare_torperf_failures <- function(start_p = NULL, end_p = NULL,
    server_p = NULL, filesize_p = NULL) {
  read_csv(file = paste(stats_dir, "torperf-1.1.csv", sep = ""),
      col_types = cols(
        date = col_date(format = ""),
        filesize = col_double(),
        source = col_character(),
        server = col_character(),
        q1 = col_skip(),
        md = col_skip(),
        q3 = col_skip(),
        timeouts = col_double(),
        failures = col_double(),
        requests = col_double())) %>%
    filter(if (!is.null(start_p)) date >= as.Date(start_p) else TRUE) %>%
    filter(if (!is.null(end_p)) date <= as.Date(end_p) else TRUE) %>%
    filter(if (!is.null(filesize_p))
        filesize == ifelse(filesize_p == "50kb", 50 * 1024,
        ifelse(filesize_p == "1mb", 1024 * 1024, 5 * 1024 * 1024)) else
        TRUE) %>%
    filter(if (!is.null(server_p)) server == server_p else TRUE) %>%
    filter(requests > 0) %>%
    transmute(date, filesize, source, server, timeouts = timeouts / requests,
        failures = failures / requests)
}

plot_torperf_failures <- function(start_p, end_p, server_p, filesize_p,
    path_p) {
  prepare_torperf_failures(start_p, end_p, server_p, filesize_p) %>%
    filter(source != "") %>%
    gather(variable, value, -c(date, filesize, source, server)) %>%
    mutate(variable = factor(variable, levels = c("timeouts", "failures"),
      labels = c("Timeouts", "Failures"))) %>%
    ggplot(aes(x = date, y = value, colour = source)) +
    geom_point(size = 2, alpha = 0.5) +
    scale_x_date(name = "", breaks = custom_breaks,
      labels = custom_labels, minor_breaks = custom_minor_breaks) +
    scale_y_continuous(name = "", labels = percent, limits = c(0, NA)) +
    scale_colour_hue(name = "Source") +
    facet_grid(variable ~ .) +
    ggtitle(paste("Timeouts and failures of",
        ifelse(filesize_p == "50kb", "50 KiB",
        ifelse(filesize_p == "1mb", "1 MiB", "5 MiB")),
        "requests to", server_p, "server")) +
    labs(caption = copyright_notice) +
    theme(legend.position = "top")
  ggsave(filename = path_p, width = 8, height = 5, dpi = 150)
}

prepare_onionperf_buildtimes <- function(start_p = NULL, end_p = NULL) {
  read_csv(file = paste(stats_dir, "buildtimes.csv", sep = ""),
      col_types = cols(
        date = col_date(format = ""),
        source = col_character(),
        position = col_double(),
        q1 = col_double(),
        md = col_double(),
        q3 = col_double())) %>%
    filter(if (!is.null(start_p)) date >= as.Date(start_p) else TRUE) %>%
    filter(if (!is.null(end_p)) date <= as.Date(end_p) else TRUE)
}

plot_onionperf_buildtimes <- function(start_p, end_p, path_p) {
  prepare_onionperf_buildtimes(start_p, end_p) %>%
    filter(source != "") %>%
    mutate(date = as.Date(date),
      position = factor(position, levels = seq(1, 3, 1),
        labels = c("1st hop", "2nd hop", "3rd hop"))) %>%
    complete(date = full_seq(date, period = 1), nesting(source, position)) %>%
    ggplot(aes(x = date, y = md, ymin = q1, ymax = q3, fill = source)) +
    geom_ribbon(alpha = 0.5) +
    geom_line(aes(colour = source), size = 0.75) +
    facet_grid(position ~ .) +
    scale_x_date(name = "", breaks = custom_breaks,
      labels = custom_labels, minor_breaks = custom_minor_breaks) +
    scale_y_continuous(name = "", labels = unit_format(unit = "ms"),
      limits = c(0, NA)) +
    scale_fill_hue(name = "Source") +
    scale_colour_hue(name = "Source") +
    ggtitle("Circuit build times") +
    labs(caption = copyright_notice) +
    theme(legend.position = "top")
  ggsave(filename = path_p, width = 8, height = 5, dpi = 150)
}

prepare_onionperf_latencies <- function(start_p = NULL, end_p = NULL,
    server_p = NULL) {
  read_csv(file = paste(stats_dir, "latencies.csv", sep = ""),
      col_types = cols(
        date = col_date(format = ""),
        source = col_character(),
        server = col_character(),
        low = col_double(),
        q1 = col_double(),
        md = col_double(),
        q3 = col_double(),
        high = col_double())) %>%
    filter(if (!is.null(start_p)) date >= as.Date(start_p) else TRUE) %>%
    filter(if (!is.null(end_p)) date <= as.Date(end_p) else TRUE) %>%
    filter(if (!is.null(server_p)) server == server_p else TRUE)
}

plot_onionperf_latencies <- function(start_p, end_p, server_p, path_p) {
  prepare_onionperf_latencies(start_p, end_p, server_p) %>%
    filter(source != "") %>%
    complete(date = full_seq(date, period = 1), nesting(source)) %>%
    ggplot(aes(x = date, ymin = q1, ymax = q3, fill = source)) +
    geom_ribbon(alpha = 0.5) +
    geom_line(aes(y = md, colour = source), size = 0.75) +
    geom_line(aes(y = high, colour = source), size = 0.375) +
    geom_line(aes(y = low, colour = source), size = 0.375) +
    scale_x_date(name = "", breaks = custom_breaks,
      labels = custom_labels, minor_breaks = custom_minor_breaks) +
    scale_y_continuous(name = "", labels = unit_format(unit = "ms"),
      limits = c(0, NA)) +
    scale_fill_hue(name = "Source") +
    scale_colour_hue(name = "Source") +
    facet_grid(source ~ ., scales = "free", space = "free") +
    ggtitle(paste("Circuit round-trip latencies to", server_p, "server")) +
    labs(caption = copyright_notice) +
    theme(legend.position = "none",
          strip.text.y = element_text(angle = 0, hjust = 0),
          strip.background = element_rect(fill = NA))
  ggsave(filename = path_p, width = 8, height = 5, dpi = 150)
}

prepare_onionperf_throughput <- function(start_p = NULL, end_p = NULL,
    server_p = NULL) {
  read_csv(file = paste(stats_dir, "onionperf-throughput.csv", sep = ""),
      col_types = cols(
        date = col_date(format = ""),
        source = col_character(),
        server = col_character(),
        low = col_double(),
        q1 = col_double(),
        md = col_double(),
        q3 = col_double(),
        high = col_double())) %>%
    filter(if (!is.null(start_p)) date >= as.Date(start_p) else TRUE) %>%
    filter(if (!is.null(end_p)) date <= as.Date(end_p) else TRUE) %>%
    filter(if (!is.null(server_p)) server == server_p else TRUE)
}

plot_onionperf_throughput <- function(start_p, end_p, server_p, path_p) {
  prepare_onionperf_throughput(start_p, end_p, server_p) %>%
    complete(date = full_seq(date, period = 1), nesting(source)) %>%
    ggplot(aes(x = date, ymin = q1 / 1000, ymax = q3 / 1000, fill = source)) +
    geom_ribbon(alpha = 0.5) +
    geom_line(aes(y = md / 1000, colour = source), size = 0.75) +
    geom_line(aes(y = high / 1000, colour = source), size = 0.375) +
    geom_line(aes(y = low / 1000, colour = source), size = 0.375) +
    scale_x_date(name = "", breaks = custom_breaks,
      labels = custom_labels, minor_breaks = custom_minor_breaks) +
    scale_y_continuous(name = "", labels = unit_format(unit = "Mbps"),
      limits = c(0, NA)) +
    scale_fill_hue(name = "Source") +
    scale_colour_hue(name = "Source") +
    facet_grid(source ~ ., scales = "free", space = "free") +
    ggtitle(paste("Throughput when downloading from", server_p, "server")) +
    labs(caption = copyright_notice) +
    theme(legend.position = "none",
          strip.text.y = element_text(angle = 0, hjust = 0),
          strip.background = element_rect(fill = NA))
  ggsave(filename = path_p, width = 8, height = 5, dpi = 150)
}

prepare_connbidirect <- function(start_p = NULL, end_p = NULL) {
  read_csv(file = paste(stats_dir, "connbidirect2.csv", sep = ""),
      col_types = cols(
        date = col_date(format = ""),
        direction = col_factor(levels = NULL),
        quantile = col_double(),
        fraction = col_double())) %>%
    filter(if (!is.null(start_p)) date >= as.Date(start_p) else TRUE) %>%
    filter(if (!is.null(end_p)) date <= as.Date(end_p) else TRUE) %>%
    mutate(quantile = paste("X", quantile, sep = ""),
      fraction = fraction / 100) %>%
    dcast(date + direction ~ quantile, value.var = "fraction") %>%
    rename(q1 = X0.25, md = X0.5, q3 = X0.75)
}

plot_connbidirect <- function(start_p, end_p, path_p) {
  prepare_connbidirect(start_p, end_p) %>%
    complete(date = full_seq(date, period = 1), nesting(direction)) %>%
    ggplot(aes(x = date, y = md, ymin = q1, ymax = q3, fill = direction)) +
    geom_ribbon(alpha = 0.5) +
    geom_line(aes(colour = direction), size = 0.75) +
    scale_x_date(name = "", breaks = custom_breaks,
      labels = custom_labels, minor_breaks = custom_minor_breaks) +
    scale_y_continuous(name = "", labels = percent, limits = c(0, NA)) +
    scale_colour_hue(name = "Medians and interquartile ranges",
                     breaks = c("both", "write", "read"),
        labels = c("Both reading and writing", "Mostly writing",
                   "Mostly reading")) +
    scale_fill_hue(name = "Medians and interquartile ranges",
                   breaks = c("both", "write", "read"),
        labels = c("Both reading and writing", "Mostly writing",
                   "Mostly reading")) +
    ggtitle("Fraction of connections used uni-/bidirectionally") +
    labs(caption = copyright_notice) +
    theme(legend.position = "top")
  ggsave(filename = path_p, width = 8, height = 5, dpi = 150)
}

prepare_bandwidth_flags <- function(start_p = NULL, end_p = NULL) {
  advbw <- read_csv(file = paste(stats_dir, "advbw.csv", sep = ""),
      col_types = cols(
        date = col_date(format = ""),
        isexit = col_logical(),
        isguard = col_logical(),
        advbw = col_double())) %>%
    transmute(date, have_guard_flag = isguard, have_exit_flag = isexit,
      variable = "advbw", value = advbw * 8 / 1e9)
  bwhist <- read_csv(file = paste(stats_dir, "bandwidth.csv", sep = ""),
      col_types = cols(
        date = col_date(format = ""),
        isexit = col_logical(),
        isguard = col_logical(),
        bwread = col_double(),
        bwwrite = col_double(),
        dirread = col_double(),
        dirwrite = col_double())) %>%
    transmute(date, have_guard_flag = isguard, have_exit_flag = isexit,
      variable = "bwhist", value = (bwread + bwwrite) * 8 / 2e9)
  rbind(advbw, bwhist) %>%
    filter(if (!is.null(start_p)) date >= as.Date(start_p) else TRUE) %>%
    filter(if (!is.null(end_p)) date <= as.Date(end_p) else TRUE) %>%
    filter(!is.na(have_exit_flag)) %>%
    filter(!is.na(have_guard_flag)) %>%
    dcast(date + have_guard_flag + have_exit_flag ~ variable,
      value.var = "value")
}

plot_bandwidth_flags <- function(start_p, end_p, path_p) {
  prepare_bandwidth_flags(start_p, end_p) %>%
    gather(variable, value, c(advbw, bwhist)) %>%
    unite(flags, have_guard_flag, have_exit_flag) %>%
    mutate(flags = factor(flags,
      levels = c("FALSE_TRUE", "TRUE_TRUE", "TRUE_FALSE", "FALSE_FALSE"),
      labels = c("Exit only", "Guard and Exit", "Guard only",
      "Neither Guard nor Exit"))) %>%
    mutate(variable = ifelse(variable == "advbw",
      "Advertised bandwidth", "Consumed bandwidth")) %>%
    ggplot(aes(x = date, y = value, fill = flags)) +
    geom_area() +
    scale_x_date(name = "", breaks = custom_breaks,
      labels = custom_labels, minor_breaks = custom_minor_breaks) +
    scale_y_continuous(name = "", labels = unit_format(unit = "Gbit/s"),
      limits = c(0, NA)) +
    scale_fill_manual(name = "",
      values = c("#03B3FF", "#39FF02", "#FFFF00", "#AAAA99")) +
    facet_grid(variable ~ .) +
    ggtitle("Advertised and consumed bandwidth by relay flags") +
    labs(caption = copyright_notice) +
    theme(legend.position = "top")
  ggsave(filename = path_p, width = 8, height = 5, dpi = 150)
}

prepare_bandwidth <- function(start_p = NULL, end_p = NULL) {
  prepare_bandwidth_flags(start_p, end_p) %>%
    group_by(date) %>%
    summarize(advbw = sum(advbw), bwhist = sum(bwhist))
}

plot_bandwidth <- function(start_p, end_p, path_p) {
  prepare_bandwidth(start_p, end_p) %>%
    gather(variable, value, -date) %>%
    ggplot(aes(x = date, y = value, colour = variable)) +
    geom_line() +
    scale_x_date(name = "", breaks = custom_breaks,
      labels = custom_labels, minor_breaks = custom_minor_breaks) +
    scale_y_continuous(name = "", labels = unit_format(unit = "Gbit/s"),
      limits = c(0, NA)) +
    scale_colour_hue(name = "", h.start = 90,
        breaks = c("advbw", "bwhist"),
        labels = c("Advertised bandwidth", "Bandwidth history")) +
    ggtitle("Total relay bandwidth") +
    labs(caption = copyright_notice) +
    theme(legend.position = "top")
  ggsave(filename = path_p, width = 8, height = 5, dpi = 150)
}

prepare_userstats_relay_country <- function(start_p = NULL, end_p = NULL,
    country_p = NULL, events_p = NULL) {
  read_csv(file = paste(stats_dir, "clients.csv", sep = ""),
      col_types = cols(
        date = col_date(format = ""),
        node = col_character(),
        country = col_character(),
        transport = col_character(),
        version = col_character(),
        lower = col_double(),
        upper = col_double(),
        clients = col_double(),
        frac = col_double()),
      na = character()) %>%
    filter(node == "relay") %>%
    filter(if (!is.null(start_p)) date >= as.Date(start_p) else TRUE) %>%
    filter(if (!is.null(end_p)) date <= as.Date(end_p) else TRUE) %>%
    filter(if (!is.null(country_p))
      country == ifelse(country_p == "all", "", country_p) else TRUE) %>%
    filter(transport == "") %>%
    filter(version == "") %>%
    select(date, country, clients, lower, upper, frac) %>%
    rename(users = clients)
}

plot_userstats_relay_country <- function(start_p, end_p, country_p, events_p,
    path_p) {
  u <- prepare_userstats_relay_country(start_p, end_p, country_p, events_p) %>%
    complete(date = full_seq(date, period = 1))
  plot <- ggplot(u, aes(x = date, y = users))
  if (length(na.omit(u$users)) > 0 & events_p != "off" &
      country_p != "all") {
    upturns <- u[u$users > u$upper, c("date", "users")]
    downturns <- u[u$users < u$lower, c("date", "users")]
    if (events_p == "on") {
      u[!is.na(u$lower) & u$lower < 0, "lower"] <- 0
      plot <- plot +
        geom_ribbon(data = u, aes(ymin = lower, ymax = upper), fill = "gray")
    }
    if (length(upturns$date) > 0)
      plot <- plot +
          geom_point(data = upturns, aes(x = date, y = users), size = 5,
          colour = "dodgerblue2")
    if (length(downturns$date) > 0)
      plot <- plot +
          geom_point(data = downturns, aes(x = date, y = users), size = 5,
          colour = "firebrick2")
  }
  plot <- plot +
    geom_line() +
    scale_x_date(name = "", breaks = custom_breaks,
      labels = custom_labels, minor_breaks = custom_minor_breaks) +
    scale_y_continuous(name = "", labels = formatter, limits = c(0, NA)) +
    ggtitle(paste("Directly connecting users",
        ifelse(country_p == "all", "",
        paste(" from", countryname(country_p))), sep = "")) +
    labs(caption = copyright_notice)
  ggsave(filename = path_p, width = 8, height = 5, dpi = 150)
}

prepare_userstats_bridge_country <- function(start_p = NULL, end_p = NULL,
    country_p = NULL) {
  read_csv(file = paste(stats_dir, "clients.csv", sep = ""),
      col_types = cols(
        date = col_date(format = ""),
        node = col_character(),
        country = col_character(),
        transport = col_character(),
        version = col_character(),
        lower = col_double(),
        upper = col_double(),
        clients = col_double(),
        frac = col_double()),
      na = character()) %>%
    filter(node == "bridge") %>%
    filter(if (!is.null(start_p)) date >= as.Date(start_p) else TRUE) %>%
    filter(if (!is.null(end_p)) date <= as.Date(end_p) else TRUE) %>%
    filter(if (!is.null(country_p))
      country == ifelse(country_p == "all", "", country_p) else TRUE) %>%
    filter(transport == "") %>%
    filter(version == "") %>%
    select(date, country, clients, frac) %>%
    rename(users = clients)
}

plot_userstats_bridge_country <- function(start_p, end_p, country_p, path_p) {
  prepare_userstats_bridge_country(start_p, end_p, country_p) %>%
    complete(date = full_seq(date, period = 1)) %>%
    ggplot(aes(x = date, y = users)) +
    geom_line() +
    scale_x_date(name = "", breaks = custom_breaks,
      labels = custom_labels, minor_breaks = custom_minor_breaks) +
    scale_y_continuous(name = "", labels = formatter, limits = c(0, NA)) +
    ggtitle(paste("Bridge users",
        ifelse(country_p == "all", "",
        paste(" from", countryname(country_p))), sep = "")) +
    labs(caption = copyright_notice)
  ggsave(filename = path_p, width = 8, height = 5, dpi = 150)
}

prepare_userstats_bridge_transport <- function(start_p = NULL, end_p = NULL,
    transport_p = NULL) {
  u <- read_csv(file = paste(stats_dir, "clients.csv", sep = ""),
      col_types = cols(
        date = col_date(format = ""),
        node = col_character(),
        country = col_character(),
        transport = col_character(),
        version = col_character(),
        lower = col_double(),
        upper = col_double(),
        clients = col_double(),
        frac = col_double())) %>%
    filter(node == "bridge") %>%
    filter(if (!is.null(start_p)) date >= as.Date(start_p) else TRUE) %>%
    filter(if (!is.null(end_p)) date <= as.Date(end_p) else TRUE) %>%
    filter(is.na(country)) %>%
    filter(is.na(version)) %>%
    filter(!is.na(transport)) %>%
    select(date, transport, clients, frac)
  if (is.null(transport_p) || "!<OR>" %in% transport_p) {
    n <- u %>%
      filter(transport != "<OR>") %>%
      group_by(date, frac) %>%
      summarize(clients = sum(clients))
    u <- rbind(u, data.frame(date = n$date, transport = "!<OR>",
                             clients = n$clients, frac = n$frac))
  }
  u %>%
    filter(if (!is.null(transport_p)) transport %in% transport_p else TRUE) %>%
    select(date, transport, clients, frac) %>%
    rename(users = clients) %>%
    arrange(date, transport)
}

plot_userstats_bridge_transport <- function(start_p, end_p, transport_p,
    path_p) {
  if (length(transport_p) > 1) {
    title <- paste("Bridge users by transport")
  } else {
    title <- paste("Bridge users using",
             ifelse(transport_p == "<??>", "unknown pluggable transport(s)",
             ifelse(transport_p == "<OR>", "default OR protocol",
             ifelse(transport_p == "!<OR>", "any pluggable transport",
             ifelse(transport_p == "fte", "FTE",
             ifelse(transport_p == "websocket", "Flash proxy/websocket",
             paste("transport", transport_p)))))))
  }
  u <- prepare_userstats_bridge_transport(start_p, end_p, transport_p) %>%
    complete(date = full_seq(date, period = 1), nesting(transport))
  if (length(transport_p) > 1) {
    plot <- ggplot(u, aes(x = date, y = users, colour = transport))
  } else {
    plot <- ggplot(u, aes(x = date, y = users))
  }
  plot <- plot +
    geom_line() +
    scale_x_date(name = "", breaks = custom_breaks,
      labels = custom_labels, minor_breaks = custom_minor_breaks) +
    scale_y_continuous(name = "", labels = formatter, limits = c(0, NA)) +
    ggtitle(title) +
    labs(caption = copyright_notice)
  if (length(transport_p) > 1) {
    plot <- plot +
      scale_colour_hue(name = "", breaks = transport_p,
            labels = ifelse(transport_p == "<??>", "Unknown PT",
                     ifelse(transport_p == "<OR>", "Default OR protocol",
                     ifelse(transport_p == "!<OR>", "Any PT",
                     ifelse(transport_p == "fte", "FTE",
                     ifelse(transport_p == "websocket", "Flash proxy/websocket",
                     transport_p))))))
  }
  ggsave(filename = path_p, width = 8, height = 5, dpi = 150)
}

prepare_userstats_bridge_version <- function(start_p = NULL, end_p = NULL,
    version_p = NULL) {
  read_csv(file = paste(stats_dir, "clients.csv", sep = ""),
      col_types = cols(
        date = col_date(format = ""),
        node = col_character(),
        country = col_character(),
        transport = col_character(),
        version = col_character(),
        lower = col_double(),
        upper = col_double(),
        clients = col_double(),
        frac = col_double())) %>%
    filter(node == "bridge") %>%
    filter(if (!is.null(start_p)) date >= as.Date(start_p) else TRUE) %>%
    filter(if (!is.null(end_p)) date <= as.Date(end_p) else TRUE) %>%
    filter(is.na(country)) %>%
    filter(is.na(transport)) %>%
    filter(if (!is.null(version_p)) version == version_p else TRUE) %>%
    select(date, version, clients, frac) %>%
    rename(users = clients)
}

plot_userstats_bridge_version <- function(start_p, end_p, version_p, path_p) {
  prepare_userstats_bridge_version(start_p, end_p, version_p) %>%
    complete(date = full_seq(date, period = 1)) %>%
    ggplot(aes(x = date, y = users)) +
    geom_line() +
    scale_x_date(name = "", breaks = custom_breaks,
      labels = custom_labels, minor_breaks = custom_minor_breaks) +
    scale_y_continuous(name = "", labels = formatter, limits = c(0, NA)) +
    ggtitle(paste("Bridge users using IP", version_p, sep = "")) +
    labs(caption = copyright_notice)
  ggsave(filename = path_p, width = 8, height = 5, dpi = 150)
}

prepare_userstats_bridge_combined <- function(start_p = NULL, end_p = NULL,
    country_p = NULL) {
  if (!is.null(country_p) && country_p == "all") {
    prepare_userstats_bridge_country(start_p, end_p, country_p)
  } else {
    read_csv(file = paste(stats_dir, "userstats-combined.csv", sep = ""),
        col_types = cols(
          date = col_date(format = ""),
          node = col_skip(),
          country = col_character(),
          transport = col_character(),
          version = col_skip(),
          frac = col_double(),
          low = col_double(),
          high = col_double()),
        na = character()) %>%
      filter(if (!is.null(start_p)) date >= as.Date(start_p) else TRUE) %>%
      filter(if (!is.null(end_p)) date <= as.Date(end_p) else TRUE) %>%
      filter(if (!is.null(country_p)) country == country_p else TRUE) %>%
      select(date, country, transport, low, high, frac) %>%
      arrange(date, country, transport)
  }
}

plot_userstats_bridge_combined <- function(start_p, end_p, country_p, path_p) {
  if (country_p == "all") {
    plot_userstats_bridge_country(start_p, end_p, country_p, path_p)
  } else {
    top <- 3
    u <- prepare_userstats_bridge_combined(start_p, end_p, country_p)
    a <- aggregate(list(mid = (u$high + u$low) / 2),
                   by = list(transport = u$transport), FUN = sum)
    a <- a[order(a$mid, decreasing = TRUE)[1:top], ]
    u <- u[u$transport %in% a$transport, ] %>%
      complete(date = full_seq(date, period = 1), nesting(country, transport))
    title <- paste("Bridge users by transport from ",
                   countryname(country_p), sep = "")
    ggplot(u, aes(x = as.Date(date), ymin = low, ymax = high,
      fill = transport, colour = transport)) +
    geom_ribbon(alpha = 0.5, size = 0.5) +
    scale_x_date(name = "", breaks = custom_breaks,
      labels = custom_labels, minor_breaks = custom_minor_breaks) +
    scale_y_continuous(name = "", limits = c(0, NA), labels = formatter) +
    scale_colour_hue("Top-3 transports") +
    scale_fill_hue("Top-3 transports") +
    ggtitle(title) +
    labs(caption = copyright_notice) +
    theme(legend.position = "top")
    ggsave(filename = path_p, width = 8, height = 5, dpi = 150)
  }
}

prepare_advbwdist_perc <- function(start_p = NULL, end_p = NULL, p_p = NULL) {
  read_csv(file = paste(stats_dir, "advbwdist.csv", sep = ""),
      col_types = cols(
        date = col_date(format = ""),
        isexit = col_logical(),
        relay = col_skip(),
        percentile = col_integer(),
        advbw = col_double())) %>%
    filter(if (!is.null(start_p)) date >= as.Date(start_p) else TRUE) %>%
    filter(if (!is.null(end_p)) date <= as.Date(end_p) else TRUE) %>%
    filter(if (!is.null(p_p)) percentile %in% as.numeric(p_p) else
      percentile != "") %>%
    transmute(date, percentile = as.factor(percentile),
      variable = ifelse(is.na(isexit), "all", "exits"),
      advbw = advbw * 8 / 1e9) %>%
    dcast(date + percentile ~ variable, value.var = "advbw") %>%
    rename(p = percentile)
}

plot_advbwdist_perc <- function(start_p, end_p, p_p, path_p) {
  prepare_advbwdist_perc(start_p, end_p, p_p) %>%
    gather(variable, advbw, -c(date, p)) %>%
    mutate(variable = ifelse(variable == "all", "All relays",
      "Exits only")) %>%
    complete(date = full_seq(date, period = 1), nesting(p, variable)) %>%
    ggplot(aes(x = date, y = advbw, colour = p)) +
    facet_grid(variable ~ .) +
    geom_line() +
    scale_x_date(name = "", breaks = custom_breaks,
      labels = custom_labels, minor_breaks = custom_minor_breaks) +
    scale_y_continuous(name = "", labels = unit_format(unit = "Gbit/s"),
      limits = c(0, NA)) +
    scale_colour_hue(name = "Percentile") +
    ggtitle("Advertised bandwidth distribution") +
    labs(caption = copyright_notice)
  ggsave(filename = path_p, width = 8, height = 5, dpi = 150)
}

prepare_advbwdist_relay <- function(start_p = NULL, end_p = NULL, n_p = NULL) {
  read_csv(file = paste(stats_dir, "advbwdist.csv", sep = ""),
      col_types = cols(
        date = col_date(format = ""),
        isexit = col_logical(),
        relay = col_integer(),
        percentile = col_skip(),
        advbw = col_double())) %>%
    filter(if (!is.null(start_p)) date >= as.Date(start_p) else TRUE) %>%
    filter(if (!is.null(end_p)) date <= as.Date(end_p) else TRUE) %>%
    filter(if (!is.null(n_p)) relay %in% as.numeric(n_p) else
      relay != "") %>%
    transmute(date, relay = as.factor(relay),
      variable = ifelse(is.na(isexit), "all", "exits"),
      advbw = advbw * 8 / 1e9) %>%
    dcast(date + relay ~ variable, value.var = "advbw") %>%
    rename(n = relay)
}

plot_advbwdist_relay <- function(start_p, end_p, n_p, path_p) {
  prepare_advbwdist_relay(start_p, end_p, n_p) %>%
    gather(variable, advbw, -c(date, n)) %>%
    mutate(variable = ifelse(variable == "all", "All relays",
      "Exits only")) %>%
    complete(date = full_seq(date, period = 1), nesting(n, variable)) %>%
    ggplot(aes(x = date, y = advbw, colour = n)) +
    facet_grid(variable ~ .) +
    geom_line() +
    scale_x_date(name = "", breaks = custom_breaks,
      labels = custom_labels, minor_breaks = custom_minor_breaks) +
    scale_y_continuous(name = "", labels = unit_format(unit = "Gbit/s"),
      limits = c(0, NA)) +
    scale_colour_hue(name = "n") +
    ggtitle("Advertised bandwidth of n-th fastest relays") +
    labs(caption = copyright_notice)
  ggsave(filename = path_p, width = 8, height = 5, dpi = 150)
}

prepare_hidserv_dir_onions_seen <- function(start_p = NULL, end_p = NULL) {
  read_csv(file = paste(stats_dir, "hidserv.csv", sep = ""),
      col_types = cols(
        date = col_date(format = ""),
        type = col_factor(levels = NULL),
        wmean = col_skip(),
        wmedian = col_skip(),
        wiqm = col_double(),
        frac = col_double(),
        stats = col_skip())) %>%
    filter(if (!is.null(start_p)) date >= as.Date(start_p) else TRUE) %>%
    filter(if (!is.null(end_p)) date <= as.Date(end_p) else TRUE) %>%
    filter(type == "dir-onions-seen") %>%
    transmute(date, onions = ifelse(frac >= 0.01, wiqm, NA), frac)
}

plot_hidserv_dir_onions_seen <- function(start_p, end_p, path_p) {
  prepare_hidserv_dir_onions_seen(start_p, end_p) %>%
    complete(date = full_seq(date, period = 1)) %>%
    ggplot(aes(x = date, y = onions)) +
    geom_line() +
    scale_x_date(name = "", breaks = custom_breaks,
      labels = custom_labels, minor_breaks = custom_minor_breaks) +
    scale_y_continuous(name = "", limits = c(0, NA), labels = formatter) +
    ggtitle("Unique .onion addresses") +
    labs(caption = copyright_notice)
  ggsave(filename = path_p, width = 8, height = 5, dpi = 150)
}

prepare_hidserv_rend_relayed_cells <- function(start_p = NULL, end_p = NULL) {
  read_csv(file = paste(stats_dir, "hidserv.csv", sep = ""),
      col_types = cols(
        date = col_date(format = ""),
        type = col_factor(levels = NULL),
        wmean = col_skip(),
        wmedian = col_skip(),
        wiqm = col_double(),
        frac = col_double(),
        stats = col_skip())) %>%
    filter(if (!is.null(start_p)) date >= as.Date(start_p) else TRUE) %>%
    filter(if (!is.null(end_p)) date <= as.Date(end_p) else TRUE) %>%
    filter(type == "rend-relayed-cells") %>%
    transmute(date,
      relayed = ifelse(frac >= 0.01, wiqm * 8 * 512 / (86400 * 1e9), NA), frac)
}

plot_hidserv_rend_relayed_cells <- function(start_p, end_p, path_p) {
  prepare_hidserv_rend_relayed_cells(start_p, end_p) %>%
    complete(date = full_seq(date, period = 1)) %>%
    ggplot(aes(x = date, y = relayed)) +
    geom_line() +
    scale_x_date(name = "", breaks = custom_breaks,
      labels = custom_labels, minor_breaks = custom_minor_breaks) +
    scale_y_continuous(name = "", labels = unit_format(unit = "Gbit/s"),
      limits = c(0, NA)) +
    ggtitle("Onion-service traffic") +
    labs(caption = copyright_notice)
  ggsave(filename = path_p, width = 8, height = 5, dpi = 150)
}

prepare_webstats_tb <- function(start_p = NULL, end_p = NULL) {
  read_csv(file = paste(stats_dir, "webstats.csv", sep = ""),
      col_types = cols(
        log_date = col_date(format = ""),
        request_type = col_factor(levels = NULL),
        platform = col_skip(),
        channel = col_skip(),
        locale = col_skip(),
        incremental = col_skip(),
        count = col_double())) %>%
    filter(if (!is.null(start_p)) log_date >= as.Date(start_p) else TRUE) %>%
    filter(if (!is.null(end_p)) log_date <= as.Date(end_p) else TRUE) %>%
    filter(request_type %in% c("tbid", "tbsd", "tbup", "tbur")) %>%
    group_by(log_date, request_type) %>%
    summarize(count = sum(count)) %>%
    dcast(log_date ~ request_type, value.var = "count") %>%
    rename(date = log_date, initial_downloads = tbid,
      signature_downloads = tbsd, update_pings = tbup,
      update_requests = tbur)
}

plot_webstats_tb <- function(start_p, end_p, path_p) {
  prepare_webstats_tb(start_p, end_p) %>%
    gather(request_type, count, -date) %>%
    mutate(request_type = factor(request_type,
      levels = c("initial_downloads", "signature_downloads", "update_pings",
        "update_requests"),
      labels = c("Initial downloads", "Signature downloads", "Update pings",
        "Update requests"))) %>%
    ungroup() %>%
    complete(date = full_seq(date, period = 1), nesting(request_type)) %>%
    ggplot(aes(x = date, y = count)) +
    geom_point() +
    geom_line() +
    facet_grid(request_type ~ ., scales = "free_y") +
    scale_x_date(name = "", breaks = custom_breaks,
      labels = custom_labels, minor_breaks = custom_minor_breaks) +
    scale_y_continuous(name = "", labels = formatter, limits = c(0, NA)) +
    theme(strip.text.y = element_text(angle = 0, hjust = 0, size = rel(1.5)),
          strip.background = element_rect(fill = NA)) +
    ggtitle("Tor Browser downloads and updates") +
    labs(caption = copyright_notice)
  ggsave(filename = path_p, width = 8, height = 5, dpi = 150)
}

prepare_webstats_tb_platform <- function(start_p = NULL, end_p = NULL) {
  read_csv(file = paste(stats_dir, "webstats.csv", sep = ""),
      col_types = cols(
        log_date = col_date(format = ""),
        request_type = col_factor(levels = NULL),
        platform = col_factor(levels = NULL),
        channel = col_skip(),
        locale = col_skip(),
        incremental = col_skip(),
        count = col_double())) %>%
    filter(if (!is.null(start_p)) log_date >= as.Date(start_p) else TRUE) %>%
    filter(if (!is.null(end_p)) log_date <= as.Date(end_p) else TRUE) %>%
    filter(request_type %in% c("tbid", "tbup")) %>%
    group_by(log_date, platform, request_type) %>%
    summarize(count = sum(count)) %>%
    dcast(log_date + platform ~ request_type, value.var = "count") %>%
    rename(date = log_date, initial_downloads = tbid, update_pings = tbup)
}

plot_webstats_tb_platform <- function(start_p, end_p, path_p) {
  prepare_webstats_tb_platform(start_p, end_p) %>%
    gather(request_type, count, -c(date, platform)) %>%
    mutate(request_type = factor(request_type,
      levels = c("initial_downloads", "update_pings"),
      labels = c("Initial downloads", "Update pings"))) %>%
    ungroup() %>%
    complete(date = full_seq(date, period = 1),
      nesting(platform, request_type)) %>%
    ggplot(aes(x = date, y = count, colour = platform)) +
    geom_point() +
    geom_line() +
    scale_x_date(name = "", breaks = custom_breaks,
      labels = custom_labels, minor_breaks = custom_minor_breaks) +
    scale_y_continuous(name = "", labels = formatter, limits = c(0, NA)) +
    scale_colour_hue(name = "Platform",
        breaks = c("w", "m", "l", "o", ""),
        labels = c("Windows", "macOS", "Linux", "Other", "Unknown")) +
    facet_grid(request_type ~ ., scales = "free_y") +
    theme(strip.text.y = element_text(angle = 0, hjust = 0, size = rel(1.5)),
          strip.background = element_rect(fill = NA),
          legend.position = "top") +
    ggtitle("Tor Browser downloads and updates by platform") +
    labs(caption = copyright_notice)
  ggsave(filename = path_p, width = 8, height = 5, dpi = 150)
}

prepare_webstats_tb_locale <- function(start_p = NULL, end_p = NULL) {
  read_csv(file = paste(stats_dir, "webstats.csv", sep = ""),
      col_types = cols(
        log_date = col_date(format = ""),
        request_type = col_factor(levels = NULL),
        platform = col_skip(),
        channel = col_skip(),
        locale = col_factor(levels = NULL),
        incremental = col_skip(),
        count = col_double())) %>%
    filter(if (!is.null(start_p)) log_date >= as.Date(start_p) else TRUE) %>%
    filter(if (!is.null(end_p)) log_date <= as.Date(end_p) else TRUE) %>%
    filter(request_type %in% c("tbid", "tbup")) %>%
    rename(date = log_date) %>%
    group_by(date, locale, request_type) %>%
    summarize(count = sum(count)) %>%
    mutate(request_type = factor(request_type, levels = c("tbid", "tbup"))) %>%
    dcast(date + locale ~ request_type, value.var = "count") %>%
    rename(initial_downloads = tbid, update_pings = tbup)
}

plot_webstats_tb_locale <- function(start_p, end_p, path_p) {
  d <- prepare_webstats_tb_locale(start_p, end_p) %>%
    gather(request_type, count, -c(date, locale)) %>%
    mutate(request_type = factor(request_type,
      levels = c("initial_downloads", "update_pings"),
      labels = c("Initial downloads", "Update pings")))
  e <- d
  e <- aggregate(list(count = e$count), by = list(locale = e$locale), FUN = sum)
  e <- e[order(e$count, decreasing = TRUE), ]
  e <- e[1:5, ]
  d <- aggregate(list(count = d$count), by = list(date = d$date,
    request_type = d$request_type,
    locale = ifelse(d$locale %in% e$locale, d$locale, "(other)")), FUN = sum)
  d %>%
    complete(date = full_seq(date, period = 1),
      nesting(locale, request_type)) %>%
    ggplot(aes(x = date, y = count, colour = locale)) +
    geom_point() +
    geom_line() +
    scale_x_date(name = "", breaks = custom_breaks,
      labels = custom_labels, minor_breaks = custom_minor_breaks) +
    scale_y_continuous(name = "", labels = formatter, limits = c(0, NA)) +
    scale_colour_hue(name = "Locale",
        breaks = c(e$locale, "(other)"),
        labels = c(as.character(e$locale), "Other")) +
    facet_grid(request_type ~ ., scales = "free_y") +
    theme(strip.text.y = element_text(angle = 0, hjust = 0, size = rel(1.5)),
          strip.background = element_rect(fill = NA),
          legend.position = "top") +
    guides(col = guide_legend(nrow = 1)) +
    ggtitle("Tor Browser downloads and updates by locale") +
    labs(caption = copyright_notice)
  ggsave(filename = path_p, width = 8, height = 5, dpi = 150)
}

prepare_webstats_tb_channel <- function(start_p = NULL, end_p = NULL) {
  read_csv(file = paste(stats_dir, "webstats.csv", sep = ""),
      col_types = cols(
        log_date = col_date(format = ""),
        request_type = col_factor(levels = NULL),
        platform = col_skip(),
        channel = col_factor(levels = NULL),
        locale = col_skip(),
        incremental = col_skip(),
        count = col_double())) %>%
    filter(if (!is.null(start_p)) log_date >= as.Date(start_p) else TRUE) %>%
    filter(if (!is.null(end_p)) log_date <= as.Date(end_p) else TRUE) %>%
    filter(request_type %in% c("tbup", "tbur")) %>%
    filter(channel %in% c("a", "r")) %>%
    group_by(log_date, channel, request_type) %>%
    summarize(count = sum(count)) %>%
    dcast(log_date + channel ~ request_type, value.var = "count") %>%
    rename(date = log_date, update_pings = tbup, update_requests = tbur)
}

plot_webstats_tb_channel <- function(start_p, end_p, path_p) {
  prepare_webstats_tb_channel(start_p, end_p) %>%
    gather(request_type, count, -c(date, channel)) %>%
    unite("request_type_channel", request_type, channel) %>%
    mutate(request_type_channel = factor(request_type_channel,
      levels = c("update_pings_r", "update_pings_a",
                 "update_requests_r", "update_requests_a"),
      labels = c("Update pings (stable)", "Update pings (alpha)",
                 "Update requests (stable)", "Update requests (alpha)"))) %>%
    ungroup() %>%
    complete(date = full_seq(date, period = 1),
      nesting(request_type_channel)) %>%
    ggplot(aes(x = date, y = count)) +
    geom_point() +
    geom_line() +
    scale_x_date(name = "", breaks = custom_breaks,
      labels = custom_labels, minor_breaks = custom_minor_breaks) +
    scale_y_continuous(name = "", labels = formatter, limits = c(0, NA)) +
    facet_grid(request_type_channel ~ ., scales = "free_y") +
    theme(strip.text.y = element_text(angle = 0, hjust = 0, size = rel(1.5)),
          strip.background = element_rect(fill = NA)) +
    ggtitle("Tor Browser updates by release channel") +
    labs(caption = copyright_notice)
  ggsave(filename = path_p, width = 8, height = 5, dpi = 150)
}

prepare_webstats_tm <- function(start_p = NULL, end_p = NULL) {
  read_csv(file = paste(stats_dir, "webstats.csv", sep = ""),
      col_types = cols(
        log_date = col_date(format = ""),
        request_type = col_factor(levels = NULL),
        platform = col_skip(),
        channel = col_skip(),
        locale = col_skip(),
        incremental = col_skip(),
        count = col_double())) %>%
    filter(if (!is.null(start_p)) log_date >= as.Date(start_p) else TRUE) %>%
    filter(if (!is.null(end_p)) log_date <= as.Date(end_p) else TRUE) %>%
    filter(request_type %in% c("tmid", "tmup")) %>%
    group_by(log_date, request_type) %>%
    summarize(count = sum(count)) %>%
    mutate(request_type = factor(request_type, levels = c("tmid", "tmup"))) %>%
    dcast(log_date ~ request_type, value.var = "count", drop = FALSE,
      fill = 0) %>%
    rename(date = log_date, initial_downloads = tmid, update_pings = tmup)
}

plot_webstats_tm <- function(start_p, end_p, path_p) {
  prepare_webstats_tm(start_p, end_p) %>%
    gather(request_type, count, -date) %>%
    mutate(request_type = factor(request_type,
      levels = c("initial_downloads", "update_pings"),
      labels = c("Initial downloads", "Update pings"))) %>%
    ungroup() %>%
    complete(date = full_seq(date, period = 1), nesting(request_type)) %>%
    ggplot(aes(x = date, y = count)) +
    geom_point() +
    geom_line() +
    facet_grid(request_type ~ ., scales = "free_y") +
    scale_x_date(name = "", breaks = custom_breaks,
      labels = custom_labels, minor_breaks = custom_minor_breaks) +
    scale_y_continuous(name = "", labels = formatter, limits = c(0, NA)) +
    theme(strip.text.y = element_text(angle = 0, hjust = 0, size = rel(1.5)),
          strip.background = element_rect(fill = NA)) +
    ggtitle("Tor Messenger downloads and updates") +
    labs(caption = copyright_notice)
  ggsave(filename = path_p, width = 8, height = 5, dpi = 150)
}

prepare_relays_ipv6 <- function(start_p = NULL, end_p = NULL) {
  read_csv(file = paste(stats_dir, "ipv6servers.csv", sep = ""),
      col_types = cols(
        valid_after_date = col_date(format = ""),
        server = col_factor(levels = NULL),
        guard_relay = col_skip(),
        exit_relay = col_skip(),
        announced_ipv6 = col_logical(),
        exiting_ipv6_relay = col_logical(),
        reachable_ipv6_relay = col_logical(),
        server_count_sum_avg = col_double(),
        advertised_bandwidth_bytes_sum_avg = col_skip())) %>%
    filter(if (!is.null(start_p))
        valid_after_date >= as.Date(start_p) else TRUE) %>%
    filter(if (!is.null(end_p))
        valid_after_date <= as.Date(end_p) else TRUE) %>%
    filter(server == "relay") %>%
    group_by(valid_after_date) %>%
    summarize(total = sum(server_count_sum_avg),
      announced = sum(server_count_sum_avg[announced_ipv6]),
      reachable = sum(server_count_sum_avg[reachable_ipv6_relay]),
      exiting = sum(server_count_sum_avg[exiting_ipv6_relay])) %>%
    rename(date = valid_after_date)
}

plot_relays_ipv6 <- function(start_p, end_p, path_p) {
  prepare_relays_ipv6(start_p, end_p) %>%
    complete(date = full_seq(date, period = 1)) %>%
    gather(category, count, -date) %>%
    ggplot(aes(x = date, y = count, colour = category)) +
    geom_line() +
    scale_x_date(name = "", breaks = custom_breaks,
      labels = custom_labels, minor_breaks = custom_minor_breaks) +
    scale_y_continuous(name = "", labels = formatter, limits = c(0, NA)) +
    scale_colour_hue(name = "", h.start = 90,
      breaks = c("total", "announced", "reachable", "exiting"),
      labels = c("Total (IPv4) OR", "IPv6 announced OR", "IPv6 reachable OR",
        "IPv6 exiting")) +
    ggtitle("Relays by IP version") +
    labs(caption = copyright_notice) +
    theme(legend.position = "top")
  ggsave(filename = path_p, width = 8, height = 5, dpi = 150)
}

prepare_bridges_ipv6 <- function(start_p = NULL, end_p = NULL) {
  read_csv(file = paste(stats_dir, "ipv6servers.csv", sep = ""),
      col_types = cols(
        valid_after_date = col_date(format = ""),
        server = col_factor(levels = NULL),
        guard_relay = col_skip(),
        exit_relay = col_skip(),
        announced_ipv6 = col_logical(),
        exiting_ipv6_relay = col_skip(),
        reachable_ipv6_relay = col_skip(),
        server_count_sum_avg = col_double(),
        advertised_bandwidth_bytes_sum_avg = col_skip())) %>%
    filter(if (!is.null(start_p))
        valid_after_date >= as.Date(start_p) else TRUE) %>%
    filter(if (!is.null(end_p))
        valid_after_date <= as.Date(end_p) else TRUE) %>%
    filter(server == "bridge") %>%
    group_by(valid_after_date) %>%
    summarize(total = sum(server_count_sum_avg),
      announced = sum(server_count_sum_avg[announced_ipv6])) %>%
    rename(date = valid_after_date)
}

plot_bridges_ipv6 <- function(start_p, end_p, path_p) {
  prepare_bridges_ipv6(start_p, end_p) %>%
    complete(date = full_seq(date, period = 1)) %>%
    gather(category, count, -date) %>%
    ggplot(aes(x = date, y = count, colour = category)) +
    geom_line() +
    scale_x_date(name = "", breaks = custom_breaks,
      labels = custom_labels, minor_breaks = custom_minor_breaks) +
    scale_y_continuous(name = "", labels = formatter, limits = c(0, NA)) +
    scale_colour_hue(name = "", h.start = 90,
      breaks = c("total", "announced"),
      labels = c("Total (IPv4) OR", "IPv6 announced OR")) +
    ggtitle("Bridges by IP version") +
    labs(caption = copyright_notice) +
    theme(legend.position = "top")
  ggsave(filename = path_p, width = 8, height = 5, dpi = 150)
}

prepare_advbw_ipv6 <- function(start_p = NULL, end_p = NULL) {
  read_csv(file = paste(stats_dir, "ipv6servers.csv", sep = ""),
      col_types = cols(
        valid_after_date = col_date(format = ""),
        server = col_factor(levels = NULL),
        guard_relay = col_logical(),
        exit_relay = col_logical(),
        announced_ipv6 = col_logical(),
        exiting_ipv6_relay = col_logical(),
        reachable_ipv6_relay = col_logical(),
        server_count_sum_avg = col_skip(),
        advertised_bandwidth_bytes_sum_avg = col_double())) %>%
    filter(if (!is.null(start_p))
        valid_after_date >= as.Date(start_p) else TRUE) %>%
    filter(if (!is.null(end_p))
        valid_after_date <= as.Date(end_p) else TRUE) %>%
    filter(server == "relay") %>%
    mutate(advertised_bandwidth_bytes_sum_avg =
        advertised_bandwidth_bytes_sum_avg * 8 / 1e9) %>%
    group_by(valid_after_date) %>%
    summarize(total = sum(advertised_bandwidth_bytes_sum_avg),
      total_guard = sum(advertised_bandwidth_bytes_sum_avg[guard_relay]),
      total_exit = sum(advertised_bandwidth_bytes_sum_avg[exit_relay]),
      reachable_guard = sum(advertised_bandwidth_bytes_sum_avg[
        reachable_ipv6_relay & guard_relay]),
      reachable_exit = sum(advertised_bandwidth_bytes_sum_avg[
        reachable_ipv6_relay & exit_relay]),
      exiting = sum(advertised_bandwidth_bytes_sum_avg[
        exiting_ipv6_relay])) %>%
    rename(date = valid_after_date)
}

plot_advbw_ipv6 <- function(start_p, end_p, path_p) {
  prepare_advbw_ipv6(start_p, end_p) %>%
    complete(date = full_seq(date, period = 1)) %>%
    gather(category, advbw, -date) %>%
    ggplot(aes(x = date, y = advbw, colour = category)) +
    geom_line() +
    scale_x_date(name = "", breaks = custom_breaks,
      labels = custom_labels, minor_breaks = custom_minor_breaks) +
    scale_y_continuous(name = "", labels = unit_format(unit = "Gbit/s"),
      limits = c(0, NA)) +
    scale_colour_hue(name = "", h.start = 90,
      breaks = c("total", "total_guard", "total_exit", "reachable_guard",
        "reachable_exit", "exiting"),
      labels = c("Total (IPv4) OR", "Guard total (IPv4)", "Exit total (IPv4)",
        "Reachable guard IPv6 OR", "Reachable exit IPv6 OR", "IPv6 exiting")) +
    ggtitle("Advertised bandwidth by IP version") +
    labs(caption = copyright_notice) +
    theme(legend.position = "top") +
    guides(colour = guide_legend(nrow = 2, byrow = TRUE))
  ggsave(filename = path_p, width = 8, height = 5, dpi = 150)
}

prepare_totalcw <- function(start_p = NULL, end_p = NULL) {
  read_csv(file = paste(stats_dir, "totalcw.csv", sep = ""),
      col_types = cols(
        valid_after_date = col_date(format = ""),
        nickname = col_character(),
        have_guard_flag = col_logical(),
        have_exit_flag = col_logical(),
        measured_sum_avg = col_double())) %>%
    filter(if (!is.null(start_p))
        valid_after_date >= as.Date(start_p) else TRUE) %>%
    filter(if (!is.null(end_p))
        valid_after_date <= as.Date(end_p) else TRUE) %>%
    group_by(valid_after_date, nickname) %>%
    summarize(measured_sum_avg = sum(measured_sum_avg)) %>%
    rename(date = valid_after_date, totalcw = measured_sum_avg) %>%
    arrange(date, nickname)
}

plot_totalcw <- function(start_p, end_p, path_p) {
  prepare_totalcw(start_p, end_p) %>%
    mutate(nickname = ifelse(is.na(nickname), "consensus", nickname)) %>%
    mutate(nickname = factor(nickname,
      levels = c("consensus", unique(nickname[nickname != "consensus"])))) %>%
    ungroup() %>%
    complete(date = full_seq(date, period = 1), nesting(nickname)) %>%
    ggplot(aes(x = date, y = totalcw, colour = nickname)) +
    geom_line(na.rm = TRUE) +
    scale_x_date(name = "", breaks = custom_breaks,
      labels = custom_labels, minor_breaks = custom_minor_breaks) +
    scale_y_continuous(name = "", labels = formatter, limits = c(0, NA)) +
    scale_colour_hue(name = "") +
    ggtitle("Total consensus weights across bandwidth authorities") +
    labs(caption = copyright_notice)
  ggsave(filename = path_p, width = 8, height = 5, dpi = 150)
}

countrynames <- function(countries) {
  sapply(countries, countryname)
}

write_userstats <- function(start, end, node, path) {
  end <- min(end, as.character(Sys.Date()))
  c <- read.csv(paste("/srv/metrics.torproject.org/metrics/shared/stats/",
                "clients.csv", sep = ""), stringsAsFactors = FALSE)
  c <- c[c$date >= start & c$date <= end & c$country != '' &
         c$transport == '' & c$version == '' & c$node == node, ]
  u <- data.frame(country = c$country, users = c$clients,
                  stringsAsFactors = FALSE)
  u <- u[!is.na(u$users), ]
  u <- aggregate(list(users = u$users), by = list(country = u$country),
                 mean)
  total <- sum(u$users)
  u <- u[!(u$country %in% c("zy", "??", "a1", "a2", "o1", "ap", "eu")), ]
  u <- u[order(u$users, decreasing = TRUE), ]
  u <- u[1:10, ]
  u <- data.frame(
    cc = as.character(u$country),
    country = sub('the ', '', countrynames(as.character(u$country))),
    abs = round(u$users),
    rel = sprintf("%.2f", round(100 * u$users / total, 2)))
  write.csv(u, path, quote = FALSE, row.names = FALSE)
}

write_userstats_relay <- function(start, end, path) {
  write_userstats(start, end, 'relay', path)
}

write_userstats_bridge <- function(start, end, path) {
  write_userstats(start, end, 'bridge', path)
}

write_userstats_censorship_events <- function(start, end, path) {
  end <- min(end, as.character(Sys.Date()))
  c <- read.csv(paste("/srv/metrics.torproject.org/metrics/shared/stats/",
                "clients.csv", sep = ""), stringsAsFactors = FALSE)
  c <- c[c$date >= start & c$date <= end & c$country != '' &
         c$transport == '' & c$version == '' & c$node == 'relay', ]
  r <- data.frame(date = c$date, country = c$country,
                  upturn = ifelse(!is.na(c$upper) &
                                  c$clients > c$upper, 1, 0),
                  downturn = ifelse(!is.na(c$lower) &
                                    c$clients < c$lower, 1, 0))
  r <- aggregate(r[, c("upturn", "downturn")],
    by = list(country = r$country), sum)
  r <- r[(r$country %in% names(countrylist)), ]
  r <- r[order(r$downturn, r$upturn, decreasing = TRUE), ]
  r <- r[1:10, ]
  r <- data.frame(cc = r$country,
    country = sub('the ', '', countrynames(as.character(r$country))),
    downturns = r$downturn,
    upturns = r$upturn)
  write.csv(r, path, quote = FALSE, row.names = FALSE)
}

prepare_bridgedb_transport <- function(start_p = NULL, end_p = NULL) {
  read_csv(file = paste(stats_dir, "bridgedb-stats.csv", sep = ""),
      col_types = cols(
        date = col_date(format = ""),
        distributor = col_skip(),
        transport = col_character(),
        requests = col_double())) %>%
    filter(if (!is.null(start_p)) date >= as.Date(start_p) else TRUE) %>%
    filter(if (!is.null(end_p)) date <= as.Date(end_p) else TRUE) %>%
    group_by(date, transport) %>%
    summarize(requests = sum(requests)) %>%
    arrange(date, transport)
}

plot_bridgedb_transport <- function(start_p, end_p, path_p) {
  prepare_bridgedb_transport(start_p, end_p) %>%
    complete(date = full_seq(date, period = 1), nesting(transport)) %>%
    ggplot(aes(x = date, y = requests, colour = transport)) +
    geom_line(na.rm = TRUE) +
    scale_x_date(name = "", breaks = custom_breaks,
      labels = custom_labels, minor_breaks = custom_minor_breaks) +
    scale_y_continuous(name = "", labels = formatter, limits = c(0, NA)) +
    scale_colour_hue(name = "") +
    ggtitle("BridgeDB requests by requested transport") +
    labs(caption = copyright_notice)
  ggsave(filename = path_p, width = 8, height = 5, dpi = 150)
}

prepare_bridgedb_distributor <- function(start_p = NULL, end_p = NULL) {
  read_csv(file = paste(stats_dir, "bridgedb-stats.csv", sep = ""),
      col_types = cols(
        date = col_date(format = ""),
        distributor = col_character(),
        transport = col_skip(),
        requests = col_double())) %>%
    filter(if (!is.null(start_p)) date >= as.Date(start_p) else TRUE) %>%
    filter(if (!is.null(end_p)) date <= as.Date(end_p) else TRUE) %>%
    group_by(date, distributor) %>%
    summarize(requests = sum(requests)) %>%
    arrange(date, distributor)
}

plot_bridgedb_distributor <- function(start_p, end_p, path_p) {
  prepare_bridgedb_distributor(start_p, end_p) %>%
    complete(date = full_seq(date, period = 1), nesting(distributor)) %>%
    ggplot(aes(x = date, y = requests, colour = distributor)) +
    geom_line(na.rm = TRUE) +
    scale_x_date(name = "", breaks = custom_breaks,
      labels = custom_labels, minor_breaks = custom_minor_breaks) +
    scale_y_continuous(name = "", labels = formatter, limits = c(0, NA)) +
    scale_colour_hue(name = "") +
    ggtitle("BridgeDB requests by distributor") +
    labs(caption = copyright_notice)
  ggsave(filename = path_p, width = 8, height = 5, dpi = 150)
}

