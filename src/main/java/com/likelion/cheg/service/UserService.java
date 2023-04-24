package com.likelion.cheg.service;

import com.likelion.cheg.domain.cart.CartRepository;
import com.likelion.cheg.domain.order.OrderRepository;
import com.likelion.cheg.domain.product.Product;
import com.likelion.cheg.domain.user.User;
import com.likelion.cheg.domain.user.UserRepository;
import com.likelion.cheg.handler.ex.CustomException;
import com.likelion.cheg.handler.ex.CustomValidationApiException;
import com.likelion.cheg.web.dto.user.UserUpdateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    @Transactional
    public void deleteUser(int userId){
        try{
            userRepository.deleteById(userId); //회원 삭제
            orderRepository.deleteByUserId(userId); //회원 주문 삭제

        }catch(Exception e){
            throw new CustomException(e.getMessage());
        }
    }

    @Transactional
    public User update(int userId, UserUpdateDto userUpdateDto){

        User user = userRepository.findById(userId).orElseThrow(()->{
            return new CustomValidationApiException("찾을 수 없는 id입니다.");
        });

        user.changeUser(userUpdateDto.getName(),
                userUpdateDto.getAddress(),
                userUpdateDto.getEmail(),
                userUpdateDto.getPhone());

        return user;
    }

    @Transactional
    public List<User> searchUserByKeyword(String keyword){
        List<User> userList = userRepository.searchByKeyword(keyword);
        return userList;
    }

}
