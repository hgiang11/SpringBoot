package com.thg.chickenshop.config;

import com.thg.chickenshop.model.Category;
import com.thg.chickenshop.model.Product;
import com.thg.chickenshop.model.Role;
import com.thg.chickenshop.model.User;
import com.thg.chickenshop.repository.CategoryRepository;
import com.thg.chickenshop.repository.ProductRepository;
import com.thg.chickenshop.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;       // thêm field

    public DataInitializer(CategoryRepository categoryRepository,
                           ProductRepository productRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {      // thêm tham số
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedUsers();
        seedMenu();
    }

    private void seedUsers() {
        saveUser("admin", "Admin@123", "Quản trị viên", "0900000001", "Cửa hàng", Role.ADMIN);
        saveUser("customer1", "Customer@123", "Khách hàng mẫu", "0900000000", "123 Đường ABC", Role.CUSTOMER);
    }

    private void saveUser(String username, String rawPassword, String fullName,
                          String phone, String address, Role role) {
        User u = userRepository.findByUsername(username).orElseGet(User::new);
        if (u.getId() == null) {                       // tài khoản mới
            u.setUsername(username);
            u.setFullName(fullName);
            u.setPhone(phone);
            u.setAddress(address);
            u.setRole(role);
        }
        // Mật khẩu chưa mã hóa (chuỗi BCrypt luôn bắt đầu bằng "$2") thì mã hóa lại
        if (u.getPassword() == null || !u.getPassword().startsWith("$2")) {
            u.setPassword(passwordEncoder.encode(rawPassword));
        }
        userRepository.save(u);
    }
    public void seedMenu(String... args) {
        if (categoryRepository.count() > 0) return;

        Category chicken = categoryRepository.save(new Category("Gà rán"));
        Category burger  = categoryRepository.save(new Category("Burger"));
        Category side    = categoryRepository.save(new Category("Món ăn kèm"));
        Category drink   = categoryRepository.save(new Category("Nước uống"));

        productRepository.save(new Product("Gà rán 1 miếng", "Gà giòn sốt cay",
                new BigDecimal("35000"), "/images/ga-1-mieng.jpg", chicken));
        productRepository.save(new Product("Gà rán 2 miếng", "Combo 2 miếng gà giòn",
                new BigDecimal("65000"), "/images/ga-2-mieng.jpg", chicken));
        productRepository.save(new Product("Gà sốt mật ong", "Gà phủ sốt mật ong thơm ngọt",
                new BigDecimal("42000"), "/images/ga-mat-ong.jpg", chicken));
        productRepository.save(new Product("Burger gà", "Burger nhân gà giòn, rau tươi",
                new BigDecimal("45000"), "/images/burger-ga.jpg", burger));
        productRepository.save(new Product("Burger bò", "Burger bò nướng phô mai",
                new BigDecimal("55000"), "/images/burger-bo.jpg", burger));
        productRepository.save(new Product("Khoai tây chiên", "Phần vừa, giòn vàng",
                new BigDecimal("25000"), "/images/khoai-tay.jpg", side));
        productRepository.save(new Product("Salad bắp cải", "Salad tươi mát ăn kèm",
                new BigDecimal("20000"), "/images/salad.jpg", side));
        productRepository.save(new Product("Pepsi", "Lon 330ml",
                new BigDecimal("15000"), "/images/pepsi.jpg", drink));
        productRepository.save(new Product("Trà đào", "Ly lớn, nhiều đào",
                new BigDecimal("28000"), "/images/tra-dao.jpg", drink));
        productRepository.save(new Product("Combo 2 người", "2 gà, 1 khoai, 2 nước",
                new BigDecimal("129000"), "/images/combo-2.jpg", chicken));
    }
}