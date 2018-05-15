##  Copyright (c) 2011 George Danezis <gdane@microsoft.com>
##
##  All rights reserved.
##
##  Redistribution and use in source and binary forms, with or without
##  modification, are permitted (subject to the limitations in the
##  disclaimer below) provided that the following conditions are met:
##
##   * Redistributions of source code must retain the above copyright
##     notice, this list of conditions and the following disclaimer.
##
##   * Redistributions in binary form must reproduce the above copyright
##     notice, this list of conditions and the following disclaimer in the
##     documentation and/or other materials provided with the
##     distribution.
##
##   * Neither the name of <Owner Organization> nor the names of its
##     contributors may be used to endorse or promote products derived
##     from this software without specific prior written permission.
##
##  NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE
##  GRANTED BY THIS LICENSE.  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT
##  HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
##  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
##  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
##  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
##  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
##  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
##  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
##  BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
##  WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
##  OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
##  IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
##
##  (Clear BSD license: http://labs.metacarta.com/license-explanation.html#license)

##  This script reads a .csv file of the number of Tor users and finds
##  anomalies that might be indicative of censorship.

# Dep: matplotlib
from pylab import *
import matplotlib

# Dep: numpy
import numpy
from numpy import mean, std

# Dep: scipy
import scipy.stats
from scipy.stats.distributions import norm
from scipy.stats.distributions import poisson

# Std lib
from datetime import date
from datetime import timedelta
import os.path

# Country code -> Country names
import country_info

# write utf8 to file
import codecs

def get_country_name_from_cc(country_code):
  if (country_code.lower() in country_info.countries):
    return country_info.countries[country_code.lower()]
  return country_code # if we didn't find the cc in our map

"""
Represents a .csv file containing information on the number of
connecting Tor users per country.

'store': Dictionary with (<country code>, <counter>) as key, and the number of users as value.
         <country code> can also be "date"...
'all_dates': List of the data intervals (with default timedelta: 1 day).
'country_codes': List of all relevant country codes.
'MAX_INDEX': Length of store, number of country codes etc.
'date_min': The oldest date found in the .csv.
'date_min': The latest date found in the .csv.
"""
class torstatstore:
  def __init__(self, file_name):
    f = file(file_name)
    country_codes = f.readline()
    country_codes = country_codes.strip().split(",")

    store = {}
    MAX_INDEX = 0
    for i, line in enumerate(f):
        MAX_INDEX += 1
        line_parsed = line.strip().split(",")
        for j, (ccode, val) in enumerate(zip(country_codes,line_parsed)):
            processed_val = None
            if ccode == "date":
                try:
                    year, month, day = int(val[:4]), int(val[5:7]), int(val[8:10])
                    processed_val = date(year, month, day)
                except Exception, e:
                    print "Parsing error (ignoring line %s):" % j
                    print "%s" % val,e
                    break

            elif val != "NA":
                processed_val = int(val)
            store[(ccode, i)] = processed_val

    # min and max
    date_min = store[("date", 0)]
    date_max = store[("date", i)]

    all_dates = []
    d = date_min
    dt = timedelta(days=1)
    while d <= date_max:
        all_dates += [d]
        d = d + dt

    # Save for later
    self.store = store
    self.all_dates = all_dates
    self.country_codes = country_codes
    self.MAX_INDEX = MAX_INDEX
    self.date_min = date_min
    self.date_max = date_max

  """Return a list representing a time series of 'ccode' with respect
  to the number of connected users.
  """
  def get_country_series(self, ccode):
    assert ccode in self.country_codes
    series = {}
    for d in self.all_dates:
        series[d] = None
    for i in range(self.MAX_INDEX):
        series[self.store[("date", i)]] = self.store[(ccode, i)]
    sx = []
    for d in self.all_dates:
        sx += [series[d]]
    return sx

  """Return an ordered list containing tuples of the form (<number of
  users>, <country code>). The list is ordered with respect to the
  number of users for each country.
  """
  def get_largest(self, number):
    exclude = set(["all", "??", "date"])
    l = [(self.store[(c, self.MAX_INDEX-1)], c) for c in self.country_codes if c not in exclude]
    l.sort()
    l.reverse()
    return l[:number]

  """Return a dictionary, with <country code> as key, and the time
  series of the country code as the value.
  """
  def get_largest_locations(self, number):
    l = self.get_largest(number)
    res = {}
    for _, ccode in l[:number]:
      res[ccode] = self.get_country_series(ccode)
    return res

"""Return a list containing lists (?) where each such list contains
the difference in users for a time delta of 'days'
"""
def n_day_rel(series, days):
  rel = []
  for i, v in enumerate(series):
    if series[i] is None:
      rel += [None]
      continue

    if i - days < 0 or series[i-days] is None or series[i-days] == 0:
      rel += [None]
    else:
      rel += [ float(series[i]) / series[i-days]]
  return rel

# Main model: computes the expected min / max range of number of users
def make_tendencies_minmax(l, INTERVAL = 1):
  lminus1 = dict([(ccode, n_day_rel(l[ccode], INTERVAL)) for ccode in l])
  c = lminus1[lminus1.keys()[0]]
  dists = []
  minx = []
  maxx = []
  for i in range(len(c)):
    vals = [lminus1[ccode][i] for ccode in lminus1.keys() if lminus1[ccode][i] != None]
    if len(vals) < 8:
      dists += [None]
      minx += [None]
      maxx += [None]
    else:
      vals.sort()
      median = vals[len(vals)/2]
      q1 = vals[len(vals)/4]
      q2 = vals[(3*len(vals))/4]
      qd = q2 - q1
      vals = [v for v in vals if median - qd*4 < v and  v < median + qd*4]
      if len(vals) < 8:
        dists += [None]
        minx += [None]
        maxx += [None]
        continue
      mu = mean(vals)
      signma = std(vals)
      dists += [(mu, signma)]
      maxx += [norm.ppf(0.9999, mu, signma)]
      minx += [norm.ppf(1 - 0.9999, mu, signma)]
  ## print minx[-1], maxx[-1]
  return minx, maxx

"""Write a CSV report on the minimum/maximum users of each country per date."""
def write_all(tss, minc, maxc, RANGES_FILE, INTERVAL=7):
  ranges_file = file(RANGES_FILE, "w")
  ranges_file.write("date,country,minusers,maxusers\n")
  exclude = set(["all", "??", "date"])
  for c in tss.country_codes:
    if c in exclude:
      continue
    series = tss.get_country_series(c)
    for i, v in enumerate(series):
      if i > 0 and i - INTERVAL >= 0 and series[i] != None and series[i-INTERVAL] != None and series[i-INTERVAL] != 0 and minc[i]!= None and maxc[i]!= None:
        minv = minc[i] * poisson.ppf(1-0.9999, series[i-INTERVAL])
        maxv = maxc[i] * poisson.ppf(0.9999, series[i-INTERVAL])
        if not minv < maxv:
          print minv, maxv, series[i-INTERVAL], minc[i], maxc[i]
        assert minv < maxv
        if minv < 0.0:
          minv = 0.0
        ranges_file.write("%s,%s,%s,%s\n" % (tss.all_dates[i], c, minv, maxv))
  ranges_file.close()

# INTERV is the time interval to model connection rates;
# consider maximum DAYS days back.
def detect(CSV_FILE = "userstats-detector.csv",
           RANGES_FILE = "userstats-ranges.csv",
           INTERV = 7, DAYS = 6 * 31):
  tss = torstatstore(CSV_FILE)
  l = tss.get_largest_locations(50)
  minx, maxx = make_tendencies_minmax(l, INTERV)
  write_all(tss, minx, maxx, RANGES_FILE, INTERV)

def main():
  detect()

if __name__ == "__main__":
    main()
