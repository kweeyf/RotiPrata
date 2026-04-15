package com.rotiprata.application;

import com.rotiprata.api.content.domain.ContentType;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface MediaProcessingService {

    void processUpload(UUID contentId, ContentType contentType, MultipartFile file);

    void processLink(UUID contentId, String sourceUrl);

    void processLessonUpload(UUID assetId, String mediaKind, MultipartFile file);

    void processLessonLink(UUID assetId, String mediaKind, String sourceUrl);
}
