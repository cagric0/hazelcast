/*
 * Copyright (c) 2008-2013, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.mapreduce.process;

import com.hazelcast.mapreduce.KeyPredicate;
import com.hazelcast.mapreduce.Mapper;

/**
 * <p>
 * This interface describes a complex mapreduce Job process that is build by
 * {@link com.hazelcast.mapreduce.JobTracker#newProcessJob(com.hazelcast.mapreduce.KeyValueSource)}.<br>
 * Complex mapreduce Jobs can chain multiple {@link Mapper}, {@link com.hazelcast.mapreduce.Combiner} and
 * {@link com.hazelcast.mapreduce.Reducer} steps in one complex process execution.<br/>
 * It is used to execute mappings and calculations on the different cluster nodes and reduce or collate these mapped
 * values to results.
 * </p>
 * <p>
 * Implementations returned by the JobTracker are fully threadsafe and can be used concurrently and multiple
 * times <b>once the configuration is finished</b>.
 * </p>
 * <p>
 * <b>Caution: The generic types of Jobs change depending on the used methods which can make it needed to use
 * different assignment variables when used over multiple source lines.</b>
 * </p>
 * <p>
 * An example on how to use it:
 * <pre>
 * HazelcastInstance hz = getHazelcastInstance();
 * IMap&lt;Integer, Integer> map = (...) hz.getMap( "default" );
 * JobTracker tracker = hz.getJobTracker( "default" );
 * Job&lt;Integer, Integer> job = tracker.newJob( KeyValueSource.fromMap( map ) );
 * CompletableFuture&lt;Map&lt;String, Integer>> future = job
 *      .mapper( buildMapper() ).mapper( buildDifferentMapper() ).reducer( buildReducer() ).submit();
 * Map&lt;String, Integer> results = future.get();
 * </pre>
 * </p>
 *
 * @param <KeyIn>    type of key used as input key type
 * @param <ValueIn>  type of value used as input value type
 */
public interface ProcessJob<KeyIn, ValueIn> {


    /**
     * Defines keys to execute the mapper and a possibly defined reducer against. If keys are known before submitting
     * the task setting them can improve execution speed.
     *
     * @param keys keys to be executed against
     * @return instance of this ProcessJob with generics changed on usage
     */
    ProcessJob<KeyIn, ValueIn> onKeys(Iterable<KeyIn> keys);

    /**
     * Defines keys to execute the mapper and a possibly defined reducer against. If keys are known before submitting
     * the task setting them can improve execution speed.
     *
     * @param keys keys to be executed against
     * @return instance of this ProcessJob with generics changed on usage
     */
    ProcessJob<KeyIn, ValueIn> onKeys(KeyIn... keys);

    /**
     * Defines the {@link com.hazelcast.mapreduce.KeyPredicate} implementation to preselect keys the MapReduce task will be executed on.
     * Preselecting keys can speed up the job massively.<br>
     * This method can be used in conjunction with {@link #onKeys(Iterable)} or {@link #onKeys(Object...)} to define a
     * range of known and evaluated keys.
     *
     * @param predicate predicate implementation to be used to evaluate keys
     * @return instance of this ProcessJob with generics changed on usage
     */
    ProcessJob<KeyIn, ValueIn> keyPredicate(KeyPredicate<KeyIn> predicate);

    /**
     * Defines the mapper for this task. This method is not idempotent and can be callable only one time. Further
     * calls result in an {@link IllegalStateException} to be thrown telling you to not change the internal state.
     *
     * @param mapper tasks mapper
     * @return instance of this ProcessJob with generics changed on usage
     */
    <KeyOut, ValueOut> ProcessMappingJob<KeyOut, ValueOut> mapper(Mapper<KeyIn, ValueIn, KeyOut, ValueOut> mapper);

}
