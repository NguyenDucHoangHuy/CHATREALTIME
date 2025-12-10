package com.hhy.apiserver.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {
    private final Cloudinary cloudinary;

    public CloudinaryService() {
        // Thay thế bằng thông tin thật của bạn
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dgch08jzm",
                "api_key", "316815716234384",
                "api_secret", "f-H4OIkpUGRq9ULpnEjA5Xvtx6E"
        ));
    }

    public String uploadFile(MultipartFile file, String folder) throws IOException {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            originalFilename = "file_" + System.currentTimeMillis();
        }

        // 1. Xử lý Public ID (Tên file trên Cloud)
        String publicId = originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
        String publicIdWithoutExt = publicId.indexOf('.') > 0
                ? publicId.substring(0, publicId.lastIndexOf('.'))
                : publicId;

        // 2. QUAN TRỌNG: Tự động xác định Resource Type
        // Mặc định là auto (cho ảnh, video)
        String resourceType = "auto";
        String lowerName = originalFilename.toLowerCase();

        // Nếu là tài liệu văn phòng -> Ép kiểu RAW
        if (lowerName.endsWith(".pdf") ||
                lowerName.endsWith(".doc") ||
                lowerName.endsWith(".docx") ||
                lowerName.endsWith(".xls") ||
                lowerName.endsWith(".xlsx") ||
                lowerName.endsWith(".ppt") ||
                lowerName.endsWith(".pptx") ||
                lowerName.endsWith(".txt") ||
                lowerName.endsWith(".zip") ||
                lowerName.endsWith(".rar")) {

            resourceType = "raw";
        }

        Map params = ObjectUtils.asMap(
                "folder", folder,
                "public_id", publicIdWithoutExt,
                "use_filename", true,
                "unique_filename", false, // Giữ nguyên tên file
                "overwrite", false,

                // 👇 Dùng biến đã tính toán thay vì fix cứng "auto"
                "resource_type", resourceType,

                "filename_override", originalFilename,
                // Với file raw, fl_attachment đôi khi không cần thiết nếu resource_type đúng,
                // nhưng để đó cũng an toàn.
                "fl_attachment", true
        );

        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), params);
        return (String) uploadResult.get("secure_url");
    }
}
