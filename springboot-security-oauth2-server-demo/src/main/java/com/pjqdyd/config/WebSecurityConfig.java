package com.pjqdyd.config;

import com.pjqdyd.service.impl.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 *    
 *
 * @Description:  [基本web安全配置类]
 * @Author:       pjqdyd
 * @Version:      [v1.0.0]
 *  
 */

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 重写userDetailsServiceBean(), 使用自己提供的数据库查询的用户数据服务
     * @return
     */
    @Bean
    @Override
    public UserDetailsService userDetailsServiceBean() {
       return new UserDetailsServiceImpl();
    }

    /**
     * 认证配置
     * @param auth
     * @throws Exception
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        //添加使用自己提供的数据库查询的用户数据服务
        auth.userDetailsService(userDetailsServiceBean());
    }

    //重写认证管理器配置,注入bean, 用于支持密码模式
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
    //重写userDetailsService(),注入bean, 用于支持刷新token
    @Bean
    @Override
    protected UserDetailsService userDetailsService() {
        return super.userDetailsService();
    }

    /**
     * web security配置
     * @param web
     * @throws Exception
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        //忽略拦截/oauth/check_token, 方便资源服务器使用
        web
                .ignoring()
                .antMatchers("/oauth/check_token")
                .antMatchers("/logout/**");
    }
}
