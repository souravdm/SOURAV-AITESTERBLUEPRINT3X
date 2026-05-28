package com.restfulbooker.api.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.Map;

/**
 * Reads run-config.yaml and writes testsuites/custom.xml.
 * Invoked as a pre-build step when -Prun-config is active.
 */
public class RunConfigGenerator {

    private static final Logger log = LoggerFactory.getLogger(RunConfigGenerator.class);

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        Yaml yaml = new Yaml();
        Map<String, Object> config;
        try (InputStream is = new FileInputStream("run-config.yaml")) {
            config = yaml.load(is);
        }

        String parallel     = (String) config.getOrDefault("parallel", "methods");
        int    threadCount  = (int)    config.getOrDefault("threadCount", 4);
        int    retryCount   = (int)    config.getOrDefault("retryCount", 1);

        List<String> includeGroups = (List<String>) config.getOrDefault("includeGroups", List.of());
        List<String> excludeGroups = (List<String>) config.getOrDefault("excludeGroups", List.of());
        List<String> listeners     = (List<String>) config.getOrDefault("listeners", List.of());

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<!DOCTYPE suite SYSTEM \"http://testng.org/testng-1.0.dtd\">\n");
        sb.append(String.format("<suite name=\"Custom Suite\" parallel=\"%s\" thread-count=\"%d\" verbose=\"1\">\n",
                parallel, threadCount));

        // listeners
        if (!listeners.isEmpty()) {
            sb.append("  <listeners>\n");
            listeners.forEach(l -> sb.append("    <listener class-name=\"").append(l).append("\"/>\n"));
            sb.append("  </listeners>\n");
        }

        sb.append("  <test name=\"Custom Run\">\n");

        // groups
        if (!includeGroups.isEmpty() || !excludeGroups.isEmpty()) {
            sb.append("    <groups>\n      <run>\n");
            includeGroups.forEach(g -> sb.append("        <include name=\"").append(g).append("\"/>\n"));
            excludeGroups.forEach(g -> sb.append("        <exclude name=\"").append(g).append("\"/>\n"));
            sb.append("      </run>\n    </groups>\n");
        }

        // all test packages
        sb.append("    <packages>\n");
        sb.append("      <package name=\"com.restfulbooker.api.tests\"/>\n");
        sb.append("    </packages>\n");
        sb.append("  </test>\n</suite>\n");

        Path out = Paths.get("testsuites/custom.xml");
        Files.createDirectories(out.getParent());
        Files.writeString(out, sb.toString());
        log.info("Generated {}", out.toAbsolutePath());
    }
}
