package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * @author 曾文亮
 * @version 1.0.0
 * @email wenliang_zeng416@163.com
 * @date 2023年07月11日 20:23:06
 * @packageName com.nowcoder.community.controller
 * @className UserController
 * @describe 执行用户的一些相关操作
 */
@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private LikeService likeService;
    @Autowired
    private FollowService followService;

    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage() {
        return "/site/setting";
    }

    /**
     * @param headerImage 此时从浏览器中获得的是一张图片，因此用的是单个MultipartFile对象，如果是多张图片要换成数组
     * @param model       响应时可能要像页面返回一些数据，因此需要声明一个model用来给模版携带数据
     * @return
     */
    @LoginRequired
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model) {
        // 判断参数是否为空
        if (headerImage == null) {
            model.addAttribute("error", "您还没有选择图片!");
            return "/site/setting";
        }
        // 上传文件
        // PS：不能按照原始文件名来存储，否则会出现文件重名覆盖问题，为了避免覆盖需要给文件生成一个随机的名字（后缀不能改变）
        String fileName = headerImage.getOriginalFilename();
        assert fileName != null;
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "文件格式不正确");
            return "/site/setting";
        }

        // 生成随机的文件名
        fileName = CommunityUtil.generateUUID() + suffix;
        // 文件存放路径
        File dest = new File(uploadPath + "/" + fileName);
        try {
            // 存储文件
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败：" + e.getMessage());
            throw new RuntimeException("上传文件失败，服务器发生异常！", e);
        }
        // 更新当前用户的头像的路径（外部访问路径）
        // http://localhost:8080/community/user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        userService.updateHeader(user.getId(), headerUrl);
        return "redirect:/index";
    }

    // 外界获取图像的服务。该方法向浏览器返回的不是一个网页也不是一个字符串，而是一个图片，是一个二进制的数据，
    // 需要通过流手动向浏览器去输出
    // 路径已经规定，需要按照http://localhost:8080/community/user/header/xxx.png这个格式来写
    @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        // 传入的是一个文件名，怎么去找这个文件呢？
        // 需要去uploadPath中去找，所以该方法需要知道服务器存放文件的路径
        fileName = uploadPath + "/" + fileName;
        // 解析文件的后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        // 响应图片
        response.setContentType("image/" + suffix);
        // 响应图片是二进制数据，所以需要用到字节流
        try (// jdk7之后的语法，括号中的变量会自动加上finally，前提是这些变量有close方法
             OutputStream os = response.getOutputStream();
             // 得到输出流之后，如果要输出文件需要创建文件的输入流，好去读取这个文件
             FileInputStream fis = new FileInputStream(fileName);
        ) {
            // 有了输入流之后，就需要开始进行输出，输出的时候不能够一个字节一个字节地输出，需要创建一个缓冲区，比如一次最多输出1024个字节，一批一批输出
            byte[] buffer = new byte[1024];
            // 创建一个游标
            int b = 0;
            // 利用while循环去输出，b不等于-1表明已经读到数据了
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败：" + e.getMessage());
        }
    }

    @LoginRequired
    @RequestMapping(path = "/update", method = RequestMethod.POST)
    public String update(String oldPassword, String newPassword,String confirmPassword,Model model,@CookieValue("ticket") String ticket) {
        User user = hostHolder.getUser();
        Map<String,Object> map = userService.updatePassword(user.getId(), oldPassword,newPassword,confirmPassword);
        System.out.println(map);
        if  (map == null || map.isEmpty()) {
            userService.logout(ticket);
            return "redirect:/login";
        }else {
            model.addAttribute("updateOldMsg",map.get("updateOldMsg"));
            model.addAttribute("updateNewMsg",map.get("updateNewMsg"));
            model.addAttribute("updateConfirmMsg",map.get("updateConfirmMsg"));
            return "/site/setting";
        }
    }
    // 个人主页
    @RequestMapping(path = "/profile/{userId}",method = RequestMethod.GET)
    public String getProfilePager(@PathVariable int userId,Model model){
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在！");
        }
        // 用户信息
        model.addAttribute("user",user);
        // 点赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount",likeCount);
        // 查询关注数量
        long followeeCount = followService.findFolloweeCount(userId,ENTITY_TYPE_USER);
        model.addAttribute("followeeCount",followeeCount);
        // 粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER,userId);
        model.addAttribute("followerCount",followerCount);
        // 是否已关注
        boolean hasFollowed = false;
        if (hostHolder.getUser()!=null){
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(),ENTITY_TYPE_USER,userId);
        }
        model.addAttribute("hasFollowed",hasFollowed);
        return "/site/profile";
    }
}
