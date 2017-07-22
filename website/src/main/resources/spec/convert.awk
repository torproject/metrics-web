#!/usr/bin/awk -f

# Skip everything before <body> including generated header, script, and style.
BEGIN {
  insidebody = 0;
}

# Skip any lines after </body>, and include our footer.
/<\/body>/ {
  insidebody = 2
  printf("<jsp:include page=\"bottom.jsp\"/>\n")
}

# Prepend <header> and <section> with <div class="container">.
/<(header|section).*>/ {
  printf("<div class=\"container\">\n")
}

# Copy over all lines between <body> and </body> (exclusive).
{
  if (insidebody == 1) {
    print
  }
}

# Append </div> to </header> and </section>.
/<\/(header|section)>/ {
  printf("</div> <!-- container -->\n")
}

# Start copying at <body>, but first include our header.
/<body>/ {
  insidebody = 1;
  printf("<jsp:include page=\"top.jsp\">\n")
  printf("<jsp:param name=\"pageTitle\" value=\"Sources &ndash; Tor Metrics\"/>\n")
  printf("<jsp:param name=\"navActive\" value=\"Sources\"/>\n")
  printf("</jsp:include>\n")
  printf("<div class=\"container\">\n")
  printf("<ul class=\"breadcrumb\">\n")
  printf("<li><a href=\"/\">Home</a></li>\n")
  printf("<li><a href=\"sources.html\">Sources</a></li>\n")
  printf("<li class=\"active\">${breadcrumb}</li>\n")
  printf("</ul>\n")
  printf("</div>\n")
}

