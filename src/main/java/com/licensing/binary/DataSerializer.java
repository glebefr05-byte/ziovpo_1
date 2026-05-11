package com.licensing.binary;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSerializer {

    private final BinaryWriter binaryWriter;

    @Value("${jwt.magicname}")
    private String studentname;

    public byte[] serializeEntry(DataEntry entry) {
        binaryWriter.reset();

        binaryWriter.writeString(entry.getThreatName());

        binaryWriter.writeBytes(entry.getFirstBytes());

        binaryWriter.writeBytes(entry.getRemainderHash());

        binaryWriter.writeUInt64(entry.getRemainderLength());

        binaryWriter.writeString(entry.getFileType());

        binaryWriter.writeUInt64(entry.getOffsetStart());

        binaryWriter.writeUInt64(entry.getOffsetEnd());

        return binaryWriter.toByteArray();
    }

    public byte[] serializeDataFile(short version, DataEntry[] entries) {
        binaryWriter.reset();

        String magic = "DB-" + getStudentSurname();
        byte[] magicBytes = magic.getBytes();
        if (magicBytes.length != 10) {
            throw new IllegalArgumentException("Magic must be exactly 10 bytes");
        }
        binaryWriter.writeFixedBytes(magicBytes);

        binaryWriter.writeUInt16(version);

        binaryWriter.writeUInt32(entries.length);

        for (DataEntry entry : entries) {
            writeEntry(this.binaryWriter, entry);
        }

        return binaryWriter.toByteArray();
    }

    private void writeEntry(BinaryWriter writer, DataEntry entry) {
        writer.writeString(entry.getThreatName());
        writer.writeBytes(entry.getFirstBytes());
        writer.writeBytes(entry.getRemainderHash());
        writer.writeUInt64(entry.getRemainderLength());
        writer.writeString(entry.getFileType());
        writer.writeUInt64(entry.getOffsetStart());
        writer.writeUInt64(entry.getOffsetEnd());
    }

    private String getStudentSurname() {
        return studentname;
    }
}