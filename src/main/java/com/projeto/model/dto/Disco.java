package com.projeto.model.dto;

import lombok.Data;

@Data
public class Disco {
    private int id;
    private String team;

    private double x;
    private double y;

    private double vx;
    private double vy;

    private double angle;

    private boolean remover;

}