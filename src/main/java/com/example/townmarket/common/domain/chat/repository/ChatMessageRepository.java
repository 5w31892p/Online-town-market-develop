package com.example.townmarket.common.domain.chat.repository;

import com.example.townmarket.common.domain.chat.entity.ChatMessage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
  List<ChatMessage> findAllByOrderBySendDateAsc();

}
