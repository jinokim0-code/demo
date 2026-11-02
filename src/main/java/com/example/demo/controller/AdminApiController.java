package com.example.demo.controller;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.domain.Member;
import com.example.demo.model.dto.MemberForm;
import com.example.demo.model.dto.MemberResponse;
import com.example.demo.model.dto.RoleRequest;
import com.example.demo.model.service.MemberService;

@RestController // JSON 응답 컨트롤러
@RequestMapping("/api/admin/members") // 이 클래스의 공통 주소 (관리자 전용)
public class AdminApiController {

    @Autowired
    private MemberService memberService;

    @GetMapping // GET /api/admin/members?keyword=student : 목록 + 검색
    public List<MemberResponse> list(@RequestParam(required = false) String keyword) {
        return memberService.search(keyword).stream()
                .map(MemberResponse::from) // Member → MemberResponse (비밀번호 제외)
                .toList();
    }

    @GetMapping("/{id}") // GET /api/admin/members/3 : 회원 1명
    public ResponseEntity<MemberResponse> get(@PathVariable Long id) {
        return memberService.findById(id)
                .map(MemberResponse::from)
                .map(ResponseEntity::ok) // 있으면 200 + 본문
                .orElseGet(() -> ResponseEntity.notFound().build()); // 없으면 404
    }

    @PostMapping // POST /api/admin/members : 회원 등록
    public ResponseEntity<MemberResponse> create(@RequestBody MemberForm form) {
        Member saved = memberService.signup(form); // 실패하면 예외 → ApiExceptionHandler 가 400
        URI location = URI.create("/api/admin/members/" + saved.getId());
        return ResponseEntity.created(location).body(MemberResponse.from(saved)); // 201
    }

    @PatchMapping("/{id}/role") // PATCH /api/admin/members/3/role : 권한 변경
    public ResponseEntity<MemberResponse> changeRole(@PathVariable Long id,
            @RequestBody RoleRequest request) {
        if (memberService.findById(id).isEmpty()) { // 대상이 없으면 404
            return ResponseEntity.notFound().build();
        }
        Member changed = memberService.changeRole(id, request.role()); // 실패하면 예외 → 400
        return ResponseEntity.ok(MemberResponse.from(changed)); // 200
    }

    @DeleteMapping("/{id}") // DELETE /api/admin/members/3 : 회원 삭제
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (memberService.findById(id).isEmpty()) { // 대상이 없으면 404
            return ResponseEntity.notFound().build();
        }
        memberService.deleteMember(id); // 실패하면 예외 → 400
        return ResponseEntity.noContent().build(); // 204
    }
}