package com.luxf.mybatis.bootmybatisdemo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AnonymousAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.web.firewall.StrictHttpFirewall;

import javax.sql.DataSource;

/**
 * Spring Security、继承{@link WebSecurityConfigurerAdapter} 实现自定义配置、
 * Spring Security默认是没有用户的、SpringBoot会自动生成一个user用户、密码随机(控制台)
 *
 * @author 小66
 * @date 2020-06-17 19:56
 **/
@EnableWebSecurity
@Configuration
public class ApplicationSecurityConfiguration extends WebSecurityConfigurerAdapter {
    /**
     * 可以根据不同的数据源动态生成这2个SQL语句、
     */
    private static final String QUERY_PASSWORD_SQL = "SELECT USERNAME,PSW,AVAILABLE FROM S_USER WHERE USERNAME = ?";
    private static final String QUERY_ROLE_SQL = "SELECT U.USERNAME,R.ROLENAME FROM S_USER U,S_ROLE R,S_USER_ROLE UR " +
            "WHERE U.ID = UR.USER_ID AND R.ID = UR.ROLE_ID AND U.USERNAME = ? ";

    @Value(value = "application.security.secret")
    private String secret = null;

    /**
     * 用来配置用户签名服务,主要是user-details机制、可以给用户赋予角色
     * 签名服务：主要包含内存签名服务、 <P>数据库签名服务</P> 和 <P>自定义签名服务(最常用)</P>、
     *
     * @param auth 签名管理器构造器,用于构建用户具体权限控制
     * @throws Exception
     */
//    @Override
//    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//        /**
//         * 第一种：使用内存签名认证、不是主要方式,开发时可以快速进行测试
//         */
//        // 单向不可逆的加密方式、
//        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
//        /**
//         * 使用内存方式, 用户信息存储在 {@link InMemoryUserDetailsManagerConfigurer}中、
//         */
//        auth.inMemoryAuthentication()
//                // 设置密码编码器
//                .passwordEncoder(passwordEncoder)
//                // 注册用户 Jack、
//                .withUser("Jack")
//                // 密码 123
//                .password(passwordEncoder.encode("123"))
//                // 赋予角色、role()方法会给参数添加前缀 "ROLE_"
//                .roles("USER", "ADMIN")
//                // 连接方法 and()、可以配置多用户
//                .and()
//                // 注册用户 Lucy
//                .withUser("Lucy")
//                .password(passwordEncoder.encode("456"))
//                .roles("USER");
//    }

    /**
     * 数据库认证服务方式、
     *
     * @param auth
     * @throws Exception
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // 简单的加密方式, 实际中不可取、
        // PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
//      // 数据库认证方式、
//        auth.jdbcAuthentication()
//                // 密码编码器
//                .passwordEncoder(safeEncoder)
//                // 数据源
//                .dataSource(dataSource)
//                // 根据用户名查询用户密码, 自动判断密码是否一致
//                .usersByUsernameQuery(QUERY_PASSWORD_SQL)
//                // 根据用户名查询用户具有的角色, 赋予角色、查询出来有什么权限, 就赋予什么权限、
//                .authoritiesByUsernameQuery(QUERY_ROLE_SQL);

        // TODO:可以通过实现 PasswordEncoder 接口, 自定义加密方式、
        PasswordEncoder safeEncoder = new Pbkdf2PasswordEncoder(secret);
        /**
         * 最常用的方式：使用用户密码服务、可以先查Redis缓存,再查数据库、
         */
        auth.userDetailsService(userDetailsService)
                .passwordEncoder(safeEncoder);
    }

    @Autowired
    private DataSource dataSource;

    @Autowired
    private UserDetailsService userDetailsService;

    /**
     * 用来配置FilterChain、
     *
     * @param web Spring Web Security对象
     * @throws Exception
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        // 父类是空实现、需要自己定义FilterChain
        // super.configure(web);
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowUrlEncodedPercent(true);
        firewall.setAllowUrlEncodedSlash(true);
        firewall.setAllowSemicolon(true);
        web.httpFirewall(firewall);
    }

    /**
     * 用来配置拦截保护的请求、比如什么请求需要放行,什么请求需要验证、限定请求权限
     *
     * @param http Http安全请求对象
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
//        super.configure(http); 父类默认实现、通过登录认证的用户,可以请求一切页面、需要根据不同的角色限制、
        // 只对签名成功的用户请求、
//        http.authorizeRequests()
//                // 限定所有请求
//                .anyRequest()
//                // 方法对所有签名成功的用户允许方法
//                .authenticated()
//                // 连接词
//                .and()
//                // 代表请求来自使用Spring Security默认的登录界面、
//                .formLogin()
//                .and()
//                // 说明方法启用浏览器的HTTP基础认证方式、
//                .httpBasic()
//                .and()
//                .oauth2Login();

        http.authorizeRequests()
                // 限定"/user/"下的请求具有角色"ROLE_USER"和"ROLE_ADMIN"
                .antMatchers("/user/welcome", "/user/detail").hasAnyRole("USER", "ADMIN")
                // 限定"/admin/"下的所有请求权限赋予角色"ROLE_ADMIN"->需要ADMIN角色才可以访问"/admin/"下的所有请求
                .antMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
                // 其他路径允许签名后访问
                .anyRequest().permitAll()
                // 对于没有配置权限的其他请求, 允许匿名访问。
                .and().anonymous()
                /*
                * Spring Security提供了两种remember-me的实现，一种是简单的使用加密来保证基于cookie的token的安全，
                * 另一种是通过数据库或其它持久化存储机制来保存生成的token。
                * */
                // 使用 rememberMe()功能、
                .and().rememberMe().key("remember-me-key")
                // .and().rememberMe().rememberMeServices()  TODO: 使用自定义RememberMeServices 实现Spring Security的RememberMeService、
                // Spring Security默认登录界面
                .and().formLogin()
                // 启动HTTP基础验证
                .and().httpBasic().realmName("Dialog Title");
        // 关闭csrf、不推荐
        // http.csrf().disable();

        http.formLogin()
                // 设置自定义的登录页 和 默认的跳转路径、
                .loginPage("/login/page")
                .successForwardUrl("/login/welcome")
                .and().logout()
                // 登出的页面
                .logoutUrl("/logout")
                // 登出的跳转路径
                .logoutSuccessUrl("/login/page");

        // http.addFilterAfter();
        // http.addFilterBefore();

        // http.authenticationProvider();-->自定义AuthenticationProvider 实现 Spring Security 的 AuthenticationProvider
    }
}
