package com.weba11y.server.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;


@RestController
@RequiredArgsConstructor
public class  AccessibilityController {

    @PostMapping("/api/v1/accessibility/member")
    public Mono<?> checkAccessibility(){
        return
    }

}
