package com.example.demo.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class SwipeDetails {
    private String swiper;
    private String swipee;
    private String comment;
}
