Contributor's guide to the Metrics website

Dear contributor to the Metrics website.  This guide shall help you
understand the design decisions behind building the Metrics website and
give you starting points where you should look to make it bigger and
better.

First of all, let's talk briefly about the scope of the Metrics website,
which we'll be calling Metrics in the following.

 - What Metrics is: Metrics is supposed to provide easy access to Tor
   network data.  The typical Metrics user is neither a researcher nor a
   developer and is just looking for an easy way to learn more about this
   Tor network they have been hearing about.  Metrics is giving them data
   in visual or tabular form, together with explanations that are easy to
   understand with as little technical language as possible.

 - What Metrics is not: The typical Tor researcher or Tor developer would
   probably want to dive deeper into the data to learn even more about the
   Tor network.  But in contrast to the average Metrics user they could
   just fetch the original data from CollecTor and run their own analysis.
   Metrics is not trying to be the solution for everyone.  If we have to
   choose, we're aiming for simplicity instead of comprehensiveness.

Now let's take a quick tour of the components that Metrics is made of.

 - Data-processing modules: The bulk of Metrics code is running in the
   background, invisible to Metrics users.  It's the code that takes
   CollecTor data as input and that produces .csv files that are the basis
   for graphs and tables on Metrics.  There's usually one such module per
   generated .csv file that focuses on a different aspect of Tor network
   data.  All these modules are periodically executed by the system's cron
   daemon, independent of user requests to the website part of Metrics.
   See the modules/ subdirectory for the existing data-processing modules.
   Note that modules don't have to be written in Java even though that's
   currently the case for all of them.  The only requirement is that
   there's a shell script to run the module using packages available in
   Debian stable.  The remaining components of Metrics are all related to
   its website part.

 - Start page: The website part of Metrics is organized into one page per
   metric, which can be a graph, table, data file, or external link, and
   the start page to browse available metrics.  Each metric has attributes
   like a descriptive name, one or more tags (relays, bridges, etc.), a
   type (graph, table, etc.), and a level (basic or advanced).  All
   metrics are defined in `website/etc/metrics.json` and displayed in the
   table on the start page.

 - Graph pages: The bulk of graph pages consist of graphing methods in
   `website/rserve/graphs.R` that are written in R and using the ggplot2
   graphing library.  These methods read one or more of the .csv files
   produced by data-processing modules and produce a graph image as
   output.  Graphs have a few additional attributes in
   `website/etc/metrics.json` like a description and parameters to
   customize the graph.  As of writing this guide, there's one exception
   with the bubble graph which is implemented using JavaScript library
   D3.js and which might soon be generated on the server using Node.js.

 - Table pages: Metrics also provides a few aspects of Tor network data in
   tabular form with customization options.  Like graphs, the data in
   these tables is provided using R by reading the previously generated
   .csv files.  All relevant R code for generating table data is located
   in `website/rserve/tables.R`.  Again, there are additional attributes
   in `website/etc/metrics.json` that define what parameters are available
   to customize table contents and how to format results.

 - Data pages: While most Metrics user are not expected to run their own
   analyses based on raw Tor network data, some of them might want to look
   deeper into the data they saw in a graph or table.  Metrics provides
   all pre-aggregated output from its data-processing modules as
   downloadable .csv files and also documents these file formats in
   sufficient detail for Metrics users to use them.

 - Link pages: Metrics is not the only game in town, and it's great that
   other developers take the publicly available Tor network data and
   visualize it in a different way.  Metrics acknowledges these efforts by
   adding link pages with thumbnails to make it easy for Metrics users to
   find those external visualizations.

 - About page: Most Metrics users have a basic understanding of how Tor
   works, most likely from reading the main Tor website.  But Metrics
   should give its users enough explanations to understand where all the
   Tor network data comes from and how that data is used to learn
   interesting facts about the Tor network.  That's where the About page
   comes into play.  The About page consists of a list of frequently used
   terms and a second list of frequently asked questions.  There could be
   more documentation, but more text doesn't necessarily mean that users
   will read more.

