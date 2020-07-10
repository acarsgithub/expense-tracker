package io.expense.expensetracker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    UserDetailsService userDetailsService;

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService);
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {

        // Authorization restrictions
        http.authorizeRequests()
                .antMatchers("/all-users").hasAnyRole("USER", "ADMIN")
                .antMatchers("transaction-history/{username}").hasAnyRole("USER", "ADMIN")
                .antMatchers("/modify-account/{username}").hasAnyRole("USER", "ADMIN")
                .antMatchers("/total-net-worth/{username}").hasAnyRole("USER", "ADMIN")
                .antMatchers("/add-new-account/{username}").hasAnyRole("USER", "ADMIN")
                .antMatchers("/create-new-user").permitAll()
                .antMatchers("/").permitAll()
                .and().formLogin();

        // CSRF DISABLED SECURITY ISSUE!!!
        http.csrf().disable();
    }

    @Bean
    public PasswordEncoder getPasswordEncoder(){
        return NoOpPasswordEncoder.getInstance();
    }
}
