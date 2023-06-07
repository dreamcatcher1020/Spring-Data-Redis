/*
 * Copyright 2019-2022 the original author or authors.
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
package org.springframework.data.redis.core

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Unit tests for `ReactiveSetOperationsExtensions`.
 *
 * @author Mark Paluch
 * @author Christoph Strobl
 * @author Sebastien Deleuze
 */
class ReactiveSetOperationsExtensionsUnitTests {

	@Test // DATAREDIS-937
	fun add() {

		val operations = mockk<ReactiveSetOperations<String, String>>()
		every { operations.add("foo", "bar", "baz") } returns Mono.just(1)

		runBlocking {
			assertThat(operations.addAndAwait("foo", "bar", "baz")).isEqualTo(1)
		}

		verify {
			operations.add("foo", "bar", "baz")
		}
	}

	@Test // DATAREDIS-937
	fun remove() {

		val operations = mockk<ReactiveSetOperations<String, String>>()
		every { operations.remove("foo", "bar", "baz") } returns Mono.just(1)

		runBlocking {
			assertThat(operations.removeAndAwait("foo", "bar", "baz")).isEqualTo(1)
		}

		verify {
			operations.remove("foo", "bar", "baz")
		}
	}

	@Test // DATAREDIS-937
	fun pop() {

		val operations = mockk<ReactiveSetOperations<String, String>>()
		every { operations.pop(any()) } returns Mono.just("bar")

		runBlocking {
			assertThat(operations.popAndAwait("foo")).isEqualTo("bar")
		}

		verify {
			operations.pop("foo")
		}
	}

	@Test // DATAREDIS-1033
	fun `pop as Flow`() {

		val operations = mockk<ReactiveSetOperations<String, String>>()
		every { operations.pop(any(), any()) } returns Flux.just("bar")

		runBlocking {
			assertThat(operations.popAsFlow("foo", 1).toList()).contains("bar")
		}

		verify {
			operations.pop("foo", 1)
		}
	}

	@Test // DATAREDIS-937
	fun `pop returning an empty Mono`() {

		val operations = mockk<ReactiveSetOperations<String, String>>()
		every { operations.pop(any()) } returns Mono.empty();

		runBlocking {
			assertThat(operations.popAndAwait("foo")).isNull()
		}

		verify {
			operations.pop("foo")
		}
	}

	@Test // DATAREDIS-937
	fun move() {

		val operations = mockk<ReactiveSetOperations<String, String>>()
		every { operations.move(any(), any(), any()) } returns Mono.just(true)

		runBlocking {
			assertThat(operations.moveAndAwait("foo", "from", "to")).isTrue()
		}

		verify {
			operations.move("foo", "from", "to")
		}
	}

	@Test // DATAREDIS-937
	fun size() {

		val operations = mockk<ReactiveSetOperations<String, String>>()
		every { operations.size(any()) } returns Mono.just(1)

		runBlocking {
			assertThat(operations.sizeAndAwait("foo")).isEqualTo(1)
		}

		verify {
			operations.size("foo")
		}
	}

	@Test // DATAREDIS-937
	fun isMember() {

		val operations = mockk<ReactiveSetOperations<String, String>>()
		every { operations.isMember("foo", "bar") } returns Mono.just(true)

		runBlocking {
			assertThat(operations.isMemberAndAwait("foo", "bar")).isTrue()
		}

		verify {
			operations.isMember("foo", "bar")
		}
	}

	@Test // DATAREDIS-1033
	fun intersect() {
		val operations = mockk<ReactiveSetOperations<String, String>>()
		every { operations.intersect("foo", "bar") } returns Flux.just("baz")

		runBlocking {
			assertThat(operations.intersectAsFlow("foo", "bar").toList()).contains("baz")
		}

		verify {
			operations.intersect("foo", "bar")
		}
	}

	@Test // DATAREDIS-1033
	fun `intersect with key and collection`() {
		val operations = mockk<ReactiveSetOperations<String, String>>()
		every { operations.intersect("foo", listOf("bar")) } returns Flux.just("baz")

		runBlocking {
			assertThat(operations.intersectAsFlow("foo", listOf("bar")).toList()).contains("baz")
		}

		verify {
			operations.intersect("foo", listOf("bar"))
		}
	}

	@Test // DATAREDIS-1033
	fun `intersect with collection`() {
		val operations = mockk<ReactiveSetOperations<String, String>>()
		every { operations.intersect(listOf("bar")) } returns Flux.just("baz")

		runBlocking {
			assertThat(operations.intersectAsFlow(listOf("bar")).toList()).contains("baz")
		}

		verify {
			operations.intersect(listOf("bar"))
		}
	}

	@Test // DATAREDIS-937
	fun intersectAndStore() {

		val operations = mockk<ReactiveSetOperations<String, String>>()
		every { operations.intersectAndStore("foo", "bar", "baz") } returns Mono.just(3)

		runBlocking {
			assertThat(operations.intersectAndStoreAndAwait("foo", "bar", "baz")).isEqualTo(3)
		}

		verify {
			operations.intersectAndStore("foo", "bar", "baz")
		}
	}

	@Test // DATAREDIS-937
	fun intersectAndStoreCollection() {

		val operations = mockk<ReactiveSetOperations<String, String>>()
		every { operations.intersectAndStore(listOf("foo", "bar"), "baz") } returns Mono.just(3)

		runBlocking {
			assertThat(operations.intersectAndStoreAndAwait(listOf("foo", "bar"), "baz")).isEqualTo(3)
		}

		verify {
			operations.intersectAndStore(listOf("foo", "bar"), "baz")
		}
	}

	@Test // DATAREDIS-1033
	fun union() {

		val operations = mockk<ReactiveSetOperations<String, String>>()
		every { operations.union("foo", "bar") } returns Flux.just("baz")

		runBlocking {
			assertThat(operations.unionAsFlow("foo", "bar").toList()).contains("baz")
		}

		verify {
			operations.union("foo", "bar")
		}
	}

	@Test // DATAREDIS-1033
	fun `union with key and collection`() {

		val operations = mockk<ReactiveSetOperations<String, String>>()
		every { operations.union("foo", listOf("bar")) } returns Flux.just("baz")

		runBlocking {
			assertThat(operations.unionAsFlow("foo", listOf("bar")).toList()).contains("baz")
		}

		verify {
			operations.union("foo", listOf("bar"))
		}
	}

	@Test // DATAREDIS-1033
	fun `union with collection`() {

		val operations = mockk<ReactiveSetOperations<String, String>>()
		every { operations.union(listOf("bar")) } returns Flux.just("baz")

		runBlocking {
			assertThat(operations.unionAsFlow(listOf("bar")).toList()).contains("baz")
		}

		verify {
			operations.union(listOf("bar"))
		}
	}

	@Test // DATAREDIS-937
	fun unionAndStore() {

		val operations = mockk<ReactiveSetOperations<String, String>>()
		every { operations.unionAndStore("foo", "bar", "baz") } returns Mono.just(3)

		runBlocking {
			assertThat(operations.unionAndStoreAndAwait("foo", "bar", "baz")).isEqualTo(3)
		}

		verify {
			operations.unionAndStore("foo", "bar", "baz")
		}
	}

	@Test // DATAREDIS-937
	fun unionAndStoreCollection() {

		val operations = mockk<ReactiveSetOperations<String, String>>()
		every { operations.unionAndStore(listOf("foo", "bar"), "baz") } returns Mono.just(3)

		runBlocking {
			assertThat(operations.unionAndStoreAndAwait(listOf("foo", "bar"), "baz")).isEqualTo(3)
		}

		verify {
			operations.unionAndStore(listOf("foo", "bar"), "baz")
		}
	}

	@Test // DATAREDIS-1033
	fun difference() {

		val operations = mockk<ReactiveSetOperations<String, String>>()
		every { operations.difference("foo", "bar") } returns Flux.just("baz")

		runBlocking {
			assertThat(operations.differenceAsFlow("foo", "bar").toList()).contains("baz")
		}

		verify {
			operations.difference("foo", "bar")
		}
	}

	@Test // DATAREDIS-1033
	fun `difference with key and collection`() {

		val operations = mockk<ReactiveSetOperations<String, String>>()
		every { operations.difference("foo", listOf("bar")) } returns Flux.just("baz")

		runBlocking {
			assertThat(operations.differenceAsFlow("foo", listOf("bar")).toList()).contains("baz")
		}

		verify {
			operations.difference("foo", listOf("bar"))
		}
	}

	@Test // DATAREDIS-1033
	fun `difference with collection`() {

		val operations = mockk<ReactiveSetOperations<String, String>>()
		every { operations.difference(listOf("bar")) } returns Flux.just("baz")

		runBlocking {
			assertThat(operations.differenceAsFlow(listOf("bar")).toList()).contains("baz")
		}

		verify {
			operations.difference(listOf("bar"))
		}
	}

	@Test // DATAREDIS-937
	fun differenceAndStore() {

		val operations = mockk<ReactiveSetOperations<String, String>>()
		every { operations.differenceAndStore("foo", "bar", "baz") } returns Mono.just(3)

		runBlocking {
			assertThat(operations.differenceAndStoreAndAwait("foo", "bar", "baz")).isEqualTo(3)
		}

		verify {
			operations.differenceAndStore("foo", "bar", "baz")
		}
	}

	@Test // DATAREDIS-937
	fun differenceAndStoreCollection() {

		val operations = mockk<ReactiveSetOperations<String, String>>()
		every { operations.differenceAndStore(listOf("foo", "bar"), "baz") } returns Mono.just(3)

		runBlocking {
			assertThat(operations.differenceAndStoreAndAwait(listOf("foo", "bar"), "baz")).isEqualTo(3)
		}

		verify {
			operations.differenceAndStore(listOf("foo", "bar"), "baz")
		}
	}

	@Test // DATAREDIS-1033
	fun members() {

		val operations = mockk<ReactiveSetOperations<String, String>>()
		every { operations.members("foo") } returns Flux.just("baz")

		runBlocking {
			assertThat(operations.membersAsFlow("foo").toList()).contains("baz")
		}

		verify {
			operations.members("foo")
		}
	}

	@Test // DATAREDIS-1033
	fun scan() {

		val operations =  mockk<ReactiveSetOperations<String, String>>()
		every { operations.scan(any(), any()) } returns Flux.just("bar")

		runBlocking {
			assertThat(operations.scanAsFlow("foo").toList()).contains("bar")
		}

		verify {
			operations.scan("foo", ScanOptions.NONE)
		}
	}

	@Test // DATAREDIS-937
	fun randomMember() {

		val operations = mockk<ReactiveSetOperations<String, String>>()
		every { operations.randomMember(any()) } returns Mono.just("bar")

		runBlocking {
			assertThat(operations.randomMemberAndAwait("foo")).isEqualTo("bar")
		}

		verify {
			operations.randomMember("foo")
		}
	}

	@Test // DATAREDIS-937
	fun `randomMember returning an empty Mono`() {

		val operations = mockk<ReactiveSetOperations<String, String>>()
		every { operations.randomMember(any()) } returns Mono.empty()

		runBlocking {
			assertThat(operations.randomMemberAndAwait("foo")).isNull()
		}

		verify {
			operations.randomMember("foo")
		}
	}

	@Test // DATAREDIS-1033
	fun distinctRandomMembers() {

		val operations = mockk<ReactiveSetOperations<String, String>>()
		every { operations.distinctRandomMembers(any(), any()) } returns Flux.just("bar")

		runBlocking {
			assertThat(operations.distinctRandomMembersAsFlow("foo", 1).toList()).contains("bar")
		}

		verify {
			operations.distinctRandomMembers("foo", 1)
		}
	}

	@Test // DATAREDIS-1033
	fun randomMembers() {

		val operations = mockk<ReactiveSetOperations<String, String>>()
		every { operations.randomMembers(any(), any()) } returns Flux.just("bar")

		runBlocking {
			assertThat(operations.randomMembersAsFlow("foo", 1).toList()).contains("bar")
		}

		verify {
			operations.randomMembers("foo", 1)
		}
	}

	@Test // DATAREDIS-937
	fun delete() {

		val operations = mockk<ReactiveSetOperations<String, String>>()
		every { operations.delete(any()) } returns Mono.just(true)

		runBlocking {
			assertThat(operations.deleteAndAwait("foo")).isTrue()
		}

		verify {
			operations.delete("foo")
		}
	}
}
