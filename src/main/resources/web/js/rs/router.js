// ~ router.js ~
define([
  'jquery',
  'underscore',
  'backbone',
  'views/details/main',
  'views/search/main',
  'views/search/do',
  'views/aggregate/search',
  'views/aggregate/map',
  'jssha'
], function($, _, Backbone, mainDetailsView, mainSearchView, doSearchView, aggregateSearchView, aggregateMapView, jsSHA){
  var AppRouter = Backbone.Router.extend({
    routes: {
       // Define the routes for the actions in Atlas
        'details/': 'mainDetails',
    	'details/:fingerprint': 'mainDetails',
    	'search/:query': 'doSearch',
    	'search/': 'doSearch',
        'top10': 'showTopRelays',
        'toprelays': 'showTopRelays',
        'aggregate/:aType(/:query)': 'aggregateSearch',
        'aggregate(/:aType)/': 'emptyAggregateSearch',
        'map(_:property)(/:query)': 'aggregateMap',
    	// Default
    	'*actions': 'defaultAction'
    },

    hashFingerprint: function(fp){
        if (fp.match(/^[a-f0-9]{40}$/i) != null)
            return new jsSHA(fp, "HEX").getHash("SHA-1", "HEX").toUpperCase();
        else
            return fp
    },

    // Show the details page of a node
    mainDetails: function(fingerprint){

        $("#content").hide();
        $(".progress").show();

        fingerprint = ( fingerprint == null ) ? "" : fingerprint;

        mainDetailsView.model.fingerprint = this.hashFingerprint(fingerprint);
        mainDetailsView.model.lookup({
            success: function(relay) {
                mainDetailsView.render();
                $(".progress").hide();
                $("#content").show();
                $(".breadcrumb").html("<li><a href=\"/\">Home</a></li><li><a href=\"/services.html\">Services</a></li><li><a href=\"#\">Relay Search</a></li><li class=\"active\">Details for " + relay.get('nickname') + "</li>");
                $("#secondary-search").show();

            },
            error: function() {
                mainDetailsView.error();
                $(".progress").hide();
                $("#content").show();
                $(".breadcrumb").html("<li><a href=\"/\">Home</a></li><li><a href=\"/services.html\">Services</a></li><li><a href=\"#\">Relay Search</a></li><li class=\"active\">Error</li>");
                $("#secondary-search").show();
            }
        });
    },
    // Empty aggregation query
    emptyAggregateSearch: function() {
        $(".breadcrumb").html("<li><a href=\"/\">Home</a></li><li><a href=\"/services.html\">Services</a></li><li><a href=\"#\">Relay Search</a></li><li class=\"active\">Error</li>");
        $("#secondary-search-query").val("");

        $("#secondary-search").hide();
        $("#content").hide();
        $(".progress").show();
        aggregateSearchView.error = 5;
        aggregateSearchView.renderError();
        $(".progress").hide();
        $("#secondary-search").show();
        $("#content").show();

    },
    // Perform an aggregate search
    aggregateSearch: function(aType, query){
        $(".breadcrumb").html("<li><a href=\"/\">Home</a></li><li><a href=\"/services.html\">Services</a></li><li><a href=\"#\">Relay Search</a></li><li class=\"active\">Aggregated search" + ((query) ? " for " + query : "") + "</li>");

        $("#content").hide();
        $("#secondary-search").hide();
        $(".progress").show();

        aggregateSearchView.collection.aType = (aType) ? aType : "all";

        if (query) {
          query = query.trim();
          $("#secondary-search-query").val(query);
          aggregateSearchView.collection.url =
            aggregateSearchView.collection.baseurl + "&search=" + this.hashFingerprint(query);
        } else {
          aggregateSearchView.collection.url =
            aggregateSearchView.collection.baseurl;
          query = "";
        }
        aggregateSearchView.collection.lookup({
          success: function(err, relaysPublished, bridgesPublished){
          aggregateSearchView.error = err;
          aggregateSearchView.relaysPublished = relaysPublished;
          aggregateSearchView.bridgesPublished = bridgesPublished;
          aggregateSearchView.render(query);
          $("#search-title").text("Aggregated results" + ((query) ? " for " + query : ""));
          $(".progress").hide();
          $("#secondary-search").show();
          $("#content").show();
        },
        error: function(err){
          aggregateSearchView.error = err;
          aggregateSearchView.renderError();
          $(".progress").hide();
          $("#secondary-search").show();
          $("#content").show();
        }
      });
    },
    // Perform an aggregate search
    aggregateMap: function(property, query){
        $(".breadcrumb").html("<li><a href=\"/\">Home</a></li><li><a href=\"/services.html\">Services</a></li><li><a href=\"#\">Relay Search</a></li><li class=\"active\">Map view" + ((query) ? " for " + query : "") + "</li>");

        $("#content").hide();
        $("#secondary-search").hide();
        $(".progress").show();

        aggregateMapView.collection.aType = "cc";
        aggregateMapView.mapProperty = (property) ? property : "consensus_weight_fraction";

        if (query) {
          query = query.trim();
          $("#secondary-search-query").val(query);
          aggregateMapView.collection.url =
            aggregateMapView.collection.baseurl + "&search=" + this.hashFingerprint(query);
        } else {
          aggregateMapView.collection.url =
            aggregateMapView.collection.baseurl;
          query = "";
        }
        aggregateMapView.collection.lookup({
          success: function(err, relaysPublished, bridgesPublished){
          aggregateMapView.error = err;
          aggregateMapView.relaysPublished = relaysPublished;
          aggregateMapView.bridgesPublished = bridgesPublished;
          aggregateMapView.render(query);
          $("#search-title").text("Map view" + ((query) ? " for " + query : ""));
          $(".progress").hide();
          $("#secondary-search").show();
          $("#content").show();
        },
        error: function(err){
          aggregateMapView.error = err;
          aggregateMapView.renderError();
          $(".progress").hide();
          $("#secondary-search").show();
          $("#content").show();
        }
      });
    },
    // Perform a search on Atlas
    doSearch: function(query){
        $(".breadcrumb").html("<li><a href=\"/\">Home</a></li><li><a href=\"/services.html\">Services</a></li><li><a href=\"#\">Relay Search</a></li><li class=\"active\">Search for " + query + "</li>");

        $("#secondary-search").hide();
        $("#content").hide();
        $(".progress").show();

        if (query == null) {
	    doSearchView.error = 5;
            doSearchView.renderError();
            $(".progress").hide();
            $("#content").show();
            $("#secondary-search").show();
        } else {
          query = query.trim();
          $("#secondary-search-query").val(query);
          doSearchView.collection.url =
              doSearchView.collection.baseurl + this.hashFingerprint(query);
          doSearchView.collection.lookup({
              success: function(err, relaysPublished, bridgesPublished){
                  doSearchView.relays = doSearchView.collection.models;
                  // Redirect to the details page when there is exactly one
                  // search result.
                  if (doSearchView.relays.length == 1) {
                      document.location.replace("#details/" +
                          doSearchView.relays[0].fingerprint);
                      return;
                  }
		    doSearchView.error = err;
                  doSearchView.relaysPublished = relaysPublished;
                  doSearchView.bridgesPublished = bridgesPublished;
                  doSearchView.render(query);
		  $("#search-title").text(query);
                  $("#secondary-search").show();
                  $(".progress").hide();
                  $("#content").show();
              },

              error: function(err){
		    doSearchView.error = err;
		    doSearchView.renderError();
                  $(".progress").hide();
                  $("#content").show();
                  $("#secondary-search").show();
              }
          });
        }
    },
    showTopRelays: function(){
        $(".breadcrumb").html("<li><a href=\"/\">Home</a></li><li><a href=\"/services.html\">Services</a></li><li><a href=\"#\">Relay Search</a></li><li class=\"active\">Top Relays</li>");

        $("#secondary-search-query").val("");

        $("#secondary-search").hide();
        $("#content").hide();
        $(".progress").show();

        doSearchView.collection.url = "https://onionoo.torproject.org/details?type=relay&order=-consensus_weight&limit=250&running=true";
            doSearchView.collection.lookup({
                success: function(err){
                    doSearchView.relays = doSearchView.collection.models;
                    doSearchView.error = err;
                    doSearchView.render("");
		    $("#search-title").text("Top Relays by Consensus Weight");
                    $(".progress").hide();
                    $("#content").show();
                    $("#secondary-search").show();
                },

                error: function(erno){
                    doSearchView.error = erno;
                    doSearchView.renderError();
                    $(".progress").hide();
                    $("#content").show();
                    $("#secondary-search").show();
                }
            });
    },

    // No matched rules go to the default home page
    defaultAction: function(actions){
        $(".breadcrumb").html("<li><a href=\"/\">Home</a></li><li><a href=\"/services.html\">Services</a></li><li class=\"active\">Relay Search</li>");
        $("#secondary-search").hide();
        $("#secondary-search-query").val("");

        mainSearchView.render();

        if (actions == "aggregate") {
          $('.search').hide();
          $('#aggregated-search-tab-content').fadeIn();
          $('.search-tabs').removeClass('active');
          $('#aggregated-search-tab').addClass('active');
        } else if (actions == "advanced") {
          $('.search').hide();
          $('#advanced-search-tab-content').fadeIn();
          $('.search-tabs').removeClass('active');
          $('#advanced-search-tab').addClass('active');
          $('.well').hide();
        } else {
          $('.search').hide();
          $('#main-search-tab-content').fadeIn();
          $('.search-tabs').removeClass('active');
          $('#main-search-tab').addClass('active');
        }

        $(".progress").hide();
        $("#content").show();
    }

  });

  var initialize = function(){
    var app_router = new AppRouter;
    Backbone.history.start();

    $("#secondary-search-submit").bind('click', function(){
      document.location = "#search/"+encodeURI($('#secondary-search-query').val());
      return false;
    });

    $("#secondary-search-aggregate").bind('click', function(){
      document.location = "#aggregate/all/"+encodeURI($('#secondary-search-query').val());
      return false;
    });

    $("#secondary-search-clear").bind('click', function(){
      $("#secondary-search-query").val("");
      return false;
    });

    $("#secondary-search").bind('submit', function(){
      document.location = "#search/"+encodeURI($('#secondary-search-query').val());
      return false;
    });

  };
  return {
    initialize: initialize
  };
});
