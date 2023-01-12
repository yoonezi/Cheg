package com.likelion.cheg.web.controller;

import com.likelion.cheg.domain.user.User;
import com.likelion.cheg.domain.user.UserRepository;
import com.likelion.cheg.handler.ex.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@RequiredArgsConstructor
@Controller
public class UserController {

    private final UserRepository userRepository;
    @GetMapping("/mypage/{userId}")
    public String myPage(@PathVariable int userId, Model model){
        User user = userRepository.findById(userId).orElseThrow(()->{
            return new CustomException("사용자가 존재하지 않습니다.");
        });
        model.addAttribute("user",user);
        return "user/mypage";

    }
}
