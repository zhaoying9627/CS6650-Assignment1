package com.example.demo.controller;

import com.example.demo.model.SwipeDetails;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@ResponseBody
@AllArgsConstructor
public class Twinder {
    private final int SWIPER_BOUND = 5000;

    private final int SWIPEE_BOUND = 1000000;
    @PostMapping(value = "/swipe/{direction}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<String> chargeInitialization (
            @RequestBody SwipeDetails swipeDetails, HttpServletRequest httpServletRequest
    ) throws
            ClassNotFoundException, JsonProcessingException {
        // get current url
        String url = httpServletRequest.getRequestURL().toString();

        // split url
        String[] urlParts = url.split("/");
        // validate direction
        if(!(urlParts[urlParts.length - 1].equals("left") ||
                urlParts[urlParts.length -1].equals("right"))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid inputs");
        }

        // validate swiper
        if (!(1 <= Integer.parseInt(swipeDetails.getSwiper()) &&
                Integer.parseInt(swipeDetails.getSwiper()) <= SWIPER_BOUND)){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User not found");
        }

        // validate swipee
        if(!(1 <= Integer.parseInt(swipeDetails.getSwipee()) &&
                Integer.parseInt(swipeDetails.getSwipee()) <= SWIPEE_BOUND)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User not found");
        }

        // validate comment
        if(!(1 <= swipeDetails.getComment().length() &&
                swipeDetails.getComment().length() <= 256)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid inputs");
        }

        // write successful
        return ResponseEntity.status(HttpStatus.OK).body("Write successful");
    }
}
