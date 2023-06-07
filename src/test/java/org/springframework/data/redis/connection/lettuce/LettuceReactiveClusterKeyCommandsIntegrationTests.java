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
import static org.springframework.data.redis.connection.RedisClusterNode.*;
import static org.springframework.data.redis.connection.lettuce.LettuceReactiveCommandsTestSupport.*;

import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.data.redis.connection.RedisClusterNode;

/**
 * @author Christoph Strobl
 */
class LettuceReactiveClusterKeyCommandsIntegrationTests extends LettuceReactiveClusterTestSupport {

	private static final RedisClusterNode NODE_1 = newRedisClusterNode().listeningAt("127.0.0.1", 7379).build();

	@Test // DATAREDIS-525
	void keysShouldReturnOnlyKeysFromSelectedNode() {

		nativeCommands.set(KEY_1, VALUE_1);
		nativeCommands.set(KEY_2, VALUE_2);

		List<ByteBuffer> result = connection.keyCommands().keys(NODE_1, ByteBuffer.wrap("*".getBytes())).block();
		assertThat(result).hasSize(1);
		assertThat(result).containsExactly(KEY_1_BBUFFER);
	}

	@Test // DATAREDIS-525
	void randomkeyShouldReturnOnlyKeysFromSelectedNode() {

		nativeCommands.set(KEY_1, VALUE_1);
		nativeCommands.set(KEY_2, VALUE_2);

		Mono<ByteBuffer> randomkey = connection.keyCommands().randomKey(NODE_1);

		for (int i = 0; i < 10; i++) {
			assertThat(randomkey.block()).isEqualTo(KEY_1_BBUFFER);
		}
	}

}
