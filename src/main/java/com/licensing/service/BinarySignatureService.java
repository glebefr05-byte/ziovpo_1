package com.licensing.service;

import com.licensing.binary.*;
import com.licensing.entities.MalwareSignature;
import com.licensing.model.enums.SignatureStatus;
import com.licensing.repository.MalwareSignatureRepository;
import com.licensing.signature.SignatureKeyStoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class BinarySignatureService {

    private final MalwareSignatureRepository signatureRepository;
    private final ManifestSerializer manifestSerializer;
    private final DataSerializer dataSerializer;
    private final SignatureKeyStoreService signingService;

    private static final short VERSION = 1;
    private static final String STUDENT_SURNAME = "EFREMOV";

    public byte[] getFullManifest() throws Exception {
        List<MalwareSignature> signatures = signatureRepository.findByStatus(SignatureStatus.ACTUAL);
        return buildManifest(signatures, (byte) 0, -1);
    }

    public byte[] getFullData() throws Exception {
        List<MalwareSignature> signatures = signatureRepository.findByStatus(SignatureStatus.ACTUAL);
        return buildData(signatures);
    }

    public byte[] getIncrementManifest(Instant since) throws Exception {
        if (since == null) {
            throw new IllegalArgumentException("Parameter 'since' is required");
        }
        List<MalwareSignature> signatures = signatureRepository.findAllUpdatedAfter(since);
        return buildManifest(signatures, (byte) 1, since.toEpochMilli());
    }

    public byte[] getIncrementData(Instant since) throws Exception {
        List<MalwareSignature> signatures = signatureRepository.findAllUpdatedAfter(since);
        return buildData(signatures);
    }

    public byte[] getByIdsManifest(List<UUID> ids) throws Exception {
        if (ids == null || ids.isEmpty()) {
            return buildManifest(Collections.emptyList(), (byte) 2, -1);
        }
        List<MalwareSignature> signatures = signatureRepository.findAllByIds(ids);
        return buildManifest(signatures, (byte) 2, -1);
    }

    public byte[] getByIdsData(List<UUID> ids) throws Exception {
        if (ids == null || ids.isEmpty()) {
            return buildData(Collections.emptyList());
        }
        List<MalwareSignature> signatures = signatureRepository.findAllByIds(ids);
        return buildData(signatures);
    }

    private byte[] buildManifest(List<MalwareSignature> signatures, byte exportType, long sinceEpochMillis) throws Exception {
        byte[] dataBytes = buildData(signatures);

        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] dataSha256 = sha256.digest(dataBytes);

        ManifestEntry[] entries = new ManifestEntry[signatures.size()];
        long currentOffset = 0;

        for (int i = 0; i < signatures.size(); i++) {
            MalwareSignature signature = signatures.get(i);
            DataEntry dataEntry = toDataEntry(signature);
            byte[] dataEntryBytes = dataSerializer.serializeEntry(dataEntry);

            byte[] signatureBytes = Base64.getDecoder().decode(signature.getDigitalSignatureBase64());

            entries[i] = ManifestEntry.builder()
                    .id(signature.getId())
                    .statusCode(signature.getStatus() == SignatureStatus.ACTUAL ? (byte) 0 : (byte) 1)
                    .updatedAtEpochMillis(signature.getUpdatedAt().toEpochMilli())
                    .dataOffset(currentOffset)
                    .dataLength(dataEntryBytes.length)
                    .recordSignatureBytes(signatureBytes)
                    .build();

            currentOffset += dataEntryBytes.length;
        }

        Manifest manifest = Manifest.builder()
                .magic("MF-" + STUDENT_SURNAME)
                .version(VERSION)
                .exportType(exportType)
                .generatedAtEpochMillis(Instant.now().toEpochMilli())
                .sinceEpochMillis(sinceEpochMillis)
                .recordCount(signatures.size())
                .dataSha256(dataSha256)
                .entries(entries)
                .build();

        byte[] unsignedManifest = manifestSerializer.serializeWithoutSignature(manifest);

        byte[] manifestSignature = signingService.signBytesToBytes(unsignedManifest);

        return manifestSerializer.serializeWithSignature(manifest, manifestSignature);
    }

    private byte[] buildData(List<MalwareSignature> signatures) throws Exception {
        DataEntry[] entries = signatures.stream()
                .map(this::toDataEntry)
                .toArray(DataEntry[]::new);

        return dataSerializer.serializeDataFile(VERSION, entries);
    }

    private DataEntry toDataEntry(MalwareSignature signature) {
        byte[] firstBytes = hexStringToByteArray(signature.getFirstBytesHex());
        byte[] remainderHash = hexStringToByteArray(signature.getRemainderHashHex());

        return DataEntry.builder()
                .threatName(signature.getThreatName())
                .firstBytes(firstBytes)
                .remainderHash(remainderHash)
                .remainderLength(signature.getRemainderLength())
                .fileType(signature.getFileType())
                .offsetStart(signature.getOffsetStart())
                .offsetEnd(signature.getOffsetEnd())
                .build();
    }

    private byte[] hexStringToByteArray(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}