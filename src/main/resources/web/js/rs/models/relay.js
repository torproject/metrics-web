// ~ models/relay ~
define([
  'jquery',
  'underscore',
  'backbone',
  'helpers',
  'fallbackdir'
], function($, _, Backbone){

	var relayModel = Backbone.Model.extend({
        baseurl: 'https://onionoo.torproject.org',
        fingerprint: '',
        parseflags: function(flags, is_bridge) {
            var output = [];
            var model = this;
            _.each(flags, function(flag) {
                if (flag == "Authority") {
                    output.push([flag, "authority", "This relay is a directory authority."]);
                }
                if (flag == "BadExit") {
                    model.set({badexit: true});
                    output.push([flag, "badexit", "This relay is believed to be useless as an exit node (because its ISP censors it, because it is behind a restrictive proxy, or for some similar reason)."]);
                }
                if (flag == "Fast") {
                    output.push([flag, "fast", "This relay is suitable for high-bandwidth circuits."]);
                }
                if (flag == "Guard") {
                    if (!is_bridge) {
                        output.push([flag, "guard", "This relay is suitable for use as an entry guard."]);
                    }
                }
                if (flag == "HSDir") {
                    if (!is_bridge) {
                        output.push([flag, "hsdir", "This relay is considered a v2 hidden service directory."]);
                    }
                }
                if (flag == "NoEdConsensus") {
                    output.push([flag, "noedconensus", "An Ed25519 key in the relay's descriptor or microdesriptor does not reflect authority consensus."]);
                }
                if (flag == "Running") {
                    output.push([flag, "running", "This relay is currently usable."]);
                }
                if (flag == "Stable") {
                    output.push([flag, "stable", "This relay is suitable for long-lived circuits."]);
                }
                if (flag == "V2Dir") {
                    output.push([flag, "v2dir", "This relay implements the v2 directory protocol or higher."]);
                }
                if (flag == "Valid") {
                    output.push([flag, "valid", "This relay has been 'validated'."]);
                }
                if (flag == "Exit") {
                    output.push([flag, "exit", "This relay is more useful for building general-purpose exit circuits than for relay circuits."]);
                }
            });
            return output;
        },
        parseadditionalflags: function(flags) {
            var output = [];
            var model = this;
            _.each(flags, function(flag) {
                if (flag == "Not Recommended") {
                    output.push([flag,"notrecommended", "This relay is running a Tor version that is not recommended by the directory authorities and may contain known issues. This includes both obsolete and experimental versions."]);
                }
                if (flag == "Outdated") {
                    output.push([flag,"outdated", "This relay is running a Tor version that is considered obsolete. If this is your relay then you should upgrade at the earliest opportunity."]);
                }
                if (flag == "Experimental") {
                    output.push([flag,"experimental", "This relay is running a Tor version that is considered experimental. Please report any bugs found. If this is not intentional, you may consider switching to the most recent release instead."]);
                }
                if (flag == "Unmeasured") {
                    output.push([flag,"unmeasured", "This relay has not been measured by at least 3 bandwidth authorities and so its consensus weight is currently capped. This is expected for new relays."]);
                }
                if (flag == "FallbackDir") {
                    output.push([flag,"fallbackdir", "Tor clients contact fallback directory mirrors during bootstrap, and download the consensus and authority certificates from them. We include a default list of mirrors in the Tor source code. These default mirrors need to be long-term stable, and on the same IPv4 and IPv6 addresses and ports."]);
                }
                if (flag == "ReachableIPv6") {
                    output.push([flag,"reachableipv6", "This relay claims to accept OR connections using IPv6 and the directory authorities have confirmed it is reachable."]);
                }
                if (flag == "UnreachableIPv6") {
                    output.push([flag,"unreachableipv6", "This relay claims to accept OR connections using IPv6 but the directory authorities failed to confirm it was reachable."]);
                }
                if (flag == "UnreachableIPv4") {
                    output.push([flag,"unreachableipv4", "This relay claims to accept OR connections using IPv4 but the directory authorities failed to confirm it was reachable."]);
                }
                if (flag == "IPv6 Exit") {
                    output.push([flag, "ipv6exit", "This relay allows exit connections using IPv6."]);
                }
                if (flag == "Hibernating") {
                    output.push([flag, "hibernating", "This relay indicated that it is hibernating in its last known server descriptor."]);
                }
            });
            return output;
        },
        parsedate: function(utctime) {
            var hr_magic = [10];
            var t = utctime.split(" ");
            var utcd = t[0].split("-");
            var utct = t[1].split(":");
            var d = new Date(utcd[0], utcd[1]-1, utcd[2], utct[0], utct[1], utct[2]);
            var now = new Date();
            now = new Date(now.getUTCFullYear(), now.getUTCMonth(), now.getUTCDate(),  now.getUTCHours(), now.getUTCMinutes(), now.getUTCSeconds());
            var diff = now-d;
            var secs = Math.round(diff/1000);
            var mins = Math.floor(secs/60);
            var hours = Math.floor(mins/60);
            var days = Math.floor(hours/24);
            var years = Math.floor(days/365);
            // XXX check if this formula is correct.
            secs = secs % 60;
            mins = mins % 60;
            hours = hours % 24;
            days = days % 365;

            var hr_date = "";
            var hr_date_full = "";
            var hr = 0;

            if (years > 0) {
                hr_date += years + "y ";
                hr += 1;
                if (years > 1) {
                    hr_date_full += years + " years ";
                } else {
                    hr_date_full += years + " year ";
                }
            }

            if (days > 0) {
                hr_date += days + "d ";
                hr += 1;
                if (days > 1) {
                    hr_date_full += days + " days ";
                } else {
                    hr_date_full += days + " day ";
                }
            }

            if (hours > 0) {
                hr_date += hours + "h ";
                hr += 1;
                if (hours > 1) {
                    hr_date_full += hours + " hours ";
                } else {
                    hr_date_full += hours + " hour ";
                }
            }


            if (mins > 0) {
                if (hr < 2) {
                    hr_date += mins + "m ";
                    hr += 1;
                }
                if (hours > 1) {
                    hr_date_full += mins + " minutes ";
                } else {
                    hr_date_full += mins + " minute ";
                }
            }

            if (hr < 2) {
                hr_date += secs + "s ";
                hr += 1;
            }
            if (hr > 1) {
                hr_date_full += "and ";
            }
            if (secs > 1) {
                hr_date_full += secs + " seconds";
            } else {
                hr_date_full += secs + " second";
            }
            var output = {hrfull: hr_date_full, hr: hr_date, millisecs: diff};
            return output

        },
        is_new: function(utctime) {
            var hr_magic = [10];
            var t = utctime.split(" ");
            var utcd = t[0].split("-");
            var utct = t[1].split(":");
            var d = new Date(utcd[0], utcd[1]-1, utcd[2], utct[0], utct[1], utct[2]);
            var now = new Date();
            now = new Date(now.getUTCFullYear(), now.getUTCMonth(), now.getUTCDate(),  now.getUTCHours(), now.getUTCMinutes(), now.getUTCSeconds());
            var diff = now-d;
            var secs = Math.round(diff/1000);
            var mins = Math.floor(secs/60);
            var hours = Math.floor(mins/60);
            var days = Math.floor(hours/24);
            return days < 15;
        },
        processRelay: function(options, model, relay) {
                    relay.contact = relay.contact ? relay.contact : 'undefined';
                    relay.platform = relay.platform ? relay.platform : null;
                    relay.dir_address = relay.dir_address ? relay.dir_address : null;
                    relay.exit_policy = relay.exit_policy ? relay.exit_policy : null;
                    relay.exit_policy_summary = relay.exit_policy_summary ?  relay.exit_policy_summary : null;
                    relay.exit_policy_v6_summary = relay.exit_policy_v6_summary ?  relay.exit_policy_v6_summary : null;
                    relay.bandwidthr = (typeof relay.bandwidth_rate !== 'undefined') ? hrBandwidth(relay.bandwidth_rate) : null;
                    relay.bandwidthb = (typeof relay.bandwidth_burst !== 'undefined') ? hrBandwidth(relay.bandwidth_burst) : null;
                    relay.obandwidth = (typeof relay.observed_bandwidth !== 'undefined') ? hrBandwidth(relay.observed_bandwidth) : null;
                    relay.bandwidth = (typeof relay.advertised_bandwidth !== 'undefined') ? relay.advertised_bandwidth : null;
                    relay.bandwidth_hr = (typeof relay.advertised_bandwidth !== 'undefined') ? hrBandwidth(relay.advertised_bandwidth) : null;
                    // the filter here is a temporary fix, and can be removed again later if you notice that #25241 is fixed
                    relay.effective_family = relay.effective_family ? relay.effective_family.filter(function(x){return x !== relay.fingerprint}) : null;
                    relay.alleged_family = relay.alleged_family ? relay.alleged_family : null;
                    if (relay.is_bridge) {
                        var new_addresses = [];
                        _.each(relay.or_addresses, function(or_addr) {
                            var addr = or_addr[0] == '[' ? "IPv6" : "IPv4";
                            new_addresses.push(addr);
                        });
                        relay.or_addresses = new_addresses;
                    }
                    relay.or_address = relay.or_addresses ? relay.or_addresses[0].split(":")[0] : null;
                    relay.unreachable_or_addresses = relay.unreachable_or_addresses ? relay.unreachable_or_addresses : [];
                    relay.or_v6_addresses = $.grep(relay.or_addresses, function(n, i) { return n.indexOf("[") == 0; });
                    relay.or_v6_address = (relay.or_v6_addresses.length > 0) ? relay.or_v6_addresses[0].split("]")[0].replace(/\[/, "") : null;
                    relay.unreachable_or_v4_addresses = $.grep(relay.unreachable_or_addresses, function(n, i) { return n.indexOf(".") != -1; });
                    relay.unreachable_or_v6_addresses = $.grep(relay.unreachable_or_addresses, function(n, i) { return n.indexOf("[") == 0; });
                    relay.or_port = relay.or_addresses ? relay.or_addresses[0].split(":")[1] : 0;
                    relay.dir_port = relay.dir_address ? relay.dir_address.split(":")[1] : 0;
                    relay.exit_addresses = relay.exit_addresses ? relay.exit_addresses : null;
                    relay.verified_host_names = relay.verified_host_names ? relay.verified_host_names : null;
                    relay.unverified_host_names = relay.unverified_host_names ? relay.unverified_host_names : null;
                    relay.country = relay.country ? relay.country.toLowerCase() : null;
                    relay.countryname = relay.country ? CountryCodes[relay.country] : null;
                    relay.age = relay.first_seen ? model.parsedate(relay.first_seen).hrfull : null;
                    relay.new_relay = relay.first_seen ? model.is_new(relay.first_seen) : null;
                    relay.uptime = relay.last_restarted ? model.parsedate(relay.last_restarted) : null;
                    relay.uptime_hr = relay.last_restarted ? relay.uptime.hr : null;
                    relay.uptime_hrfull = relay.last_restarted ? relay.uptime.hrfull : null;
                    relay.uptime = relay.last_restarted ? relay.uptime.millisecs : null;
                    relay.last_restarted = relay.last_restarted ? relay.last_restarted : null;
                    relay.downtime = relay.last_seen ? model.parsedate(relay.last_seen).hrfull : null;
                    relay.as = relay.as ? relay.as : null;
                    relay.as_name = relay.as_name ? relay.as_name : null;
                    relay.transports = relay.transports ? relay.transports : null;
                    relay.bridgedb_distributor = relay.bridgedb_distributor ? relay.bridgedb_distributor : null;
                    relay.fingerprint = relay.hashed_fingerprint ? relay.hashed_fingerprint : relay.fingerprint;
                    model.set({badexit: false});
                    relay.flags = model.parseflags(relay.flags, relay.is_bridge);

                    relay.version_consistent = relay.version == relay.platform.split(" ")[1];
                    if (relay.version_consistent) {
                        relay.version_status = relay.version_status ? relay.version_status : "recommended";
                    } else {
                        relay.version_status = "recommended";
                    }

                    /* Synthetic Additional Flags */
                    var additional_flags = []
                    if (!((typeof relay.recommended_version !== 'undefined') ? relay.recommended_version : true) && relay.version_consistent) additional_flags.push("Not Recommended");
                    if (relay.version_status === 'obsolete') additional_flags.push("Obsolete");
                    if (relay.version_status === 'experimental') additional_flags.push("Experimental");
                    if (!((typeof relay.measured !== 'undefined') ? relay.measured : true)) additional_flags.push("Unmeasured");
                    if (((typeof relay.hibernating !== 'undefined') ? relay.hibernating : false)) additional_flags.push("Hibernating");
                    if (IsFallbackDir(relay.fingerprint)) additional_flags.push("FallbackDir");
                    if (relay.or_v6_addresses.length > 0) additional_flags.push("ReachableIPv6");
                    if (relay.unreachable_or_v4_addresses.length > 0) additional_flags.push("UnreachableIPv4");
                    if (relay.unreachable_or_v6_addresses.length > 0) additional_flags.push("UnreachableIPv6");
                    if (relay.exit_policy_v6_summary !== null) additional_flags.push("IPv6 Exit");

                    relay.additional_flags = model.parseadditionalflags(additional_flags);

                    model.set(relay, options);

        },
        lookup: function(options) {
            var success = options.success;
            var error = options.error;
            var model = this;
            if (model.relay) {
                var relay = model.relay;
                model.processRelay(options, model, relay);
                success(model, relay);
            } else {
                var xhr = $.getJSON(this.baseurl+'/details?lookup='+this.fingerprint, function(data) {
                    checkIfDataIsUpToDate(xhr.getResponseHeader("Last-Modified"));
                    var relay = null;
                    if (data.relays.length >= 1) {
                        relay = data.relays[0];
                        relay.is_bridge = false;
                    } else if (data.bridges.length >= 1) {
                        relay = data.bridges[0];
                        relay.is_bridge = true;
                    }
                    if (relay) {
                        relay.onionooVersion = data.version;
                        relay.buildRevision = data.build_revision;
                        relay.bridgesPublished = data.bridges_published;
                        relay.relaysPublished = data.relays_published;
                        model.processRelay(options, model, relay);
                        success(model, relay);
                    } else {
                        error(model)
                    }
                }).fail(function() {
                    error();
                });
            }
        }

	});

	return relayModel;
});
