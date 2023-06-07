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
package org.springframework.data.redis.repository.query;

import static org.assertj.core.api.Assertions.*;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.geo.Box;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.geo.Shape;
import org.springframework.data.keyvalue.core.query.KeyValueQuery;
import org.springframework.data.redis.core.convert.ConversionTestEntities.Person;
import org.springframework.data.redis.repository.query.RedisOperationChain.PathAndValue;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.DefaultParameters;
import org.springframework.data.repository.query.ParametersParameterAccessor;
import org.springframework.data.repository.query.parser.PartTree;

/**
 * Unit tests for {@link RedisQueryCreator}.
 *
 * @author Christoph Strobl
 * @author Mark Paluch
 */
@ExtendWith(MockitoExtension.class)
class RedisQueryCreatorUnitTests {

	private @Mock RepositoryMetadata metadataMock;

	@Test // DATAREDIS-425
	void findBySingleSimpleProperty() throws SecurityException, NoSuchMethodException {

		RedisQueryCreator creator = createQueryCreatorForMethodWithArgs(
				SampleRepository.class.getMethod("findByFirstname", String.class), new Object[] { "eddard" });

		KeyValueQuery<RedisOperationChain> query = creator.createQuery();

		assertThat(query.getCriteria().getSismember()).hasSize(1);
		assertThat(query.getCriteria().getSismember()).contains(new PathAndValue("firstname", "eddard"));
	}

	@Test // DATAREDIS-425
	void findByMultipleSimpleProperties() throws SecurityException, NoSuchMethodException {

		RedisQueryCreator creator = createQueryCreatorForMethodWithArgs(
				SampleRepository.class.getMethod("findByFirstnameAndAge", String.class, Integer.class),
				new Object[] { "eddard", 43 });

		KeyValueQuery<RedisOperationChain> query = creator.createQuery();

		assertThat(query.getCriteria().getSismember()).hasSize(2);
		assertThat(query.getCriteria().getSismember()).contains(new PathAndValue("firstname", "eddard"));
		assertThat(query.getCriteria().getSismember()).contains(new PathAndValue("age", 43));
	}

	@Test // DATAREDIS-425
	void findByMultipleSimplePropertiesUsingOr() throws SecurityException, NoSuchMethodException {

		RedisQueryCreator creator = createQueryCreatorForMethodWithArgs(
				SampleRepository.class.getMethod("findByAgeOrFirstname", Integer.class, String.class),
				new Object[] { 43, "eddard" });

		KeyValueQuery<RedisOperationChain> query = creator.createQuery();

		assertThat(query.getCriteria().getOrSismember()).hasSize(2);
		assertThat(query.getCriteria().getOrSismember()).contains(new PathAndValue("age", 43));
		assertThat(query.getCriteria().getOrSismember()).contains(new PathAndValue("firstname", "eddard"));
	}

	@Test // DATAREDIS-533
	void findWithinCircle() throws SecurityException, NoSuchMethodException {

		RedisQueryCreator creator = createQueryCreatorForMethodWithArgs(
				SampleRepository.class.getMethod("findByLocationWithin", Circle.class),
				new Object[] { new Circle(new Point(1, 2), new Distance(200, Metrics.KILOMETERS)) });

		KeyValueQuery<RedisOperationChain> query = creator.createQuery();

		assertThat(query.getCriteria().getNear()).isNotNull();
		assertThat(query.getCriteria().getNear().getPoint()).isEqualTo(new Point(1, 2));
		assertThat(query.getCriteria().getNear().getDistance()).isEqualTo(new Distance(200, Metrics.KILOMETERS));
	}

	@Test // DATAREDIS-533
	void findNearWithPointAndDistance() throws SecurityException, NoSuchMethodException {

		RedisQueryCreator creator = createQueryCreatorForMethodWithArgs(
				SampleRepository.class.getMethod("findByLocationNear", Point.class, Distance.class),
				new Object[] { new Point(1, 2), new Distance(200, Metrics.KILOMETERS) });

		KeyValueQuery<RedisOperationChain> query = creator.createQuery();

		assertThat(query.getCriteria().getNear()).isNotNull();
		assertThat(query.getCriteria().getNear().getPoint()).isEqualTo(new Point(1, 2));
		assertThat(query.getCriteria().getNear().getDistance()).isEqualTo(new Distance(200, Metrics.KILOMETERS));
	}

	@Test // DATAREDIS-533
	void findNearWithPointAndNumericValueDefaultsToKilometers() throws SecurityException, NoSuchMethodException {

		RedisQueryCreator creator = createQueryCreatorForMethodWithArgs(
				SampleRepository.class.getMethod("findByLocationNear", Shape.class, Object.class),
				new Object[] { new Point(1, 2), 200F });

		KeyValueQuery<RedisOperationChain> query = creator.createQuery();

		assertThat(query.getCriteria().getNear()).isNotNull();
		assertThat(query.getCriteria().getNear().getPoint()).isEqualTo(new Point(1, 2));
		assertThat(query.getCriteria().getNear().getDistance()).isEqualTo(new Distance(200, Metrics.KILOMETERS));
	}

	@Test // DATAREDIS-533
	void findNearWithInvalidShapeParameter() throws SecurityException, NoSuchMethodException {

		RedisQueryCreator creator = createQueryCreatorForMethodWithArgs(
				SampleRepository.class.getMethod("findByLocationNear", Shape.class, Object.class),
				new Object[] { new Box(new Point(0, 0), new Point(1, 1)), 200F });

		assertThatExceptionOfType(InvalidDataAccessApiUsageException.class).isThrownBy(creator::createQuery)
				.withMessageContaining("Expected to find a Circle or Point/Distance");
	}

	@Test // DATAREDIS-533
	void findNearWithInvalidDistanceParameter() throws SecurityException, NoSuchMethodException {

		RedisQueryCreator creator = createQueryCreatorForMethodWithArgs(
				SampleRepository.class.getMethod("findByLocationNear", Shape.class, Object.class),
				new Object[] { new Point(0, 0), "200" });

		assertThatExceptionOfType(InvalidDataAccessApiUsageException.class).isThrownBy(creator::createQuery)
				.withMessageContaining("Expected to find Distance or Numeric value");
	}

	@Test // DATAREDIS-533
	void findNearWithMissingDistanceParameter() throws SecurityException, NoSuchMethodException {

		RedisQueryCreator creator = createQueryCreatorForMethodWithArgs(
				SampleRepository.class.getMethod("findByLocationNear", Shape.class), new Object[] { new Point(0, 0) });

		assertThatExceptionOfType(InvalidDataAccessApiUsageException.class).isThrownBy(creator::createQuery)
				.withMessageContaining("Are you missing a parameter");
	}

	@Test // DATAREDIS-771
	void findByBooleanIsTrue() throws SecurityException, NoSuchMethodException {

		RedisQueryCreator creator = createQueryCreatorForMethodWithArgs(
				SampleRepository.class.getMethod("findByAliveIsTrue"), new Object[0]);

		KeyValueQuery<RedisOperationChain> query = creator.createQuery();

		assertThat(query.getCriteria().getSismember()).hasSize(1);
		assertThat(query.getCriteria().getSismember()).contains(new PathAndValue("alive", true));
	}

	@Test // DATAREDIS-771
	void findByBooleanIsFalse() throws SecurityException, NoSuchMethodException {

		RedisQueryCreator creator = createQueryCreatorForMethodWithArgs(
				SampleRepository.class.getMethod("findByAliveIsFalse"), new Object[0]);

		KeyValueQuery<RedisOperationChain> query = creator.createQuery();

		assertThat(query.getCriteria().getSismember()).hasSize(1);
		assertThat(query.getCriteria().getSismember()).contains(new PathAndValue("alive", false));
	}

	private RedisQueryCreator createQueryCreatorForMethodWithArgs(Method method, Object[] args) {

		PartTree partTree = new PartTree(method.getName(), method.getReturnType());
		RedisQueryCreator creator = new RedisQueryCreator(partTree,
				new ParametersParameterAccessor(new DefaultParameters(method), args));

		return creator;
	}

	private interface SampleRepository extends Repository<Person, String> {

		Person findByFirstname(String firstname);

		Person findByFirstnameAndAge(String firstname, Integer age);

		Person findByAgeOrFirstname(Integer age, String firstname);

		Person findByAliveIsTrue();

		Person findByAliveIsFalse();

		Person findByLocationWithin(Circle circle);

		Person findByLocationNear(Point point, Distance distance);

		Person findByLocationNear(Shape point, Object distance);

		Person findByLocationNear(Shape point);
	}
}
