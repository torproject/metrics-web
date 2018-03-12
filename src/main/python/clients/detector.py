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

days = ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"]

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
      mu, signma = norm.fit(vals)
      dists += [(mu, signma)]
      maxx += [norm.ppf(0.9999, mu, signma)]
      minx += [norm.ppf(1 - 0.9999, mu, signma)]
  ## print minx[-1], maxx[-1]
  return minx, maxx

# Makes pretty plots
def raw_plot(series, minc, maxc, labels, xtitle):
    assert len(xtitle) == 3
    fname, stitle, slegend = xtitle

    font = {'family' : 'Bitstream Vera Sans',
        'weight' : 'normal',
        'size'   : 8}
    matplotlib.rc('font', **font)

    ylim( (-max(series)*0.1, max(series)*1.1) )
    plot(labels, series, linewidth=1.0, label="Users")

    wherefill = []
    for mm,mx in zip(minc, maxc):
      wherefill += [not (mm == None and mx == None)]
      assert mm < mx or (mm == None and mx == None)

    fill_between(labels, minc, maxc, where=wherefill, color="gray", label="Prediction")

    vdown = []
    vup = []
    for i,v in enumerate(series):
      if minc[i] != None and v < minc[i]:
        vdown += [v]
        vup += [None]
      elif maxc[i] != None and v > maxc[i]:
        vdown += [None]
        vup += [v]
      else:
        vup += [None]
        vdown += [None]

    plot(labels, vdown, 'o', ms=10, lw=2, alpha=0.5, mfc='orange', label="Downturns")
    plot(labels, vup, 'o', ms=10, lw=2, alpha=0.5, mfc='green', label="Upturns")

    legend(loc=2)

    xlabel('Time (days)')
    ylabel('Users')
    title(stitle)
    grid(True)
    F = gcf()

    F.set_size_inches(10,5)
    F.savefig(fname,  format="png", dpi = (150))
    close()

def absolute_plot(series, minc, maxc, labels,INTERVAL, xtitle):
  in_minc = []
  in_maxc = []
  for i, v in enumerate(series):
    if i > 0 and i - INTERVAL >= 0 and series[i] != None and series[i-INTERVAL] != None and series[i-INTERVAL] != 0 and minc[i]!= None and maxc[i]!= None:
      in_minc += [minc[i] * poisson.ppf(1-0.9999, series[i-INTERVAL])]
      in_maxc += [maxc[i] * poisson.ppf(0.9999, series[i-INTERVAL])]
      if not in_minc[-1] < in_maxc[-1]:
        print in_minc[-1], in_maxc[-1], series[i-INTERVAL], minc[i], maxc[i]
      assert in_minc[-1] < in_maxc[-1]
    else:
      in_minc += [None]
      in_maxc += [None]
  raw_plot(series, in_minc, in_maxc, labels, xtitle)

"""Return the number of downscores and upscores of a time series
'series', given tendencies 'minc' and 'maxc' for the time interval
'INTERVAL'.

If 'scoring_interval' is specifed we only consider upscore/downscore
that happened in the latest 'scoring_interval' days.
"""
def censor_score(series, minc, maxc, INTERVAL, scoring_interval=None):
  upscore = 0
  downscore = 0

  if scoring_interval is None:
    scoring_interval = len(series)
  assert(len(series) >= scoring_interval)

  for i, v in enumerate(series):
    if i > 0 and i - INTERVAL >= 0 and series[i] != None and series[i-INTERVAL] != None and series[i-INTERVAL] != 0 and minc[i]!= None and maxc[i]!= None:
      in_minc = minc[i] * poisson.ppf(1-0.9999, series[i-INTERVAL])
      in_maxc = maxc[i] * poisson.ppf(0.9999, series[i-INTERVAL])
      if (i >= (len(series) - scoring_interval)):
        downscore += 1 if minc[i] != None and v < in_minc else 0
        upscore += 1 if maxc[i] != None and v > in_maxc else 0

  return downscore, upscore

def plot_target(tss, TARGET, xtitle, minx, maxx, DAYS=365, INTERV = 7):
  ctarget = tss.get_country_series(TARGET)
  c = n_day_rel(ctarget, INTERV)
  absolute_plot(ctarget[-DAYS:], minx[-DAYS:], maxx[-DAYS:], tss.all_dates[-DAYS:],INTERV, xtitle = xtitle)

def write_censorship_report_prologue(report_file, dates, notification_period):
  if (notification_period == 1):
    date_str = "%s" % (dates[-1]) # no need for date range if it's just one day
  else:
    date_str = "%s to %s" % (dates[-notification_period], dates[-1])

  prologue = "=======================\n"
  prologue += "Automatic Censorship Report for %s\n" % (date_str)
  prologue += "=======================\n\n"
  report_file.write(prologue)

## Make a league table of censorship + nice graphs
def plot_all(tss, minx, maxx, INTERV, DAYS=None, rdir="img"):
  rdir = os.path.realpath(rdir)
  if not os.path.exists(rdir) or not os.path.isdir(rdir):
    print "ERROR: %s does not exist or is not a directory." % rdir
    return

  summary_file = file(os.path.join(rdir, "summary.txt"), "w")

  if DAYS == None:
    DAYS = 6*31

  s = tss.get_largest(200)
  scores = []
  for num, li in s:
    print ".",
    ds,us = censor_score(tss.get_country_series(li)[-DAYS:], minx[-DAYS:], maxx[-DAYS:], INTERV)
    # print ds, us
    scores += [(ds,num, us, li)]
  scores.sort()
  scores.reverse()
  s = "\n=======================\n"
  s+= "Report for %s to %s\n" % (tss.all_dates[-DAYS], tss.all_dates[-1])
  s+= "=======================\n"
  print s
  summary_file.write(s)
  for a,nx, b,c in scores:
    if a > 0:
      s = "%s -- down: %2d (up: %2d affected: %s)" % (c, a, b, nx)
      print s
      summary_file.write(s + "\n")
      xtitle = (os.path.join(rdir, "%03d-%s-censor.png" % (a,c)), "Tor report for %s -- down: %2d (up: %2d affected: %s)" % (c, a, b, nx),"")
      plot_target(tss, c,xtitle, minx, maxx, DAYS, INTERV)
  summary_file.close()

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

"""Return a URL that points to a graph in metrics.tpo that displays
the number of direct Tor users in country 'country_code', for a
'period'-days period.

Let's hope that the metrics.tpo URL scheme doesn't change often.
"""
def get_tor_usage_graph_url_for_cc_and_date(country_code, dates, period):
  url = "https://metrics.torproject.org/users.html?graph=userstats-relay-country&start=%s&end=%s&country=%s&events=on#userstats-relay-country\n" % \
      (dates[-period], dates[-1], country_code)
  return url

"""Write a file containing a short censorship report over the last
'notification_period' days.
"""
def write_ml_report(tss, minx, maxx, INTERV, DAYS, notification_period=None):
  if notification_period is None:
    notification_period = DAYS

  report_file = codecs.open('short_censorship_report.txt', 'w', 'utf-8')
  file_prologue_written = False

  s = tss.get_largest(None) # no restrictions, get 'em all.
  scores = []
  for num, li in s:
    ds,us = censor_score(tss.get_country_series(li)[-DAYS:], minx[-DAYS:], maxx[-DAYS:], INTERV, notification_period)
    scores += [(ds,num, us, li)]
  scores.sort()
  scores.reverse()

  for downscores,users_n,upscores,country_code in scores:
    if (downscores > 0) or (upscores > 0):
      if not file_prologue_written:
        write_censorship_report_prologue(report_file, tss.all_dates, notification_period)
        file_prologue_written = True

      if ((upscores > 0) and (downscores == 0)):
        s = "We detected an unusual spike of Tor users in %s (%d upscores, %d users):\n" % \
            (get_country_name_from_cc(country_code), upscores, users_n)
      else:
        s = "We detected %d potential censorship events in %s (users: %d, upscores: %d):\n" % \
            (downscores, get_country_name_from_cc(country_code), users_n, upscores)

      # Also give out a link for the appropriate usage graph for a 90-days period.
      s += get_tor_usage_graph_url_for_cc_and_date(country_code, tss.all_dates, 90)

      report_file.write(s + "\n")

  report_file.close()

# INTERV is the time interval to model connection rates;
# consider maximum DAYS days back.
def detect(CSV_FILE = "userstats-detector.csv",
           RANGES_FILE = "userstats-ranges.csv", GRAPH_DIR = "img",
           INTERV = 7, DAYS = 6 * 31, REPORT = True):
  tss = torstatstore(CSV_FILE)
  l = tss.get_largest_locations(50)
  minx, maxx = make_tendencies_minmax(l, INTERV)
  #plot_all(tss, minx, maxx, INTERV, DAYS, rdir=GRAPH_DIR)
  write_all(tss, minx, maxx, RANGES_FILE, INTERV)

  if REPORT:
    # Make our short report; only consider events of the last day
    write_ml_report(tss, minx, maxx, INTERV, DAYS, 1)

def main():
  detect()

if __name__ == "__main__":
    main()
