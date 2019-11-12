package com.stratumn.sdk.model.trace;

import java.util.List;

/***
 * The trace filter object used to search through all traces of a workflow.
 */
public class SearchTracesFilter {
  private List<String> tags;

  public SearchTracesFilter(List<String> tags) {
    this.tags = tags;
  }

  public List<String> getTags() {
    return tags;
  }

  public void setTags(List<String> tags) {
    this.tags = tags;
  }
}
