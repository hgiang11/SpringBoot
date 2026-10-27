package com.thg.chickenshop.service;

import com.thg.chickenshop.dto.RegisterForm;
import com.thg.chickenshop.exception.BusinessException;
import com.thg.chickenshop.model.Role;
import com.thg.chickenshop.model.User;
import com.thg.chickenshop.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(RegisterForm form) {
        String username = form.getUsername().trim();
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException("Tên đăng nhập đã tồn tại");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(form.getPassword()));   // lưu bản hash BCrypt, không lưu mật khẩu gốc
        user.setFullName(form.getFullName().trim());
        user.setPhone(form.getPhone().trim());
        user.setAddress(form.getAddress() == null ? null : form.getAddress().trim());
        user.setRole(Role.CUSTOMER);                                    // đăng ký công khai luôn là CUSTOMER
        return userRepository.save(user);
    }
}