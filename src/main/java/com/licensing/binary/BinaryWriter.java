package com.licensing.binary;

import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class BinaryWriter {

    private final ByteArrayOutputStream outputStream;

    public BinaryWriter() {
        this.outputStream = new ByteArrayOutputStream();
    }

    public BinaryWriter writeUInt8(int value) {
        outputStream.write((byte) (value & 0xFF));
        return this;
    }

    public BinaryWriter writeUInt16(int value) {
        outputStream.write((byte) ((value >>> 8) & 0xFF));
        outputStream.write((byte) (value & 0xFF));
        return this;
    }

    public BinaryWriter writeUInt32(long value) {
        outputStream.write((byte) ((value >>> 24) & 0xFF));
        outputStream.write((byte) ((value >>> 16) & 0xFF));
        outputStream.write((byte) ((value >>> 8) & 0xFF));
        outputStream.write((byte) (value & 0xFF));
        return this;
    }

    public BinaryWriter writeUInt64(long value) {
        outputStream.write((byte) ((value >>> 56) & 0xFF));
        outputStream.write((byte) ((value >>> 48) & 0xFF));
        outputStream.write((byte) ((value >>> 40) & 0xFF));
        outputStream.write((byte) ((value >>> 32) & 0xFF));
        outputStream.write((byte) ((value >>> 24) & 0xFF));
        outputStream.write((byte) ((value >>> 16) & 0xFF));
        outputStream.write((byte) ((value >>> 8) & 0xFF));
        outputStream.write((byte) (value & 0xFF));
        return this;
    }

    public BinaryWriter writeInt64(long value) {
        return writeUInt64(value);
    }

    public BinaryWriter writeUUID(UUID uuid) {
        long mostSigBits = uuid.getMostSignificantBits();
        long leastSigBits = uuid.getLeastSignificantBits();
        writeUInt64(mostSigBits);
        writeUInt64(leastSigBits);
        return this;
    }

    public BinaryWriter writeString(String value) {
        if (value == null) {
            writeUInt32(0);
            return this;
        }
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        writeUInt32(bytes.length);
        outputStream.write(bytes, 0, bytes.length);
        return this;
    }

    public BinaryWriter writeBytes(byte[] value) {
        if (value == null) {
            writeUInt32(0);
            return this;
        }
        writeUInt32(value.length);
        outputStream.write(value, 0, value.length);
        return this;
    }

    public BinaryWriter writeBytesWithLength(byte[] value) {
        return writeBytes(value);
    }

    public BinaryWriter writeFixedBytes(byte[] value) {
        if (value != null) {
            outputStream.write(value, 0, value.length);
        }
        return this;
    }

    public BinaryWriter writeByte(byte value) {
        outputStream.write(value);
        return this;
    }

    public byte[] toByteArray() {
        return outputStream.toByteArray();
    }

    public void reset() {
        outputStream.reset();
    }
}