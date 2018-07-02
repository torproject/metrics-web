// ~ collections/results ~
define([
  'jquery',
  'underscore',
  'backbone',
  'models/relay'
], function($, _, Backbone, relayModel){
	var resultsCollection = Backbone.Collection.extend({
		model: relayModel,
		baseurl: 'https://onionoo.torproject.org/details?search=',
		url: '',
		lookup: function(options) {
            var success = options.success;
            var error = options.error;
            var err = 0;
            var collection = this;
            options.success = $.getJSON(this.url, function(response) {
                checkIfDataIsUpToDate(options.success.getResponseHeader("Last-Modified"));
                this.fresh_until = response.fresh_until;
                this.valid_after = response.valid_after;
                var relays = [];
                var relaysPublished = response.relays_published;
                var bridgesPublished = response.bridges_published;
                options.error = function(options) {
                    error(options.error, collection, options);
                }
                _.each(response.relays, function(relay, resultsC) {
                    crelay = new relayModel;
                    crelay.fingerprint = relay.fingerprint;
                    crelay.relay = relay;
                    crelay.relay.is_bridge = false;
                    relays.push(crelay);
                });
                _.each(response.bridges, function(relay, resultsC) {
                    crelay = new relayModel;
                    crelay.fingerprint = relay.hashed_fingerprint;
                    crelay.relay = relay;
                    crelay.relay.is_bridge = true;
                    relays.push(crelay);
                });
                if (relays.length == 0) {
                    error(0);
                    return false;
                } else if (relays.length > 2000) {
                   relays = relays.slice(0, 2000);
                   err = 4;
                }
                var lookedUpRelays = 0;
                var relayChunks = relays.chunk(600);
                var chunkedLookup = function() {
                  _.each(relayChunks.pop(), function(relay) {
                      relay.lookup({
                          success: function(){
                              lookedUpRelays++;
                          },
                          error: function() {
                              lookedUpRelays++;
                              error(0);
                          }
                      });
                  });
                  if (lookedUpRelays == relays.length) {
                    $('.progress-bar').width("100%");
                    $('.progress-bar').html("Rendering results...");
                    setTimeout(function() {
                      collection[options.add ? 'add' : 'reset'](relays, options);
                      success(err, relaysPublished, bridgesPublished);
                    }, 500);
                  } else {
                    $('.progress-bar').width((lookedUpRelays / relays.length * 100) + "%");
                    $('.progress-bar').html(lookedUpRelays + " of " + relays.length + " loaded");
                    setTimeout(chunkedLookup, 50);
                  }
                }
                chunkedLookup();
            }).fail(
                function(jqXHR, textStatus, errorThrown) {
                if(jqXHR.statusText == "error") {
                    error(2);
                } else {
                    error(3);
                }
                }
            );
        }

	});
	return resultsCollection;
});

