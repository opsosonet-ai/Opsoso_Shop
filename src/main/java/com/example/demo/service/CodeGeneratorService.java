package com.example.demo.service;

import com.example.demo.repository.NhaPhanPhoiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CodeGeneratorService {
    
    @Autowired
    private NhaPhanPhoiRepository nhaPhanPhoiRepository;
    
    /**
     * Tạo mã nhà phân phối tự động theo định dạng NPPxxx
     * Ví dụ: NPP001, NPP002, NPP003, ...
     * Đảm bảo không trùng lặp với mã hiện có
     */
    public String generateNhaPhanPhoiCode() {
        int counter = 1;
        String code;
        
        // Tìm mã tiếp theo không trùng lặp
        do {
            code = "NPP%03d".formatted(counter);
            counter++;
            // Validate code is not null before checking in repository
            if (code == null || code.trim().isEmpty()) {
                throw new RuntimeException("Generated code is invalid");
            }
        } while (nhaPhanPhoiRepository.existsById(code) && counter <= 9999);
        
        // Nếu vượt quá 9999, báo lỗi
        if (counter > 9999) {
            throw new RuntimeException("Không thể tạo mã nhà phân phối - đã hết số khả dụng");
        }
        
        return code;
    }
}
