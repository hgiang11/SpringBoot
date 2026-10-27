package com.thg.chickenshop.service;

import com.thg.chickenshop.exception.BusinessException;
import com.thg.chickenshop.model.User;
import com.thg.chickenshop.repository.UserRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** Người đang đăng nhập, hoặc rỗng nếu là khách chưa đăng nhập. Dùng cho trang công khai. */
    public Optional<User> findCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        return userRepository.findByUsername(auth.getName());
    }

    /** Dùng ở các trang đã bắt buộc đăng nhập (giỏ hàng, đơn hàng). */
    public User getCurrentUser() {
        return findCurrentUser()
                .orElseThrow(() -> new BusinessException("Vui lòng đăng nhập để tiếp tục"));
    }

    public boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated()
                && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}