package com.example.townmarket.common.domain.user.service;

import com.example.townmarket.common.enums.RoleEnum;
import com.example.townmarket.common.domain.user.dto.LoginRequestDto;
import com.example.townmarket.common.domain.user.dto.PasswordUpdateRequestDto;
import com.example.townmarket.common.domain.user.dto.ProfileRequestDto;
import com.example.townmarket.common.domain.user.dto.ProfileResponseDto;
import com.example.townmarket.common.domain.user.dto.RegionUpdateRequestDto;
import com.example.townmarket.common.domain.user.dto.SignupRequestDto;
import com.example.townmarket.common.domain.user.entity.User;

import com.example.townmarket.common.jwtUtil.JwtUtil;
import com.example.townmarket.common.domain.user.entity.Profile;
import com.example.townmarket.common.domain.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final JwtUtil jwtUtil;
  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional
  public void signup(SignupRequestDto request) {
    String username = request.getUsername();
    String phoneNum = request.getPhoneNumber();
    String email = request.getEmail();
    String password = passwordEncoder.encode(request.getPassword());

    Profile profile = new Profile(request.getNickname());

    User user = User.builder()
        .username(username)
        .password(password)
        .phoneNumber(phoneNum)
        .email(email)
        .region(request.getRegion())
        .email(request.getEmail())
        .role(RoleEnum.MEMBER)
        .profile(profile)
        .build();

    userRepository.save(user);
  }

  @Override
  @Transactional
  public void login(HttpServletResponse response, LoginRequestDto request) {
    String username = request.getUsername();
    String password = request.getPassword();

    // 사용자 확인
    User user = this.findByUsername(username);

    // 비밀번호 확인
    if (!passwordEncoder.matches(password, user.getPassword())) {
      throw new IllegalArgumentException("비밀번호가 틀립니다.");
    }
    String token = jwtUtil.createToken(user.getUsername(), user.getRole());
    response.addHeader(JwtUtil.AUTHORIZATION_HEADER, token);
  }

  @Override
  @Transactional
  public void logout(User user) {
  }


  @Override
  @Transactional
  public void updateUser(String username, PasswordUpdateRequestDto updateDto) {
    User user = this.findByUsername(username);

    if (!user.checkAuthorization(user)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정 권한이 없습니다.");
    }

    String password = passwordEncoder.encode(updateDto.getPassword());
    user.updatePassword(password);
    this.userRepository.save(user);
  }

  @Override
  @Transactional
  public void updateRegion(String username, RegionUpdateRequestDto updateRequestDto) {
    User user = this.findByUsername(username);

    if (!user.checkAuthorization(user)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정 권한이 없습니다.");
    }
    user.updateRegion(updateRequestDto);
    this.userRepository.save(user);
  }

  @Override
  @Transactional
  public void deleteUser(Long userId, String username) {
    User user = this.findByUsername(username);

    if (!user.checkAuthorization(user)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제 권한이 없습니다.");
    }
    userRepository.deleteById(userId);
  }

  @Transactional
  @Override
  public ProfileResponseDto updateProfile(Long userId, ProfileRequestDto request) {
    Profile profileSaved = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("회원 없음")).getProfile();
    profileSaved.update(request);
    return new ProfileResponseDto(profileSaved);
  }

  @Transactional
  @Override
  public ProfileResponseDto showProfile(Long userId) {
    Profile profile = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("회원 없음")).getProfile();
    return new ProfileResponseDto(profile);
  }


  @Override
  public void setUserGrade(User reviewee, int grade, int count) {
    reviewee.getGrade().setUserGrade(grade, count);
  }

  @Override
  public List<User> findAllUser() {
    return userRepository.findAll();
  }

  @Override
  public User findUserById(Long userId) {
    return userRepository.findById(userId).orElseThrow(
        () -> new RuntimeException("회원을 찾을 수 없습니다.")
    );
  }

  @Override
  public User findByUsername(String username) {
    return userRepository.findByUsername(username).orElseThrow(
        () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "회원을 찾을 수 없습니다.")
    );
  }

  @Override
  public Page<User> pagingUsers(Pageable pageable) {
    return userRepository.findAll(pageable);
  }

  @Override
  public void updateUserGrade(User reviewee, int grade) {
    reviewee.getGrade().updateUserGrade(grade);
  }


  @Override
  public boolean existsByEmail(String email) {
    return userRepository.existsByEmail(email);
  }

}
