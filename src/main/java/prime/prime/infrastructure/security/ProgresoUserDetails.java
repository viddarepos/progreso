package prime.prime.infrastructure.security;

import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class ProgresoUserDetails implements UserDetails {

  private Long id;
  private Long userId;
  private String email;
  private String password;
  private Collection<? extends GrantedAuthority> authorities;

  public ProgresoUserDetails(Long id, Long userId, String email, String password,
      Collection<? extends GrantedAuthority> authorities) {
    this.id = id;
    this.userId = userId;
    this.email = email;
    this.password = password;
    this.authorities = authorities;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  public Long getUserId() {
    return userId;
  }

  public boolean hasRole(String role) {
    return authorities
        .stream()
        .anyMatch(r -> r.getAuthority().equals(role));
  }
}
