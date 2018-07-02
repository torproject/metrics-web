var loadSortingExtensions = function(){
    /**
     * Atlas renders speed as MiB/s, KiB/sec, etc - which as a string does
     * not sort nicely at all. This sorting plugin fixes that by converting
     * the speeds back into a raw Bytes value, and then sorting. It is based
     * on two different versions of the datatabes file-size sorting plugin -
     * https://datatables.net/plug-ins/sorting/file-size - the latest edition
     * that has superior matching, and a very old version from 2011 that works
     * with the version of datatables that Atlas runs on.
     *  @name File size
     *  @author mhenderson
     */

    var getBytesFromString = function(xstring,ystring){
        var xmatches = xstring.match( /(\d+(?:\.\d+)?)\s*([a-z]+)/i );
        var ymatches = ystring.match( /(\d+(?:\.\d+)?)\s*([a-z]+)/i );
        var multipliers = {
            b:  1,
            bytes: 1,
            kb: 1000,
            kib: 1024,
            mb: 1000000,
            mib: 1048576,
            gb: 1000000000,
            gib: 1073741824,
            tb: 1000000000000,
            tib: 1099511627776,
            pb: 1000000000000000,
            pib: 1125899906842624
        };
    
        var x = xmatches[1];
        if (xmatches){
            x = x * multipliers[xmatches[2].toLowerCase()];
        }
        var y = ymatches[1]; 
        if (ymatches) {
            y = y * multipliers[ymatches[2].toLowerCase()];
        }

        return [x,y]
    }

    jQuery.extend( jQuery.fn.dataTableExt.oSort, {
        "file-size-asc": function ( a, b ) {
            var raw = getBytesFromString(a,b)
            var x = raw[0]
            var y = raw[1]
            return ((x < y) ? -1 : ((x > y) ?  1 : 0));
        },

        "file-size-desc": function ( a, b ) {
            var raw = getBytesFromString(a,b)
            var x = raw[0]
            var y = raw[1]          
            return ((x < y) ?  1 : ((x > y) ? -1 : 0));
        }
    } );
    /**
     * Sorts a column containing IP addresses (IPv4 and IPv6) in typical dot
     * notation / colon. This can be most useful when using DataTables for a
     * networking application, and reporting information containing IP address.
     *
     *  @name IP addresses
     *  @summary Sort IP addresses numerically
     *  @author Dominique Fournier
     *  @author Brad Wasson
     *
     *  @example
     *    $('#example').dataTable( {
     *       columnDefs: [
     *         { type: 'ip-address', targets: 0 }
     *       ]
     *    } );
     */

    jQuery.extend( jQuery.fn.dataTableExt.oSort, {
        "ip-address-pre": function ( a ) {
            var i, item;
            var m = a.split("."),
                n = a.split(":"),
                x = "",
                xa = "";

            if (m.length == 4) {
                // IPV4
                for(i = 0; i < m.length; i++) {
                    item = m[i];

                    if(item.length == 1) {
                        x += "00" + item;
                    }
                    else if(item.length == 2) {
                        x += "0" + item;
                    }
                    else {
                        x += item;
                    }
                }
            }
            else if (n.length > 0) {
                // IPV6
                var count = 0;
                for(i = 0; i < n.length; i++) {
                    item = n[i];

                    if (i > 0) {
                        xa += ":";
                    }

                    if(item.length === 0) {
                        count += 0;
                    }
                    else if(item.length == 1) {
                        xa += "000" + item;
                        count += 4;
                    }
                    else if(item.length == 2) {
                        xa += "00" + item;
                        count += 4;
                    }
                    else if(item.length == 3) {
                        xa += "0" + item;
                        count += 4;
                    }
                    else {
                        xa += item;
                        count += 4;
                    }
                }

                // Padding the ::
                n = xa.split(":");
                var paddDone = 0;

                for (i = 0; i < n.length; i++) {
                    item = n[i];

                    if (item.length === 0 && paddDone === 0) {
                        for (var padding = 0 ; padding < (32-count) ; padding++) {
                            x += "0";
                            paddDone = 1;
                        }
                    }
                    else {
                        x += item;
                    }
                }
            }

            return x;
        },

        "ip-address-asc": function ( a, b ) {
            return ((a < b) ? -1 : ((a > b) ? 1 : 0));
        },

        "ip-address-desc": function ( a, b ) {
            return ((a < b) ? 1 : ((a > b) ? -1 : 0));
        }
    } );
}