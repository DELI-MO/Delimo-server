package com.cos.security1.controller;

import com.cos.security1.controller.response.EmailVerificationResponse;
import com.cos.security1.controller.response.AuthResponse;
import com.cos.security1.controller.response.SigninResponse;
import com.cos.security1.controller.status.ResponseMessage;
import com.cos.security1.controller.status.StatusCode;
import com.cos.security1.domain.Member;
import com.cos.security1.dto.EmailDto;
import com.cos.security1.dto.MemberDto;
import com.cos.security1.dto.ResolutionDto;
import com.cos.security1.dto.TokenDto;
import com.cos.security1.service.MemberService;
import jakarta.transaction.Transactional;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Optional;

@RestController
@Transactional
@RequestMapping("/users")
public class MemberController {
    private final MemberService memberService;

    @Autowired
    MemberController(MemberService memberService){
        this.memberService = memberService;
    }

    /**
     * 회원 가입
     * @param user
     * @return
     */
    @PostMapping("/new")
    public ResponseEntity<SigninResponse> signIn(@RequestBody MemberDto user){
        SigninResponse response = new SigninResponse();

        Optional<Member> memberFind = memberService.getUserByEmail(user.getEmail());
        if(memberFind.isEmpty()){
            Member member = memberService.insertUser(user);
            TokenDto tokenDto = new TokenDto(member.getToken());

            response.builder()
                        .code(StatusCode.OK)
                        .message(ResponseMessage.SIGNIN_SUCCESS)
                        .data(tokenDto);

            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Email 중복 체크
     * @param emailDto
     * @return
     */
    @GetMapping("/verifyEmail")
    public ResponseEntity<EmailVerificationResponse> verifyEmail(@RequestBody EmailDto emailDto){
        EmailVerificationResponse response = new EmailVerificationResponse();

        Optional<Member> memberFind = memberService.getUserByEmail(emailDto.getEmail());
        if (memberFind.isEmpty()){

            response.builder()
                    .code(StatusCode.OK)
                    .message(ResponseMessage.EMAIL_OK);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
    }

    Optional<Member> findMember(String token){
        String bearerToken = token.substring(7);
        return memberService.verifyMember(bearerToken);
    }

    /**
     * 다짐 추가 및 수정
     * @param token
     * @param resolutionDto
     * @return
     */
    @PatchMapping("/updateResolution")
    public ResponseEntity<AuthResponse> updateResolution(
            @RequestHeader("Authorization") String token,
            @RequestBody ResolutionDto resolutionDto) {

        AuthResponse response = new AuthResponse();

        Optional<Member> member = findMember(token);
        if (member.isEmpty()){
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        memberService.updateResolution(member.get().getId(), resolutionDto.getResolution());

        response.builder()
                .code(StatusCode.OK)
                .message(ResponseMessage.RESOLUTION_UPDATED)
                .data(resolutionDto);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 나의 정보 가져오기 (마이 페이지)
     * @param token
     * @return
     */
    @GetMapping("/myPage")
    public ResponseEntity<AuthResponse> getMyInfo(@RequestHeader("Authorization") String token) {
        AuthResponse response = new AuthResponse();

        Optional<Member> member = findMember(token);
        if (member.isEmpty()) {
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        response.builder()
                .code(StatusCode.OK)
                .message(ResponseMessage.MEMBER_INFO_SUCCESS)
                .data(member);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping
    public List<Member> getAllUsers(){
        return memberService.getAllUsers();
    }

    @GetMapping("/{email}")
    public Optional<Member> getUserByEmail(@PathVariable String email){
        return memberService.getUserByEmail(email);
    }
}
