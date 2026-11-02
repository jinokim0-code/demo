package com.example.demo.model.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.demo.model.domain.Member;
import com.example.demo.model.dto.MemberForm;
import com.example.demo.model.repository.MemberRepository;

@Service // 서비스 등록
public class MemberService implements UserDetailsService { // 로그인을 위한 인터페이스 제공
    @Autowired
    private MemberRepository memberRepository;

    @Autowired // SecurityConfig 에 등록한 BCryptPasswordEncoder 주입
    private PasswordEncoder passwordEncoder;

    public Member signup(MemberForm form) { // 회원가입
        // [7주차] 빈 값 검사 : JSON 으로는 필드를 빼고 보낼 수 있다
        if (form.getUsername() == null || form.getUsername().isBlank()
                || form.getPassword() == null || form.getPassword().isBlank()
                || form.getName() == null || form.getName().isBlank()) {
            throw new IllegalArgumentException("아이디, 비밀번호, 이름은 필수입니다.");
        }
        if (form.getPassword() == null
                || !form.getPassword().equals(form.getPasswordConfirm())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        if (memberRepository.existsByUsername(form.getUsername())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        Member member = new Member();
        member.setUsername(form.getUsername());
        member.setPassword(passwordEncoder.encode(form.getPassword())); // ★ 암호화
        member.setName(form.getName());
        member.setRole("USER"); // 기본 권한
        return memberRepository.save(member); // INSERT
    }

    @Override // 로그인 시 시큐리티가 자동 호출
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("회원 없음 : " + username));

        return User.builder() // 시큐리티용 사용자 객체로 변환
                .username(member.getUsername())
                .password(member.getPassword()) // 암호화 값 → matches()로 비교
                .roles(member.getRole()) // USER → ROLE_USER
                .build();
    }

    // [6주차] 로그인한 아이디로 회원 조회
    public Member findByUsername(String username) {
        return memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException(
                        "회원이 존재하지 않습니다 : " + username));
    }

    // [6주차] 전체 회원 (id 순서)
    public List<Member> findAll() {
        return memberRepository.findAll(Sort.by("id"));
    }

    // [연습①] 검색어가 없으면 전체, 있으면 아이디·이름에 포함된 회원
    public List<Member> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAll();
        }
        String k = keyword.trim();
        return memberRepository.findByUsernameContainingOrNameContainingOrderByIdAsc(k, k);
    }

    // [7주차] 번호로 회원 1명 조회 (없으면 빈 Optional → 컨트롤러가 404 처리)
    public Optional<Member> findById(Long id) {
        return memberRepository.findById(id);
    }

    // 관리자가 부여할 수 있는 권한 (화면에서 넘어온 값을 그대로 믿지 않는다)
    private static final List<String> ROLES = List.of("USER", "MANAGER", "ADMIN");

    // 자기 자신의 권한 변경·삭제 금지
    private void checkNotSelf(Member target) {
        String me = SecurityContextHolder.getContext().getAuthentication().getName();
        if (target.getUsername().equals(me)) {
            throw new IllegalArgumentException("자기 자신의 권한 변경·삭제는 할 수 없습니다.");
        }
    }

    @PreAuthorize("hasRole('ADMIN')") // 관리자만 실행 가능 (2차 잠금)
    public Member changeRole(Long id, String role) { // 권한 변경 → [7주차] 변경된 회원 반환
        if (!ROLES.contains(role)) { // 허용된 권한만
            throw new IllegalArgumentException("허용되지 않는 권한입니다 : " + role);
        }
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));
        checkNotSelf(member);
        member.setRole(role);
        return memberRepository.save(member); // UPDATE (id 가 있으면 수정)
    }

    @PreAuthorize("hasRole('ADMIN')") // 관리자만 실행 가능 (2차 잠금)
    public void deleteMember(Long id) { // 회원 삭제
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));
        checkNotSelf(member);
        memberRepository.delete(member); // DELETE
    }
}