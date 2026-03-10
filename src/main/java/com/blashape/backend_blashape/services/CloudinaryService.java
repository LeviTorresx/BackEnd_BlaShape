package com.blashape.backend_blashape.services;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    // -------------------------------
    //  SUBIR IMÁGENES
    // -------------------------------
    public String uploadImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        Map uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "resource_type", "image",
                        "folder", "furniture/images"
                )
        );

        return uploadResult.get("secure_url").toString();
    }

    // -------------------------------
    //  SUBIR DOCUMENTOS / PDF
    // -------------------------------
    public String uploadDocument(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        Map uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "resource_type", "auto",        // detecta PDF, DOCX, etc
                        "folder", "furniture/documents",
                        "public_id", removeExtension(file.getOriginalFilename())
                )
        );

        return uploadResult.get("secure_url").toString();
    }

    // Quita extensión al nombre
    private String removeExtension(String filename) {
        if (filename == null) return "file";
        return filename.replaceFirst("[.][^.]+$", "");
    }
}
