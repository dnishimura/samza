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
package org.apache.samza.startpoint;

import org.apache.samza.Partition;
import org.apache.samza.container.TaskName;
import org.apache.samza.system.SystemStreamPartition;
import org.junit.Assert;
import org.junit.Test;


public class TestStartpoint {

  @Test
  public void TestWithMethods() {
    Startpoint startpoint = Startpoint.withSpecificOffset("123");
    Assert.assertEquals(PositionType.SPECIFIC_OFFSET, startpoint.getPositionType());
    Assert.assertEquals("123", startpoint.getPosition());

    startpoint = Startpoint.withTimestamp(2222222L);
    Assert.assertEquals(PositionType.TIMESTAMP, startpoint.getPositionType());
    Assert.assertEquals("2222222", startpoint.getPosition());

    startpoint = Startpoint.withEarliest();
    Assert.assertEquals(PositionType.EARLIEST, startpoint.getPositionType());
    Assert.assertEquals(null, startpoint.getPosition());

    startpoint = Startpoint.withLatest();
    Assert.assertEquals(PositionType.LATEST, startpoint.getPositionType());
    Assert.assertEquals(null, startpoint.getPosition());

    startpoint = Startpoint.withBootstrap("Bootstrap info");
    Assert.assertEquals(PositionType.BOOTSTRAP, startpoint.getPositionType());
    Assert.assertEquals("Bootstrap info", startpoint.getPosition());
  }

  @Test
  public void TestStartpointKey() {
    SystemStreamPartition ssp1 = new SystemStreamPartition("system", "stream", new Partition(2));
    SystemStreamPartition ssp2 = new SystemStreamPartition("system", "stream", new Partition(3));

    StartpointKey startpointKey1 = new StartpointKey(ssp1);
    StartpointKey startpointKey2 = new StartpointKey(ssp1);
    StartpointKey startpointKeyWithDifferentSSP = new StartpointKey(ssp2);
    StartpointKey startpointKeyWithTask1 = new StartpointKey(ssp1, new TaskName("t1"));
    StartpointKey startpointKeyWithTask2 = new StartpointKey(ssp1, new TaskName("t1"));
    StartpointKey startpointKeyWithDifferentTask = new StartpointKey(ssp1, new TaskName("t2"));

    Assert.assertEquals(startpointKey1, startpointKey2);
    Assert.assertEquals(startpointKey1.toString(), startpointKey2.toString());
    Assert.assertEquals(startpointKeyWithTask1, startpointKeyWithTask2);
    Assert.assertEquals(startpointKeyWithTask1.toString(), startpointKeyWithTask2.toString());

    Assert.assertNotEquals(startpointKey1, startpointKeyWithTask1);
    Assert.assertNotEquals(startpointKey1.toString(), startpointKeyWithTask1.toString());

    Assert.assertNotEquals(startpointKey1, startpointKeyWithDifferentSSP);
    Assert.assertNotEquals(startpointKey1.toString(), startpointKeyWithDifferentSSP.toString());
    Assert.assertNotEquals(startpointKeyWithTask1, startpointKeyWithDifferentTask);
    Assert.assertNotEquals(startpointKeyWithTask1.toString(), startpointKeyWithDifferentTask.toString());

    Assert.assertNotEquals(startpointKeyWithTask1, startpointKeyWithDifferentTask);
    Assert.assertNotEquals(startpointKeyWithTask1.toString(), startpointKeyWithDifferentTask.toString());
  }

  @Test
  public void TestStartpointSerde() {
    StartpointSerde startpointSerde = new StartpointSerde();

    Startpoint startpoint1 = Startpoint.withSpecificOffset("123");
    Startpoint startpoint2 = Startpoint.withSpecificOffset("123");
    Assert.assertEquals(startpoint1, startpointSerde.fromBytes(startpointSerde.toBytes(startpoint2)));
    Assert.assertNotEquals(startpoint1.copyWithStoredAt(1234567L), startpointSerde.fromBytes(startpointSerde.toBytes(startpoint2)));
    Assert.assertEquals(startpoint1.copyWithStoredAt(1234567L), startpointSerde.fromBytes(startpointSerde.toBytes(startpoint2.copyWithStoredAt(1234567L))));

    startpoint1 = Startpoint.withTimestamp(2222222L);
    startpoint2 = Startpoint.withTimestamp(2222222L);
    Assert.assertEquals(startpoint1, startpointSerde.fromBytes(startpointSerde.toBytes(startpoint2)));
    Assert.assertNotEquals(startpoint1.copyWithStoredAt(1234567L), startpointSerde.fromBytes(startpointSerde.toBytes(startpoint2)));
    Assert.assertEquals(startpoint1.copyWithStoredAt(1234567L), startpointSerde.fromBytes(startpointSerde.toBytes(startpoint2.copyWithStoredAt(1234567L))));

    startpoint1 = Startpoint.withEarliest();
    startpoint2 = Startpoint.withEarliest();
    Assert.assertEquals(startpoint1, startpointSerde.fromBytes(startpointSerde.toBytes(startpoint2)));
    Assert.assertNotEquals(startpoint1.copyWithStoredAt(1234567L), startpointSerde.fromBytes(startpointSerde.toBytes(startpoint2)));
    Assert.assertEquals(startpoint1.copyWithStoredAt(1234567L), startpointSerde.fromBytes(startpointSerde.toBytes(startpoint2.copyWithStoredAt(1234567L))));

    startpoint1 = Startpoint.withLatest();
    startpoint2 = Startpoint.withLatest();
    Assert.assertEquals(startpoint1, startpointSerde.fromBytes(startpointSerde.toBytes(startpoint2)));
    Assert.assertNotEquals(startpoint1.copyWithStoredAt(1234567L), startpointSerde.fromBytes(startpointSerde.toBytes(startpoint2)));
    Assert.assertEquals(startpoint1.copyWithStoredAt(1234567L), startpointSerde.fromBytes(startpointSerde.toBytes(startpoint2.copyWithStoredAt(1234567L))));

    startpoint1 = Startpoint.withBootstrap("Bootstrap info");
    startpoint2 = Startpoint.withBootstrap("Bootstrap info");
    Assert.assertEquals(startpoint1, startpointSerde.fromBytes(startpointSerde.toBytes(startpoint2)));
    Assert.assertNotEquals(startpoint1.copyWithStoredAt(1234567L), startpointSerde.fromBytes(startpointSerde.toBytes(startpoint2)));
    Assert.assertEquals(startpoint1.copyWithStoredAt(1234567L), startpointSerde.fromBytes(startpointSerde.toBytes(startpoint2.copyWithStoredAt(1234567L))));
  }
}
