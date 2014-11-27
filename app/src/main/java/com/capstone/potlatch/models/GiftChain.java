package com.capstone.potlatch.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Created by alvaro on 13/11/14.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GiftChain {
    public Long id;
    public String name;
    public List<Gift> gifts;

    @Override
    public String toString() {
        return name;
    }
}
