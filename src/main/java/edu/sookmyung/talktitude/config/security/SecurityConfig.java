package edu.sookmyung.talktitude.config.security;

import edu.sookmyung.talktitude.client.service.ClientDetailService;
import edu.sookmyung.talktitude.member.service.MemberDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@RequiredArgsConstructor
@Configuration
public class SecurityConfig {

    private final TokenAuthenticationFilter tokenAuthenticationFilter;
    private final MemberDetailService memberDetailService;
    private final ClientDetailService clientDetailService;
    //스프링 시큐리티 기능 비활성화
    @Bean
    public WebSecurityCustomizer configure(){
        return (web) -> web.ignoring()
                .requestMatchers(new AntPathRequestMatcher("/static/")); //정적 리소스
    }

    //특정 HTTP 요청에 대한 웹 기반 보안 구성
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CORS: 빈으로 주입한 설정 사용 (Customizer.withDefaults())
                .cors(Customizer.withDefaults())

                // CSRF: REST API 개발환경에서는 보통 비활성화
                .csrf(csrf -> csrf.disable())

                // 인증/인가
                .authorizeHttpRequests(auth -> auth
                        // CORS preflight(OPTIONS) 허용 — 문제 생기면 추가
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // REVIEW /ws/info가 SockJS에서 자동으로 보내는 사전 확인용 요청이지만 성공하면 -> /ws.. 로 추가적인 요청이 이루어진다고 해서 /ws/**를 열어두었습니다(커스텀 헤더 설정 불가능) -> 혹시 아니라면 말씀해주세요
                        .requestMatchers("/ws/**").permitAll()
                        // 비인증 허용 API
                        .requestMatchers("/members/**", "/clients/**", "/reports/run").permitAll()

                        // 그 외는 인증 필요
                        .anyRequest().authenticated()
                )

                // 토큰 필터
                .addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();

    }
    //인증 관리자 관련 설정
    //Member 전용 AuthenticationManager
    @Bean
    @Primary
    @Qualifier("memberAuthManager")
    public AuthenticationManager memberAuthManager() throws Exception {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(memberDetailService);
        authProvider.setPasswordEncoder(bCryptPasswordEncoder());
        return new ProviderManager(authProvider);
    }

    //client 전용 AuthenticationManager
    @Bean
    @Qualifier("clientAuthManager")
    public AuthenticationManager clientAuthManager() throws Exception {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(clientDetailService);
        authProvider.setPasswordEncoder(bCryptPasswordEncoder());
        return new ProviderManager(authProvider);
    }

    @Bean
    public PasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:3001",
                "https://talktitude-client-fe.vercel.app"
        ));
        config.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization")); // 필요 시
        config.setAllowCredentials(true); // 쿠키/인증헤더 사용할 경우
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

}