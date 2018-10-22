package com.sri.ai.praisewm.service.dto;

import java.util.Objects;

public class GraphVariableRangeDto {
  private double first;
  private double last;
  private double step;

  // no-arg constructor for JSON conversion
  public GraphVariableRangeDto() {}

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
    GraphVariableRangeDto that = (GraphVariableRangeDto) o;
    return Double.compare(that.first, first) == 0 &&
        Double.compare(that.last, last) == 0 &&
        Double.compare(that.step, step) == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(first, last, step);
  }

  @Override
  public String toString() {
    return "GraphVariableRangeDto{" + "first=" + first + ", last=" + last + ", step=" + step + '}';
  }
}
