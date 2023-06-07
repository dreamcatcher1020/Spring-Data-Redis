/*
 * Copyright 2016-2022 the original author or authors.
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
package org.springframework.data.redis.repository.configuration;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.RedisKeyValueAdapter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.convert.ReferenceResolver;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.repository.Repository;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Unit tests for Redis Repository configuration.
 *
 * @author Christoph Strobl
 * @author Mark Paluch
 */
public class RedisRepositoryConfigurationUnitTests {

	static RedisTemplate<?, ?> createTemplateMock() {

		RedisTemplate<?, ?> template = mock(RedisTemplate.class);
		RedisConnectionFactory connectionFactory = mock(RedisConnectionFactory.class);
		RedisConnection connection = mock(RedisConnection.class);

		when(template.getConnectionFactory()).thenReturn(connectionFactory);
		when(connectionFactory.getConnection()).thenReturn(connection);

		return template;
	}

	@ExtendWith(SpringExtension.class)
	@DirtiesContext
	@ContextConfiguration(classes = { ContextWithCustomReferenceResolverUnitTests.Config.class })
	public static class ContextWithCustomReferenceResolverUnitTests {

		@EnableRedisRepositories(considerNestedRepositories = true,
				includeFilters = { @ComponentScan.Filter(type = FilterType.REGEX, pattern = { ".*ContextSampleRepository" }) })
		static class Config {

			@Bean
			RedisTemplate<?, ?> redisTemplate() {
				return createTemplateMock();
			}

			@Bean
			ReferenceResolver redisReferenceResolver() {
				return mock(ReferenceResolver.class);
			}

		}

		@Autowired ApplicationContext ctx;

		@Test // DATAREDIS-425
		public void shouldPickUpReferenceResolver() {

			RedisKeyValueAdapter adapter = (RedisKeyValueAdapter) ctx.getBean("redisKeyValueAdapter");

			Object referenceResolver = ReflectionTestUtils.getField(adapter.getConverter(), "referenceResolver");

			assertThat(referenceResolver).isEqualTo((ctx.getBean("redisReferenceResolver")));
			assertThat(mockingDetails(referenceResolver).isMock()).isTrue();
		}
	}

	@ExtendWith(SpringExtension.class)
	@DirtiesContext
	@ContextConfiguration(classes = { ContextWithoutCustomizationUnitTests.Config.class })
	public static class ContextWithoutCustomizationUnitTests {

		@EnableRedisRepositories(considerNestedRepositories = true,
				includeFilters = { @ComponentScan.Filter(type = FilterType.REGEX, pattern = { ".*ContextSampleRepository" }) })
		static class Config {

			@Bean
			RedisTemplate<?, ?> redisTemplate() {
				return createTemplateMock();
			}
		}

		@Autowired ApplicationContext ctx;

		@Test // DATAREDIS-425
		public void shouldInitWithDefaults() {
			assertThat(ctx.getBean(ContextSampleRepository.class)).isNotNull();

		}

		@Test // DATAREDIS-425
		public void shouldRegisterDefaultBeans() {

			assertThat(ctx.getBean(ContextSampleRepository.class)).isNotNull();
			assertThat(ctx.getBean("redisKeyValueAdapter")).isNotNull();
			assertThat(ctx.getBean("redisCustomConversions")).isNotNull();
			assertThat(ctx.getBean("redisReferenceResolver")).isNotNull();
		}
	}

	@ExtendWith(SpringExtension.class)
	@DirtiesContext
	@ContextConfiguration(classes = { WithMessageListenerConfigurationUnitTests.Config.class })
	public static class WithMessageListenerConfigurationUnitTests {

		@EnableRedisRepositories(considerNestedRepositories = true,
				includeFilters = { @ComponentScan.Filter(type = FilterType.REGEX, pattern = { ".*ContextSampleRepository" }) },
				keyspaceNotificationsConfigParameter = "", messageListenerContainerRef = "myContainer")
		static class Config {

			@Bean
			RedisMessageListenerContainer myContainer() {
				return mock(RedisMessageListenerContainer.class);
			}

			@Bean
			RedisTemplate<?, ?> redisTemplate() {
				return createTemplateMock();
			}
		}

		@Autowired ApplicationContext ctx;

		@Test // DATAREDIS-425
		public void shouldConfigureMessageListenerContainer() {

			RedisKeyValueAdapter adapter = ctx.getBean("redisKeyValueAdapter", RedisKeyValueAdapter.class);
			Object messageListenerContainer = ReflectionTestUtils.getField(adapter, "messageListenerContainer");

			assertThat(Mockito.mockingDetails(messageListenerContainer).isMock()).isTrue();
		}
	}

	@RedisHash
	static class Sample {
		String id;
	}

	interface ContextSampleRepository extends Repository<Sample, Long> {}
}
