package com.segment.analytics.android.integrations.optimizelyx;

import com.optimizely.ab.android.sdk.OptimizelyClient;
import com.optimizely.ab.config.Experiment;
import com.optimizely.ab.config.LiveVariableUsageInstance;
import com.optimizely.ab.config.TrafficAllocation;
import com.optimizely.ab.config.Variation;
import com.segment.analytics.Analytics;
import com.segment.analytics.Properties;
import com.segment.analytics.Traits;
import com.segment.analytics.ValueMap;
import com.segment.analytics.core.tests.BuildConfig;
import com.segment.analytics.integrations.Logger;
import com.segment.analytics.test.TrackPayloadBuilder;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.segment.analytics.Analytics.LogLevel.VERBOSE;
import static com.segment.analytics.android.integrations.optimizelyx.OptimizelyXIntegration.options;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
@PowerMockIgnore({ "org.mockito.*", "org.roboelectric.*", "android.*" })
@PrepareForTest({ OptimizelyClient.class })
public class OptimizelyXTest {

  @Rule public PowerMockRule rule = new PowerMockRule();
  @Mock Analytics analytics;
  private OptimizelyXIntegration integration;
  private OptimizelyClient client;

  @Before public void setUp() {
    initMocks(this);
    PowerMockito.mock(OptimizelyClient.class);

    client = mock(OptimizelyClient.class);
    integration = new OptimizelyXIntegration(analytics, client, new ValueMap().putValue("trackKnownUsers", false), Logger.with(VERBOSE));
  }

  @Test public void track() {
    Properties properties = new Properties();
    Traits traits = new Traits()
            .putValue("userId", "123")
            .putValue("anonymousId", "456");
    integration.track(new TrackPayloadBuilder().properties(properties).traits(traits).event("event").build());

    verify(client).track("event", "456", properties.toStringMap());
  }

  @Test public void trackKnownUsers() {
    integration.trackKnownUsers = true;

    Properties properties = new Properties();
    Traits traits = new Traits()
            .putValue("userId", "123");
    integration.track(new TrackPayloadBuilder().properties(properties).traits(traits).event("event").build());

    verify(client).track("event", "123", properties.toStringMap());
  }

  @Test public void onExperimentActivated() {
    String id = "123";
    String experimentKey = "experiment_key";
    List<String> audienceIds = new ArrayList<>();
    List<Variation> variations = new ArrayList<>();
    Map<String, String> userIdToVariationKeyMap = new HashMap<>();
    List<TrafficAllocation> trafficAllocation = new ArrayList<>();
    String groupId = "123";

    String variationKey = "variation_key";
    List<LiveVariableUsageInstance> liveVariableUsageInstancess = new ArrayList<>();

    Experiment experiment = new Experiment(id, experimentKey, null, null, audienceIds, variations, userIdToVariationKeyMap, trafficAllocation, groupId);
    String userId = "123";
    Map<String, String> attributes = new HashMap<>();
    attributes.put("name", "brennan");
    Variation variation = new Variation(id, variationKey, liveVariableUsageInstancess);

    integration.listener.onExperimentActivated(experiment, userId, attributes, variation);

    Properties properties = new Properties()
            .putValue("experimentId", "123")
            .putValue("experimentName", "experiment_key")
            .putValue("variationId", "123")
            .putValue("variationName", "variation_key");
    verify(analytics).track("Experiment Viewed", properties, options);
  }

  @Test public void reset() {
    integration.reset();
    verify(client).removeNotificationListener(integration.listener);
  }
}
