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

import reactor.test.StepVerifier;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.Test;

import org.springframework.data.redis.connection.ReactiveClusterCommands;
import org.springframework.data.redis.connection.RedisClusterNode;

/**
 * Integration tests for {@link LettuceReactiveRedisClusterConnection} via {@link ReactiveClusterCommands}.
 * <p>
 * Some assertions check against node 1 and node 4 (ports 7379/7382) as sometimes a failover happens and node 4 becomes
 * the master node.
 *
 * @author Mark Paluch
 * @author Christoph Strobl
 */
class LettuceReactiveClusterCommandsIntegrationTests extends LettuceReactiveClusterTestSupport {

	@Test // DATAREDIS-1150
	void pingShouldReturnPong() {
		connection.ping().as(StepVerifier::create).expectNext("PONG").verifyComplete();
	}

	@Test // DATAREDIS-1150
	void pingShouldReturnPongForServers() {
		connection.clusterGetNodes().flatMap(connection::ping).as(StepVerifier::create)
				.expectNext("PONG", "PONG", "PONG", "PONG").verifyComplete();
	}

	@Test // DATAREDIS-1150
	void clusterGetNodesShouldReturnNodes() {

		connection.clusterGetNodes().collectList() //
				.as(StepVerifier::create) //
				.consumeNextWith(actual -> {

					assertThat(actual).hasSizeGreaterThan(3);
				}).verifyComplete();
	}

	@Test // DATAREDIS-1150
	void clusterGetReplicasShouldReturnNodes() {

		connection.clusterGetNodes().filter(RedisClusterNode::isMaster)
				.filter(node -> (node.getPort() == 7379 || node.getPort() == 7382))
				.flatMap(it -> connection.clusterGetReplicas(it)) //
				.collectList() //
				.as(StepVerifier::create) //
				.consumeNextWith(actual -> {

					assertThat(actual).hasSize(1);
				}).verifyComplete();
	}

	@Test // DATAREDIS-1150
	void clusterGetMasterReplicaMapShouldReportTopology() {

		connection.clusterGetMasterReplicaMap() //
				.as(StepVerifier::create) //
				.consumeNextWith(actual -> {

					assertThat(actual).hasSize(3);
				}).verifyComplete();
	}

	@Test // DATAREDIS-1150
	void clusterGetSlotForKeyShouldResolveSlot() {

		connection.clusterGetSlotForKey(ByteBuffer.wrap("hello".getBytes())) //
				.as(StepVerifier::create) //
				.expectNext(866) //
				.verifyComplete();
	}

	@Test // DATAREDIS-1150
	void clusterGetNodeForSlotShouldReportNode() {

		connection.clusterGetNodeForSlot(866) //
				.as(StepVerifier::create) //
				.consumeNextWith(actual -> {

					assertThat(actual.getPort()).isIn(7379, 7382);
				}).verifyComplete();
	}

	@Test // DATAREDIS-1150
	void clusterGetNodeForKeyShouldReportNode() {

		connection.clusterGetNodeForKey(ByteBuffer.wrap("hello".getBytes())) //
				.as(StepVerifier::create) //
				.consumeNextWith(actual -> {

					assertThat(actual.getPort()).isIn(7379, 7382);
				}).verifyComplete();
	}

	@Test // DATAREDIS-1150
	void clusterGetClusterInfoShouldReportState() {

		connection.clusterGetClusterInfo() //
				.as(StepVerifier::create) //
				.consumeNextWith(actual -> {

					assertThat(actual.getSlotsAssigned()).isEqualTo(16384);
				}).verifyComplete();
	}
}
