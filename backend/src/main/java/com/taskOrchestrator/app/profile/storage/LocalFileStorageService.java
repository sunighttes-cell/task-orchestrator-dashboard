package com.taskOrchestrator.app.profile.storage;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.taskOrchestrator.app.config.StorageProperties;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

//LocalFileStorageService: validate type // validate size // generate UUID // save file // return URL

@Service
public class LocalFileStorageService implements FileStorageService {
    private final StorageProperties StorageProperties;

    public LocalFileStorageService(StorageProperties StorageProperties) {
        this.StorageProperties = StorageProperties;
    }

    @Override
    public String store(MultipartFile file) throws IOException {
        //Set Maximum Size to 5MB
        long MAX_SIZE = 1024 * 1024 * 5;

        Path uploadPath = Paths.get(StorageProperties.getUploadDir());
        Files.createDirectories(uploadPath);

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File is not an image");
        }

        if (file.getSize() > MAX_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum size");
        }

        String newFileName = UUID.randomUUID().toString() + ".png";
        Path destinationFile = uploadPath.resolve(newFileName);
        //copy file to destination and replace existing file if exists
        Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);

        System.out.println("File uploaded successfully" + uploadPath.resolve(newFileName));
        System.out.println("New File Name" + destinationFile);

        return destinationFile.toString();
    }
}