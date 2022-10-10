package com.ruoyi.quartz.controller.demo;

import com.ruoyi.common.core.domain.AjaxResult;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller("hello")
public class QuartzTestController {

    @RequestMapping("/hello")
    public AjaxResult printQuartz(){
        System.out.println("hello world");
        return AjaxResult.success();
    }

}
