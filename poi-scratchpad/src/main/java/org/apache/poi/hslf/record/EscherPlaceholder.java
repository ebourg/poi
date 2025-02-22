/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.hslf.record;

import java.util.Map;
import java.util.function.Supplier;

import org.apache.logging.log4j.Logger;
import org.apache.poi.logging.PoiLogManager;
import org.apache.poi.ddf.EscherRecord;
import org.apache.poi.ddf.EscherRecordFactory;
import org.apache.poi.ddf.EscherSerializationListener;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndian;

/**
 * An atom record that specifies whether a shape is a placeholder shape.
 * The number, position, and type of placeholder shapes are determined by
 * the slide layout as specified in the SlideAtom record.
 */
public class EscherPlaceholder extends EscherRecord {
    private static final Logger LOG = PoiLogManager.getLogger(EscherPlaceholder.class);

    public static final short RECORD_ID = RecordTypes.OEPlaceholderAtom.typeID;
    public static final String RECORD_DESCRIPTION = "msofbtClientTextboxPlaceholder";

    private int position = -1;
    private byte placementId;
    private byte size;
    private short unused;

    public EscherPlaceholder() {}

    public EscherPlaceholder(EscherPlaceholder other) {
        super(other);
        position = other.position;
        placementId = other.placementId;
        size = other.size;
        unused = other.unused;
    }

    @Override
    public int fillFields(byte[] data, int offset, EscherRecordFactory recordFactory) {
        int bytesRemaining = readHeader( data, offset );

        position = LittleEndian.getInt(data, offset+8);
        placementId = data[offset+12];
        size = data[offset+13];
        unused = LittleEndian.getShort(data, offset+14);

        if (bytesRemaining + 8 != 16) {
            LOG.warn("Invalid header-data received, should have 8 bytes left, but had: {}", bytesRemaining);
        }

        return bytesRemaining + 8;
    }

    @Override
    public int serialize(int offset, byte[] data, EscherSerializationListener listener) {
        listener.beforeRecordSerialize( offset, getRecordId(), this );

        LittleEndian.putShort(data, offset, getOptions());
        LittleEndian.putShort(data, offset+2, getRecordId());
        LittleEndian.putInt(data, offset+4, 8);
        LittleEndian.putInt(data, offset+8, position);
        LittleEndian.putByte(data, offset+12, placementId);
        LittleEndian.putByte(data, offset+13, size);
        LittleEndian.putShort(data, offset+14, unused);

        listener.afterRecordSerialize( offset+getRecordSize(), getRecordId(), getRecordSize(), this );
        return getRecordSize();
    }

    @Override
    public int getRecordSize() {
        return 8 + 8;
    }

    @Override
    public String getRecordName() {
        return "ClientTextboxPlaceholder";
    }

    public int getPosition() {
        return position;
    }

    public byte getPlacementId() {
        return placementId;
    }

    public byte getSize() {
        return size;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "base", super::getGenericProperties,
            "position", this::getPosition,
            "placementId", this::getPlacementId,
            "size", this::getSize
        );
    }

    @Override
    public Enum getGenericRecordType() {
        return RecordTypes.OEPlaceholderAtom;
    }

    @Override
    public EscherPlaceholder copy() {
        return new EscherPlaceholder(this);
    }
}
