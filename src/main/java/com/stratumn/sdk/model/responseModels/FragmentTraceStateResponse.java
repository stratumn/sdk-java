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
package com.stratumn.sdk.model.responseModels;

public class FragmentTraceStateResponse {

	public String getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(String updatedAt) {
		this.updatedAt = updatedAt;
	}

	public Object getState() {
		return state;
	}

	public void setState(Object state) {
		this.state = state;
	}

	public HeadResponse getHead() {
		return head;
	}

	public void setHead(HeadResponse head) {
		this.head = head;
	}

	private String updatedAt;
	private Object state;
	private HeadResponse head;
}

class HeadResponse {

	public Object getRaw() {
		return raw;
	}

	public void setRaw(Object raw) {
		this.raw = raw;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	private Object raw;
	private Object data;
}
