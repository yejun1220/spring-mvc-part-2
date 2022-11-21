package hello.login.web.filter;

import hello.login.web.SessionConst;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.PatternMatchUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Slf4j
public class LoginCheckFilter implements Filter {

    private static final String[] whitelist = {"/", "/members/add", "/login", "/logout", "/css/*"};

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestURI = httpRequest.getRequestURI();

        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try {
            log.info("인증 체크 필터 시작 {}", requestURI);
            if(isLoginCheckPath(requestURI)) {
                log.info("인증 체크 로직 실행 {}", requestURI);
                HttpSession session = httpRequest.getSession(false);
                // session이 null 이면 로그인이 안된 것이다.
                if(session == null || session.getAttribute(SessionConst.LOGIN_MEMBER) == null) {
                    log.info("미인증 사용자 요청 {}", requestURI);
                    // 미인증 사용자이므로 로그인으로 redirect 해줘야 한다.
                    // redirect를 해주고 다시 원래 페이지로 redirect를 해주기 위해 ?redirectURL= 를 포함해서 전달해준다.
                    httpResponse.sendRedirect("/login?redirectURL=" + requestURI);
                    // 다음 servlet이나 controller 호출을 안하게 된다.
                    return;
                }
            }

            // whiltelist면 다음 필터로 바로 이동한다.
            // 다음 filter로 이동시킨다.
            chain.doFilter(request, response);
        } catch (Exception e) {
            throw e; // 예외 로깅 가능하나, 톰캣까지 예외를 보내주어야 한다.
        } finally {
            log.info("인증 체크 필터 종료 {}", requestURI);
        }
    }

    /**
     * 화이트 리스트의 경우 인증 체크X
     */
    private boolean isLoginCheckPath(String requestURI) {
        // whitelist와 requestURI가 매칭 되는지 확인 해주는 메서드다.
        // 매칭되면 false를 해줘야 filter에 안 걸린다.
        return !PatternMatchUtils.simpleMatch(whitelist, requestURI);
    }
}
