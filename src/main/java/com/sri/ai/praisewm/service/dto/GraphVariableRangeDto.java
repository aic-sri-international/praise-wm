package com.sri.ai.praisewm.service.dto;

import java.util.Objects;

public class GraphVariableRangeDto {
  // Could be null
  private String unitName;
  // Could be null
  private String unitSymbol;
  private double first;
  private double last;
  private double step;

  // no-arg constructor for JSON conversion
  public GraphVariableRangeDto() {}

  public String getUnitName() {
    return unitName;
  }

  public GraphVariableRangeDto setUnitName(String unitName) {
    this.unitName = unitName;
    return this;
  }

  public String getUnitSymbol() {
    return unitSymbol;
  }

  public GraphVariableRangeDto setUnitSymbol(String unitSymbol) {
    this.unitSymbol = unitSymbol;
    return this;
  }

  public double getFirst() {
    return first;
  }

  public GraphVariableRangeDto setFirst(double first) {
    this.first = first;
    return this;
  }

  public double getLast() {
    return last;
  }

  public GraphVariableRangeDto setLast(double last) {
    this.last = last;
    return this;
  }

  public double getStep() {
    return step;
  }

  public GraphVariableRangeDto setStep(double step) {
    this.step = step;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GraphVariableRangeDto rangeDto = (GraphVariableRangeDto) o;
    return Double.compare(rangeDto.first, first) == 0 &&
        Double.compare(rangeDto.last, last) == 0 &&
        Double.compare(rangeDto.step, step) == 0 &&
        Objects.equals(unitName, rangeDto.unitName) &&
        Objects.equals(unitSymbol, rangeDto.unitSymbol);
  }

  @Override
  public int hashCode() {
    return Objects.hash(unitName, unitSymbol, first, last, step);
  }

  @Override
  public String toString() {
    return "GraphVariableRangeDto{" + "first=" + first + ", last=" + last + ", step=" + step + '}';
  }
}
