package com.project.tekwira.Services.implementations;

import com.project.tekwira.Services.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageServiceImpl  implements FileStorageService {

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";
    @Override
    public String storeFile(MultipartFile file) throws IOException {
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        File destination = new File(UPLOAD_DIR + fileName);
        file.transferTo(destination);

        return "/uploads/" + fileName;
    }

    @Override
    public List<String> storeFiles(List<MultipartFile> files) throws IOException {
        List<String> urls = new ArrayList<>();
        if (files != null){
            for (MultipartFile file : files) {
                urls.add(storeFile(file));
            }
        }
        return urls;
    }
}
