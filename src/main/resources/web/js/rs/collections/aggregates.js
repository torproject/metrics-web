// ~ collections/aggregates ~
define([
  'jquery',
  'underscore',
  'backbone',
  'models/aggregate'
], function($, _, Backbone, aggregateModel){
  var aggregatesCollection = Backbone.Collection.extend({
    model: aggregateModel,
    baseurl: 'https://onionoo.torproject.org/details?running=true&type=relay&fields=country,guard_probability,middle_probability,exit_probability,consensus_weight,consensus_weight_fraction,advertised_bandwidth,flags,as_number,as_name,measured,version',
    url: '',
    aType: 'cc',
    lookup: function(options) {
      var success = options.success;
      var error = options.error;
      var err = -1;
      var collection = this;
      options.success = $.getJSON(this.url, function(response) {
        checkIfDataIsUpToDate(options.success.getResponseHeader("Last-Modified"));
        this.fresh_until = response.fresh_until;
        this.valid_after = response.valid_after;
        var aggregates = {};
        var relaysPublished = response.relays_published;
        var bridgesPublished = response.bridges_published;
        options.error = function(options) {
          error(options.error, collection, options);
        }
        _.each(response.relays, function(relay) {
          /* If a relay country is unknown, use XZ as the country code.
             This code will never be assigned for use with ISO 3166-1 and is "user-assigned".
             Fun fact: UN/LOCODE assigns XZ to represent installations in international waters. */
          relay.country = ((typeof relay.country) == "undefined") ? "xz" : relay.country;
          relay.as_number = ((typeof relay.as_number) == "undefined") ? 0 : relay.as_number;
          if (relay.as_number == 0) relay.as_name = "Unknown";

          var ccAggregate = false;
          var asAggregate = false;
          var versionAggregate = false;

          if (collection.aType == "all") {
            aggregateKey = "zz"; // A user-assigned ISO 3166-1 code, but really just a static key
          } else if (collection.aType == "cc") {
            aggregateKey = relay.country;
            ccAggregate = true;
          } else if (collection.aType == "as") {
            aggregateKey = relay.as_number;
            asAggregate = true;
          } else if (collection.aType == "version") {
            aggregateKey = relay.version.split(".").slice(0, 3).join(".") + ".";
            versionAggregate = true;
          } else {
            aggregateKey = relay.country + "/" + relay.as_number;
            ccAggregate = asAggregate = true;
          }

          if (!(aggregateKey in aggregates)) {
            aggregates[aggregateKey] = new aggregateModel;
            if (ccAggregate) {
              aggregates[aggregateKey].country = relay.country;
            } else {
              aggregates[aggregateKey].country = new Set();
            }
            if (asAggregate) {
              aggregates[aggregateKey].as = relay.as_number;
            } else {
              aggregates[aggregateKey].as = new Set();
            }
            if (versionAggregate) {
              aggregates[aggregateKey].version = aggregateKey;
            }
            aggregates[aggregateKey].as_name = relay.as_name;
          }

          if (!ccAggregate) {
            if (relay.country !== "xz") aggregates[aggregateKey].country.add(relay.country);
          }
          if (!asAggregate) {
            if (relay.as_number !== 0) aggregates[aggregateKey].as.add(relay.as_number);
          }
          aggregates[aggregateKey].relays++;
          if ((typeof relay.guard_probability) !== "undefined") aggregates[aggregateKey].guard_probability += relay.guard_probability;
          if ((typeof relay.middle_probability) !== "undefined") aggregates[aggregateKey].middle_probability += relay.middle_probability;
          if ((typeof relay.exit_probability) !== "undefined") aggregates[aggregateKey].exit_probability += relay.exit_probability;
          if ((typeof relay.consensus_weight) !== "undefined") aggregates[aggregateKey].consensus_weight += relay.consensus_weight;
          if ((typeof relay.consensus_weight_fraction) !== "undefined") aggregates[aggregateKey].consensus_weight_fraction += relay.consensus_weight_fraction;
          if ((typeof relay.advertised_bandwidth) !== "undefined" && relay.advertised_bandwidth > 0) {
            aggregates[aggregateKey].advertised_bandwidth += relay.advertised_bandwidth;
            if (relay.measured) {
              aggregates[aggregateKey].consensus_weight_to_bandwidth_count++;
              aggregates[aggregateKey].consensus_weight_to_bandwidth += ((relay.consensus_weight*1024)/relay.advertised_bandwidth); // This is divided by number of relays for which data existed below to provide a mean average
            }
          }
          _.each(relay.flags, function(flag) {
            if (flag == "Guard") aggregates[aggregateKey].guards++;
            if (flag == "Exit") aggregates[aggregateKey].exits++;
          });
        });
        if (Object.keys(aggregates).length == 0) {
          error(0);
          return false;
        }
        var aggregatesArr = [];
        _.each(Object.keys(aggregates), function(aggregateKey) {
          var aggregate = aggregates[aggregateKey];
          if ((typeof aggregate.as) !== "string") {
            if (aggregate.as.size == 1) {
              aggregate.as.forEach(function(value1, value2, set) {
                aggregate.as = value1;
              });
            }
          }
          if ((typeof aggregate.country) !== "string") {
            if (aggregate.country.size == 1) {
              aggregate.country.forEach(function(value1, value2, set) {
                aggregate.country = value1;
              });
            }
          }
          if (aggregate.consensus_weight_to_bandwidth_count > 0) {
            aggregate.consensus_weight_to_bandwidth = aggregate.consensus_weight_to_bandwidth/aggregate.consensus_weight_to_bandwidth_count;
          }
          aggregatesArr.push(aggregate);
        });
        collection[options.add ? 'add' : 'reset'](aggregatesArr, options);
        success(err, relaysPublished, bridgesPublished);
      }).fail(function(jqXHR, textStatus, errorThrown) {
        if(jqXHR.statusText == "error") {
          error(2);
        } else {
          error(3);
        }
      });
    }
  });
  return aggregatesCollection;
});

