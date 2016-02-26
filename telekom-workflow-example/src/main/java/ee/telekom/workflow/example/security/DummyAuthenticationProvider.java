package ee.telekom.workflow.example.security;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Component("webConsoleAuthenticationProvider")
public class DummyAuthenticationProvider implements AuthenticationProvider{

    @Override
    public Authentication authenticate( Authentication authentication ) throws AuthenticationException{
        return new UsernamePasswordAuthenticationToken( authentication.getName(), authentication.getCredentials().toString(), getGrantedAuthorities() );
    }

    @Override
    public boolean supports( Class<?> authentication ){
        return authentication.equals( UsernamePasswordAuthenticationToken.class );
    }

    private Collection<? extends GrantedAuthority> getGrantedAuthorities(){
        return Arrays.asList( new GrantedAuthority[]{new SimpleGrantedAuthority( "ROLE_TWE_ADMIN" )} );
    }

}