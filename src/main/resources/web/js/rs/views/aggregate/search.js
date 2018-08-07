// ~ views/search/do ~
define([
  'jquery',
  'underscore',
  'backbone',
  'collections/aggregates',
  'text!templates/aggregate/search.html',
  'datatables',
  'datatablessort',
  'helpers',
  'bootstrap',
  'datatablesbs'
], function($, _, Backbone, aggregatesCollection, aggregateSearchTemplate){
  var aggregateSearchView = Backbone.View.extend({
    el: "#content",
    initialize: function() {
      this.collection = new aggregatesCollection;
    },
    render: function(query){
      document.title = "Relay Search";
      var compiledTemplate = _.template(aggregateSearchTemplate);
      var aggregates = this.collection.models;
      this.$el.html(compiledTemplate({query: query,
                                     aggregates: aggregates,
                                     aType: this.collection.aType,
                                     countries: CountryCodes,
                                     error: this.error,
                                     onionooVersion: this.onionooVersion,
                                     buildRevision: this.buildRevision,
                                     relaysPublished: this.relaysPublished,
                                     bridgesPublished: this.bridgesPublished}));

      // This creates the table using DataTables
      var oTable = $('#torstatus_results').dataTable({
        "sDom": "<\"top\"l>rt<\"bottom\"ip><\"clear\">",
        "bStateSave": false,
        "aaSorting": [[(this.collection.aType == "version") ? 3 : 2, "desc"]],
        "fnDrawCallback": function( oSettings ) {
          $(".tip").tooltip({'html': true});
        },
        "footerCallback": function( tfoot, data, start, end, display ) {
          var sumConsensusWeight = 0;
          var sumAdvertisedBandwidths = 0;
          var sumGuardProbability = 0;
          var sumMiddleProbability = 0;
          var sumExitProbability = 0;
          var sumRelays = 0;
          var sumGuards = 0;
          var sumExits = 0;
          for (var i = 0; i < aggregates.length; i++) {
            sumConsensusWeight += aggregates[i]["consensus_weight_fraction"];
            sumAdvertisedBandwidths += aggregates[i]["advertised_bandwidth"];
            sumGuardProbability += aggregates[i]["guard_probability"];
            sumMiddleProbability += aggregates[i]["middle_probability"];
            sumExitProbability += aggregates[i]["exit_probability"];
            sumRelays += aggregates[i]["relays"];
            sumGuards += aggregates[i]["guards"];
            sumExits += aggregates[i]["exits"];
          }
          $(tfoot).find('th').eq(1).html((sumConsensusWeight * 100).toFixed(2) + "%");
          $(tfoot).find('th').eq(2).html(hrBandwidth(sumAdvertisedBandwidths));
          $(tfoot).find('th').eq(3).html((sumGuardProbability * 100).toFixed(2) + "%");
          $(tfoot).find('th').eq(4).html((sumMiddleProbability * 100).toFixed(2) + "%");
          $(tfoot).find('th').eq(5).html((sumExitProbability * 100).toFixed(2) + "%");
          $(tfoot).find('th').eq(6).html(sumRelays);
          $(tfoot).find('th').eq(7).html(sumGuards);
          $(tfoot).find('th').eq(8).html(sumExits);
        }
      });
    },
    renderError: function(){
      var compiledTemplate = _.template(aggregateSearchTemplate);
      this.$el.html(compiledTemplate({aggregates: null, error: this.error, countries: null}));
    }
  });
  return new aggregateSearchView;
});

