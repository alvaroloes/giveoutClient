package com.capstone.potlatch.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Created by alvaro on 13/11/14.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Gift {
    public Long id;
    public String title;
    public String description;
    private String imageUrlFull;
    private String imageUrlMedium;
    private String imageUrlSmall;
    public GiftChain giftChain;
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
