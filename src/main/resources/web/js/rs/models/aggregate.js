// ~ models/aggregateModel ~
define([
  'jquery',
  'underscore',
  'backbone',
  'helpers'
], function($, _, Backbone){
	var aggregateModel = Backbone.Model.extend({
          country: null,
          as: null,
          as_name: null,
          guard_probability: 0,
          middle_probability: 0,
          exit_probability: 0,
          advertised_bandwidth: 0,
          consensus_weight: 0,
          consensus_weight_fraction: 0,
          consensus_weight_to_bandwidth: 0,
          consensus_weight_to_bandwidth_count: 0,
          relays: 0,
          guards: 0,
          exits: 0,
          version: null
	});
	return aggregateModel;
});
