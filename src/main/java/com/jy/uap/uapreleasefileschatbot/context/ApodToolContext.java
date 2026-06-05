package com.jy.uap.uapreleasefileschatbot.context;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
@Getter
@Setter
public class ApodToolContext {

    private String imageUrl;

}
