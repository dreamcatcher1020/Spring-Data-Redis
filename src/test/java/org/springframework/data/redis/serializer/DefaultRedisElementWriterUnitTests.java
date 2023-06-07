/*
 * Copyright 2017-2022 the original author or authors.
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
package org.springframework.data.redis.serializer;

import static org.assertj.core.api.Assertions.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link DefaultRedisElementWriter}.
 *
 * @author Mark Paluch
 */
class DefaultRedisElementWriterUnitTests {

	@Test // DATAREDIS-602
	void shouldSerializeInputCorrectly() {

		String input = "123ü?™";
		byte[] bytes = input.getBytes(StandardCharsets.UTF_8);

		DefaultRedisElementWriter<String> writer = new DefaultRedisElementWriter<>(
				new StringRedisSerializer(StandardCharsets.UTF_8));

		ByteBuffer result = writer.write(input);

		assertThat(result.array()).isEqualTo(bytes);
	}

	@Test // DATAREDIS-602
	void shouldWrapByteArrayForAbsentSerializer() {

		DefaultRedisElementWriter<Object> writer = new DefaultRedisElementWriter<>(null);

		byte[] input = { 1, 2, 3 };
		ByteBuffer result = writer.write(input);

		assertThat(result.array()).isEqualTo(input);
	}

	@Test // DATAREDIS-602
	void shouldPassThroughByteBufferForAbsentSerializer() {

		DefaultRedisElementWriter<Object> writer = new DefaultRedisElementWriter<>(null);

		byte[] input = { 1, 2, 3 };
		ByteBuffer result = writer.write(ByteBuffer.wrap(input));

		assertThat(result.array()).isEqualTo(input);
	}

	@Test // DATAREDIS-602
	void shouldFailForUnsupportedTypeWithAbsentSerializer() {

		DefaultRedisElementWriter<Object> writer = new DefaultRedisElementWriter<>(null);

		assertThatIllegalStateException().isThrownBy(() -> writer.write(new Object()));
	}
}
