package com.licensing.binary;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

@Slf4j
@Component
@RequiredArgsConstructor
public class ManifestSerializer {

    private final BinaryWriter binaryWriter;

    public byte[] serializeWithoutSignature(Manifest manifest) {
        binaryWriter.reset();

        byte[] magicBytes = manifest.getMagic().getBytes();
        if (magicBytes.length != 10) {
            throw new IllegalArgumentException("Magic must be exactly 10 bytes");
        }
        binaryWriter.writeFixedBytes(magicBytes);

        binaryWriter.writeUInt16(manifest.getVersion());

        binaryWriter.writeUInt8(manifest.getExportType());

        binaryWriter.writeInt64(manifest.getGeneratedAtEpochMillis());

        binaryWriter.writeInt64(manifest.getSinceEpochMillis());

        binaryWriter.writeUInt32(manifest.getRecordCount());

        if (manifest.getDataSha256().length != 32) {
            throw new IllegalArgumentException("Data SHA256 must be exactly 32 bytes");
        }
        binaryWriter.writeFixedBytes(manifest.getDataSha256());

        for (ManifestEntry entry : manifest.getEntries()) {
            binaryWriter.writeUUID(entry.getId());

            binaryWriter.writeUInt8(entry.getStatusCode());

            binaryWriter.writeInt64(entry.getUpdatedAtEpochMillis());

            binaryWriter.writeUInt64(entry.getDataOffset());

            binaryWriter.writeUInt32(entry.getDataLength());

            binaryWriter.writeUInt32(entry.getRecordSignatureBytes().length);

            binaryWriter.writeFixedBytes(entry.getRecordSignatureBytes());
        }

        return binaryWriter.toByteArray();
    }

    public byte[] serializeWithSignature(Manifest manifest, byte[] signature) {
        byte[] unsignedManifest = serializeWithoutSignature(manifest);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.writeBytes(unsignedManifest);

        byte[] signatureLength = new BinaryWriter()
                .writeUInt32(signature.length)
                .toByteArray();
        baos.writeBytes(signatureLength);

        baos.writeBytes(signature);

        return baos.toByteArray();
    }
}