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
package org.springframework.data.redis.connection.lettuce;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.data.redis.connection.lettuce.LettuceReactiveCommandsTestSupport.*;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import org.springframework.data.redis.connection.ReactiveListCommands;

/**
 * @author Christoph Strobl
 */
class LettuceReactiveClusterListCommandsIntegrationTests extends LettuceReactiveClusterTestSupport {

	@Test // DATAREDIS-525
	void bRPopLPushShouldWorkCorrectlyWhenAllKeysMapToSameSlot() {

		nativeCommands.rpush(SAME_SLOT_KEY_1, VALUE_1, VALUE_2, VALUE_3);
		nativeCommands.rpush(SAME_SLOT_KEY_2, VALUE_1);

		ByteBuffer result = connection.listCommands()
				.bRPopLPush(SAME_SLOT_KEY_1_BBUFFER, SAME_SLOT_KEY_2_BBUFFER, Duration.ofSeconds(1)).block();

		assertThat(result).isEqualTo(VALUE_3_BBUFFER);
		assertThat(nativeCommands.llen(SAME_SLOT_KEY_2)).isEqualTo(2L);
		assertThat(nativeCommands.lindex(SAME_SLOT_KEY_2, 0)).isEqualTo(VALUE_3);
	}

	@Test // DATAREDIS-525
	void blPopShouldReturnFirstAvailableWhenAllKeysMapToTheSameSlot() {

		nativeCommands.rpush(SAME_SLOT_KEY_1, VALUE_1, VALUE_2, VALUE_3);

		ReactiveListCommands.PopResult result = connection.listCommands()
				.blPop(Arrays.asList(SAME_SLOT_KEY_1_BBUFFER, SAME_SLOT_KEY_2_BBUFFER), Duration.ofSeconds(1L)).block();
		assertThat(result.getKey()).isEqualTo(SAME_SLOT_KEY_1_BBUFFER);
		assertThat(result.getValue()).isEqualTo(VALUE_1_BBUFFER);
	}

}
