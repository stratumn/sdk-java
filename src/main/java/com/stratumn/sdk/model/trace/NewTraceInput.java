package com.stratumn.sdk.model.trace;

public class NewTraceInput<TLinkData> {

  String formId;
  TLinkData data;

  public NewTraceInput(String formId, TLinkData data) throws IllegalArgumentException {
    if (formId == null) {
      throw new IllegalArgumentException("formId cannot be null in NewTraceInput");
    }
    this.formId = formId;
    this.data = data;
  }

  public TLinkData getData() {
    return this.data;
  }

}
