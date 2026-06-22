package com.demo.my_backend_java.controller; 

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@CrossOrigin(origins = "*") // Mở cửa cho App gọi vào không bị chặn
public class TestController {

    @GetMapping("/api/test")
    public String chaoSinhVien() {
        return "Kết nối thành công! Đây là dữ liệu trả về từ Java Backend của em.";
    }
}