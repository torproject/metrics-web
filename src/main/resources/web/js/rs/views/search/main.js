// ~ views/search/main ~
define([
  'jquery',
  'underscore',
  'backbone',
  'text!templates/search/main.html',
  'helpers',
], function($, _, Backbone, mainSearchTemplate){
  var mainSearchView = Backbone.View.extend({
	    el: "#content",

	    render: function(query){
			document.title = "Relay Search";
			var data = {countries: CountryCodes};
			var compiledTemplate = _.template(mainSearchTemplate);
			this.$el.html(compiledTemplate(data));

            $("#do-top-relays").bind('click', function(){
                document.location = "#toprelays";
                return false;
            });

            $("#do-search").bind('click', function(){
                document.location = "#search/"+encodeURI($('#query').val());
                return false;
            });

            $("#home-search").bind('submit', function(){
                document.location = "#search/"+encodeURI($('#query').val());
                return false;
            });

            $("#do-aggregate").bind('click', function(){
                document.location = "#aggregate/all/"+encodeURI($('#aggregated-query').val());
                return false;
            });

            $("#do-full-aggregation").bind('click', function(){
                document.location = "#aggregate/all";
                return false;
            });

            $("#home-aggregate-search").bind('submit', function(){
                document.location = "#aggregate/all/"+encodeURI($('#aggregated-query').val());
                return false;
            });

            var buildAdvancedQuery = function(){
              var query = "";
              if($('#advanced-search-nickname').val().trim() != "") query += $('#advanced-search-nickname').val().trim() + " ";
              if($('#advanced-search-family')[0].checked) {
                if($('#advanced-search-fingerprint').val().trim() != "") query += "family:" + $('#advanced-search-fingerprint').val().trim().split(" ")[0] + " ";
              } else {
                if($('#advanced-search-fingerprint').val().trim() != "") query += "fingerprint:" + $('#advanced-search-fingerprint').val().trim().split(" ")[0] + " ";
              }
              if($('#advanced-search-flag').val() !== "") query += "flag:" + $('#advanced-search-flag').val() + " ";
              if($('#advanced-search-country').val() !== "") query += "country:" + $('#advanced-search-country').val() + " ";
              if($('#advanced-search-as').val().trim() !== "") query += "as:" + $('#advanced-search-as').val().trim() + " ";
              if($('#advanced-search-contact').val().trim() != "") query += "contact:" + $('#advanced-search-contact').val().trim().split(" ")[0] + " ";
              if($('#advanced-search-hostname').val().trim() != "") query += "host_name:" + $('#advanced-search-hostname').val().trim().split(" ")[0] + " ";
              if($('#advanced-search-type').val() !== "") query += "type:" + $('#advanced-search-type').val() + " ";
              if($('#advanced-search-running').val() !== "") query += "running:" + $('#advanced-search-running').val() + " ";
              if($('#advanced-search-first-seen-from').val() !== "0" || $('#advanced-search-first-seen-to').val() !== "0") query += "first_seen_days:" + $('#advanced-search-first-seen-from').val() + "-" + $('#advanced-search-first-seen-to').val() + " ";
              if($('#advanced-search-last-seen-from').val() !== "0" || $('#advanced-search-last-seen-to').val() !== "0") query += "last_seen_days:" + $('#advanced-search-last-seen-from').val() + "-" + $('#advanced-search-last-seen-to').val() + " ";
              if($('#advanced-search-version').val().trim() != "") query += "version:" + $('#advanced-search-version').val().trim().split(" ")[0] + " ";
              return query;
            }

            $("#do-advanced").bind('click', function(){
                var query = buildAdvancedQuery();
                document.location = "#search/"+encodeURI(query);
                return false;
            });

            var goAggregate = function(type) {
                var query = buildAdvancedQuery();
                document.location = "#aggregate/" + type + ((query) ? "/" + encodeURI(query) : "");
                return false;
            }

            $("#do-advanced-aggregation").bind('click', function(){
                return goAggregate("all")
            });

            $("#do-advanced-aggregation-cc").bind('click', function(){
                return goAggregate("cc")
            });

            $("#do-advanced-aggregation-as").bind('click', function(){
                return goAggregate("as")
            });

            $("#do-advanced-aggregation-ascc").bind('click', function(){
                return goAggregate("ascc")
            });

            $("#do-advanced-aggregation-version").bind('click', function(){
                return goAggregate("version")
            });

            $("#home-advanced-search").bind('submit', function(){
                var query = buildAdvancedQuery();
                document.location = "#search/"+encodeURI(query);
                return false;
            });

            $(".tip").tooltip();

	    }
  });
  return new mainSearchView;
});

