package com.vpp.cc.dto;

import java.io.Serializable;

public record BatteryUpdate(String name, int capacity) implements Serializable {}