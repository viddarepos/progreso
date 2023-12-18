package prime.prime.infrastructure.password.encoder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordEncoderMapper {
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public PasswordEncoderMapper(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @EncodedMapping
    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}
