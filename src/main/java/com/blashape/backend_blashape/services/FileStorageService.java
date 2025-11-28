package com.blashape.backend_blashape.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class  FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    public String saveFile(MultipartFile file, String specificDir) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            Path uploadPath = Paths.get(uploadDir, specificDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String uniqueName = UUID.randomUUID() + "-" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(uniqueName);

            file.transferTo(filePath.toFile());

            // Retornamos una ruta accesible
            return "/files/" + specificDir + "/" + uniqueName;

        } catch (IOException e) {
            throw new RuntimeException("Error saving file: " + e.getMessage(), e);
        }
    }

    public boolean deleteFile(String relativePath) {
        try {
            String cleanedPath = relativePath.replaceFirst("^/files", "");
            Path filePath = Paths.get(uploadDir, cleanedPath);

            return Files.deleteIfExists(filePath);

        } catch (IOException e) {
            throw new RuntimeException("Error eliminando el archivo: " + e.getMessage(), e);
        }
    }


}
