/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.samza.storage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.samza.SamzaException;
import org.apache.samza.config.Config;
import org.apache.samza.config.JavaStorageConfig;
import org.apache.samza.config.JavaSystemConfig;
import org.apache.samza.config.StorageConfig;
import org.apache.samza.config.SystemConfig;
import org.apache.samza.container.TaskName;
import org.apache.samza.coordinator.stream.messages.CoordinatorStreamMessage;
import org.apache.samza.coordinator.stream.messages.SetChangelogMapping;
import org.apache.samza.coordinator.stream.CoordinatorStreamSystemConsumer;
import org.apache.samza.coordinator.stream.CoordinatorStreamSystemProducer;
import org.apache.samza.coordinator.stream.AbstractCoordinatorStreamManager;
import org.apache.samza.system.StreamSpec;
import org.apache.samza.system.SystemAdmin;
import org.apache.samza.system.SystemFactory;
import org.apache.samza.system.SystemStream;
import org.apache.samza.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;


/**
 * The Changelog manager is used to persist and read the changelog information from the coordinator stream.
 */
public class ChangelogPartitionManager extends AbstractCoordinatorStreamManager {

  private static final Logger log = LoggerFactory.getLogger(ChangelogPartitionManager.class);
  private boolean isCoordinatorConsumerRegistered = false;

  public ChangelogPartitionManager(CoordinatorStreamSystemProducer coordinatorStreamProducer,
      CoordinatorStreamSystemConsumer coordinatorStreamConsumer,
      String source) {
    super(coordinatorStreamProducer, coordinatorStreamConsumer, source);
  }

  /**
   * Registers this manager to write changelog mapping for a particular task.
   * @param taskName The taskname to be registered for changelog mapping.
   */
  public void register(TaskName taskName) {
    log.debug("Adding taskName {} to {}", taskName, this);
    if (!isCoordinatorConsumerRegistered) {
      registerCoordinatorStreamConsumer();
      isCoordinatorConsumerRegistered = true;
    }
    registerCoordinatorStreamProducer(taskName.getTaskName());
  }

  /**
   * Read the taskName to partition mapping that is being maintained by this ChangelogManager
   * @return TaskName to change log partition mapping, or an empty map if there were no messages.
   */
  public Map<TaskName, Integer> readChangeLogPartitionMapping() {
    log.debug("Reading changelog partition information");
    final HashMap<TaskName, Integer> changelogMapping = new HashMap<TaskName, Integer>();
    for (CoordinatorStreamMessage coordinatorStreamMessage : getBootstrappedStream(SetChangelogMapping.TYPE)) {
      SetChangelogMapping changelogMapEntry = new SetChangelogMapping(coordinatorStreamMessage);
      changelogMapping.put(new TaskName(changelogMapEntry.getTaskName()), changelogMapEntry.getPartition());
      log.debug("TaskName: {} is mapped to {}", changelogMapEntry.getTaskName(), changelogMapEntry.getPartition());
    }
    return changelogMapping;
  }

  /**
   * Write the taskName to partition mapping that is being maintained by this ChangelogManager
   * @param changelogEntries The entries that needs to be written to the coordinator stream, the map takes the taskName
   *                       and it's corresponding changelog partition.
   */
  public void writeChangeLogPartitionMapping(Map<TaskName, Integer> changelogEntries) {
    log.debug("Updating changelog information with: ");
    for (Map.Entry<TaskName, Integer> entry : changelogEntries.entrySet()) {
      log.debug("TaskName: {} to Partition: {}", entry.getKey().getTaskName(), entry.getValue());
      send(new SetChangelogMapping(getSource(), entry.getKey().getTaskName(), entry.getValue()));
    }
  }
}
