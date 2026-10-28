package com.thg.chickenshop.controller;

import com.thg.chickenshop.model.Product;
import com.thg.chickenshop.model.User;
import com.thg.chickenshop.service.CartService;
import com.thg.chickenshop.service.CategoryService;
import com.thg.chickenshop.service.CurrentUserService;
import com.thg.chickenshop.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;
    @Mock
    private CategoryService categoryService;
    @Mock
    private CartService cartService;
    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private ProductController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // Cần một ViewResolver, nếu không MockMvc báo lỗi "Circular view path" vì tên view trùng đường dẫn /menu
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/");
        viewResolver.setSuffix(".html");

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setViewResolvers(viewResolver)
                .build();
    }

    private Page<Product> onePage() {
        Product p = new Product();
        p.setId(1L);
        p.setName("Ga ran");
        p.setPrice(new BigDecimal("35000"));
        return new PageImpl<>(List.of(p));
    }

    @Test
    @DisplayName("Khách chưa đăng nhập vẫn xem được menu, giỏ hiển thị 0")
    void menu_anonymous_showsMenuWithEmptyCart() throws Exception {
        when(productService.search(any(), any(), anyInt(), anyInt())).thenReturn(onePage());
        when(currentUserService.findCurrentUser()).thenReturn(Optional.empty());

        mockMvc.perform(get("/menu"))
                .andExpect(status().isOk())
                .andExpect(view().name("menu"))
                .andExpect(model().attributeExists("products", "categories"))
                .andExpect(model().attribute("cartCount", 0))
                .andExpect(model().attribute("active", "menu"));
    }

    @Test
    @DisplayName("Từ khóa, danh mục và số trang được chuyển đúng xuống Service")
    void menu_passesFilterParametersToService() throws Exception {
        when(productService.search("burger", 2L, 1, 8)).thenReturn(onePage());
        when(currentUserService.findCurrentUser()).thenReturn(Optional.empty());

        mockMvc.perform(get("/menu")
                        .param("q", "burger")
                        .param("category", "2")
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("q", "burger"))
                .andExpect(model().attribute("selectedCategory", 2L));

        verify(productService).search("burger", 2L, 1, 8);
    }

    @Test
    @DisplayName("Người đã đăng nhập thấy số món và tổng tiền trong giỏ")
    void menu_loggedIn_showsCartSummary() throws Exception {
        User user = new User();
        user.setId(1L);
        when(productService.search(any(), any(), anyInt(), anyInt())).thenReturn(onePage());
        when(currentUserService.findCurrentUser()).thenReturn(Optional.of(user));
        when(cartService.countItems(user)).thenReturn(3);
        when(cartService.getTotal(user)).thenReturn(new BigDecimal("115000"));

        mockMvc.perform(get("/menu"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("cartCount", 3))
                .andExpect(model().attribute("cartTotal", new BigDecimal("115000")));
    }
}