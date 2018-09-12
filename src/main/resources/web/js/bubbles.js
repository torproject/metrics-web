function get_prefix(relay) { return /^[0-9]+\.[0-9]+\./.exec(relay.or_addresses[0]); }

var graphs = {
     'default': {
         extra_fields: [],
         group: undefined,
         group_id_func: function(relay) { return undefined; },
         group_name_func: function(relay) { return 'Relays'; },
  }, 'as': {
         extra_fields: ['as', 'as_name'],
         group: "autonomous systems",
         group_id_func: function(relay) { return relay.as; },
         group_name_func: function(relay) { return relay.as_name; },
  }, 'contact': {
         extra_fields: ['contact'],
         group: "contact infos",
         group_id_func: function(relay) { return relay.contact; },
         group_name_func: function(relay) { return relay.contact; },
  }, 'country': {
         extra_fields: ['country', 'country_name'],
         group: "countries",
         group_id_func: function(relay) { return relay.country; },
         group_name_func: function(relay) { return relay.country_name; },
  }, 'network-family': {
         extra_fields: ['or_addresses'],
         group: "network families (/16)",
         group_id_func: function(relay) { return get_prefix(relay); },
         group_name_func: function(relay) { return get_prefix(relay) + "0.0/16"; },
  },
};

function make_bubble_graph(graph_name) {
  var onionoo_url = "https://onionoo.torproject.org/details?type=relay&running=true&fields=consensus_weight,running,nickname,exit_probability,advertised_bandwidth";
  var diameter = 800;
  var legendWidth = 270;
  var legendIconSize = 50;
  var legendItems = 3;
  var legendIconMargin = 15;
  var legendHeight = legendItems * (legendIconMargin * 2 + legendIconSize) - legendIconMargin;

  var cutOff = 100 / 8.0 * 1000.0 * 1000.0; // 100 Mbit/s

  var format = d3.format(",d");
  var color = d3.scale.category20c();

  var old_graph = document.getElementById("bubble-graph");
  if (old_graph) {
     old_graph.parentNode.removeChild(old_graph);
  }

  var bubble = d3.layout.pack()
    .sort(null)
    .size([diameter, diameter])
    .padding(1.5);

  var svg = d3.select("#bubble-graph-placeholder").append("svg")
    .attr("id", "bubble-graph")
    .attr("width", diameter + legendWidth)
    .attr("height", diameter)
    .attr("class", "bubble");

  var defs = svg.append("defs")

  defs.append("filter")
      .attr("id", "middle-filter")
      .append("feColorMatrix")
        .attr("type", "hueRotate")
        .attr("in", "SourceGraphic")
        .attr("values", "90");

  var node_circle = defs.append("svg").attr("id", "node-circle").attr("viewBox", "0 0 120.50655 184.78298");

  node_circle.append("path")
      .attr("style", "fill:#7d4698;fill-opacity:1;stroke:none")
      .attr("d", "m 119.19492,135.63045 a 56.525425,56.525425 0 1 1 -113.0508541,0 56.525425,56.525425 0 1 1 113.0508541,0 z")
      .attr("transform", "matrix(1.048913,0,0,0.95108692,-5.4815686,2.0260454)");

  var node_onion = defs.append("svg").attr("id", "node-onion").attr("viewBox", "0 0 120.50655 184.78298")
      .append("g")
        .attr("transform", "translate(-195.35846,-64.183782)");

  node_onion.append("path")
        .attr("style", "fill:#abcd03;fill-rule:evenodd;stroke:none")
        .attr("d", "m 264.513,77.977773 -4.917,19.529001 c 6.965,-13.793001 18.027,-24.172001 30.729,-33.323001 -9.287,10.789 -17.754,21.579001 -22.944,32.368001 8.741,-12.292001 20.486,-19.120001 33.733,-23.627001 -17.618,15.706001 -31.60228,32.559277 -42.25528,49.494277 l -8.467,-3.687 c 1.501,-13.521 6.60928,-27.369276 14.12128,-40.754277 z");
  node_onion.append("path")
        .attr("style", "fill:#fffcdb;fill-rule:evenodd;stroke:none")
        .attr("d", "m 241.90113,115.14152 16.116,6.68594 c 0,4.098 -0.33313,16.59703 2.22938,20.28403 26.80289,34.5191 22.29349,103.71329 -5.42951,105.48829 -42.21656,0 -58.317,-28.679 -58.317,-55.03801 0,-24.037 28.816,-40.016 46.025,-54.219 4.37,-3.824 3.61113,-12.27525 -0.62387,-23.20125 z")
  node_onion.append("path")
        .attr("style", "fill:#7d4698;fill-rule:evenodd;stroke:none")
        .attr("d", "m 258.02197,121.58695 5.80803,2.96282 c -0.546,3.823 0.273,12.292 4.096,14.476 16.936,10.516 32.914,21.988 39.197,33.46 22.398,40.42601 -15.706,77.84601 -48.62,74.29501 17.891,-13.248 23.081,-40.42501 16.389,-70.06201 -2.731,-11.609 -6.966,-22.125 -14.478,-34.007 -3.25421,-5.83246 -2.11803,-13.06582 -2.39203,-21.12482 z");
  node_onion.append("path")
        .attr("style", "fill:#000000;fill-opacity:1;stroke:none")
        .attr("d", "m 255.226,120.58877 12.018,1.639 c -3.551,11.745 6.966,19.939 10.38,21.852 7.64801,4.234 15.02301,8.604 20.89601,13.93 11.063,10.106 17.345,24.31 17.345,39.333 0,14.886 -6.829,29.226 -18.301,38.786 -10.789,9.014 -25.67501,12.838 -40.15201,12.838 -9.014,0 -17.072,-0.409 -25.812,-3.278 -19.939,-6.692 -34.826,-23.763 -36.055,-44.25 -1.093,-15.979 2.458,-28.134 14.887,-40.835 6.418,-6.692 19.393,-14.34 28.271,-20.486 4.371,-3.005 9.014,-11.473 0.136,-27.451 l 1.776,-1.366 13.15659,8.81203 -11.10759,-4.57803 c 0.956,1.366 3.551,7.512 4.098,9.287 1.229,5.053 0.683,9.971 -0.41,12.155 -5.599,10.107 -15.159,12.838 -22.124,18.574 -12.292,10.106 -25.676,18.164 -24.174,45.888 0.683,13.657 11.336,30.319 27.314,38.104 9.014,4.371 19.394,6.146 29.91,6.692 9.423,0.41 27.45101,-5.19 37.28401,-13.384 10.516,-8.74 16.389,-21.988 16.389,-35.508 0,-13.658 -5.463,-26.632 -15.706,-35.783 -5.873,-5.326 -15.56901,-11.745 -21.57801,-15.16 -6.009,-3.414 -13.521,-12.974 -11.063,-22.124 z");
  node_onion.append("path")
        .attr("style", "fill:#000000;fill-opacity:1;stroke:none")
        .attr("d", "m 251.539,140.80177 c -1.229,6.283 -2.595,17.618 -8.058,21.852 -2.322,1.638 -4.644,3.278 -7.102,4.916 -9.833,6.693 -19.667,12.974 -24.173,29.09 -0.956,3.415 -0.136,7.102 0.684,10.516 2.458,9.833 9.423,20.486 14.886,26.769 0,0.273 1.093,0.956 1.093,1.229 4.507,5.327 5.873,6.829 22.944,10.652 l -0.41,1.913 c -10.243,-2.731 -18.71,-5.189 -24.037,-11.336 0,-0.136 -0.956,-1.093 -0.956,-1.093 -5.736,-6.556 -12.702,-17.481 -15.296,-27.724 -0.956,-4.098 -1.775,-7.238 -0.683,-11.473 4.643,-16.661 14.75,-23.217 24.993,-30.182 2.322,-1.502 5.053,-2.869 7.238,-4.644 4.233,-3.14 6.554,-12.701 8.877,-20.485 z");
  node_onion.append("path")
        .attr("style", "fill:#000000;fill-opacity:1;stroke:none")
        .attr("d", "m 255.90625,166.74951 c 0.137,7.102 -0.55625,10.66475 1.21875,15.71875 1.092,3.004 4.782,7.1015 5.875,11.0625 1.502,5.327 3.138,11.19901 3,14.75001 0,4.09799 -0.25625,11.74249 -2.03125,19.93749 -1.35362,6.77108 -4.47323,12.58153 -9.71875,15.875 -5.37327,-1.10644 -11.68224,-2.99521 -15.40625,-6.1875 -7.238,-6.282 -13.64875,-16.7865 -14.46875,-25.9375 -0.682,-7.51099 6.27275,-18.5885 15.96875,-24.1875 8.194,-4.78 10.1,-10.22775 11.875,-18.96875 -2.458,7.648 -4.7665,14.05925 -12.6875,18.15625 -11.472,6.009 -17.3585,16.09626 -16.8125,25.65625 0.819,12.291 5.7415,20.6195 15.4375,27.3125 4.097,2.868 11.75125,5.89875 16.53125,6.71875 l 0,-0.625 c 3.62493,-0.67888 8.31818,-6.63267 10.65625,-14.6875 2.049,-7.238 2.85675,-16.502 2.71875,-22.37499 -0.137,-3.414 -1.643,-10.80801 -4.375,-17.50001 -1.502,-3.687 -3.8095,-7.37375 -5.3125,-9.96875 -1.637,-2.597 -1.64875,-8.195 -2.46875,-14.75 z");
  node_onion.append("path")
        .attr("style", "fill:#000000;fill-opacity:1;stroke:none")
        .attr("d", "m 255.09375,193.53076 c 0.136,4.78 2.056,10.90451 2.875,17.18751 0.684,4.64399 0.387,9.30824 0.25,13.40624 -0.13495,4.74323 -1.7152,13.24218 -3.875,17.375 -2.03673,-0.93403 -2.83294,-1.99922 -4.15625,-3.71875 -1.638,-2.322 -2.75075,-4.644 -3.84375,-7.375 -0.819,-2.049 -1.7765,-4.394 -2.1875,-7.125 -0.546,-4.097 -0.393,-10.5065 4.25,-17.06249 3.551,-5.19001 4.36475,-5.58476 5.59375,-11.59376 -1.64,5.326 -2.8625,5.869 -6.6875,10.37501 -4.233,4.917 -4.9375,12.15924 -4.9375,18.03124 0,2.459 0.9805,5.18725 1.9375,7.78125 1.092,2.732 2.02925,5.452 3.53125,7.5 2.25796,3.32082 5.14798,5.20922 6.5625,5.5625 0.009,0.002 0.022,-0.002 0.0312,0 0.0303,0.007 0.0649,0.0255 0.0937,0.0312 l 0,-0.15625 c 2.64982,-2.95437 4.24444,-5.88934 4.78125,-8.84375 0.683,-3.551 0.84,-7.10975 1.25,-11.34375 0.409,-3.551 0.11225,-8.334 -0.84375,-13.24999 -1.365,-6.146 -3.669,-12.41226 -4.625,-16.78126 z");
  node_onion.append("path")
        .attr("style", "fill:#000000;fill-opacity:1;stroke:none")
        .attr("d", "m 255.499,135.06577 c 0.137,7.101 0.683,20.35 2.595,25.539 0.546,1.775 5.599,9.56 9.149,18.983 2.459,6.556 3.005,12.565 3.415,14.34 1.639,7.785 -0.41,20.896 -3.142,33.324 -1.365,6.692 -6.009,15.023 -11.335,18.301 l -1.092,1.912 c 3.005,-0.137 10.379,-7.375 12.974,-16.389 4.371,-15.296 6.146,-22.398 4.098,-39.333 -0.273,-1.64 -0.956,-7.238 -3.551,-13.248 -3.824,-9.151 -9.287,-17.891 -9.969,-19.667 -1.23,-2.867 -2.869,-15.295 -3.142,-23.762 z");
  node_onion.append("path")
        .attr("style", "fill:#000000;fill-opacity:1;stroke:none")
        .attr("d", "m 258.06151,125.35303 c -0.40515,7.29812 -0.51351,9.98574 0.85149,15.31174 1.502,5.873 9.151,14.34 12.292,24.037 6.009,18.574 4.507,42.884 0.136,61.867 -1.638,6.691 -9.424,16.389 -17.208,19.529 l 5.736,1.366 c 3.141,-0.137 11.198,-7.648 14.34,-16.252 5.052,-13.521 6.009,-29.636 3.96,-46.571 -0.137,-1.639 -2.869,-16.252 -5.463,-22.398 -3.688,-9.15 -10.244,-17.345 -10.926,-19.119 -1.228,-3.005 -3.92651,-9.24362 -3.71849,-17.77074 z");

  if (!graph_name) {
    graph_name = window.location.hash.substring(1);
  }
  var exits_only = false;
  if (/-exits-only$/.exec(graph_name)) {
    exits_only = true;
    graph_name = graph_name.replace("-exits-only", "");
  }

  var graph = graphs[graph_name];
  if (!graph) {
    graph = graphs['default'];
  }

  onionoo_url += ',' + graph.extra_fields.join(',')

  d3.json(onionoo_url, function(error, data) {
    var groups = {};
    var relay_count = 0;
    data.relays.forEach(function(relay) {
      if (0 == relay.consensus_weight || !relay.running) {
        return;
      }
      if (exits_only && relay.exit_probability == 0) {
        return;
      }
      group_id = graph.group_id_func(relay);
      group_name = graph.group_name_func(relay);
      if (!group_id) {
        group_id = 'unknown';
        group_name = 'Unknown';
      }
      if (!groups.hasOwnProperty(group_id)) {
        groups[group_id] = { name: group_name, children: [] };
      }
      groups[group_id].children.push(
          { name: relay.nickname ? relay.fingerprint : relay.nickname,
            value: relay.consensus_weight,
            exit: relay.exit_probability > 0,
            bandwidth: relay.advertised_bandwidth,
          });
      relay_count++;
    });

    var bubbles = svg.selectAll(".node")
        .data(bubble.nodes({ children: d3.values(groups) }));
    var node = bubbles.enter().append("g")
        .attr("class", "node")
        .attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; });

    if (graph.group) {
      node.append("circle")
          .filter(function(d) { return d.children && d.name; })
          .attr("r", function(d) { return d.r; })
          .style("fill", "#888888")
          .style("fill-opacity", ".25");
    }

    var relays = node.filter(function(d) { return !d.children && d.r > 1;})
      .append("use")
        .attr("xlink:href", function(d) { return "#node-" + (d.bandwidth > cutOff ? "onion" : "circle"); })
        .attr("transform", function(d) { return "translate(" + -d.r + "," + -d.r + ")"; })
        .attr("width", function(d) { return d.r * 2; })
        .attr("height", function(d) { return d.r * 2; })
        .attr("preserveAspectRatio", "xMidYMin")
        .attr("filter", function(d) { return d.exit ? "" : "url(#middle-filter)"; });

    if (!graph.group) {
      relays
        .on("mouseover", function(d) {
             svg.append("text")
                 .attr("transform", "translate(" + diameter + "," + (diameter - legendHeight - 50) + ")")
                 .attr("id", "relay-bw")
                 .style("text-anchor", "start")
                 .style("font-size", "14pt")
                 .text(d.className.substring(0, 20) + ": " + (d.bandwidth * 8.0 / 1000.0 / 1000.0).toFixed(2) + " Mbit/s");
           })
        .on("mouseout", function() {
             d3.select("#relay-bw").remove();
           })
    };

    if (graph.group) {
      node.filter(function(d) { return d.children && d.name; })
        .each(function(d) {
          var g = svg.append("g")
            .attr("transform", "translate(" + d.x + "," + d.y + ")");
          g.append("circle")
            .attr("r", d.r)
            .style("fill", "#000000")
            .style("fill-opacity", "0")
            .style("stroke", "none");
          g.on("mouseover", function() {
                svg.append("text")
                    .attr("transform", "translate(" + d.x + "," + (d.y - d.r) + ")")
                    .attr("id", "group-name")
                    .style("text-anchor", "middle")
                    .style("font-size", "14pt")
                    .text((d.name + "").substring(0, 50));
              })
           .on("mouseout", function() {
                d3.select("#group-name").remove();
              });
        });
    }

    var titleText = relay_count + " " + (exits_only ? "exits" : "relays") + " (" +
        (node.filter(function(d) { return !d.children && d.r > 1; }).size())  + " visible)";
    if (graph.group) {
      titleText = Object.keys(groups).length + " " + graph.group + " with " + titleText;
    }
    var title = svg.append("g")
        .attr("transform", "translate(10, " + (diameter - 30) + ")");
    title.append("text")
        .text(titleText)
        .attr("text-anchor", "start")
        .attr("style", "font-size: 18pt");
    title.append("text")
        .text(data['relays_published'])
        .attr("text-anchor", "start")
        .attr("dy", "15")
        .attr("style", "font-size: 10pt");

    var legend = svg.append("g")
        .attr("transform", "translate(" + (diameter - 10) +", " + (diameter - legendHeight - 10) + ")")
    legend.append("rect")
        .attr("width", legendWidth)
        .attr("height", legendHeight)
        .attr("fill", "#cccccc")
        .attr("stroke", "#000000");
    var legendOnion = legend.append("g")
        .attr("transform", "translate(0, " + legendIconMargin + ")");
    legendOnion.append("use")
      .attr("xlink:href", "#node-onion")
        .attr("width", legendIconSize)
        .attr("height", legendIconSize)
        .attr("preserveAspectRatio", "xMidYMin");
    legendOnion.append("text")
        .text((cutOff * 8 / 1000 / 1000) + "+ Mbit/s relays")
        .attr("text-anchor", "start")
        .attr("dx", legendIconSize)
        .attr("dy", legendIconSize / 2)
    var legendCircle = legend.append("g")
        .attr("transform", "translate(0, " + (legendIconSize + legendIconMargin * 2) + ")");
    legendCircle.append("use")
      .attr("xlink:href", "#node-circle")
        .attr("width", legendIconSize)
        .attr("height", legendIconSize)
        .attr("preserveAspectRatio", "xMidYMin");
    legendCircle.append("text")
        .text("smaller relays")
        .attr("text-anchor", "start")
        .attr("dx", legendIconSize)
        .attr("dy", legendIconSize / 2)
    var legendExit = legend.append("g")
        .attr("transform", "translate(0, " + ((legendIconSize + legendIconMargin * 2) * 2) + ")");
    legendExit.append("use")
      .attr("xlink:href", "#node-onion")
        .attr("width", legendIconSize / 2)
        .attr("height", legendIconSize / 2)
        .attr("preserveAspectRatio", "xMidYMin")
        .attr("filter", "url(#middle-filter)");
    legendExit.append("use")
      .attr("xlink:href", "#node-circle")
        .attr("width", legendIconSize / 2)
        .attr("height", legendIconSize / 2)
        .attr("preserveAspectRatio", "xMidYMin")
        .attr("transform", "translate(" + (legendIconSize / 2) + ", " + (legendIconSize / 2) + ")")
        .attr("filter", "url(#middle-filter)");
    legendExit.append("text")
        .text("non-exits")
        .attr("text-anchor", "start")
        .attr("dx", legendIconSize)
        .attr("dy", legendIconSize / 2);

  });

  d3.select(self.frameElement).style("height", diameter + "px");
}
