/*
 * Copyright 2015-2022 the original author or authors.
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
package org.springframework.data.redis.core.mapping;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.data.redis.core.convert.KeyspaceConfiguration;
import org.springframework.data.redis.core.convert.KeyspaceConfiguration.KeyspaceSettings;
import org.springframework.data.redis.core.mapping.RedisMappingContext.ConfigAwareKeySpaceResolver;

/**
 * @author Christoph Strobl
 */
class ConfigAwareKeySpaceResolverUnitTests {

	static final String CUSTOM_KEYSPACE = "car'a'carn";
	private KeyspaceConfiguration config = new KeyspaceConfiguration();
	private ConfigAwareKeySpaceResolver resolver;

	@BeforeEach
	void setUp() {
		this.resolver = new ConfigAwareKeySpaceResolver(config);
	}

	@Test // DATAREDIS-425
	void resolveShouldThrowExceptionWhenTypeIsNull() {
		assertThatIllegalArgumentException().isThrownBy(() -> resolver.resolveKeySpace(null));
	}

	@Test // DATAREDIS-425
	void resolveShouldUseClassNameAsDefaultKeyspace() {
		assertThat(resolver.resolveKeySpace(TypeWithoutAnySettings.class))
				.isEqualTo(TypeWithoutAnySettings.class.getName());
	}

	@Test // DATAREDIS-425
	void resolveShouldFavorConfiguredNameOverClassName() {

		config.addKeyspaceSettings(new KeyspaceSettings(TypeWithoutAnySettings.class, "ji'e'toh"));
		assertThat(resolver.resolveKeySpace(TypeWithoutAnySettings.class)).isEqualTo("ji'e'toh");
	}

	private static class TypeWithoutAnySettings {

	}

}
