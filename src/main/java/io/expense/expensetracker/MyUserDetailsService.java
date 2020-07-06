package io.expense.expensetracker;

import io.expense.expensetracker.models.MyUserDetails;
import io.expense.expensetracker.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String user_name) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByUsername(user_name);
        user.orElseThrow(() -> new UsernameNotFoundException("Not found: " + user_name));
        return user.map(MyUserDetails::new).get();
    }
}
