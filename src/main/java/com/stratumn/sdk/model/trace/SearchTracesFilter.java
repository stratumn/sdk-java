package com.stratumn.sdk.model.trace;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/***
 * The trace filter object used to search through all traces of a workflow.
 */
public class SearchTracesFilter {

  public enum SEARCH_TYPE {
    TAGS_CONTAINS, TAGS_OVERLAPS
  }

  private Map<String, Object> filters;

  public SearchTracesFilter(List<String> tags) {
    // By default, search for any tags (for non breaking change)
    this(tags, SEARCH_TYPE.TAGS_OVERLAPS);
  }

  public SearchTracesFilter(List<String> tags, SEARCH_TYPE searchType) {
    filters = new HashMap<String, Object>();

    Map<String, Object> searchFilter = new HashMap<String, Object>();
    this.filters = new HashMap<String, Object>();

    switch (searchType) {
    case TAGS_CONTAINS:
      // search for all tags
      searchFilter.put("contains", tags);
      this.filters.put("tags", searchFilter);
      break;
    case TAGS_OVERLAPS:
      searchFilter.put("overlaps", tags);
      this.filters.put("tags", searchFilter);
      break;
    default:
      // By default, search for any tags (for non breaking change)
      searchFilter.put("overlaps", tags);
      this.filters.put("tags", searchFilter);
      break;
    }
  }

  public Map<String, Object> getFilters() {
    return this.filters;
  }

  public void setFilters(Map<String, Object> filters) {
    this.filters = filters;
  }

}
