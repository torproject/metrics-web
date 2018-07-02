// This is the boilerplate file
// it should be used as a base for every module
define([
  'jquery',
  'underscore',
  'backbone'
], function($, _, Backbone){

    function parseHistory(history, model, relay, name) {
        var newar;
        _.each(_.keys(history), function(period, i) {
            var first = history[period].first.split(' ');
            var date = first[0].split('-');
            var time = first[1].split(':');
            first = new Date(date[0], date[1]-1, date[2],
                            time[0], time[1], time[2]);
            var y = first.getTime();

            _.each(history[period].values, function(value, i) {
                var x = null
                if (value != null) {
                    x = value*history[period].factor;
                }

                // This is quite a hack to conform to backbone.js
                // funky way of setting and getting attributes in
                // models.
                // XXX probably want to refactor.
                var mperiod = "bw_" + period.split("_")[1]
                var newar = model.get(mperiod)[name];
                newar.push([y,x]);
                y += history[period].interval*1000;
            });
        });
        return newar;
    };

    function parseWeightHistory(history, model, name) {
        var newar;
        _.each(_.keys(history), function(period, i) {
            var first = history[period].first.split(' ');
            var date = first[0].split('-');
            var time = first[1].split(':');
            first = new Date(date[0], date[1]-1, date[2],
                            time[0], time[1], time[2]);
            var y = first.getTime();
            _.each(history[period].values, function(value, i) {
                var x = null
                if (value != null) {
                    x = value*history[period].factor;
                }
                var mperiod = "weights_" + period.split("_")[1]
                newar = model.get(mperiod)[name];
                newar.push([y,x]);
                y += history[period].interval*1000;
            });
        });
        return newar;
    };

    function parseClients(history, model, name) {
        var newar;
        _.each(_.keys(history), function(period, i) {
            var first = history[period].first.split(' ');
            var date = first[0].split('-');
            var time = first[1].split(':');
            first = new Date(date[0], date[1]-1, date[2],
                            time[0], time[1], time[2]);
            var y = first.getTime();
            _.each(history[period].values, function(value, i) {
                var x = null
                if (value != null) {
                    x = value*history[period].factor;
                }
                var mperiod = "clients_" + period.split("_")[1]
                newar = model.get(mperiod)[name];
                newar.push([y,x]);
                y += history[period].interval*1000;
            });
        });
        return newar;
    };

    var graphModel = Backbone.Model.extend({
        baseurl: 'https://onionoo.torproject.org',
        initialize: function() {
        this.set({
            bw_month: {write: [], read: []},
            bw_months: {write: [], read: []},
            bw_year: {write: [], read: []},
            bw_years: {write: [], read: []},
            clients_week: {average: []},
            clients_month: {average: []},
            clients_months: {average: []},
            clients_year: {average: []},
            clients_years: {average: []},
            weights_week: {cw: [], guard: [], middle: [], exit: []},
            weights_month: {cw: [], guard: [], middle: [], exit: []},
            weights_months: {cw: [], guard: [], middle: [], exit: []},
            weights_year: {cw: [], guard: [], middle: [], exit: []},
            weights_years: {cw: [], guard: [], middle: [], exit: []}
            });
        },
        lookup_bw: function(fingerprint, options) {
            var model = this;
            var success = options.success;
            // Clear the model
            this.set({
                bw_month: {write: [], read: []},
                bw_months: {write: [], read: []},
                bw_year: {write: [], read: []},
                bw_years: {write: [], read: []}
            });

            var xhr = $.getJSON(this.baseurl+'/bandwidth?lookup='+fingerprint, function(data) {
                checkIfDataIsUpToDate(xhr.getResponseHeader("Last-Modified"));
                model.data = data;
                success(model, data);
            });
        },
        parse_bw_data: function(data) {
            var model = this;
            var relay = data.relays[0];
            if (!relay)
                relay = data.bridges[0];
            this.fingerprint = relay.fingerprint;
            // Parse the read and write history of the relay
            var write_history = parseHistory(relay.write_history, model, relay, 'write');
            var read_history = parseHistory(relay.read_history, model, relay, 'read');
            var toset = {mperiod: {read: read_history, write: write_history}};
            model.set(toset);
        },
        lookup_weights: function(fingerprint, options) {
            var model = this;
            var success = options.success;
            // Clear the model
            this.set({
                weights_week: {cw: [], guard: [], middle: [], exit: []},
                weights_month: {cw: [], guard: [], middle: [], exit: []},
                weights_months: {cw: [], guard: [], middle: [], exit: []},
                weights_year: {cw: [], guard: [], middle: [], exit: []},
                weights_years: {cw: [], guard: [], middle: [], exit: []}
            });

            var xhr = $.getJSON(this.baseurl+'/weights?lookup='+fingerprint, function(data) {
                checkIfDataIsUpToDate(xhr.getResponseHeader("Last-Modified"));
                model.data = data;
                success(model, data);
            });
        },
        parse_weights_data: function(data) {
            var model = this;
            var relay = data.relays[0];
            if (!relay)
                relay = data.bridges[0];
            this.fingerprint = relay.fingerprint;

            if ("consensus_weight_fraction" in relay) {
                var cw = parseWeightHistory(relay.consensus_weight_fraction, model, 'cw');
                model.set({mperiod: {cw: cw}});
            }

            if ("guard_probability" in relay) {
                var guard = parseWeightHistory(relay.guard_probability, model, 'guard');
                model.set({mperiod: {guard: guard}});
            }

            if ("middle_probability" in relay) {
                var middle = parseWeightHistory(relay.middle_probability, model, 'middle');
                model.set({mperiod: {middle: middle}});
            }

            if ("exit_probability" in relay) {
                var exit = parseWeightHistory(relay.exit_probability, model, 'exit');
                model.set({mperiod: {exit: exit}});
            }
        },
        lookup_clients: function(fingerprint, options) {
            var model = this;
            var success = options.success;
            // Clear the model
            this.set({
                clients_week: {average: []},
                clients_month: {average: []},
                clients_months: {average: []},
                clients_year: {average: []},
                clients_years: {average: []}
            });

            var xhr = $.getJSON(this.baseurl+'/clients?lookup='+fingerprint, function(data) {
                checkIfDataIsUpToDate(xhr.getResponseHeader("Last-Modified"));
                model.data = data;
                success(model, data);
            });
        },
        parse_clients_data: function(data) {
            var model = this;
            var relay = data.relays[0];
            if (!relay)
                relay = data.bridges[0];
            this.fingerprint = relay.fingerprint;

            if ("average_clients" in relay) {
                var average = parseClients(relay.average_clients, model, 'average');
                model.set({mperiod: {average: average}});
            }
        }
    })
    return graphModel;
});

