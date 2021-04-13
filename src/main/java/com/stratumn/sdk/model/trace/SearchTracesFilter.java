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

  private List<String> tags;
  private SEARCH_TYPE searchType;

  public SearchTracesFilter() {
    super();
  }

  public SearchTracesFilter(List<String> tags) {
    super();
    // By default, search for any tags (for non breaking change)
    this.tags = tags;
  }

  public List<String> getTags() {
    return this.tags;
  }

  public void setTags(List<String> tags) {
    this.tags = tags;
  }

  public SEARCH_TYPE getSearchType() {
    return this.searchType;
  }

  public void setSearchType(SEARCH_TYPE searchType) {
    this.searchType = searchType;
  }

  public Map<String, Object> getFilters() {
    Map<String, Object> filters = new HashMap<String, Object>();

    Map<String, Object> searchFilter = new HashMap<String, Object>();

    if (null == this.searchType) {
      // By default, search for any tags (for non breaking change)
      searchFilter.put("overlaps", this.getTags());
      filters.put("tags", searchFilter);
    } else {
      switch (this.searchType) {
      case TAGS_CONTAINS:
        // search for all tags
        searchFilter.put("contains", this.getTags());
        filters.put("tags", searchFilter);
        break;
      case TAGS_OVERLAPS:
        // search for any tags
        searchFilter.put("overlaps", this.getTags());
        filters.put("tags", searchFilter);
        break;
      default:
        break;
      }
    }

    return filters;
  }

}
