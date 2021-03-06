package com.pjqdyd.config;

import com.pjqdyd.enums.PermEnum;
import com.pjqdyd.filter.JwtTokenFilter;
import com.pjqdyd.handler.AuthenticationAccessDeniedHandler;
import com.pjqdyd.handler.JwtAuthenticationEntryPointHandler;
import com.pjqdyd.service.impl.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.annotation.Resource;

/**
 *    
 *
 * @Description:  [Spring Security的配置类]
 * @Author:       pjqdyd
 * @Version:      [v1.0.0]
 *  
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true) //是否开启方法安全注解
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {

    @Resource
    private AuthenticationAccessDeniedHandler accessDeniedHandler;

    @Resource
    private JwtAuthenticationEntryPointHandler entryPointHandler;

    /**
     * 注册Jwt Token过滤器的bean
     * @return
     */
    @Bean
    public JwtTokenFilter authenticationTokenFilterBean() {
        return new JwtTokenFilter();
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
     * 注册认证管理器的bean
     * @return
     * @throws Exception
     */
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    /**
     * 安全管理器方法, 提供认证信息
     * @param auth
     * @throws Exception
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        //添加使用自己提供的数据库查询的用户数据服务
        auth.userDetailsService(userDetailsServiceBean()).passwordEncoder(new BCryptPasswordEncoder());
    }

    /**
     * http安全配置方法,拦截,配置接口所需用户权限
     * @param http
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http    .exceptionHandling().accessDeniedHandler(accessDeniedHandler)    //认证过的用户访问无权限资源时的异常处理
                .and()
                .exceptionHandling().authenticationEntryPoint(entryPointHandler) //匿名用户访问无权限资源时的异常处理
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) //使用JWT所以关闭session
                .and()
                .authorizeRequests()
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .antMatchers(HttpMethod.POST, "/auth/**").permitAll()
                .antMatchers("/user/**").hasAuthority(PermEnum.PERM_USER.getPermName())
                .antMatchers("/**").fullyAuthenticated()
                .and()
                .addFilterBefore(authenticationTokenFilterBean(), UsernamePasswordAuthenticationFilter.class) //添加Jwt过滤器
                .csrf().disable()           //关闭CSRF跨站请求伪造的防范,因为不再依赖于Cookie
                .headers().cacheControl();  //禁用缓存
    }

    /**
     * 配置忽略拦截一些静态页面资源,如swagger的页面
     * @param web
     */
    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers("/v2/api-docs",
                    "/swagger-ui.html",
                    "**/swagger-ui.html",
                    "/**/*.css",
                    "/**/*.js",
                    "/**/*.png",
                    "/swagger-resources/**",
                    "/v2/**",
                    "/**/*.ttf"
        );
    }
}
