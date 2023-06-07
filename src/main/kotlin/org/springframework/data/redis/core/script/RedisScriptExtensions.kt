/*
 * Copyright 2022-2022 the original author or authors.
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
package org.springframework.data.redis.core.script

import org.springframework.core.io.Resource


/**
 * Inline fun variant with reified generics for [RedisScript].
 *
 * @author Mikhael Sokolov
 * @since 2.6.1
 */
@Suppress("FunctionName")
inline fun <reified T : Any> RedisScript(script: String): RedisScript<T> =
    RedisScript.of(script, T::class.java)

/**
 * Inline fun variant with reified generics for [RedisScript].
 *
 * @author Mikhael Sokolov
 * @since 2.6.1
 */
@Suppress("FunctionName")
inline fun <reified T : Any> RedisScript(script: Resource): RedisScript<T> =
    RedisScript.of(script, T::class.java)
