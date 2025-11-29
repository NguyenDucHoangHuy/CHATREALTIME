package com.hhy.apiserver.service;

import com.hhy.apiserver.dto.request.conversation.CreateGroupRequestDTO;
import com.hhy.apiserver.dto.request.conversation.OpenConversationRequestDTO;
import com.hhy.apiserver.dto.response.ConversationDTO;
import com.hhy.apiserver.entity.Conversation;
import com.hhy.apiserver.entity.Friendship;
import com.hhy.apiserver.entity.Participant;
import com.hhy.apiserver.entity.User;
import com.hhy.apiserver.exception.AppException;
import com.hhy.apiserver.exception.ErrorCode;
import com.hhy.apiserver.respository.ConversationRepository;
import com.hhy.apiserver.respository.FriendshipRepository;
import com.hhy.apiserver.respository.ParticipantRepository;
import com.hhy.apiserver.respository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final ParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;

    // 1. Mở hoặc lấy cuộc hội thoại 1-1 (LOGIC MỚI)
    @Transactional
    public ConversationDTO openOneToOneConversation(User currentUser, OpenConversationRequestDTO request) {
        Long targetUserId = request.getUserId();

        if (currentUser.getUserId().equals(targetUserId)) {
            throw new RuntimeException("Không thể chat với chính mình");
        }

        // A. Kiểm tra đã có hội thoại chưa
        Optional<Conversation> existing = conversationRepository.findExistingOneToOneConversation(
                currentUser.getUserId(), targetUserId);

        if (existing.isPresent()) {
            return mapToConversationDTO(existing.get(), currentUser.getUserId());
        }

        // B. Nếu chưa có -> Tạo mới
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Conversation conversation = new Conversation();
        conversation.setType(Conversation.ConversationType.one_to_one);
        conversation.setCreatedAt(new Date());
        Conversation savedConv = conversationRepository.save(conversation);

        // --- KIỂM TRA TÌNH TRẠNG BẠN BÈ ---
        boolean isFriend = false;
        Optional<Friendship> friendship = friendshipRepository.findFriendshipBetweenUsers(
                currentUser.getUserId(), targetUserId);

        if (friendship.isPresent() && friendship.get().getStatus() == Friendship.FriendshipStatus.accepted) {
            isFriend = true;
        }

        // --- THÊM PARTICIPANTS ---
        // Lưu ý: Sửa hàm addParticipant để nó TRẢ VỀ đối tượng Participant vừa tạo
        Participant p1 = addParticipant(savedConv, currentUser, Participant.Role.member, Participant.ConversationStatus.active);

        Participant.ConversationStatus targetStatus = isFriend
                ? Participant.ConversationStatus.active
                : Participant.ConversationStatus.pending;

        Participant p2 = addParticipant(savedConv, targetUser, Participant.Role.member, targetStatus);

        // ⚠️ QUAN TRỌNG: Gán danh sách participants vào conversation để map không bị null
        savedConv.setParticipants(Set.of(p1, p2));

        return mapToConversationDTO(savedConv, currentUser.getUserId());
    }

    // 2. Tạo nhóm
    @Transactional
    public ConversationDTO createGroup(User currentUser, CreateGroupRequestDTO request) {
        Conversation conversation = new Conversation();
        conversation.setType(Conversation.ConversationType.group);
        conversation.setGroupName(request.getGroupName());
        conversation.setGroupAvatarUrl("https://via.placeholder.com/150");
        conversation.setCreatedAt(new Date());

        Conversation savedConv = conversationRepository.save(conversation);

        // Admin luôn Active
        addParticipant(savedConv, currentUser, Participant.Role.admin, Participant.ConversationStatus.active);

        // Thành viên khác: Tùy bạn, thường tạo nhóm thì add thẳng vào (Active)
        List<User> members = userRepository.findAllById(request.getMemberIds());
        for (User member : members) {
            addParticipant(savedConv, member, Participant.Role.member, Participant.ConversationStatus.active);
        }

        return mapToConversationDTO(savedConv, currentUser.getUserId());
    }

    // 3. Lấy danh sách hội thoại
    public List<ConversationDTO> getMyConversations(User currentUser) {
        // Dùng Repository để lấy danh sách conversation user đang tham gia
        // (Giả sử bạn đã có hàm này trong ParticipantRepository từ các bước trước)
        List<Conversation> conversations = participantRepository.findActiveConversationsByUserId(currentUser.getUserId());

        return conversations.stream()
                .map(c -> mapToConversationDTO(c, currentUser.getUserId()))
                .collect(Collectors.toList());
    }

    // 4. Đổi tên nhóm
    @Transactional
    public void renameGroup(Long conversationId, String newName) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cuộc trò chuyện"));

        if(conversation.getType() != Conversation.ConversationType.group) {
            throw new RuntimeException("Không thể đổi tên chat 1-1");
        }

        conversation.setGroupName(newName);
        conversationRepository.save(conversation);
    }

    // 5. Xóa (Ẩn) hội thoại
    @Transactional
    public void deleteConversation(User currentUser, Long conversationId) {
        Participant participant = participantRepository.findByUserIdAndConversationId(currentUser.getUserId(), conversationId)
                .orElseThrow(() -> new RuntimeException("Bạn không ở trong cuộc trò chuyện này"));

        // Xóa bản ghi Participant -> User sẽ không thấy cuộc trò chuyện này nữa
        // (Nhưng dữ liệu chat vẫn còn cho người kia xem)
        participantRepository.delete(participant);

        // Nâng cao: Nếu nhóm không còn ai -> Xóa luôn Conversation (làm sau nếu kịp)
    }

    // 6. Lấy danh sách tin nhắn đang chờ (Pending)
    public List<ConversationDTO> getPendingConversations(User currentUser) {
        // Gọi hàm findPendingConversationsByUserId đã viết trong ParticipantRepository
        List<Conversation> conversations = participantRepository.findPendingConversationsByUserId(currentUser.getUserId());

        return conversations.stream()
                .map(c -> mapToConversationDTO(c, currentUser.getUserId()))
                .collect(Collectors.toList());
    }

    // 7. Chấp nhận tin nhắn chờ (Chuyển từ Pending -> Active)
    // (Bạn sẽ cần cái này khi người dùng bấm "Chấp nhận" trong giao diện)
    @Transactional
    public void acceptPendingConversation(User currentUser, Long conversationId) {
        Participant participant = participantRepository.findByUserIdAndConversationId(currentUser.getUserId(), conversationId)
                .orElseThrow(() -> new RuntimeException("Bạn không tham gia cuộc trò chuyện này"));

        if (participant.getStatus() == Participant.ConversationStatus.pending) {
            participant.setStatus(Participant.ConversationStatus.active);
            participantRepository.save(participant);
        }
    }

    // 8. Từ chối tin nhắn chờ -> Thực ra chính là hàm deleteConversation (số 5) bạn đã có.
    // Khi xóa participant, cuộc trò chuyện sẽ biến mất khỏi danh sách.



    // --- Helper Methods ---

    private Participant addParticipant(Conversation c, User u, Participant.Role role, Participant.ConversationStatus status) {
        Participant p = new Participant();
        p.setConversation(c);
        p.setUser(u);
        p.setUserId(u.getUserId());
        p.setConversationId(c.getConversationId());
        p.setRole(role);
        p.setStatus(status);
        p.setJoinedAt(new Date());

        return participantRepository.save(p); // Return
    }
    // Hàm quan trọng: Chuyển Entity -> DTO và tính toán tên hiển thị
    private ConversationDTO mapToConversationDTO(Conversation c, Long currentUserId) {
        ConversationDTO dto = new ConversationDTO();
        dto.setConversationId(c.getConversationId());
        dto.setType(c.getType());

        // Logic hiển thị Tên và Avatar
        if (c.getType() == Conversation.ConversationType.group) {
            dto.setDisplayName(c.getGroupName());
            dto.setDisplayAvatarUrl(c.getGroupAvatarUrl());
        } else {
            // Nếu là 1-1, tìm người "kia"
            // Lọc trong list participants, lấy người có ID khác currentUserId
            Optional<Participant> other = c.getParticipants().stream()
                    .filter(p -> !p.getUserId().equals(currentUserId))
                    .findFirst();

            if (other.isPresent()) {
                User u = other.get().getUser();
                dto.setDisplayName(u.getUsername());
                dto.setDisplayAvatarUrl(u.getAvatarUrl());
                // ✅ MAP DỮ LIỆU ONLINE/LAST SEEN TỪ USER SANG DTO
                if (u.getOnlineStatus() != null) {
                    dto.setOnlineStatus(u.getOnlineStatus().toString());
                }
                dto.setLastSeen(u.getLastSeen());
            } else {
                dto.setDisplayName("Unknown User");
            }
        }

        // Map tin nhắn cuối cùng (nếu có)
        if (c.getLastMessage() != null) {
            dto.setLastMessageContent(c.getLastMessage().getMessageContent());
            dto.setLastMessageTimestamp(c.getLastMessage().getCreatedAt());
            dto.setLastMessageSenderId(c.getLastMessage().getSender().getUserId());
        }

        return dto;
    }
}