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
      var compiledTemplate = _.template(aggregateSearchTemplate)
      this.$el.html(compiledTemplate({query: query,
                                     aggregates: this.collection.models,
                                     aType: this.collection.aType,
                                     countries: CountryCodes,
                                     error: this.error,
                                     relaysPublished: this.relaysPublished,
                                     bridgesPublished: this.bridgesPublished}));

      // This creates the table using DataTables
      var oTable = $('#torstatus_results').dataTable({
        "sDom": "<\"top\"l>rt<\"bottom\"ip><\"clear\">",
        "bStateSave": false,
        "aaSorting": [[(this.collection.aType == "version") ? 3 : 2, "desc"]],
        "fnDrawCallback": function( oSettings ) {
          $(".tip").tooltip({'html': true});
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

