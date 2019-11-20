package org.torproject.metrics.stats.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class Main {

  private static final Logger log = LoggerFactory.getLogger(Main.class);

  private static final String baseDir = System.getProperty("metrics.basedir",
      "/srv/metrics.torproject.org/metrics");

  public static final File modulesDir = new File(baseDir, "work/modules");

  public static final File descriptorsDir = new File(baseDir, "work/shared/in");

  private static final File statsDir = new File(baseDir, "shared/stats");

  /** Start the metrics update run. */
  public static void main(String[] args) {

    log.info("Starting metrics update run.");

    File[] outputDirs = new File[] { modulesDir, statsDir };
    for (File outputDir : outputDirs) {
      if (outputDir.exists()) {
        continue;
      }
      if (outputDir.mkdirs()) {
        log.info("Successfully created module base directory {} and any "
              + "nonexistent parent directories.",
            outputDir.getAbsolutePath());
      } else {
        log.error("Unable to create module base directory {} and any "
              + "nonexistent parent directories. Exiting.",
            outputDir.getAbsolutePath());
        return;
      }
    }

    Class<?>[] modules = new Class<?>[] {
        org.torproject.metrics.stats.collectdescs.Main.class,
        org.torproject.metrics.stats.connbidirect.Main.class,
        org.torproject.metrics.stats.onionperf.Main.class,
        org.torproject.metrics.stats.bwhist.Main.class,
        org.torproject.metrics.stats.advbwdist.Main.class,
        org.torproject.metrics.stats.hidserv.Main.class,
        org.torproject.metrics.stats.clients.Main.class,
        org.torproject.metrics.stats.servers.Main.class,
        org.torproject.metrics.stats.webstats.Main.class,
        org.torproject.metrics.stats.totalcw.Main.class
    };

    for (Class<?> module : modules) {
      try {
        log.info("Starting {} module.", module.getName());
        module.getDeclaredMethod("main", String[].class)
            .invoke(null, (Object) args);
        log.info("Completed {} module.", module.getName());
      } catch (NoSuchMethodException | IllegalAccessException
          | InvocationTargetException e) {
        log.warn("Caught an exception when invoking the main method of the {} "
            + "module. Moving on to the next module, if available.",
            module.getName(), e);
      }
    }

    log.info("Making module data available.");
    File[] moduleStatsDirs = new File[] {
        new File(modulesDir, "connbidirect/stats"),
        new File(modulesDir, "onionperf/stats"),
        new File(modulesDir, "bwhist/stats"),
        new File(modulesDir, "advbwdist/stats/advbwdist.csv"),
        new File(modulesDir, "hidserv/stats"),
        new File(modulesDir, "clients/stats/clients.csv"),
        new File(modulesDir, "clients/stats/userstats-combined.csv"),
        new File(modulesDir, "servers/stats"),
        new File(modulesDir, "webstats/stats"),
        new File(modulesDir, "totalcw/stats")
    };
    List<String> copiedFiles = new ArrayList<>();
    for (File moduleStatsDir : moduleStatsDirs) {
      if (!moduleStatsDir.exists()) {
        log.warn("Skipping nonexistent module stats dir {}.", moduleStatsDir);
        continue;
      }
      File[] moduleStatsFiles = moduleStatsDir.isDirectory()
          ? moduleStatsDir.listFiles() : new File[] { moduleStatsDir };
      if (null == moduleStatsFiles) {
        log.warn("Skipping nonexistent module stats dir {}.", moduleStatsDir);
        continue;
      }
      for (File statsFile : moduleStatsFiles) {
        if (!statsFile.isFile() || !statsFile.getName().endsWith(".csv")) {
          continue;
        }
        try {
          Files.copy(statsFile.toPath(),
              new File(statsDir, statsFile.getName()).toPath(),
              StandardCopyOption.REPLACE_EXISTING);
          copiedFiles.add(statsFile.getName());
        } catch (IOException e) {
          log.warn("Unable to copy module stats file {} to stats output "
              + "directory {}. Skipping.", statsFile, statsDir, e);
        }
      }
    }
    if (!copiedFiles.isEmpty()) {
      log.info("Successfully copied {} files to stats output directory: {}",
          copiedFiles.size(), copiedFiles);
    }

    log.info("Completed metrics update run.");
  }
}
