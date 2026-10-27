package com.thg.chickenshop.service;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
public class MessageService {

    private final MessageSource messageSource;

    public MessageService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /** Tra key theo ngôn ngữ hiện tại. Không có key thì trả lại chính chuỗi đó (nên chữ tiếng Việt cũ vẫn chạy). */
    public String get(String keyOrText, Object... args) {
        return messageSource.getMessage(keyOrText, args, keyOrText, LocaleContextHolder.getLocale());
    }
}