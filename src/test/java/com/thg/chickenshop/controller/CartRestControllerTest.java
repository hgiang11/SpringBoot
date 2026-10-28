package com.thg.chickenshop.controller;

import com.thg.chickenshop.exception.ApiExceptionHandler;
import com.thg.chickenshop.exception.BusinessException;
import com.thg.chickenshop.model.User;
import com.thg.chickenshop.service.CartService;
import com.thg.chickenshop.service.CurrentUserService;
import com.thg.chickenshop.service.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CartRestControllerTest {

    @Mock
    private CartService cartService;
    @Mock
    private CurrentUserService currentUserService;

    private MockMvc mockMvc;
    private User user;

    @BeforeEach
    void setUp() {
        // MessageSource rỗng: MessageService trả lại chính key, nên test không phụ thuộc file ngôn ngữ
        MessageService messageService = new MessageService(new StaticMessageSource());
        CartRestController controller = new CartRestController(cartService, currentUserService, messageService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ApiExceptionHandler(messageService))
                .build();

        user = new User();
        user.setId(1L);
    }

    @Test
    @DisplayName("Thêm món thành công trả JSON gồm số món và tổng tiền")
    void add_success_returnsCartSummary() throws Exception {
        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(cartService.countItems(user)).thenReturn(3);
        when(cartService.getTotal(user)).thenReturn(new BigDecimal("115000"));

        mockMvc.perform(post("/api/cart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":1,\"quantity\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("cart.added"))
                .andExpect(jsonPath("$.cartCount").value(3));

        verify(cartService).addItem(user, 1L, 1);
    }

    @Test
    @DisplayName("Số lượng không hợp lệ trả 400 dạng JSON")
    void add_invalidQuantity_returns400() throws Exception {
        mockMvc.perform(post("/api/cart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":1,\"quantity\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("cart.qtyRange"));
    }

    @Test
    @DisplayName("Lỗi nghiệp vụ từ Service trả 400 kèm thông báo")
    void add_businessError_returns400WithMessage() throws Exception {
        when(currentUserService.getCurrentUser()).thenReturn(user);
        doThrow(new BusinessException("cart.maxPerItem", 20))
                .when(cartService).addItem(user, 1L, 1);

        mockMvc.perform(post("/api/cart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":1,\"quantity\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("cart.maxPerItem"));
    }
}