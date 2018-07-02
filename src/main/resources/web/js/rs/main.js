// ~ main.js ~
// Main GLClient starter
// based on work done by @jrburke
// Configure require.js shortcut aliases
require.config({
  shim : {
    "bootstrap" : { "deps" :['jquery'] }
  },
  paths: {
    jquery: '/js/jquery-3.2.1.min',
    underscore: '/js/rs/libs/underscore/underscore-min',
    backbone: '/js/rs/libs/backbone/backbone.min',
    text: '/js/rs/libs/require/text',
    datatables: '/js/rs/libs/datatables/jquery.dataTables.min',
    datatablest: '/js/rs/libs/datatables/dataTables.TorStatus',
    datatablessort: '/js/rs/libs/datatables/dataTables.Sorting',
    bootstrap: '/js/rs/libs/bootstrap/bootstrap.min',
    datatablesbs: '/js/rs/libs/datatables/dataTables.bootstrap',
    d3js: '/js/rs/libs/d3js/d3.v3.min',
    "d3-geo-projection": '/js/rs/libs/d3js/d3-geo-projection.v2.min',
    "d3-geo": '/js/rs/libs/d3js/d3-geo.v1.min',
    "d3-array": '/js/rs/libs/d3js/d3-array.v1.min',
    topojson: '/js/rs/libs/d3js/topojson.v1.min',
    jssha: '/js/rs/libs/jssha/sha1',
    templates: '/templates/rs',
    fallbackdir: '/js/rs/fallback_dir'
  }

});

require([

  // Load our app module and pass it to our definition function
  'app'

  // Some plugins have to be loaded in order due to their non AMD compliance
  // Because these scripts are not "modules" they do not pass any values to the definition function below
], function(App){
  // The "app" dependency is passed in as "App"
  // Again, the other dependencies passed in are not "AMD" therefore don't pass a parameter to this function
  App.initialize();
});
