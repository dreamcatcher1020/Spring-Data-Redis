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
package org.springframework.data.redis.repository;

import static org.assertj.core.api.Assertions.*;

import lombok.Data;
import lombok.Value;
import lombok.With;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Reference;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.keyvalue.core.KeyValueTemplate;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.convert.KeyspaceConfiguration;
import org.springframework.data.redis.core.index.GeoIndexed;
import org.springframework.data.redis.core.index.IndexConfiguration;
import org.springframework.data.redis.core.index.IndexDefinition;
import org.springframework.data.redis.core.index.Indexed;
import org.springframework.data.redis.core.index.SimpleIndexDefinition;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;

/**
 * Base for testing Redis repository support in different configurations.
 *
 * @author Christoph Strobl
 * @author Mark Paluch
 */
public abstract class RedisRepositoryIntegrationTestBase {

	@Autowired PersonRepository repo;
	@Autowired CityRepository cityRepo;
	@Autowired ImmutableObjectRepository immutableObjectRepo;
	@Autowired KeyValueTemplate kvTemplate;

	@BeforeEach
	void setUp() {

		// flush keyspaces
		kvTemplate.delete(Person.class);
		kvTemplate.delete(City.class);
	}

	@Test // DATAREDIS-425
	void simpleFindShouldReturnEntitiesCorrectly() {

		Person rand = new Person();
		rand.firstname = "rand";
		rand.lastname = "al'thor";

		Person egwene = new Person();
		egwene.firstname = "egwene";

		repo.saveAll(Arrays.asList(rand, egwene));

		assertThat(repo.count()).isEqualTo(2L);

		assertThat(repo.findById(rand.id)).isEqualTo(Optional.of(rand));
		assertThat(repo.findById(egwene.id)).isEqualTo(Optional.of(egwene));

		assertThat(repo.findByFirstname("rand").size()).isEqualTo(1);
		assertThat(repo.findByFirstname("rand")).contains(rand);

		assertThat(repo.findByFirstname("egwene").size()).isEqualTo(1);
		assertThat(repo.findByFirstname("egwene")).contains(egwene);

		assertThat(repo.findByLastname("al'thor")).contains(rand);
	}

	@Test // DATAREDIS-425
	void simpleFindByMultipleProperties() {

		Person egwene = new Person();
		egwene.firstname = "egwene";
		egwene.lastname = "al'vere";

		Person marin = new Person();
		marin.firstname = "marin";
		marin.lastname = "al'vere";

		repo.saveAll(Arrays.asList(egwene, marin));

		assertThat(repo.findByLastname("al'vere").size()).isEqualTo(2);

		assertThat(repo.findByFirstnameAndLastname("egwene", "al'vere").size()).isEqualTo(1);
		assertThat(repo.findByFirstnameAndLastname("egwene", "al'vere").get(0)).isEqualTo(egwene);
	}

	@Test // GH-2080
	void simpleFindAndSort() {

		Person egwene = new Person();
		egwene.firstname = "egwene";
		egwene.lastname = "al'vere";

		Person marin = new Person();
		marin.firstname = "marin";
		marin.lastname = "al'vere";

		repo.saveAll(Arrays.asList(egwene, marin));

		assertThat(repo.findByLastname("al'vere", Sort.by(Sort.Direction.ASC, "firstname"))).containsSequence(egwene,
				marin);
		assertThat(repo.findByLastname("al'vere", Sort.by(Sort.Direction.DESC, "firstname"))).containsSequence(marin,
				egwene);

		assertThat(repo.findByLastnameOrderByFirstnameAsc("al'vere")).containsSequence(egwene, marin);
		assertThat(repo.findByLastnameOrderByFirstnameDesc("al'vere")).containsSequence(marin, egwene);
	}

	@Test // GH-2080
	void simpleFindAllWithSort() {

		Person egwene = new Person();
		egwene.firstname = "egwene";
		egwene.lastname = "al'vere";

		Person marin = new Person();
		marin.firstname = "marin";
		marin.lastname = "al'vere";

		repo.saveAll(Arrays.asList(egwene, marin));

		assertThat(repo.findAll(Sort.by(Sort.Direction.ASC, "firstname"))).containsSequence(egwene, marin);
		assertThat(repo.findAll(Sort.by(Sort.Direction.DESC, "firstname"))).containsSequence(marin, egwene);
	}

	@Test // DATAREDIS-425
	void findReturnsReferenceDataCorrectly() {

		// Prepare referenced data entry
		City tarValon = new City();
		tarValon.id = "1";
		tarValon.name = "tar valon";

		kvTemplate.insert(tarValon);

		// Prepare domain entity
		Person moiraine = new Person();
		moiraine.firstname = "moiraine";
		moiraine.city = tarValon; // reference data

		// save domain entity
		repo.save(moiraine);

		// find and assert current location set correctly
		Optional<Person> loaded = repo.findById(moiraine.getId());
		assertThat(loaded.get().city).isEqualTo(tarValon);

		// remove reference location data
		kvTemplate.delete("1", City.class);

		// find and assert the location is gone
		Optional<Person> reLoaded = repo.findById(moiraine.getId());
		assertThat(reLoaded.get().city).isNull();
	}

	@Test // DATAREDIS-425
	void findReturnsPageCorrectly() {

		Person eddard = new Person("eddard", "stark");
		Person robb = new Person("robb", "stark");
		Person sansa = new Person("sansa", "stark");
		Person arya = new Person("arya", "stark");
		Person bran = new Person("bran", "stark");
		Person rickon = new Person("rickon", "stark");

		repo.saveAll(Arrays.asList(eddard, robb, sansa, arya, bran, rickon));

		Page<Person> page1 = repo.findPersonByLastname("stark", PageRequest.of(0, 5));

		assertThat(page1.getNumberOfElements()).isEqualTo(5);
		assertThat(page1.getTotalElements()).isEqualTo(6L);

		Page<Person> page2 = repo.findPersonByLastname("stark", page1.nextPageable());

		assertThat(page2.getNumberOfElements()).isEqualTo(1);
		assertThat(page2.getTotalElements()).isEqualTo(6L);
	}

	@Test // DATAREDIS-425
	void findUsingOrReturnsResultCorrectly() {

		Person eddard = new Person("eddard", "stark");
		Person robb = new Person("robb", "stark");
		Person jon = new Person("jon", "snow");

		repo.saveAll(Arrays.asList(eddard, robb, jon));

		List<Person> eddardAndJon = repo.findByFirstnameOrLastname("eddard", "snow");

		assertThat(eddardAndJon).hasSize(2);
		assertThat(eddardAndJon).contains(eddard, jon);
	}

	@Test // DATAREDIS-547
	void shouldApplyFirstKeywordCorrectly() {

		Person eddard = new Person("eddard", "stark");
		Person robb = new Person("robb", "stark");
		Person jon = new Person("jon", "snow");

		repo.saveAll(Arrays.asList(eddard, robb, jon));

		assertThat(repo.findFirstBy()).hasSize(1);
	}

	@Test // DATAREDIS-547
	void shouldApplyPageableCorrectlyWhenUsingFindAll() {

		Person eddard = new Person("eddard", "stark");
		Person robb = new Person("robb", "stark");
		Person jon = new Person("jon", "snow");

		repo.saveAll(Arrays.asList(eddard, robb, jon));

		Page<Person> firstPage = repo.findAll(PageRequest.of(0, 2));
		assertThat(firstPage.getContent()).hasSize(2);
		assertThat(repo.findAll(firstPage.nextPageable()).getContent()).hasSize(1);
	}

	@Test // DATAREDIS-551
	void shouldApplyPageableCorrectlyWhenUsingFindByWithoutCriteria() {

		Person eddard = new Person("eddard", "stark");
		Person robb = new Person("robb", "stark");
		Person jon = new Person("jon", "snow");

		repo.saveAll(Arrays.asList(eddard, robb, jon));

		Page<Person> firstPage = repo.findBy(PageRequest.of(0, 2));
		assertThat(firstPage.getContent()).hasSize(2);
		assertThat(firstPage.getTotalElements()).isEqualTo(3L);
		assertThat(repo.findBy(firstPage.nextPageable()).getContent()).hasSize(1);
	}

	@Test // DATAREDIS-771
	void shouldFindByBooleanIsTrue() {

		Person eddard = new Person("eddard", "stark");
		eddard.setAlive(true);

		Person robb = new Person("robb", "stark");
		robb.setAlive(false);

		Person jon = new Person("jon", "snow");

		repo.saveAll(Arrays.asList(eddard, robb, jon));

		List<Person> result = repo.findPersonByAliveIsTrue();

		assertThat(result).hasSize(1);
		assertThat(result).containsExactly(eddard);
	}

	@Test // DATAREDIS-771
	void shouldFindByBooleanIsFalse() {

		Person eddard = new Person("eddard", "stark");
		eddard.setAlive(true);

		Person robb = new Person("robb", "stark");
		robb.setAlive(false);

		Person jon = new Person("jon", "snow");

		repo.saveAll(Arrays.asList(eddard, robb, jon));

		List<Person> result = repo.findPersonByAliveIsFalse();

		assertThat(result).hasSize(1);
		assertThat(result).containsExactly(robb);
	}

	@Test // DATAREDIS-547
	void shouldReturnEmptyListWhenPageableOutOfBoundsUsingFindAll() {

		Person eddard = new Person("eddard", "stark");
		Person robb = new Person("robb", "stark");
		Person jon = new Person("jon", "snow");

		repo.saveAll(Arrays.asList(eddard, robb, jon));

		Page<Person> firstPage = repo.findAll(PageRequest.of(100, 2));
		assertThat(firstPage.getContent()).hasSize(0);
	}

	@Test // DATAREDIS-547
	void shouldReturnEmptyListWhenPageableOutOfBoundsUsingQueryMethod() {

		Person eddard = new Person("eddard", "stark");
		Person robb = new Person("robb", "stark");
		Person sansa = new Person("sansa", "stark");

		repo.saveAll(Arrays.asList(eddard, robb, sansa));

		Page<Person> page1 = repo.findPersonByLastname("stark", PageRequest.of(1, 3));

		assertThat(page1.getNumberOfElements()).isEqualTo(0);
		assertThat(page1.getContent()).hasSize(0);
		assertThat(page1.getTotalElements()).isEqualTo(3L);

		Page<Person> page2 = repo.findPersonByLastname("stark", PageRequest.of(2, 3));

		assertThat(page2.getNumberOfElements()).isEqualTo(0);
		assertThat(page2.getContent()).hasSize(0);
		assertThat(page2.getTotalElements()).isEqualTo(3L);
	}

	@Test // DATAREDIS-547
	void shouldApplyTopKeywordCorrectly() {

		Person eddard = new Person("eddard", "stark");
		Person robb = new Person("robb", "stark");
		Person jon = new Person("jon", "snow");

		repo.saveAll(Arrays.asList(eddard, robb, jon));

		assertThat(repo.findTop2By()).hasSize(2);
	}

	@Test // DATAREDIS-547
	void shouldApplyTopKeywordCorrectlyWhenCriteriaPresent() {

		Person eddard = new Person("eddard", "stark");
		Person tyrion = new Person("tyrion", "lannister");
		Person robb = new Person("robb", "stark");
		Person jon = new Person("jon", "snow");
		Person arya = new Person("arya", "stark");

		repo.saveAll(Arrays.asList(eddard, tyrion, robb, jon, arya));

		List<Person> result = repo.findTop2ByLastname("stark");

		assertThat(result).hasSize(2);
		for (Person p : result) {
			assertThat(p.getLastname()).isEqualTo("stark");
		}
	}

	@Test // DATAREDIS-605
	void shouldFindByExample() {

		Person eddard = new Person("eddard", "stark");
		Person tyrion = new Person("tyrion", "lannister");
		Person robb = new Person("robb", "stark");
		Person jon = new Person("jon", "snow");
		Person arya = new Person("arya", "stark");

		repo.saveAll(Arrays.asList(eddard, tyrion, robb, jon, arya));

		List<Person> result = repo.findAll(Example.of(new Person(null, "stark")));

		assertThat(result).hasSize(3);
	}

	@Test // DATAREDIS-533
	void nearQueryShouldReturnResultsCorrectly() {

		City palermo = new City();
		palermo.location = new Point(13.361389D, 38.115556D);

		City catania = new City();
		catania.location = new Point(15.087269D, 37.502669D);

		cityRepo.saveAll(Arrays.asList(palermo, catania));

		List<City> result = cityRepo.findByLocationNear(new Point(15D, 37D), new Distance(200, Metrics.KILOMETERS));
		assertThat(result).contains(palermo, catania);

		result = cityRepo.findByLocationNear(new Point(15D, 37D), new Distance(100, Metrics.KILOMETERS));
		assertThat(result).contains(catania).doesNotContain(palermo);
	}

	@Test // DATAREDIS-533
	void nearQueryShouldFindNothingIfOutOfRange() {

		City palermo = new City();
		palermo.location = new Point(13.361389D, 38.115556D);

		City catania = new City();
		catania.location = new Point(15.087269D, 37.502669D);

		cityRepo.saveAll(Arrays.asList(palermo, catania));

		List<City> result = cityRepo.findByLocationNear(new Point(15D, 37D), new Distance(10, Metrics.KILOMETERS));
		assertThat(result).isEmpty();
	}

	@Test // DATAREDIS-533
	void nearQueryShouldReturnResultsCorrectlyOnNestedProperty() {

		City palermo = new City();
		palermo.location = new Point(13.361389D, 38.115556D);

		City catania = new City();
		catania.location = new Point(15.087269D, 37.502669D);

		Person p1 = new Person("foo", "bar");
		p1.hometown = palermo;

		Person p2 = new Person("two", "two");
		p2.hometown = catania;

		repo.saveAll(Arrays.asList(p1, p2));

		List<Person> result = repo.findByHometownLocationNear(new Point(15D, 37D), new Distance(200, Metrics.KILOMETERS));
		assertThat(result).contains(p1, p2);

		result = repo.findByHometownLocationNear(new Point(15D, 37D), new Distance(100, Metrics.KILOMETERS));
		assertThat(result).contains(p2).doesNotContain(p1);
	}

	@Test // DATAREDIS-849
	void shouldReturnNewObjectInstanceOnImmutableSave() {

		Immutable object = new Immutable(null, "Walter", new Immutable("heisenberg", "White", null));
		Immutable saved = immutableObjectRepo.save(object);

		assertThat(object.id).isNull();
		assertThat(saved.id).isNotNull();
	}

	@Test // DATAREDIS-849
	void shouldReturnNewObjectInstanceOnImmutableSaveAll() {

		Immutable object = new Immutable(null, "Walter", new Immutable("heisenberg", "White", null));
		List<Immutable> saved = (List) immutableObjectRepo.saveAll(Collections.singleton(object));

		assertThat(object.id).isNull();
		assertThat(saved.get(0).id).isNotNull();
	}

	@Test // DATAREDIS-849
	void shouldProperlyReadNestedImmutableObject() {

		Immutable nested = new Immutable("heisenberg", "White", null);
		Immutable object = new Immutable(null, "Walter", nested);
		Immutable saved = immutableObjectRepo.save(object);

		Immutable loaded = immutableObjectRepo.findById(saved.id).get();
		assertThat(loaded.nested).isEqualTo(nested);
	}

	public static interface PersonRepository
			extends PagingAndSortingRepository<Person, String>, CrudRepository<Person, String>,
			QueryByExampleExecutor<Person> {

		List<Person> findByFirstname(String firstname);

		List<Person> findByLastname(String lastname);

		List<Person> findByLastname(String lastname, Sort sort);

		List<Person> findByLastnameOrderByFirstnameAsc(String lastname);

		List<Person> findByLastnameOrderByFirstnameDesc(String lastname);

		Page<Person> findPersonByLastname(String lastname, Pageable page);

		List<Person> findPersonByAliveIsTrue();

		List<Person> findPersonByAliveIsFalse();

		List<Person> findByFirstnameAndLastname(String firstname, String lastname);

		List<Person> findByFirstnameOrLastname(String firstname, String lastname);

		List<Person> findFirstBy();

		List<Person> findTop2By();

		List<Person> findTop2ByLastname(String lastname);

		Page<Person> findBy(Pageable page);

		List<Person> findByHometownLocationNear(Point point, Distance distance);

		@Override
		<S extends Person> List<S> findAll(Example<S> example);
	}

	public interface CityRepository extends CrudRepository<City, String> {

		List<City> findByLocationNear(Point point, Distance distance);
	}

	public interface ImmutableObjectRepository extends CrudRepository<Immutable, String> {}

	/**
	 * Custom Redis {@link IndexConfiguration} forcing index of {@link Person#lastname}.
	 *
	 * @author Christoph Strobl
	 */
	static class MyIndexConfiguration extends IndexConfiguration {

		@Override
		protected Iterable<IndexDefinition> initialConfiguration() {
			return Collections.<IndexDefinition> singleton(new SimpleIndexDefinition("persons", "lastname"));
		}
	}

	/**
	 * Custom Redis {@link IndexConfiguration} forcing index of {@link Person#lastname}.
	 *
	 * @author Christoph Strobl
	 */
	static class MyKeyspaceConfiguration extends KeyspaceConfiguration {

		@Override
		protected Iterable<KeyspaceSettings> initialConfiguration() {
			return Collections.singleton(new KeyspaceSettings(City.class, "cities"));
		}
	}

	@RedisHash("persons")
	@Data
	public static class Person {

		@Id String id;
		@Indexed String firstname;
		@Indexed Boolean alive;
		String lastname;
		@Reference City city;
		City hometown;

		public Person() {}

		public Person(String firstname, String lastname) {

			this.firstname = firstname;
			this.lastname = lastname;
		}
	}

	@Data
	static class City {

		@Id String id;
		String name;

		@GeoIndexed Point location;
	}

	@Value
	@With
	static class Immutable {

		@Id String id;
		String name;

		Immutable nested;
	}
}
