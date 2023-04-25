package com.likelion.cheg.domain.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.likelion.cheg.domain.delivery.Delivery;
import com.likelion.cheg.domain.orderItem.OrderItem;
import com.likelion.cheg.domain.user.User;
import lombok.*;

import java.util.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name="member_id")
    private User user;

    @OneToMany(mappedBy = "order")
    @JsonManagedReference
    @Builder.Default
    private List<OrderItem> orderItemList = new ArrayList<>(); //주문상품

    @OneToOne
    @JsonIgnoreProperties({"order"})
    @JoinColumn(name="delivery_id")
    private Delivery delivery; //배송

    private int orderStatus; //주문상태 (0 or 1)
    private String orderNumber; //주문번호

    private int orderPrice; //주문총액
    private int orderProductCount; //상품갯수 -> 마이페이지에서 구분을 위해
    private LocalDateTime createDate; //날짜

    @PrePersist //db에 insert되기 직전에 실행
    public void createDate() {
        this.createDate = LocalDateTime.now();
    }

    private void setOrderPrice(int orderPrice){
        this.orderPrice = orderPrice;
    }
    //연관 관계 매핑 메서드
    private void setUser(User user){
        this.user = user;
        user.getOrders().add(this);
    }
    private void addOrderItem(OrderItem orderItem){
        orderItemList.add(orderItem);
        orderItem.setOrder(this);
    }
    private void setDelivery(Delivery delivery){
        this.delivery = delivery;
        delivery.setOrder(this);
    }

    //주문번호 생성 메서드
    private static String createOrderNumber(){
        Random random = new Random();
        String number = "23_";
        for(int i=0;i<7;i++){
            number += Integer.toString(random.nextInt(9));
        }
        return number;
    }

    //Order 생성 메서드
    public static Order createOrder(User user, Delivery delivery, List<OrderItem> orderItems) {
        Order order = Order.builder()
                .orderNumber(createOrderNumber())
                .orderStatus(1)
                .orderProductCount(orderItems.size())
                .build();

        //주문 금액 세팅
        int sum = 0;
        for (OrderItem orderItem : orderItems) {
            order.addOrderItem(orderItem);
            sum += orderItem.getOrderItemTotalPrice();
        }
        order.setOrderPrice(sum);

        //양방향 연관관계 매핑
        order.setUser(user);
        order.setDelivery(delivery);

        return order;
    }
}
