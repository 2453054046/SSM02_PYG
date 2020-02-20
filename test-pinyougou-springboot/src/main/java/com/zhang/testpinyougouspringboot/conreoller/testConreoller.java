package com.zhang.testpinyougouspringboot.conreoller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class testConreoller {

    @Autowired
    Environment evnl;

    @RequestMapping("/info")
    public String info(){
        return "helloworlad!!"+evnl.getProperty("testurl");
    }
}
