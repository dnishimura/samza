package org.apache.samza.checkpoint;

public class SamzaOffset {
  public enum OffsetSource {
    FromCheckpoint,
    FromStartpoint,
    FromApplication,
    FromEvent,
    FromSideInput
  }

  private final OffsetSource offsetSource;
  private final String offset;

  public SamzaOffset(String offset) {
    this(OffsetSource.FromCheckpoint, offset);
  }

  public SamzaOffset(OffsetSource offsetSource, String offset) {
    this.offsetSource = offsetSource;
    this.offset = offset;
  }

  public OffsetSource getOffsetSource() {
    return offsetSource;
  }

  public String getOffset() {
    return offset;
  }
}
