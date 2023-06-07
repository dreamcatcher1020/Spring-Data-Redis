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

import java.util.Arrays;

import org.junit.jupiter.api.Test;

/**
 * @author Christoph Strobl
 */
class LettuceReactiveClusterZSetCommandsIntegrationTests extends LettuceReactiveClusterTestSupport {

	@Test // DATAREDIS-525
	void zUnionStoreShouldWorkWhenAllKeysMapToSameSlot() {

		nativeCommands.zadd(SAME_SLOT_KEY_1, 1D, VALUE_1);
		nativeCommands.zadd(SAME_SLOT_KEY_1, 2D, VALUE_2);
		nativeCommands.zadd(SAME_SLOT_KEY_2, 1D, VALUE_1);
		nativeCommands.zadd(SAME_SLOT_KEY_2, 2D, VALUE_2);
		nativeCommands.zadd(SAME_SLOT_KEY_2, 3D, VALUE_3);

		assertThat(connection.zSetCommands().zUnionStore(SAME_SLOT_KEY_3_BBUFFER,
				Arrays.asList(SAME_SLOT_KEY_1_BBUFFER, SAME_SLOT_KEY_2_BBUFFER), Arrays.asList(2D, 3D)).block()).isEqualTo(3L);

	}

	@Test // DATAREDIS-525
	void zInterStoreShouldWorkCorrectlyWhenKeysMapToSameSlot() {

		nativeCommands.zadd(SAME_SLOT_KEY_1, 1D, VALUE_1);
		nativeCommands.zadd(SAME_SLOT_KEY_1, 2D, VALUE_2);
		nativeCommands.zadd(SAME_SLOT_KEY_2, 1D, VALUE_1);
		nativeCommands.zadd(SAME_SLOT_KEY_2, 2D, VALUE_2);
		nativeCommands.zadd(SAME_SLOT_KEY_2, 3D, VALUE_3);

		assertThat(connection.zSetCommands().zInterStore(SAME_SLOT_KEY_3_BBUFFER,
				Arrays.asList(SAME_SLOT_KEY_1_BBUFFER, SAME_SLOT_KEY_2_BBUFFER), Arrays.asList(2D, 3D)).block()).isEqualTo(2L);
	}
}
