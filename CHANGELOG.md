# Changes in version 1.?.? - 2019-1?-??

 * Medium changes
   - Improve runtime performance of the hidserv module by storing
     extrapolated statistics even if computed network fractions are
     zero, to avoid re-processing these statistics over and over.

 * Minor changes
   - Make Jetty host configurable.
   - Configure a base URL in order to turn ExoneraTor's permanent
     links into https:// links.
   - Set default locale `US` at the beginning of the execution.
   - Set default time zone `UTC` at the beginning of the execution.


# Changes in version 1.3.0 - 2019-11-09

 * Medium changes
   - Start downloading and processing votes.
   - Add Apache Commons Math 3.6.1 as dependency.
   - Extend ipv6servers module to generate servers part of legacy
     module.
   - Use Ivy for resolving external dependencies rather than relying
     on files found in Debian stable packages. Requires installing Ivy
     (using `apt-get install ivy`, `brew install ivy`, or similar) and
     running `ant resolve` (or `ant -lib /usr/share/java resolve`).
     Retrieved files are then copied to the `lib/` directory, except
     for dependencies on other metrics libraries that still need to be
     copied to the `lib/` directory manually. Current dependency
     versions resolved by Ivy are the same as in Debian stretch with
     few exceptions.
   - Remove Cobertura from the build process.
   - Update PostgreSQL JDBC driver version to 42.2.5.
   - Update to metrics-lib 2.9.1 and ExoneraTor 4.2.0.


# Changes in version 1.2.0 - 2018-08-25

 * Medium changes
   - Add ExoneraTor 4.0.0 thin jar as dependency.


# Changes in version 1.1.0 - 2018-05-29

 * Medium changes
   - Replace Gson with Jackson.

 * Minor changes
   - Avoid sending an error after a (partial) response.


# Changes in version 1.0.3 - 2017-12-20

 * Major changes
   - Use an embedded Jetty.
   - Use metrics-base as build environment.
   - Add metrics timeline events underneath graphs.
   - Replace broken SVGs with higher-resolution PNGs.


# Changes in version 1.0.2 - 2017-10-04

 * Minor changes
   - Update news.json to version 147 of doc/MetricsTimeline.


# Changes in version 1.0.1 - 2017-09-25

 * Minor changes
   - Update link to old user number estimates.


# Changes in version 1.0.0 - 2017-09-19

 * Major changes
   - This is the initial release after almost eight years of
     development.

