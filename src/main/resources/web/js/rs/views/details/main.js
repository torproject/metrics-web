// This is the boilerplate file
// it should be used as a base for every module
define([
  'jquery',
  'underscore',
  'backbone',
  'models/relay',
  'models/graph',
  'text!templates/details/router.html',
  'text!templates/details/bridge.html',
  'text!templates/details/error.html',
  'd3js',
  'helpers'
], function($, _, Backbone, relayModel, graphModel,
        routerDetailsTemplate, bridgeDetailsTemplate, errorDetailsTemplate){
    var mainDetailsView = Backbone.View.extend({
        el: "#content",
        initialize: function() {
           this.model = new relayModel;
           this.graph = new graphModel;
        },
        plot: function(g, data, labels, legendPos, colors, tickFormat,
                       tooltipFormat) {

            /* do not plot graph if there is no data */
            if (data[0].length==0) return;

            /* Initialize graph. */
            var margin = {top: 30, right: 10, bottom: 20, left: 60},
                width = 550 - margin.left - margin.right,
                height = 342 - margin.top - margin.bottom;
            var svg = d3.select("#" + g).append("svg:svg")
                .attr("version", 1.1)
                .attr("xmlns", "http://www.w3.org/2000/svg")
                .attr("viewBox", "0 0 550 342")
                .append("g")
                .attr("transform",
                    "translate(" + margin.left + "," + margin.top + ")");

            /* Define scales to convert domain values to pixels. */
            var xExtents = d3.extent(d3.merge(data), function(d) {
                return d[0]; });
            var yExtents = d3.extent(d3.merge(data), function(d) {
                return d[1]; });
            var xScale = d3.time.scale()
                .domain(xExtents)
                .range([0, width]);
            var yScale = d3.scale.linear()
                .domain([0, yExtents[1]])
                .range([height, 0]);

            /* Add the x axis. */
            var xAxis = d3.svg.axis()
                .scale(xScale)
                .ticks(4)
                .tickFormat(d3.time.format("%d %b %Y"))
                .orient("bottom");
            var xAxisContainer = svg.append("g")
                .attr("class", "x axis")
                .attr("transform", "translate(0, " + height + ")")
                .call(xAxis);
            xAxisContainer.selectAll("line")
                .style("stroke", "lightgrey");
            xAxisContainer.selectAll("path")
                .style("display", "none");
            xAxisContainer.selectAll("minor")
                .style("stroke-opacity", .5);
            xAxisContainer.selectAll("text")
                .style("font-size", "12px");

            /* Add the y axis. */
            var yAxis = d3.svg.axis()
                .scale(yScale)
                .orient("left")
                .ticks(4)
                .tickSize(-width - 4)
                .tickFormat(d3.format(tickFormat));
            var yAxisContainer = svg.append("g")
                .attr("class", "y axis")
                .call(yAxis);
            yAxisContainer.selectAll("line")
                .style("stroke", "lightgrey");
            yAxisContainer.selectAll("path")
                .style("display", "none");
            yAxisContainer.selectAll("minor")
                .style("stroke-opacity", .5);
            yAxisContainer.selectAll("text")
                .style("font-size", "12px");

            /* Add one group per data line. */
            var lineContainers = svg.selectAll("g.line")
                .data(data)
                .enter().append("svg:g")
                .attr("class", "line")
                .style("stroke-width", 2)
                .style("stroke", function(d) {
                    return colors[data.indexOf(d)]; });

            /* Add path between all line values. */
            var line = d3.svg.line()
                .defined(function(d) { return d[1] != null; })
                .x(function (d) { return xScale(d[0]); })
                .y(function (d) { return yScale(d[1]); });
            lineContainers.append("svg:path")
                .attr("d", line)
                .style("fill", "none");


            /* Add dots for all line values, and add tooltips. */
            lineContainers.selectAll("circle")
                .data(function(d) { return d; })
                .enter()
                .append("svg:circle")
                .attr("class", "dot")
                .attr("cx", function(d) { return xScale(d[0]); })
                .attr("cy", function(d) { return yScale(d[1]); })
                .attr("r", 3)
                .style("fill", "white");

            $("#" + g + ">svg circle").tooltip({
              title: function() {
                var d = this.__data__;
                var datetimeFormat = function(seconds) {
                  var date = new Date(seconds);
                  return d3.time.format("%Y-%m-%d %H:%M")(date);
                }
                var tooltipFormatter = d3.format(tooltipFormat);
                return datetimeFormat(d[0]) + ": " + tooltipFormatter(d[1]);
              },
              container: 'body'
            });

            /* Add a legend. */
            var legend = svg.append("g")
                .attr("class", "legend");
            legend.selectAll("g").data(data)
                .enter().append("g")
                .each(function(d, i) {
                    var g = d3.select(this);
                    g.append("svg:circle")
                        .attr("cx", legendPos[i][0] - margin.left + 5)
                        .attr("cy", legendPos[i][1] - margin.top + 10)
                        .attr("r", 3)
                        .style("stroke-width", 2)
                        .style("stroke", colors[i])
                        .style("fill", "white");
                    g.append("svg:text")
                        .attr("x", legendPos[i][0] - margin.left + 15)
                        .attr("y", legendPos[i][1] - margin.top + 14)
                        .style("fill", colors[i])
                        .style("font-size", "12px")
                        .text(labels[i]);
                });

            /* Remove placeholder image. */
            d3.select("#" + g).selectAll("img").remove();

            /* Encode SVG image for download link. */
            html = d3.select("#" + g)
                .node()
                .innerHTML;
            d3.select("#save_" + g)
                .attr("href", "data:data/xml;base64," + btoa(html));
        },
        render: function() {
            var data = {relay: this.model};
            var compiledTemplate;
            if (!this.model)
                compiledTemplate = _.template(errorDetailsTemplate);
            else if (this.model.get('is_bridge'))
                compiledTemplate = _.template(bridgeDetailsTemplate);
            else
                compiledTemplate = _.template(routerDetailsTemplate);
            document.title = "Relay Search: " + this.model.get('nickname');
            this.$el.html(compiledTemplate(data));

            canSvg = !!(document.createElementNS && document.createElementNS('http://www.w3.org/2000/svg','svg').createSVGRect);
            if (canSvg) {
              var graph = this.graph;
              var plot = this.plot;
              this.graph.lookup_bw(this.model.fingerprint, {
                  success: function() {
                      graph.parse_bw_data(graph.data);
                      graphs = ['bw_month',
                              'bw_months', 'bw_year', 'bw_years'];
                      _.each(graphs, function(g) {
                          var data = [graph.get(g).write, graph.get(g).read];
                          var labels = ["written bytes per second", "read bytes per second"];
                          var legendPos = [[140, 0], [310, 0]];
                          var colors = ["#edc240", "#afd8f8"];
                          plot(g, data, labels, legendPos, colors, "s", ".4s");
                      });
                  }
              });

              if (!this.model.get('is_bridge')) {
                  this.graph.lookup_weights(this.model.fingerprint, {
                      success: function() {
                          graph.parse_weights_data(graph.data);
                          graphs = ['weights_month',
                                  'weights_months', 'weights_year', 'weights_years'];
                          _.each(graphs, function(g) {
                              var data = [graph.get(g).cw, graph.get(g).middle,
                                          graph.get(g).guard, graph.get(g).exit];
                              var labels = ["consensus weight fraction",
                                            "middle probability",
                                            "guard probability",
                                            "exit probability"];
                              var legendPos = [[28, 0], [309, 0], [194, 0], [429, 0]];
                              var colors = ["#afd8f8", "#edc240",
                                            "#cb4b4b", "#4da74d"];
                              plot(g, data, labels, legendPos, colors, ".4%", ".6%");
                          });
                      }
                  });
              } else {
                  this.graph.lookup_clients(this.model.fingerprint, {
                      success: function() {
                          graph.parse_clients_data(graph.data);
                          graphs = ['clients_month',
                                  'clients_months', 'clients_year', 'clients_years'];
                          _.each(graphs, function(g) {
                              var data = [graph.get(g).average];
                              var labels = ["average number of connected clients"];
                              var legendPos = [[3, 0]];
                              var colors = ["#edc240"];
                              plot(g, data, labels, legendPos, colors, "g", ".2g");
                          });
                      }
                  });
              }
            } else {
              $(".history-tabs").hide();
              $("#history-tab-content").html("Graphs cannot be shown as your browser does not support Scalable Vector Graphics (SVG). This is the case when Tor Browser is in High Security mode.");
            }

            $(".tip").tooltip({
              placement: 'right',
              html: true
            });
        },
        error: function() {
            var compiledTemplate = _.template(errorDetailsTemplate);
            this.$el.html(compiledTemplate({'relay': null}));
        }
    });
    return new mainDetailsView;
});

