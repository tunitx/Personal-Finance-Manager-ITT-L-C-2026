package com.tunitx.PRM.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class TeamBuildResponse {

    private String projectName;
    private List<RoleAssignment> assignments;
    private String note;

    @Getter
    @AllArgsConstructor
    public static class RoleAssignment {

        private String roleName;
        private String assignedUserId;    // null if gap
        private String assignedUserName;  // null if gap
        private String reason;
        private boolean filled;           // false = gap
        private String gapReason;         // why it could not be filled
    }
}
