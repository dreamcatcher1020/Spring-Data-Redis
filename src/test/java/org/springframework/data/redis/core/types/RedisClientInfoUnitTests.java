/*
 * Copyright 2014-2022 the original author or authors.
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
package org.springframework.data.redis.core.types;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.data.redis.core.types.RedisClientInfo.RedisClientInfoBuilder;

/**
 * @author Christoph Strobl
 */
class RedisClientInfoUnitTests {

	private final String SOURCE_WITH_PLACEHOLDER = "addr=127.0.0.1:57013#fd=6#name=client-1#age=16#idle=0#flags=N#db=0#sub=0#psub=0#multi=-1#qbuf=0#qbuf-free=32768#obl=0#oll=0#omem=0#events=r#cmd=client";
	private final String SINGLE_LINE = SOURCE_WITH_PLACEHOLDER.replace('#', ' ');
	private final String[] VALUES = SOURCE_WITH_PLACEHOLDER.split("#");

	private RedisClientInfo info;

	@BeforeEach
	void setUp() {
		info = RedisClientInfoBuilder.fromString(SINGLE_LINE);
	}

	@Test
	void testBuilderShouldReadsInfoCorrectlyFromSingleLineString() {
		assertValues(info, VALUES);
	}

	@Test
	void testGetRequiresNonNullKey() {
		assertThatIllegalArgumentException().isThrownBy(() -> info.get((String) null));
	}

	@Test
	void testGetRequiresNonBlankKey() {
		assertThatIllegalArgumentException().isThrownBy(() -> info.get(""));
	}

	@Test
	void testGetReturnsNullForPropertiesNotAvailable() {
		assertThat(info.get("foo-bar")).isEqualTo(null);
	}

	private void assertValues(RedisClientInfo info, String[] values) {
		for (String potentialValue : values) {
			if (potentialValue.contains("=")) {
				String[] keyValuePair = potentialValue.split("=");
				assertThat(info.get(keyValuePair[0])).isEqualTo(keyValuePair[1]);
			} else {
				assertThat(info.get(potentialValue)).isNotEqualTo(null);
			}
		}

	}

}
