/*
 * Copyright (c) 2014, Christoph Engelbert (aka noctarius) and
 * contributors. All rights reserved.
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
package com.noctarius.snowcast.impl.operations.client;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.hazelcast.spi.Operation;
import com.noctarius.snowcast.SnowcastEpoch;
import com.noctarius.snowcast.impl.SequencerDefinition;
import com.noctarius.snowcast.impl.SequencerPortableHook;

import java.io.IOException;

public class ClientDetachLogicalNodeRequest
        extends AbstractClientSequencerPartitionRequest {

    private int logicalNodeId;
    private SequencerDefinition definition;

    public ClientDetachLogicalNodeRequest() {
    }

    public ClientDetachLogicalNodeRequest(SequencerDefinition definition, int partitionId, int logicalNodeId) {
        super(definition.getSequencerName(), partitionId);
        this.definition = definition;
        this.logicalNodeId = logicalNodeId;
    }

    @Override
    protected Operation prepareOperation() {
        return new ClientDetachLogicalNodeOperation(getSequencerName(), definition, getEndpoint(), logicalNodeId);
    }

    @Override
    public int getClassId() {
        return SequencerPortableHook.TYPE_DETACH_LOGICAL_NODE;
    }

    @Override
    public void write(PortableWriter writer)
            throws IOException {

        super.write(writer);
        writer.writeInt("lni", logicalNodeId);
        writer.writeLong("epoch", definition.getEpoch().getEpochOffset());
        writer.writeInt("mnc", definition.getMaxLogicalNodeCount());
        writer.writeShort("bc", definition.getBackupCount());
    }

    @Override
    public void read(PortableReader reader)
            throws IOException {

        super.read(reader);
        this.logicalNodeId = reader.readInt("lni");

        long epochOffset = reader.readLong("epoch");
        int maxLogicalNodeCount = reader.readInt("mnc");
        short backupCount = reader.readShort("bc");

        SnowcastEpoch epoch = SnowcastEpoch.byTimestamp(epochOffset);
        definition = new SequencerDefinition(getSequencerName(), epoch, maxLogicalNodeCount, backupCount);
    }
}
