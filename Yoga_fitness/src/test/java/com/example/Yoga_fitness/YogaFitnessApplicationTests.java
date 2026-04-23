package com.example.Yoga_fitness;

import com.example.Yoga_fitness.mapper.UserMapper;
import com.example.Yoga_fitness.pojo.User;
import com.example.Yoga_fitness.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class YogaFitnessApplicationTests {


    @Autowired
    private UserService userService;

    @Test
    void testLink(){
        List<User> list = userService.list();
        for(User user:list){
            System.out.println(user);
        }
    }

}
