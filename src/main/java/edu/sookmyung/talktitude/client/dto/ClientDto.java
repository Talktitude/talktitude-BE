package edu.sookmyung.talktitude.client.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ClientDto {
    private String loginId;
    private String password;
    private String name;
    private String phone;
    private String address;
}