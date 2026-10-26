package com.thg.chickenshop.service;

import com.thg.chickenshop.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private final Path root = Paths.get("uploads");

    /** Trả về đường dẫn ảnh (vd /uploads/abc.jpg), hoặc null nếu không có file được chọn. */
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String contentType = file.getContentType();

        if (ext == null || !ALLOWED_EXTENSIONS.contains(ext.toLowerCase())
                || contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException("Chỉ chấp nhận ảnh JPG, PNG hoặc WEBP");
        }

        try {
            Files.createDirectories(root);
            String newName = UUID.randomUUID() + "." + ext.toLowerCase();
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, root.resolve(newName), StandardCopyOption.REPLACE_EXISTING);
            }
            return "/uploads/" + newName;
        } catch (IOException e) {
            throw new BusinessException("Không thể lưu ảnh, vui lòng thử lại");
        }
    }
}