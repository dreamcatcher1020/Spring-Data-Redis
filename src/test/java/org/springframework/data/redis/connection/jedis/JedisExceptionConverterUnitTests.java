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
package org.springframework.data.redis.connection.jedis;

import static org.assertj.core.api.Assertions.*;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.exceptions.JedisAskDataException;
import redis.clients.jedis.exceptions.JedisClusterOperationException;
import redis.clients.jedis.exceptions.JedisMovedDataException;

import org.junit.jupiter.api.Test;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.ClusterRedirectException;
import org.springframework.data.redis.TooManyClusterRedirectionsException;

/**
 * @author Christoph Strobl
 */
class JedisExceptionConverterUnitTests {

	private JedisExceptionConverter converter = new JedisExceptionConverter();

	@Test // DATAREDIS-315
	void shouldConvertMovedDataException() {

		DataAccessException converted = converter
				.convert(new JedisMovedDataException("MOVED 3999 127.0.0.1:6381", new HostAndPort("127.0.0.1", 6381), 3999));

		assertThat(converted).isInstanceOf(ClusterRedirectException.class);
		assertThat(((ClusterRedirectException) converted).getSlot()).isEqualTo(3999);
		assertThat(((ClusterRedirectException) converted).getTargetHost()).isEqualTo("127.0.0.1");
		assertThat(((ClusterRedirectException) converted).getTargetPort()).isEqualTo(6381);
	}

	@Test // DATAREDIS-315
	void shouldConvertAskDataException() {

		DataAccessException converted = converter
				.convert(new JedisAskDataException("ASK 3999 127.0.0.1:6381", new HostAndPort("127.0.0.1", 6381), 3999));

		assertThat(converted).isInstanceOf(ClusterRedirectException.class);
		assertThat(((ClusterRedirectException) converted).getSlot()).isEqualTo(3999);
		assertThat(((ClusterRedirectException) converted).getTargetHost()).isEqualTo("127.0.0.1");
		assertThat(((ClusterRedirectException) converted).getTargetPort()).isEqualTo(6381);
	}

	@Test // DATAREDIS-315
	void shouldConvertMaxRedirectException() {

		DataAccessException converted = converter
				.convert(new JedisClusterOperationException("No more cluster attempts left"));

		assertThat(converted).isInstanceOf(TooManyClusterRedirectionsException.class);
	}
}
