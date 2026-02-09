package com.photopia.photopia_back.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CapsuleMemberId implements Serializable {
    private UUID capsuleId;
    private UUID userId;
}
