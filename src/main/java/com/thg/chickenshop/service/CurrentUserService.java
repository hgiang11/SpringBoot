package com.thg.chickenshop.service;

import com.thg.chickenshop.exception.NotFoundException;
import com.thg.chickenshop.model.User;
import com.thg.chickenshop.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    // TẠM THỜI: Ngày 5 sẽ đổi thành lấy người đăng nhập thật từ Spring Security
    private static final String TEMP_USERNAME = "customer1";

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        return userRepository.findByUsername(TEMP_USERNAME)
                .orElseThrow(() -> new NotFoundException("Chưa có tài khoản khách mẫu"));
    }
}