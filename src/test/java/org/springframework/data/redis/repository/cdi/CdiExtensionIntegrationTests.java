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
package org.springframework.data.redis.repository.cdi;

import static org.assertj.core.api.Assertions.*;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.se.SeContainerInitializer;
import jakarta.enterprise.inject.spi.Bean;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for Spring Data Redis CDI extension.
 *
 * @author Mark Paluch
 */
class CdiExtensionIntegrationTests {

	private static SeContainer container;

	@BeforeAll
	static void setUp() {

		container = SeContainerInitializer.newInstance() //
				.disableDiscovery() //
				.addPackages(RepositoryConsumer.class) //
				.initialize();
	}

	@AfterAll
	static void cleanUp() {
		container.close();
	}

	@Test // DATAREDIS-425, DATAREDIS-700
	@SuppressWarnings("rawtypes")
	void beanShouldBeRegistered() {

		Set<Bean<?>> beans = container.getBeanManager().getBeans(PersonRepository.class);

		assertThat(beans).hasSize(1);
		assertThat(beans.iterator().next().getScope()).isEqualTo((Class) ApplicationScoped.class);
	}

	@Test // DATAREDIS-425, DATAREDIS-700
	void saveAndFindUnqualified() {

		RepositoryConsumer repositoryConsumer = container.select(RepositoryConsumer.class).get();
		repositoryConsumer.deleteAll();

		Person person = new Person();
		person.setName("foo");
		repositoryConsumer.getUnqualifiedRepo().save(person);
		List<Person> result = repositoryConsumer.getUnqualifiedRepo().findByName("foo");

		assertThat(result).containsExactly(person);
	}

	@Test // DATAREDIS-425, DATAREDIS-700
	void saveAndFindQualified() {

		RepositoryConsumer repositoryConsumer = container.select(RepositoryConsumer.class).get();
		repositoryConsumer.deleteAll();

		Person person = new Person();
		person.setName("foo");
		repositoryConsumer.getUnqualifiedRepo().save(person);
		List<Person> result = repositoryConsumer.getQualifiedRepo().findByName("foo");

		assertThat(result).containsExactly(person);
	}

	@Test // DATAREDIS-425, DATAREDIS-700
	void callMethodOnCustomRepositoryShouldSuceed() {

		RepositoryConsumer repositoryConsumer = container.select(RepositoryConsumer.class).get();

		int result = repositoryConsumer.getUnqualifiedRepo().returnOne();
		assertThat(result).isEqualTo(1);
	}
}
