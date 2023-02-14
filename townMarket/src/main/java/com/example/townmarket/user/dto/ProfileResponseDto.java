package com.example.townmarket.user.dto;

import com.example.townmarket.user.entity.Profile;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public class ProfileResponseDto {

  private String nickname;
  private String img_url;

  public ProfileResponseDto(Profile profile) {
    this.nickname = profile.getNickName();
    this.img_url = profile.getImg_url();
  }
}
