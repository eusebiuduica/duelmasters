package org.example.duelmasters.DTOs;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserLoginResponse {
   private String token;
   private int goldReceived;
   private boolean dailyReward;
   private int totalGold;
}
