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
        value = "pretty, html:reports/validation-report.html, json:reports/validation-report.json")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME,
        value = "com.funnel2pg.stepdefs, com.funnel2pg.hooks")
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "@validation")
public class ValidationTestRunner {
    // Runs only @validation tagged scenarios (negative/edge cases)
}
