package com.vpp.cc.dto;

import java.util.List;

public record BatteryResponse(List<String> batteryNames, int totalCapacity, double averageCapacity) {}