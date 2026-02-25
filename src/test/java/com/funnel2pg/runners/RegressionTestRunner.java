package com.funnel2pg.runners;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.*;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME,
        value = "pretty, html:reports/regression-report.html, json:reports/regression-report.json")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME,
        value = "com.funnel2pg.stepdefs, com.funnel2pg.hooks")
//@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "@smoke or @regression")
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "@funnel or @regression")
public class RegressionTestRunner {
    // Runs all @smoke and @regression tagged scenarios
}
