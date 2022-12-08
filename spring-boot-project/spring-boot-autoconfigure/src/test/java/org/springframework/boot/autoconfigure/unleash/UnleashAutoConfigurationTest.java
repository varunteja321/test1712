/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.autoconfigure.unleash;

import io.getunleash.CustomHttpHeadersProvider;
import io.getunleash.FakeUnleash;
import io.getunleash.Unleash;
import io.getunleash.UnleashContextProvider;
import io.getunleash.event.UnleashSubscriber;
import io.getunleash.repository.ClientFeaturesResponse;
import io.getunleash.repository.FeatureFetcher;
import io.getunleash.repository.FeatureToggleResponse;
import io.getunleash.repository.ToggleBootstrapProvider;
import io.getunleash.strategy.Strategy;
import io.getunleash.util.UnleashConfig;
import io.getunleash.util.UnleashFeatureFetcherFactory;
import io.getunleash.util.UnleashScheduledExecutor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.net.Proxy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link UnleashAutoConfiguration}.
 *
 * @author Max Schwaab
 */
@DisplayName("UnleashAutoConfiguration")
class UnleashAutoConfigurationTest {

  private static final String[] UNLEASH_REQUIRED_PROPERTIES = new String[]{
      "spring.unleash.app-name=TestApp",
      "spring.unleash.api-url=https://unleash.com",
      "spring.unleash.api-client-secret=c13n753cr37"
  };
  private static final FeatureFetcher NO_OP_FEATURE_FETCHER = () -> new ClientFeaturesResponse(FeatureToggleResponse.Status.NOT_CHANGED, 200);
  private static final UnleashFeatureFetcherFactory NO_OP_FEATURE_FETCHER_FACTORY = config -> NO_OP_FEATURE_FETCHER;
  private static final UnleashConfig CUSTOM_UNLEASH_CONFIG = UnleashConfig.builder()
      .appName("CustomTestApp")
      .unleashAPI("https://custom.unleash.com")
      .apiKey("cu570mc13n753cr37")
      .disableMetrics()
      .unleashFeatureFetcherFactory(NO_OP_FEATURE_FETCHER_FACTORY)
      .build();

  private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
      .withConfiguration(AutoConfigurations.of(UnleashAutoConfiguration.class));

  @Test
  void shouldNotCreateUnleashOnAppNameOrUnleashConfigNotPresent() {
    this.contextRunner.withPropertyValues(
            "spring.unleash.api-url=https://unleash.com",
            "spring.unleash.api-client-secret=c13n753cr37"
        )
        .run((ctx) -> assertThat(ctx).doesNotHaveBean(Unleash.class));
  }

  @Test
  void shouldNotCreateUnleashOnApiUrlOrUnleashConfigNotPresent() {
    this.contextRunner.withPropertyValues(
        "spring.unleash.app-name=TestApp",
            "spring.unleash.api-client-secret=c13n753cr37"
        )
        .run((ctx) -> assertThat(ctx).doesNotHaveBean(Unleash.class));
  }

  @Test
  void shouldNotCreateUnleashOnApiClientSecretOrUnleashConfigNotPresent() {
    this.contextRunner.withPropertyValues(
        "spring.unleash.app-name=TestApp",
            "spring.unleash.api-url=https://unleash.com"
        )
        .run((ctx) -> assertThat(ctx).doesNotHaveBean(Unleash.class));
  }

  @Test
  void shouldNotCreateUnleashOnUnleashBeanAlreadyPresent() {
    this.contextRunner.withPropertyValues(UNLEASH_REQUIRED_PROPERTIES)
        .withBean("customUnleash", Unleash.class, FakeUnleash::new)
        .run((ctx) -> assertThat(ctx).doesNotHaveBean("unleash"));
  }

  @Test
  void shouldCreateUnleashOnUnleashRequiredPropertiesPresent() {
    this.contextRunner.withPropertyValues(UNLEASH_REQUIRED_PROPERTIES)
        .run((ctx) -> assertThat(ctx).hasSingleBean(Unleash.class));
  }

  @Test
  void shouldCreateUnleashOnUnleashConfigPresent() {
    this.contextRunner.withBean(UnleashConfig.class, () -> CUSTOM_UNLEASH_CONFIG)
        .run((ctx) -> assertThat(ctx).hasSingleBean(Unleash.class));
  }

  @Test
  void shouldCreateUnleashPropertiesOnRequiredPropertiesPresent() {
    this.contextRunner.withPropertyValues(UNLEASH_REQUIRED_PROPERTIES)
        .run((ctx) -> assertThat(ctx).hasSingleBean(UnleashProperties.class));
  }

  @Test
  void shouldNotCreateUnleashPropertiesOnRequiredPropertiesNotPresent() {
    this.contextRunner.run((ctx) -> assertThat(ctx).doesNotHaveBean(UnleashProperties.class));
  }

  @Test
  void shouldCreateUnleashConfig() {
    this.contextRunner.withPropertyValues(UNLEASH_REQUIRED_PROPERTIES)
        .run((ctx) -> assertThat(ctx).hasSingleBean(UnleashConfig.class));
  }

  @Test
  void shouldNotCreateUnleashConfigOnUnleashConfigBeanAlreadyPresent() {
    this.contextRunner.withPropertyValues(UNLEASH_REQUIRED_PROPERTIES)
        .withBean("customUnleashConfig", UnleashConfig.class, () -> CUSTOM_UNLEASH_CONFIG)
        .run((ctx) -> assertThat(ctx).doesNotHaveBean("unleashConfig"));
  }

  @Test
  void shouldNotCreateUnleashConfigOnUnleashBeanAlreadyPresent() {
    this.contextRunner.withPropertyValues(UNLEASH_REQUIRED_PROPERTIES)
        .withBean("customUnleash", Unleash.class, FakeUnleash::new)
        .run((ctx) -> assertThat(ctx).doesNotHaveBean("unleashConfig").doesNotHaveBean(UnleashConfig.class));
  }

  @Test
  void shouldCreateUnleashPropertiesConfigBuilderCustomizerOnUnleashRequiredPropertiesPresent() {
    this.contextRunner.withPropertyValues(UNLEASH_REQUIRED_PROPERTIES)
        .run((ctx) -> assertThat(ctx).hasSingleBean(UnleashPropertiesConfigBuilderCustomizer.class));
  }

  @Test
  void shouldNotCreateUnleashPropertiesConfigBuilderCustomizerOnUnleashRequiredPropertiesNotPresent() {
    this.contextRunner.run((ctx) -> assertThat(ctx).doesNotHaveBean(UnleashPropertiesConfigBuilderCustomizer.class));
  }

  @Test
  void shouldCreateUnleashBeanConfigBuilderCustomizer() {
    this.contextRunner.withPropertyValues(UNLEASH_REQUIRED_PROPERTIES)
        .run((ctx) -> assertThat(ctx).hasSingleBean(UnleashBeanConfigBuilderCustomizer.class));
  }

  @Test
  void shouldUseAdditionalUnleashConfigBuilderCustomizer() {
    final UnleashConfigBuilderCustomizer unleashConfigBuilderCustomizerMock = mock(UnleashConfigBuilderCustomizer.class);
    this.contextRunner.withPropertyValues(UNLEASH_REQUIRED_PROPERTIES)
        .withBean("customUnleash", UnleashConfigBuilderCustomizer.class, () -> unleashConfigBuilderCustomizerMock)
        .run((ctx) -> verify(unleashConfigBuilderCustomizerMock).customize(any(UnleashConfig.Builder.class)));
  }

  @Test
  void shouldCustomizeUnleashContextProvider() {
    final UnleashContextProvider unleashContextProviderMock = mock(UnleashContextProvider.class);
    this.contextRunner.withPropertyValues(UNLEASH_REQUIRED_PROPERTIES)
        .withBean("customUnleashContextProvider", UnleashContextProvider.class, () -> unleashContextProviderMock)
        .run((ctx) -> assertThat(ctx).getBean(UnleashConfig.class).hasFieldOrPropertyWithValue("contextProvider", unleashContextProviderMock));
  }

  @Test
  void shouldCustomizeStrategy() {
    final Strategy fallbackStrategyMock = mock(Strategy.class);
    this.contextRunner.withPropertyValues(UNLEASH_REQUIRED_PROPERTIES)
        .withBean("customFallbackStrategy", Strategy.class, () -> fallbackStrategyMock)
        .run((ctx) -> assertThat(ctx).getBean(UnleashConfig.class).hasFieldOrPropertyWithValue("fallbackStrategy", fallbackStrategyMock));
  }

  @Test
  void shouldCustomizeCustomHttpHeadersProvider() {
    final CustomHttpHeadersProvider customHttpHeadersProviderMock = mock(CustomHttpHeadersProvider.class);
    this.contextRunner.withPropertyValues(UNLEASH_REQUIRED_PROPERTIES)
        .withBean("customHttpHeadersProvider", CustomHttpHeadersProvider.class, () -> customHttpHeadersProviderMock)
        .run((ctx) -> assertThat(ctx).getBean(UnleashConfig.class).hasFieldOrPropertyWithValue("customHttpHeadersProvider", customHttpHeadersProviderMock));
  }

  @Test
  void shouldCustomizeProxy() {
    final Proxy customProxy = Proxy.NO_PROXY;
    this.contextRunner.withPropertyValues(UNLEASH_REQUIRED_PROPERTIES)
        .withBean("customProxy", Proxy.class, () -> customProxy)
        .run((ctx) -> assertThat(ctx).getBean(UnleashConfig.class).hasFieldOrPropertyWithValue("proxy", customProxy));
  }

  @Test
  void shouldCustomizeUnleashScheduledExecutor() {
    final UnleashScheduledExecutor unleashScheduledExecutorMock = mock(UnleashScheduledExecutor.class);
    this.contextRunner.withPropertyValues(UNLEASH_REQUIRED_PROPERTIES)
        .withBean("customUnleashScheduledExecutor", UnleashScheduledExecutor.class, () -> unleashScheduledExecutorMock)
        .run((ctx) -> assertThat(ctx).getBean(UnleashConfig.class).hasFieldOrPropertyWithValue("unleashScheduledExecutor", unleashScheduledExecutorMock));
  }

  @Test
  void shouldCustomizeUnleashSubscriber() {
    final UnleashSubscriber unleashSubscriberMock = mock(UnleashSubscriber.class);
    this.contextRunner.withPropertyValues(UNLEASH_REQUIRED_PROPERTIES)
        .withBean("customUnleashSubscriber", UnleashSubscriber.class, () -> unleashSubscriberMock)
        .run((ctx) -> assertThat(ctx).getBean(UnleashConfig.class).hasFieldOrPropertyWithValue("unleashSubscriber", unleashSubscriberMock));
  }

  @Test
  void shouldCustomizeToggleBootstrapProvider() {
    final ToggleBootstrapProvider toggleBootstrapProviderMock = mock(ToggleBootstrapProvider.class);
    this.contextRunner.withPropertyValues(UNLEASH_REQUIRED_PROPERTIES)
        .withBean("customToggleBootstrapProvider", ToggleBootstrapProvider.class, () -> toggleBootstrapProviderMock)
        .run((ctx) -> assertThat(ctx).getBean(UnleashConfig.class).hasFieldOrPropertyWithValue("toggleBootstrapProvider", toggleBootstrapProviderMock));
  }

}