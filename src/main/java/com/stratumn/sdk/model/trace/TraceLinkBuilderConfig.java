/*
Copyright 2017 Stratumn SAS. All rights reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
  limitations under the License.
*/
package com.stratumn.sdk.model.trace;

/**
 * The configuration interface for a new TraceLinkBuilder.
 */
public class TraceLinkBuilderConfig<TLinkData> {

	private String workflowId;
	private ITraceLink<TLinkData> parentLink;
	private boolean enableDebuging = false;

	public TraceLinkBuilderConfig() {

	}

	public boolean isEnableDebuging() {
		return enableDebuging;
	}

	public void setEnableDebuging(boolean enableDebuging) {
		this.enableDebuging = enableDebuging;
	}

	public TraceLinkBuilderConfig(String workflowId, ITraceLink<TLinkData> parentLink) {
		this.workflowId = workflowId;
		this.parentLink = parentLink;
	}

	public String getWorkflowId() {
		return workflowId;
	}

	public void setWorkflowId(String workflowId) {
		this.workflowId = workflowId;
	}

	public ITraceLink<TLinkData> getParentLink() {
		return parentLink;
	}

	public void setParentLink(ITraceLink<TLinkData> parentLink) {
		this.parentLink = parentLink;
	}

}
