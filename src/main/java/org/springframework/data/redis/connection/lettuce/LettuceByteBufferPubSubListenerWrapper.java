/*
 * Copyright 2021-2022 the original author or authors.
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

import io.lettuce.core.pubsub.RedisPubSubListener;

import java.nio.ByteBuffer;

import org.springframework.data.redis.util.ByteUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Wrapper around {@link RedisPubSubListener} that converts {@link ByteBuffer} into {@code byte[]}.
 *
 * @author Mark Paluch
 * @since 2.6
 */
class LettuceByteBufferPubSubListenerWrapper implements RedisPubSubListener<ByteBuffer, ByteBuffer> {

	private final RedisPubSubListener<byte[], byte[]> delegate;

	LettuceByteBufferPubSubListenerWrapper(RedisPubSubListener<byte[], byte[]> delegate) {

		Assert.notNull(delegate, "RedisPubSubListener must not be null");

		this.delegate = delegate;
	}

	public void message(ByteBuffer channel, ByteBuffer message) {
		delegate.message(getBytes(channel), getBytes(message));
	}

	public void message(ByteBuffer pattern, ByteBuffer channel, ByteBuffer message) {
		delegate.message(getBytes(channel), getBytes(message), getBytes(pattern));
	}

	public void subscribed(ByteBuffer channel, long count) {
		delegate.subscribed(getBytes(channel), count);
	}

	public void psubscribed(ByteBuffer pattern, long count) {
		delegate.psubscribed(getBytes(pattern), count);
	}

	public void unsubscribed(ByteBuffer channel, long count) {
		delegate.unsubscribed(getBytes(channel), count);
	}

	public void punsubscribed(ByteBuffer pattern, long count) {
		delegate.punsubscribed(getBytes(pattern), count);
	}

	/**
	 * Extract a byte array from {@link ByteBuffer} without consuming it.
	 *
	 * @param byteBuffer must not be {@literal null}.
	 * @return
	 */
	private static byte[] getBytes(@Nullable ByteBuffer byteBuffer) {

		if (byteBuffer == null) {
			return new byte[0];
		}

		return ByteUtils.getBytes(byteBuffer);
	}
}
