package com.example.sem4.exercise2.service;

import com.example.sem4.exercise2.dto.MemberForm;
import com.example.sem4.exercise2.entity.Member;
import com.example.sem4.exercise2.exception.BusinessException;
import com.example.sem4.exercise2.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Member get(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy độc giả"));
    }

    @Transactional(rollbackFor = Exception.class)
    public Member save(MemberForm form) {
        String code = form.getMemberCode().trim();
        String email = form.getEmail().trim();
        Member member;
        if (form.getId() == null) {
            if (memberRepository.existsByMemberCode(code)) {
                throw new BusinessException("Mã độc giả đã tồn tại");
            }
            if (memberRepository.existsByEmail(email)) {
                throw new BusinessException("Email đã tồn tại");
            }
            member = new Member();
        } else {
            member = get(form.getId());
            if (memberRepository.existsByMemberCodeAndIdNot(code, member.getId())) {
                throw new BusinessException("Mã độc giả đã tồn tại");
            }
            if (memberRepository.existsByEmailAndIdNot(email, member.getId())) {
                throw new BusinessException("Email đã tồn tại");
            }
        }
        member.setMemberCode(code);
        member.setFullName(form.getFullName().trim());
        member.setEmail(email);
        member.setPhone(form.getPhone().trim());
        return memberRepository.save(member);
    }
}
