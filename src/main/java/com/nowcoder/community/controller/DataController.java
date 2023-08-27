package com.nowcoder.community.controller;

import com.nowcoder.community.service.DataService;
import com.sun.org.apache.xpath.internal.operations.Mod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;
import java.util.Map;

/**
 * 展现数据和查询数据
 *
 * @author 曾文亮
 * @version 1.0.0
 * @email wenliang_zeng416@163.com
 * @date 2023年07月26日 22:18:52
 * @packageName com.nowcoder.community.controller
 * @className DataController
 * @describe 展现数据和查询数据
 */

@Controller
public class DataController {
    @Autowired
    private DataService dataService;

    /**
     * 统计页面
     *
     * @return
     */
    @RequestMapping(path = "/data", method = {RequestMethod.GET, RequestMethod.POST})
    public String getDataPage() {
        return "/site/admin/data";
    }

    /**
     * 统计网站UV
     * @param start
     * @param end
     * @param model
     * @return
     */
    @RequestMapping(path = "/data/uv", method = RequestMethod.POST)
    public String getUV(@DateTimeFormat(pattern = "yyyy-MM-dd") Date start,
                        @DateTimeFormat(pattern = "yyyy-MM-dd") Date end, Model model) {
            long uv = dataService.calculateUV(start,end);
            model.addAttribute("uvResult",uv);
            model.addAttribute("uvStartDate",start);
            model.addAttribute("uvEndDate",end);
            return "forward:/data";
    }

    /**
     * 统计网站DAU
     * @param start
     * @param end
     * @param model
     * @return
     */
    @RequestMapping(path = "/data/dau", method = RequestMethod.POST)
    public String getDAU(@DateTimeFormat(pattern = "yyyy-MM-dd") Date start,
                        @DateTimeFormat(pattern = "yyyy-MM-dd") Date end, Model model) {
        long dau = dataService.calculateDAU(start,end);
        model.addAttribute("dauResult",dau);
        model.addAttribute("dauStartDate",start);
        model.addAttribute("dauEndDate",end);
        return "forward:/data";
    }

}
