package com.sri.ai.praisewm.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/** JsonConverterTest. */
public class JsonConverterTest {
  private static Sample newSample(int secs) {
    Sample sample = new Sample().setInstant(Instant.now());
    sample.setSmap(new TreeMap<>());
    sample.getSmap().put(sample.getInstant(), new Slot().setName("a").setValue(1));
    sample.getSmap().put(sample.getInstant().plusSeconds(20), new Slot().setName("b").setValue(2));
    ImmutableSortedMap<Instant, Integer> map =
        new ImmutableSortedMap.Builder<Instant, Integer>(Ordering.natural())
            .put(sample.getInstant().plusSeconds(secs + 1), 1)
            .put(sample.getInstant().plusSeconds(secs + 2), 2)
            .put(sample.getInstant().plusSeconds(secs + 3), 3)
            .build();
    return sample.setMap(map).setList(map.keySet().asList());
  }

  @Test
  public void simpleInheritance() {
    InheritBase before = new Inherit().setText("hello");
    String json = JsonConverter.to(before);
    Inherit after = JsonConverter.from(json, Inherit.class);

    assertEquals(before, after);
  }

  @Test
  public void instantC() {
    Instant now = Instant.now();
    Timestamp ts = Timestamp.from(now);

    Instant i2 = ts.toInstant();

    assertEquals(now, i2);
  }

  @Test
  public void dateTimeConvertion() throws Exception {
    Samples samples =
        new Samples()
            .setSamples(Lists.asList(newSample(1), new Sample[] {newSample(1), newSample(2)}));

    String json = JsonConverter.to(samples);
    Samples s1 = JsonConverter.from(json, Samples.class);
    assertEquals(JsonConverter.to(s1), json);
  }

  public static class Slot {
    private String name;
    private Integer value;

    public String getName() {
      return name;
    }

    public Slot setName(String name) {
      this.name = name;
      return this;
    }

    public Integer getValue() {
      return value;
    }

    public Slot setValue(Integer value) {
      this.value = value;
      return this;
    }
  }

  public static class Sample {

    private Instant instant;
    private List<Instant> list;
    private Map<Instant, Integer> map;
    private TreeMap<Instant, Slot> smap;

    public Instant getInstant() {
      return instant;
    }

    Sample setInstant(Instant instant) {
      this.instant = instant;
      return this;
    }

    public List<Instant> getList() {
      return list;
    }

    public Sample setList(List<Instant> list) {
      this.list = list;
      return this;
    }

    public Map<Instant, Integer> getMap() {
      return map;
    }

    public Sample setMap(Map<Instant, Integer> map) {
      this.map = map;
      return this;
    }

    public TreeMap<Instant, Slot> getSmap() {
      return smap;
    }

    public Sample setSmap(TreeMap<Instant, Slot> smap) {
      this.smap = smap;
      return this;
    }
  }

  public static class Samples {

    List<Sample> samples;

    public List<Sample> getSamples() {
      return samples;
    }

    public Samples setSamples(List<Sample> samples) {
      this.samples = samples;
      return this;
    }
  }

  public abstract static class InheritBase {
    private Instant time = Instant.now();

    public Instant getTime() {
      return time;
    }

    public InheritBase setTime(Instant time) {
      this.time = time;
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
      InheritBase that = (InheritBase) o;
      return Objects.equals(getTime(), that.getTime());
    }

    @Override
    public int hashCode() {
      return Objects.hash(getTime());
    }
  }

  public static class Inherit extends InheritBase {
    private String text;

    public String getText() {
      return text;
    }

    public InheritBase setText(String text) {
      this.text = text;
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
      if (!super.equals(o)) {
        return false;
      }
      Inherit inherit = (Inherit) o;
      return Objects.equals(getText(), inherit.getText());
    }

    @Override
    public int hashCode() {
      return Objects.hash(super.hashCode(), getText());
    }
  }
}
