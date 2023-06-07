/*
 * Copyright 2018-2022 the original author or authors.
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
package org.springframework.data.redis.test.util;

import org.springframework.util.Assert;

/**
 * Utils for working with Hex Stings.
 *
 * @author Christoph Strobl
 * @currentRead Beyong the Shadows - Brent Weeks
 */
public class HexStringUtils {

	/**
	 * Convert a given HEX {@link String} to its byte representation.
	 *
	 * @param source must not be {@literal null}.
	 * @return
	 */
	public static byte[] hexToBytes(String source) {

		Assert.notNull(source, "Source must not be null");
		int len = source.length();

		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(source.charAt(i), 16) << 4) + Character.digit(source.charAt(i + 1), 16));
		}
		return data;
	}
}
