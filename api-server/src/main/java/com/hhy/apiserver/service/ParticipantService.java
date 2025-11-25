package com.hhy.apiserver.service;

import com.hhy.apiserver.dto.request.participant.GroupActionRequestDTO;
import com.hhy.apiserver.entity.Conversation;
import com.hhy.apiserver.entity.Participant;
import com.hhy.apiserver.entity.User;
import com.hhy.apiserver.exception.AppException;
import com.hhy.apiserver.exception.ErrorCode;
import com.hhy.apiserver.respository.ConversationRepository;
import com.hhy.apiserver.respository.ParticipantRepository;
import com.hhy.apiserver.respository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class ParticipantService {

    private final ParticipantRepository participantRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    // 1. Thêm thành viên (Chỉ Admin mới được thêm)
    @Transactional
    public void addMember(User currentUser, GroupActionRequestDTO request) {
        // Check quyền Admin
        validateAdminRole(currentUser.getUserId(), request.getConversationId());

        // Check User tồn tại
        User targetUser = userRepository.findById(request.getTargetUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Check xem đã trong nhóm chưa
        if (participantRepository.findByUserIdAndConversationId(targetUser.getUserId(), request.getConversationId()).isPresent()) {
            throw new RuntimeException("Người này đã ở trong nhóm");
        }

        Conversation conversation = conversationRepository.findById(request.getConversationId())
                .orElseThrow(() -> new RuntimeException("Nhóm không tồn tại"));

        // Thêm vào
        Participant newMember = new Participant();
        newMember.setConversation(conversation);
        newMember.setUser(targetUser);
        newMember.setUserId(targetUser.getUserId());
        newMember.setConversationId(conversation.getConversationId());
        newMember.setRole(Participant.Role.member);
        newMember.setStatus(Participant.ConversationStatus.active);
        newMember.setJoinedAt(new Date());

        participantRepository.save(newMember);
    }

    // 2. Xóa thành viên (Kick) (Chỉ Admin mới được xóa người khác)
    @Transactional
    public void removeMember(User currentUser, GroupActionRequestDTO request) {
        validateAdminRole(currentUser.getUserId(), request.getConversationId());

        Participant target = participantRepository.findByUserIdAndConversationId(request.getTargetUserId(), request.getConversationId())
                .orElseThrow(() -> new RuntimeException("Thành viên không tồn tại trong nhóm"));

        // Không thể kick chính mình (dùng hàm Leave)
        if (target.getUserId().equals(currentUser.getUserId())) {
            throw new RuntimeException("Không thể tự kick mình, hãy dùng chức năng Rời nhóm");
        }

        participantRepository.delete(target);
    }

    // 3. Rời nhóm (Ai cũng làm được)
    @Transactional
    public void leaveGroup(User currentUser, GroupActionRequestDTO request) {
        Participant self = participantRepository.findByUserIdAndConversationId(currentUser.getUserId(), request.getConversationId())
                .orElseThrow(() -> new RuntimeException("Bạn không ở trong nhóm này"));

        participantRepository.delete(self);

        // TODO: Logic nâng cao: Nếu Admin rời nhóm thì phải chuyển quyền Admin cho người khác
        // hoặc giải tán nhóm. (Hiện tại MVP ta cứ để user rời bình thường).
    }

    // 4. Thăng cấp Admin
    @Transactional
    public void promoteAdmin(User currentUser, GroupActionRequestDTO request) {
        validateAdminRole(currentUser.getUserId(), request.getConversationId());

        Participant target = participantRepository.findByUserIdAndConversationId(request.getTargetUserId(), request.getConversationId())
                .orElseThrow(() -> new RuntimeException("Thành viên không tồn tại"));

        target.setRole(Participant.Role.admin);
        participantRepository.save(target);
    }

    // 5. Hạ cấp Admin
    @Transactional
    public void demoteAdmin(User currentUser, GroupActionRequestDTO request) {
        validateAdminRole(currentUser.getUserId(), request.getConversationId());

        Participant target = participantRepository.findByUserIdAndConversationId(request.getTargetUserId(), request.getConversationId())
                .orElseThrow(() -> new RuntimeException("Thành viên không tồn tại"));

        if (target.getUserId().equals(currentUser.getUserId())) {
            throw new RuntimeException("Không thể tự hạ cấp mình");
        }

        target.setRole(Participant.Role.member);
        participantRepository.save(target);
    }

    // --- Helper: Kiểm tra quyền Admin ---
    private void validateAdminRole(Long userId, Long conversationId) {
        Participant participant = participantRepository.findByUserIdAndConversationId(userId, conversationId)
                .orElseThrow(() -> new RuntimeException("Bạn không ở trong nhóm này"));

        if (participant.getRole() != Participant.Role.admin) {
            // Bạn nên thêm ErrorCode.UNAUTHORIZED_ACTION vào Enum ErrorCode
            throw new RuntimeException("Bạn không phải là Admin của nhóm");
        }

        // Check thêm: Phải là conversation type GROUP
        if (participant.getConversation().getType() != Conversation.ConversationType.group) {
            throw new RuntimeException("Đây không phải là nhóm chat");
        }
    }
}