package com.likelion.cheg.service;

import com.likelion.cheg.domain.cart.Cart;
import com.likelion.cheg.domain.cart.CartRepository;
import com.likelion.cheg.domain.delivery.Delivery;
import com.likelion.cheg.domain.delivery.DeliveryRepository;
import com.likelion.cheg.domain.enumType.DeliveryStatus;
import com.likelion.cheg.domain.order.Order;
import com.likelion.cheg.domain.order.OrderRepository;
import com.likelion.cheg.domain.orderItem.OrderItem;
import com.likelion.cheg.domain.orderItem.OrderItemRepository;
import com.likelion.cheg.domain.product.Product;
import com.likelion.cheg.domain.product.ProductRepository;
import com.likelion.cheg.domain.user.User;
import com.likelion.cheg.domain.user.UserRepository;
import com.likelion.cheg.handler.ErrorCode;
import com.likelion.cheg.handler.ex.CustomBusinessApiException;
import com.likelion.cheg.handler.ex.CustomBusinessException;
import com.likelion.cheg.web.dto.order.OrderMyPageResponseDto;
import com.likelion.cheg.web.dto.order.OrderResponseDto;
import com.likelion.cheg.web.dto.pay.PaymentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;
import javax.transaction.Transactional;

@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final DeliveryRepository deliveryRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;

    public List<OrderMyPageResponseDto> makeMyPageResponseDto(List<Order> orderList){
        List<OrderMyPageResponseDto> orderListDtos = orderList.stream()
                .map(order -> new OrderMyPageResponseDto(
                        order.getOrderNumber(),
                        order.getOrderProductCount(),
                        order.getOrderStatus(),
                        order.getOrderPrice(),
                        order.getOrderItemList().get(0).getProduct().getBrand(),
                        order.getOrderItemList().get(0).getProduct().getUrl(),
                        order.getOrderItemList().get(0).getProduct().getName(),
                        order.getDelivery().getDeliveryStatus()))
                .collect(Collectors.toList());
        return orderListDtos;
    }

    public List<OrderResponseDto> makeResponseDto(List<Order> orderList){
        List<OrderResponseDto> orderListDtos = orderList.stream()
                .map(order -> new OrderResponseDto(
                        order.getOrderNumber(),
                        order.getCreateDate(),
                        order.getOrderStatus(),
                        order.getUser().getUsername()))
                .collect(Collectors.toList());
        return orderListDtos;
    }


    @Transactional
    public Order makeOrder(int userId, PaymentDto paymentDto){
        //회원 찾기
        User user = userRepository.findById(userId).orElseThrow(()->{
            return new CustomBusinessApiException(ErrorCode.NOT_FOUND_USER);
        });

        //delivery 생성
        Delivery delivery = Delivery.createDelivery(paymentDto.getAddress(), DeliveryStatus.배송전);

        //OrderItem 생성
        List<OrderItem> orderItemList = new ArrayList<>();
        if(paymentDto.getFlag() == 0){  //상세페이지
            //상품 찾기
            Product product = productRepository.findById(paymentDto.getProductId()).orElseThrow(()->{
                return new CustomBusinessApiException(ErrorCode.NOT_FOUND_PRODUCT);
            });

            OrderItem orderItem = OrderItem.createOrderItem(product,product.getPrice(),paymentDto.getAmount());
            orderItemList.add(orderItem);

        }else{  //장바구니
            List<Cart> cartList = cartRepository.loadCartByUserId(userId);
            for(Cart cart : cartList){
                OrderItem orderItem = OrderItem.createOrderItem(cart.getProduct(),cart.getProduct().getPrice(),cart.getProductCount());
                orderItemList.add(orderItem);
                //장바구니에서는 삭제
                cartRepository.deleteById(cart.getId());
                user.getCarts().remove(cart);
            }
        }

        //Order 생성
        Order order = Order.createOrder(user,delivery,orderItemList);

        //DB에 저장
        deliveryRepository.save(delivery);
        for(OrderItem orderItem : order.getOrderItemList()){
            orderItemRepository.save(orderItem);
        }
        orderRepository.save(order);

        return order;

    }
}
