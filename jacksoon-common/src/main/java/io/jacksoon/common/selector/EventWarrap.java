package io.jacksoon.common.selector;

import io.jacksoon.common.handler.Handler;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventWarrap {
    Handler handler;
    Long n;
    public EventWarrap(Handler handler, Long n){
        this.handler = handler;
        this.n =n;
    }
}
