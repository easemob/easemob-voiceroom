package com.voiceroom.mic.pojos;

import lombok.Data;

import java.util.List;

@Data
public class PageInfo<T> {

    private Long total;

    private String cursor;

    private List<T> list;
}
