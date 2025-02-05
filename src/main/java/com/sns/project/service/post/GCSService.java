package com.sns.project.service.post;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class GCSService {

  @Value("${gcs.bucket-name}")
  private String bucketName;

  @Value("${gcs.credentials.file-path}")
  private String credentialsPath;

  private Storage getStorage() throws IOException {
    return StorageOptions.newBuilder()
        .setCredentials(GoogleCredentials.fromStream(Files.newInputStream(Paths.get(credentialsPath))))
        .build()
        .getService();
  }

  public String uploadFile(MultipartFile file) {
    try {
      String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();
      Storage storage = getStorage();

      BlobId blobId = BlobId.of(bucketName, fileName);
      BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
          .setContentType(file.getContentType())
          .build();

      storage.create(blobInfo, file.getBytes());

      return String.format("https://storage.googleapis.com/%s/%s", bucketName, fileName);
    } catch (IOException e) {
      throw new RuntimeException("GCS upload failed", e);
    }
  }
}
