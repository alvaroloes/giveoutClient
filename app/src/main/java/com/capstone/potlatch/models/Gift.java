package com.capstone.potlatch.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Created by alvaro on 13/11/14.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Gift {
    public long id;
    public String title;
    public String description;
    public long giftChainId;
    public String giftChainName;
    public User user;
    public List<Long> touchedByUserIds;
    public List<Long> markedInappropriateByUserIds;

    public boolean touchedBy(User user) {
        return user != null && touchedByUserIds.contains(user.id);
    }

    public boolean inappropriateBy(User user) {
        return user != null && markedInappropriateByUserIds.contains(user.id);
    }
}
