/*
 * Copyright 2020-2022 the original author or authors.
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

import static org.mockito.Mockito.*;
import static org.springframework.data.redis.connection.lettuce.LettuceConnection.*;

import io.lettuce.core.api.StatefulRedisConnection;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link PipeliningFlushPolicy}.
 */
@ExtendWith(MockitoExtension.class)
class PipeliningFlushPolicyUnitTests {

	@Mock StatefulRedisConnection<?, ?> connection;

	@Test // DATAREDIS-1011
	void shouldFlushEachCommand() {

		PipeliningFlushPolicy policy = PipeliningFlushPolicy.flushEachCommand();

		PipeliningFlushState state = policy.newPipeline();

		state.onOpen(connection);
		state.onCommand(connection);
		state.onClose(connection);

		verifyNoInteractions(connection);
	}

	@Test // DATAREDIS-1011
	void shouldFlushOnClose() {

		PipeliningFlushPolicy policy = PipeliningFlushPolicy.flushOnClose();

		PipeliningFlushState state = policy.newPipeline();

		state.onOpen(connection);

		verify(connection).setAutoFlushCommands(false);

		state.onCommand(connection);

		verifyNoMoreInteractions(connection);

		state.onClose(connection);

		verify(connection).setAutoFlushCommands(true);
		verify(connection).flushCommands();
	}

	@Test // DATAREDIS-1011
	void shouldFlushOnBuffer() {

		PipeliningFlushPolicy policy = PipeliningFlushPolicy.buffered(2);

		PipeliningFlushState state = policy.newPipeline();

		state.onOpen(connection);

		verify(connection).setAutoFlushCommands(false);

		state.onCommand(connection);
		verifyNoMoreInteractions(connection);

		state.onCommand(connection);
		verify(connection).flushCommands();

		state.onClose(connection);

		verify(connection).setAutoFlushCommands(true);
		verify(connection, times(2)).flushCommands();
	}
}
