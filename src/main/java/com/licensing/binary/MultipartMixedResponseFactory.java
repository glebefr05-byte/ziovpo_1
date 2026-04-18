package com.licensing.binary;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Slf4j
@Component
public class MultipartMixedResponseFactory {
    public ResponseEntity<MultiValueMap<String, Object>> create(byte[] manifestBytes, byte[] dataBytes) {
        LinkedMultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("manifest", createPart("manifest.bin", manifestBytes));
        body.add("data", createPart("data.bin", dataBytes));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("multipart/mixed"));
        return ResponseEntity.ok()
                .headers(headers)
                .body(body);
    }

    private HttpEntity<ByteArrayResource> createPart(String filename, byte[] content) {
        HttpHeaders partHeaders = new HttpHeaders();
        partHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        partHeaders.setContentDisposition(ContentDisposition.attachment().filename(filename).build());

        ByteArrayResource resource = new ByteArrayResource(content) {
            @Override
            public String getFilename() {
                return filename;
            }
        };
        return new HttpEntity<>(resource, partHeaders);
    }
}